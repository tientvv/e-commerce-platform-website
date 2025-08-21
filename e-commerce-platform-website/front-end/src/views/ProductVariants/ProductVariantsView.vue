<template>
  <n-space vertical :size="24">
    <!-- Form tạo biến thể mới -->
    <n-card title="Thêm Biến thể Mới" size="small">
        <n-form
          ref="formRef"
          :model="newVariant"
          :rules="formRules"
          label-placement="top"
          @submit.prevent="createVariant"
        >
          <n-grid :cols="2" :x-gap="16" :y-gap="16">
            <n-form-item-gi label="Sản phẩm" path="productId">
              <n-select
                v-model:value="newVariant.productId"
                placeholder="Chọn sản phẩm"
                :options="productOptions"
                filterable
              />
            </n-form-item-gi>

            <n-form-item-gi label="Tên biến thể" path="variantName">
              <n-input v-model:value="newVariant.variantName" placeholder="VD: Màu sắc, Kích thước" />
            </n-form-item-gi>

            <n-form-item-gi label="Giá trị biến thể" path="variantValue">
              <n-input v-model:value="newVariant.variantValue" placeholder="VD: Đỏ, XL" />
            </n-form-item-gi>

            <n-form-item-gi label="Số lượng" path="quantity">
              <n-input-number v-model:value="newVariant.quantity" :min="0" placeholder="Nhập số lượng" class="w-full" />
            </n-form-item-gi>

            <n-form-item-gi label="Giá (VNĐ)" path="price" :span="2">
              <n-input-number
                v-model:value="newVariant.price"
                :min="0"
                :step="1000"
                placeholder="Nhập giá sản phẩm"
                class="w-full"
                :format="formatPrice"
                :parse="parsePrice"
              />
            </n-form-item-gi>
          </n-grid>

          <n-space class="mt-6">
            <n-button type="primary" attr-type="submit" :loading="isLoading" :disabled="!isFormValid">
              <template #icon>
                <n-icon><Plus /></n-icon>
              </template>
              Thêm Biến thể
            </n-button>
            <n-button @click="resetForm">
              <template #icon>
                <n-icon><RotateCcw /></n-icon>
              </template>
              Đặt lại
            </n-button>
          </n-space>
        </n-form>
      </n-card>

      <!-- Danh sách biến thể -->
      <n-card title="Danh sách Biến thể" size="small">
        <template #header-extra>
          <n-select
            v-model:value="selectedProductId"
            placeholder="Lọc theo sản phẩm"
            :options="[{ label: 'Tất cả sản phẩm', value: '' }, ...productOptions]"
            @update:value="loadVariants"
            class="w-60"
            clearable
          />
        </template>

        <n-spin :show="loading">
          <n-data-table
            v-if="!loading && variants.length > 0"
            :columns="columns"
            :data="variants"
            :row-key="(row) => row.id"
            :pagination="pagination"
          />

          <!-- Empty State -->
          <n-empty v-else-if="!loading && variants.length === 0" description="Chưa có biến thể nào được tạo">
            <template #icon>
              <n-icon size="48" color="#d1d5db">
                <Settings />
              </n-icon>
            </template>
          </n-empty>
        </n-spin>
      </n-card>

      <!-- Edit Modal -->
      <n-modal v-model:show="showEditModal" preset="card" title="Sửa Biến thể" :style="{ width: '500px' }">
        <n-form
          ref="editFormRef"
          :model="editingVariant"
          :rules="editFormRules"
          label-placement="top"
          @submit.prevent="updateVariant"
        >
          <n-form-item label="Tên biến thể" path="variantName">
            <n-input v-model:value="editingVariant.variantName" placeholder="VD: Màu sắc, Kích thước" />
          </n-form-item>

          <n-form-item label="Giá trị biến thể" path="variantValue">
            <n-input v-model:value="editingVariant.variantValue" placeholder="VD: Đỏ, XL" />
          </n-form-item>

          <n-form-item label="Số lượng" path="quantity">
            <n-input-number v-model:value="editingVariant.quantity" :min="0" placeholder="Nhập số lượng" class="w-full" />
          </n-form-item>

          <n-form-item label="Giá (VNĐ)" path="price">
            <n-input-number
              v-model:value="editingVariant.price"
              :min="0"
              :step="1000"
              placeholder="Nhập giá sản phẩm"
              class="w-full"
              :format="formatPrice"
              :parse="parsePrice"
            />
          </n-form-item>

          <n-form-item label="Trạng thái">
            <n-switch v-model:value="editingVariant.isActive" checked-text="Hoạt động" unchecked-text="Không hoạt động" />
          </n-form-item>
        </n-form>

        <template #footer>
          <n-space justify="end">
            <n-button @click="showEditModal = false"> Hủy </n-button>
            <n-button type="primary" @click="updateVariant"> Cập nhật </n-button>
          </n-space>
        </template>
      </n-modal>
    </n-space>
  </template>

<script setup>
import { ref, reactive, computed, onMounted, h } from 'vue'
import axios from '../../utils/axios'
import { Plus, RotateCcw, Settings, Edit, Trash2 } from 'lucide-vue-next'
import {
  NSpace,
  NCard,
  NForm,
  NFormItem,
  NFormItemGi,
  NGrid,
  NSelect,
  NInput,
  NInputNumber,
  NButton,
  NIcon,
  NSpin,
  NDataTable,
  NEmpty,
  NModal,
  NSwitch,
  NTag,
  useMessage,
  useDialog,
} from 'naive-ui'

const message = useMessage()
const dialog = useDialog()

// Refs
const formRef = ref(null)
const editFormRef = ref(null)

// State
const products = ref([])
const variants = ref([])
const selectedProductId = ref('')
const showEditModal = ref(false)
const editingVariant = ref({})
const isLoading = ref(false)
const loading = ref(false)

const newVariant = ref({
  productId: '',
  variantName: '',
  variantValue: '',
  quantity: 0,
  price: 0,
})

// Form validation rules
const formRules = {
  productId: {
    required: true,
    message: 'Vui lòng chọn sản phẩm',
    trigger: ['blur', 'change'],
  },
  variantName: {
    required: true,
    message: 'Vui lòng nhập tên biến thể',
    trigger: ['blur', 'input'],
  },
  variantValue: {
    required: true,
    message: 'Vui lòng nhập giá trị biến thể',
    trigger: ['blur', 'input'],
  },
  quantity: {
    required: true,
    type: 'number',
    message: 'Vui lòng nhập số lượng',
    trigger: ['blur', 'change'],
  },
  price: {
    required: true,
    type: 'number',
    message: 'Vui lòng nhập giá',
    trigger: ['blur', 'change'],
  },
}

const editFormRules = {
  variantName: {
    required: true,
    message: 'Vui lòng nhập tên biến thể',
    trigger: ['blur', 'input'],
  },
  variantValue: {
    required: true,
    message: 'Vui lòng nhập giá trị biến thể',
    trigger: ['blur', 'input'],
  },
  quantity: {
    required: true,
    type: 'number',
    message: 'Vui lòng nhập số lượng',
    trigger: ['blur', 'change'],
  },
  price: {
    required: true,
    type: 'number',
    message: 'Vui lòng nhập giá',
    trigger: ['blur', 'change'],
  },
}

// Computed
const productOptions = computed(() => {
  return products.value.map((product) => ({
    label: product.name,
    value: product.id,
  }))
})

const isFormValid = computed(() => {
  return (
    newVariant.value.productId &&
    newVariant.value.variantName &&
    newVariant.value.variantValue &&
    newVariant.value.quantity >= 0 &&
    newVariant.value.price > 0
  )
})

// Table columns
const columns = [
  {
    title: 'Sản phẩm',
    key: 'productName',
    render(row) {
      return h('div', { style: { fontWeight: '500' } }, getProductName(row.productId))
    },
  },
  {
    title: 'Tên biến thể',
    key: 'variantName',
  },
  {
    title: 'Giá trị',
    key: 'variantValue',
  },
  {
    title: 'Số lượng',
    key: 'quantity',
    render(row) {
      return h('span', { style: { fontWeight: '500' } }, row.quantity.toLocaleString())
    },
  },
  {
    title: 'Giá',
    key: 'price',
    render(row) {
      return h('span', { style: { color: '#18a058', fontWeight: '500' } }, formatCurrency(row.price))
    },
  },
  {
    title: 'Trạng thái',
    key: 'isActive',
    width: 120,
    render(row) {
      return h(
        NTag,
        {
          type: row.isActive ? 'success' : 'error',
          size: 'small',
        },
        {
          default: () => (row.isActive ? 'Hoạt động' : 'Không hoạt động'),
        },
      )
    },
  },
  {
    title: 'Thao tác',
    key: 'actions',
    width: 160,
    render(row) {
      return h('div', { style: { display: 'flex', gap: '8px' } }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            onClick: () => editVariant(row),
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
            onClick: () => confirmDelete(row.id, row.variantName),
          },
          {
            icon: () => h(NIcon, null, { default: () => h(Trash2) }),
            default: () => 'Xóa',
          },
        ),
      ])
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

// Methods
const loadProducts = async () => {
  try {
    const response = await axios.get('/api/products/user')
    products.value = response.data.products || []
  } catch (error) {
    message.error('Không thể tải danh sách sản phẩm')
    console.error('Error:', error)
  }
}

const loadVariants = async () => {
  loading.value = true
  try {
    let url = '/api/product-variants/product/all'
    if (selectedProductId.value) {
      url = `/api/product-variants/product/${selectedProductId.value}`
    }
    const response = await axios.get(url)
    variants.value = response.data.variants || []
  } catch (error) {
    message.error('Không thể tải danh sách biến thể')
    console.error('Error:', error)
  } finally {
    loading.value = false
  }
}

const createVariant = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    isLoading.value = true

    await axios.post('/api/product-variants/create', newVariant.value)
    message.success('Tạo biến thể thành công!')
    resetForm()
    await loadVariants()
  } catch (error) {
    if (error.errors) {
      message.error('Vui lòng kiểm tra lại thông tin')
    } else {
      message.error(error.response?.data?.message || 'Có lỗi xảy ra!')
    }
  } finally {
    isLoading.value = false
  }
}

const editVariant = (variant) => {
  editingVariant.value = { ...variant }
  showEditModal.value = true
}

const updateVariant = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()

    await axios.put(`/api/product-variants/update/${editingVariant.value.id}`, editingVariant.value)
    message.success('Cập nhật biến thể thành công!')
    showEditModal.value = false
    await loadVariants()
  } catch (error) {
    if (error.errors) {
      message.error('Vui lòng kiểm tra lại thông tin')
    } else {
      message.error(error.response?.data?.message || 'Có lỗi xảy ra!')
    }
  }
}

const confirmDelete = (id, variantName) => {
  dialog.warning({
    title: 'Xác nhận xóa',
    content: `Bạn có chắc chắn muốn xóa biến thể "${variantName}" không?`,
    positiveText: 'Xóa',
    negativeText: 'Hủy',
    onPositiveClick: () => deleteVariant(id),
  })
}

const deleteVariant = async (id) => {
  try {
    await axios.delete(`/api/product-variants/delete/${id}`)
    message.success('Xóa biến thể thành công!')
    await loadVariants()
  } catch (error) {
    message.error(error.response?.data?.message || 'Có lỗi xảy ra!')
  }
}

const resetForm = () => {
  newVariant.value = {
    productId: '',
    variantName: '',
    variantValue: '',
    quantity: 0,
    price: 0,
  }
  if (formRef.value) {
    formRef.value.restoreValidation()
  }
}

// Helper functions
const getProductName = (productId) => {
  const product = products.value.find((p) => p.id === productId)
  return product ? product.name : 'Không xác định'
}

const formatPrice = (value) => {
  if (!value) return '0'
  return new Intl.NumberFormat('vi-VN').format(value)
}

const parsePrice = (input) => {
  return parseInt(input.replace(/\D/g, '')) || 0
}

const formatCurrency = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

// Load data on mount
onMounted(() => {
  loadProducts()
  loadVariants()
})
</script>
