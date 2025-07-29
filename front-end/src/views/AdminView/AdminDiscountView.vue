<template>
  <n-space vertical :size="24" class="w-full">
    <!-- Page Header -->
    <div class="flex items-center justify-between">
      <div>
        <n-h1>Quản lý giảm giá</n-h1>
        <n-text depth="3">Thêm, sửa, xóa các chương trình giảm giá</n-text>
      </div>
      <n-button type="primary" @click="showCreateModal">
        <template #icon>
          <n-icon><Plus /></n-icon>
        </template>
        Thêm giảm giá
      </n-button>
    </div>

    <!-- Filter Tabs -->
    <n-tabs v-model:value="activeTab" type="segment">
      <n-tab-pane v-for="tab in filterTabs" :key="tab.key" :name="tab.key" :tab="tab.label" />
    </n-tabs>

    <!-- Discounts Table -->
    <n-card>
      <template #header>
        <div class="flex items-center justify-between">
          <n-h3>Danh sách giảm giá</n-h3>
          <n-text depth="3">{{ filteredDiscounts.length }} giảm giá</n-text>
        </div>
      </template>

      <n-spin :show="loading">
        <n-data-table
          v-if="!loading && filteredDiscounts.length > 0"
          :columns="columns"
          :data="filteredDiscounts"
          :row-key="(row) => row.id"
          :pagination="pagination"
        />

        <!-- Empty State -->
        <n-empty v-else-if="!loading && filteredDiscounts.length === 0" description="Chưa có giảm giá nào">
          <template #icon>
            <n-icon size="48" color="#d1d5db">
              <Tag />
            </n-icon>
          </template>
          <template #extra>
            <n-button type="primary" @click="showCreateModal"> Tạo giảm giá đầu tiên </n-button>
          </template>
        </n-empty>
      </n-spin>
    </n-card>

    <!-- Create/Edit Modal -->
    <n-modal
      v-model:show="modalVisible"
      :title="isEdit ? 'Sửa giảm giá' : 'Thêm giảm giá mới'"
      preset="card"
      :style="{ width: '700px' }"
      :mask-closable="false"
      :closable="false"
    >
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
        <n-grid :cols="2" :x-gap="16">
          <!-- Discount Name -->
          <n-form-item-grid-item :span="2" path="name" label="Tên giảm giá">
            <div class="flex space-x-2">
              <n-input v-model:value="formData.name" placeholder="Nhập tên chương trình giảm giá" clearable />
              <n-button @click="generateDiscountName" type="info" size="small"> Tạo tên tự động </n-button>
            </div>
          </n-form-item-grid-item>

          <!-- Discount Type -->
          <n-form-item-grid-item path="discountType" label="Loại giảm giá">
            <n-select
              v-model:value="formData.discountType"
              placeholder="Chọn loại giảm giá"
              :options="discountTypeOptions"
            />
          </n-form-item-grid-item>

          <!-- Discount Value -->
          <n-form-item-grid-item path="discountValue" label="Giá trị giảm giá">
            <n-input-number
              v-model:value="formData.discountValue"
              :min="0.01"
              :max="formData.discountType === 'PERCENTAGE' ? 100 : undefined"
              :precision="2"
              :placeholder="formData.discountType === 'PERCENTAGE' ? '10 (10%)' : '50000 (50,000đ)'"
              class="w-full"
            />
          </n-form-item-grid-item>

          <!-- Start Date -->
          <n-form-item-grid-item path="startDate" label="Ngày bắt đầu">
            <n-date-picker
              v-model:value="formData.startDate"
              type="datetime"
              placeholder="Chọn ngày bắt đầu"
              class="w-full"
            />
          </n-form-item-grid-item>

          <!-- End Date -->
          <n-form-item-grid-item path="endDate" label="Ngày kết thúc">
            <n-date-picker
              v-model:value="formData.endDate"
              type="datetime"
              placeholder="Chọn ngày kết thúc"
              class="w-full"
            />
          </n-form-item-grid-item>

          <!-- Min Order Value -->
          <n-form-item-grid-item :span="2" path="minOrderValue" label="Giá trị đơn hàng tối thiểu (VNĐ)">
            <n-input-number
              v-model:value="formData.minOrderValue"
              :min="0"
              :precision="0"
              placeholder="VD: 100000 (tối thiểu 100,000đ)"
              class="w-full"
            />
          </n-form-item-grid-item>

          <!-- Application Type -->
          <n-form-item-grid-item :span="2" path="applicationType" label="Phạm vi áp dụng">
            <n-select
              v-model:value="formData.applicationType"
              placeholder="Chọn phạm vi áp dụng"
              :options="applicationTypeOptions"
              @update:value="onApplicationTypeChange"
            />
          </n-form-item-grid-item>

          <!-- Target Selection -->
          <n-form-item-grid-item
            v-if="formData.applicationType && formData.applicationType !== 'ALL'"
            :span="2"
            :path="getTargetPath()"
            :label="getTargetLabel()"
          >
            <n-select
              v-if="formData.applicationType === 'CATEGORY'"
              v-model:value="formData.categoryId"
              placeholder="Chọn danh mục"
              :options="categoryOptions"
            />
            <n-select
              v-else-if="formData.applicationType === 'PRODUCT'"
              v-model:value="formData.productId"
              placeholder="Chọn sản phẩm"
              :options="productOptions"
              filterable
            />
            <n-select
              v-else-if="formData.applicationType === 'VARIANT'"
              v-model:value="formData.productVariantId"
              placeholder="Chọn variant"
              :options="variantOptions"
              filterable
            />
          </n-form-item-grid-item>

          <!-- Description -->
          <n-form-item-grid-item :span="2" path="description" label="Mô tả">
            <n-input
              v-model:value="formData.description"
              type="textarea"
              :rows="3"
              placeholder="Nhập mô tả chương trình giảm giá"
            />
          </n-form-item-grid-item>
        </n-grid>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="closeModal">Hủy</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? 'Cập nhật' : 'Tạo mới' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Delete Confirmation Modal -->
    <n-modal
      v-model:show="deleteModalVisible"
      preset="dialog"
      title="Xác nhận xóa"
      :content="`Bạn có chắc chắn muốn xóa chương trình giảm giá '${discountToDelete?.name}' không? Hành động này không thể hoàn tác.`"
      positive-text="Xóa"
      negative-text="Hủy"
      type="error"
      @positive-click="handleDelete"
    />
  </n-space>
</template>

<script setup>
import { ref, reactive, computed, onMounted, h } from 'vue'
import axios from '../../utils/axios'
import { Plus, Edit, Trash2, Tag } from 'lucide-vue-next'
import {
  NH1,
  NH3,
  NSpace,
  NCard,
  NButton,
  NIcon,
  NText,
  NSpin,
  NDataTable,
  NEmpty,
  NModal,
  NForm,
  NFormItemGridItem,
  NInput,
  NInputNumber,
  NSelect,
  NDatePicker,
  NGrid,
  NTabs,
  NTabPane,
  useMessage,
  NTag,
} from 'naive-ui'

const message = useMessage()

// State
const discounts = ref([])
const categories = ref([])
const products = ref([])
const variants = ref([])
const loading = ref(false)
const submitting = ref(false)
const modalVisible = ref(false)
const deleteModalVisible = ref(false)
const isEdit = ref(false)
const discountToDelete = ref(null)
const activeTab = ref('all')
const formRef = ref(null)

// Filter tabs
const filterTabs = [
  { key: 'all', label: 'Tất cả' },
  { key: 'active', label: 'Đang hoạt động' },
  { key: 'scheduled', label: 'Sắp diễn ra' },
  { key: 'expired', label: 'Đã hết hạn' },
]

// Form data
const formData = reactive({
  id: null,
  name: '',
  discountType: null,
  discountValue: null,
  startDate: null,
  endDate: null,
  minOrderValue: null,
  applicationType: null,
  productId: null,
  categoryId: null,
  productVariantId: null,
  description: '',
})

// Options for selects
const discountTypeOptions = [
  { label: 'Phần trăm (%)', value: 'PERCENTAGE' },
  { label: 'Số tiền cố định (VNĐ)', value: 'FIXED' },
]

const applicationTypeOptions = [
  { label: 'Áp dụng toàn bộ', value: 'ALL' },
  { label: 'Áp dụng theo danh mục', value: 'CATEGORY' },
  { label: 'Áp dụng theo sản phẩm', value: 'PRODUCT' },
  { label: 'Áp dụng theo variant', value: 'VARIANT' },
]

const categoryOptions = computed(() =>
  categories.value.map((cat) => ({
    label: cat.name,
    value: cat.id,
  })),
)

const productOptions = computed(() =>
  products.value.map((prod) => ({
    label: prod.name,
    value: prod.id,
  })),
)

const variantOptions = computed(() =>
  variants.value.map((variant) => ({
    label: `${variant.variantName || 'Variant'} - ${variant.variantValue || 'N/A'} (${variant.price ? formatCurrency(variant.price) : 'Chưa có giá'})`,
    value: variant.id,
  })),
)

// Form validation rules
const rules = {
  name: {
    required: true,
    message: 'Vui lòng nhập tên giảm giá',
    trigger: ['input', 'blur'],
  },
  discountType: {
    required: true,
    message: 'Vui lòng chọn loại giảm giá',
    trigger: 'change',
  },
  discountValue: {
    required: true,
    type: 'number',
    message: 'Vui lòng nhập giá trị giảm giá',
    trigger: ['input', 'blur'],
  },
  startDate: {
    required: true,
    message: 'Vui lòng chọn ngày bắt đầu',
    trigger: ['change', 'blur'],
    validator: (rule, value) => {
      if (!value) {
        return new Error('Vui lòng chọn ngày bắt đầu')
      }
      // Chỉ validate logic ngày khi cả startDate và endDate đều có giá trị
      if (formData.endDate && value && formData.endDate) {
        const startDate = new Date(value)
        const endDate = new Date(formData.endDate)
        if (startDate >= endDate) {
          return new Error('Ngày bắt đầu phải trước ngày kết thúc')
        }
      }
      return true
    },
  },
  endDate: {
    required: true,
    message: 'Vui lòng chọn ngày kết thúc',
    trigger: ['change', 'blur'],
    validator: (rule, value) => {
      if (!value) {
        return new Error('Vui lòng chọn ngày kết thúc')
      }
      // Chỉ validate logic ngày khi cả startDate và endDate đều có giá trị
      if (formData.startDate && value && formData.startDate) {
        const startDate = new Date(formData.startDate)
        const endDate = new Date(value)
        if (endDate <= startDate) {
          return new Error('Ngày kết thúc phải sau ngày bắt đầu')
        }
      }
      return true
    },
  },
  applicationType: {
    required: true,
    message: 'Vui lòng chọn phạm vi áp dụng',
    trigger: 'change',
  },
}

// Table columns
const columns = [
  {
    title: 'Tên giảm giá',
    key: 'name',
    render(row) {
      return h(
        NSpace,
        { align: 'center', size: 'small' },
        {
          default: () => [
            h('span', { style: { fontWeight: '500' } }, row.name),
            h(
              NTag,
              {
                size: 'small',
                type: getStatusType(row),
              },
              { default: () => getStatusText(row) },
            ),
            h(
              NTag,
              {
                size: 'small',
                type: row.discountType === 'PERCENTAGE' ? 'success' : 'info',
              },
              {
                default: () => (row.discountType === 'PERCENTAGE' ? 'Phần trăm' : 'Số tiền'),
              },
            ),
          ],
        },
      )
    },
  },
  {
    title: 'Giá trị',
    key: 'discountValue',
    width: 120,
    render(row) {
      return formatDiscountValue(row)
    },
  },
  {
    title: 'Áp dụng',
    key: 'applicationType',
    width: 150,
    render(row) {
      return getApplicationText(row)
    },
  },
  {
    title: 'Thời gian',
    key: 'dateRange',
    render(row) {
      return h(
        NSpace,
        { vertical: true, size: 0 },
        {
          default: () => [
            h(
              NText,
              { depth: 3, style: { fontSize: '12px' } },
              {
                default: () => `Từ: ${formatDate(row.startDate)}`,
              },
            ),
            h(
              NText,
              { depth: 3, style: { fontSize: '12px' } },
              {
                default: () => `Đến: ${formatDate(row.endDate)}`,
              },
            ),
          ],
        },
      )
    },
  },
  {
    title: 'Thao tác',
    key: 'actions',
    width: 150,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: 'info',
              onClick: () => showEditModal(row),
            },
            {
              icon: () => h(NIcon, null, { default: () => h(Edit) }),
              default: () => 'Sửa',
            },
          ),
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              onClick: () => confirmDelete(row),
            },
            {
              icon: () => h(NIcon, null, { default: () => h(Trash2) }),
              default: () => 'Xóa',
            },
          ),
        ],
      })
    },
  },
]

// Pagination
const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page) => {
    pagination.page = page
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
  },
})

// Computed
const filteredDiscounts = computed(() => {
  const now = new Date()

  switch (activeTab.value) {
    case 'active':
      return discounts.value.filter((d) => {
        const start = new Date(d.startDate)
        const end = new Date(d.endDate)
        return start <= now && end >= now
      })
    case 'scheduled':
      return discounts.value.filter((d) => new Date(d.startDate) > now)
    case 'expired':
      return discounts.value.filter((d) => new Date(d.endDate) < now)
    default:
      return discounts.value
  }
})

// Methods
const fetchDiscounts = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/discounts')
    discounts.value = response.data.discounts || []
  } catch {
    message.error('Không thể tải danh sách giảm giá')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/categories')
    categories.value = response.data
  } catch {
    message.error('Không thể tải danh sách danh mục')
  }
}

const fetchProducts = async () => {
  try {
    const response = await axios.get('/api/products')
    products.value = response.data.products || []
  } catch {
    message.error('Không thể tải danh sách sản phẩm')
  }
}

const fetchVariants = async () => {
  try {
    const response = await axios.get('/api/product-variants')
    variants.value = response.data.variants || []
  } catch {
    message.error('Không thể tải danh sách variants')
  }
}

const showCreateModal = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
}

const showEditModal = (discount) => {
  isEdit.value = true
  Object.assign(formData, {
    id: discount.id,
    name: discount.name,
    discountType: discount.discountType,
    discountValue: discount.discountValue,
    startDate: new Date(discount.startDate).getTime(),
    endDate: new Date(discount.endDate).getTime(),
    minOrderValue: discount.minOrderValue,
    applicationType: discount.applicationType || 'ALL',
    productId: discount.productId,
    categoryId: discount.categoryId,
    productVariantId: discount.productVariantId,
    description: discount.description || '',
  })
  modalVisible.value = true
}

const closeModal = () => {
  modalVisible.value = false
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    name: '',
    discountType: null,
    discountValue: null,
    startDate: null,
    endDate: null,
    minOrderValue: null,
    applicationType: null,
    productId: null,
    categoryId: null,
    productVariantId: null,
    description: '',
  })
}

const handleSubmit = async () => {
  try {
    // Validate tất cả fields
    await formRef.value?.validate()
    submitting.value = true

    const payload = {
      name: formData.name,
      discountType: formData.discountType,
      discountValue: formData.discountValue,
      startDate: formData.startDate ? new Date(formData.startDate).toISOString() : null,
      endDate: formData.endDate ? new Date(formData.endDate).toISOString() : null,
      minOrderValue: formData.minOrderValue || null,
      applicationType: formData.applicationType,
      description: formData.description || null,
      productId: null,
      categoryId: null,
      productVariantId: null,
    }

    // Set target fields based on application type
    if (formData.applicationType === 'CATEGORY') {
      payload.categoryId = formData.categoryId
    } else if (formData.applicationType === 'PRODUCT') {
      payload.productId = formData.productId
    } else if (formData.applicationType === 'VARIANT') {
      payload.productVariantId = formData.productVariantId
    }

    if (isEdit.value) {
      payload.id = formData.id
      console.log('Update payload:', payload)
      await axios.put(`/api/admin/discounts/${formData.id}`, payload)
      message.success('Cập nhật giảm giá thành công')
    } else {
      console.log('Create payload:', payload)
      await axios.post('/api/admin/discounts', payload)
      message.success('Tạo giảm giá thành công')
    }

    closeModal()
    await fetchDiscounts()
  } catch (error) {
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('Có lỗi xảy ra')
    }
  } finally {
    submitting.value = false
  }
}

const confirmDelete = (discount) => {
  discountToDelete.value = discount
  deleteModalVisible.value = true
}

const handleDelete = async () => {
  try {
    const response = await axios.delete(`/api/admin/discounts/${discountToDelete.value.id}`)
    if (response.data.success) {
      message.success(response.data.message || 'Xóa giảm giá thành công')
      deleteModalVisible.value = false
      discountToDelete.value = null
      await fetchDiscounts()
    } else {
      message.error(response.data.message || 'Không thể xóa giảm giá')
    }
  } catch (error) {
    console.error('Delete error:', error)
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('Không thể xóa giảm giá')
    }
  }
}

const generateDiscountName = () => {
  const prefixes = ['SAVE', 'DISCOUNT', 'OFF', 'PROMO', 'SALE', 'DEAL', 'BONUS', 'GIFT', 'CASHBACK', 'REWARD']
  const randomPrefix = prefixes[Math.floor(Math.random() * prefixes.length)]

  // Tạo số random từ 10-99
  const randomNumber = Math.floor(Math.random() * 90) + 10

  // Tạo ký tự random (chữ cái)
  const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const randomLetter = letters.charAt(Math.floor(Math.random() * letters.length))

  // Tạo tên với format: PREFIX + SỐ + CHỮ
  const generatedName = `${randomPrefix}${randomNumber}${randomLetter}`

  formData.name = generatedName
}

const onApplicationTypeChange = () => {
  formData.productId = null
  formData.categoryId = null
  formData.productVariantId = null
}

const getTargetLabel = () => {
  switch (formData.applicationType) {
    case 'CATEGORY':
      return 'Chọn danh mục'
    case 'PRODUCT':
      return 'Chọn sản phẩm'
    case 'VARIANT':
      return 'Chọn variant'
    default:
      return ''
  }
}

const getTargetPath = () => {
  switch (formData.applicationType) {
    case 'CATEGORY':
      return 'categoryId'
    case 'PRODUCT':
      return 'productId'
    case 'VARIANT':
      return 'productVariantId'
    default:
      return 'productId'
  }
}

// Helper functions
const getStatusType = (discount) => {
  const now = new Date()
  const start = new Date(discount.startDate)
  const end = new Date(discount.endDate)

  if (start > now) return 'warning'
  if (end < now) return 'default'
  return 'success'
}

const getStatusText = (discount) => {
  const now = new Date()
  const start = new Date(discount.startDate)
  const end = new Date(discount.endDate)

  if (start > now) return 'Sắp diễn ra'
  if (end < now) return 'Đã hết hạn'
  return 'Đang hoạt động'
}

const getApplicationText = (discount) => {
  switch (discount.applicationType) {
    case 'ALL':
      return 'Toàn bộ'
    case 'CATEGORY':
      return `Danh mục: ${discount.categoryName || 'N/A'}`
    case 'PRODUCT':
      return `Sản phẩm: ${discount.productName || 'N/A'}`
    case 'VARIANT':
      return `Variant: ${discount.variantName || 'N/A'}`
    default:
      return discount.applicationType || 'N/A'
  }
}

const formatDiscountValue = (discount) => {
  if (discount.discountType === 'PERCENTAGE') {
    return `${discount.discountValue}%`
  }
  return formatCurrency(discount.discountValue)
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(value)
}

// Lifecycle
onMounted(() => {
  fetchDiscounts()
  fetchCategories()
  fetchProducts()
  fetchVariants()
})
</script>
