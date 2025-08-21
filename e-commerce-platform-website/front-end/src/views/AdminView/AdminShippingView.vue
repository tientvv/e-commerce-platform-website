<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div>
      <h1 class="text-2xl font-bold text-gray-800 mb-2">Quản lý vận chuyển</h1>
      <p class="text-gray-600">Quản lý các phương thức vận chuyển và phí vận chuyển</p>
    </div>

    <!-- Add New Shipping Button -->
    <div class="flex justify-between items-center">
      <n-button type="primary" @click="showAddModal = true">
        <template #icon>
          <n-icon><Plus /></n-icon>
        </template>
        Thêm phương thức vận chuyển
      </n-button>
    </div>

    <!-- Shipping Table -->
    <n-card>
      <n-spin :show="loading">
        <n-data-table
          v-if="!loading && shippings.length > 0"
          :columns="columns"
          :data="shippings"
          :row-key="(row) => row.id"
          :pagination="pagination"
        />

        <!-- Empty State -->
        <n-empty v-else-if="!loading && shippings.length === 0" description="Chưa có phương thức vận chuyển nào">
          <template #icon>
            <n-icon size="48" color="#d1d5db">
              <Truck />
            </n-icon>
          </template>
        </n-empty>
      </n-spin>
    </n-card>

    <!-- Add/Edit Modal -->
    <n-modal
      v-model:show="showAddModal"
      preset="card"
      :title="editingShipping ? 'Sửa phương thức vận chuyển' : 'Thêm phương thức vận chuyển'"
      style="width: 600px"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="auto"
        require-mark-placement="right-hanging"
      >
        <n-form-item label="Tên phương thức" path="shippingMethod">
          <n-input v-model:value="formData.shippingMethod" placeholder="Ví dụ: Giao hàng nhanh" />
        </n-form-item>

        <n-form-item label="Phí vận chuyển" path="price">
          <n-input-number
            v-model:value="formData.price"
            placeholder="0"
            :min="0"
            :precision="0"
            :show-button="false"
            class="w-full"
          />
        </n-form-item>

        <n-form-item label="Thời gian dự kiến" path="estimatedDelivery">
          <n-input v-model:value="formData.estimatedDelivery" placeholder="Ví dụ: 2-3 ngày làm việc" />
        </n-form-item>

        <n-form-item label="Trạng thái" path="isActive">
          <n-switch v-model:value="formData.isActive" />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showAddModal = false">Hủy</n-button>
          <n-button type="primary" :loading="isSubmitting" @click="handleSubmit">
            {{ editingShipping ? 'Cập nhật' : 'Thêm' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Delete Confirmation Modal -->
    <n-modal
      v-model:show="showDeleteModal"
      preset="dialog"
      title="Xác nhận xóa"
      :content="`Bạn có chắc chắn muốn xóa phương thức vận chuyển '${shippingToDelete?.shippingMethod}' không?`"
      positive-text="Xóa"
      negative-text="Hủy"
      type="error"
      @positive-click="handleDelete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h, watch } from 'vue'
import axios from '../../utils/axios'
import { Truck, Plus, Edit, Trash2 } from 'lucide-vue-next'
import {
  NButton,
  NCard,
  NSpin,
  NDataTable,
  NEmpty,
  NIcon,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NSwitch,
  NSpace,
  useMessage,
} from 'naive-ui'

const message = useMessage()

// State
const loading = ref(true)
const isSubmitting = ref(false)
const shippings = ref([])
const showAddModal = ref(false)
const showDeleteModal = ref(false)
const editingShipping = ref(null)
const shippingToDelete = ref(null)
const formRef = ref(null)

// Form data
const formData = reactive({
  shippingMethod: '',
  price: 0,
  estimatedDelivery: '',
  isActive: true,
})

// Form validation rules
const formRules = {
  shippingMethod: {
    required: true,
    message: 'Vui lòng nhập tên phương thức vận chuyển',
    trigger: ['blur', 'input'],
  },
  price: {
    required: true,
    type: 'number',
    message: 'Vui lòng nhập phí vận chuyển',
    trigger: ['blur', 'change'],
  },
  estimatedDelivery: {
    required: true,
    message: 'Vui lòng nhập thời gian dự kiến',
    trigger: ['blur', 'input'],
  },
}

// Table columns
const columns = [
  {
    title: 'Tên phương thức',
    key: 'shippingMethod',
    width: 250,
  },
  {
    title: 'Phí vận chuyển',
    key: 'price',
    width: 150,
    render(row) {
      return h('span', { class: 'font-medium text-blue-600' }, formatPrice(row.price))
    },
  },
  {
    title: 'Thời gian dự kiến',
    key: 'estimatedDelivery',
    width: 150,
  },
  {
    title: 'Trạng thái',
    key: 'isActive',
    width: 100,
    render(row) {
      return h(
        'span',
        {
          class: row.isActive
            ? 'text-green-600 bg-green-100 px-2 py-1 rounded-full text-xs'
            : 'text-red-600 bg-red-100 px-2 py-1 rounded-full text-xs',
        },
        row.isActive ? 'Hoạt động' : 'Không hoạt động',
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
            onClick: () => editShipping(row),
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
const fetchShippings = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/admin/shippings')
    shippings.value = response.data.shippings || []
  } catch (error) {
    message.error('Không thể tải danh sách vận chuyển')
    console.error('Error:', error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formData.shippingMethod = ''
  formData.price = 0
  formData.estimatedDelivery = ''
  formData.isActive = true
  editingShipping.value = null
  if (formRef.value) {
    formRef.value.restoreValidation()
  }
}

const editShipping = (shipping) => {
  editingShipping.value = shipping
  formData.shippingMethod = shipping.shippingMethod
  formData.price = shipping.price
  formData.estimatedDelivery = shipping.estimatedDelivery
  formData.isActive = shipping.isActive
  showAddModal.value = true
}

const confirmDelete = (shipping) => {
  shippingToDelete.value = shipping
  showDeleteModal.value = true
}

const handleDelete = async () => {
  try {
    await axios.delete(`/api/admin/shippings/${shippingToDelete.value.id}`)
    message.success('Xóa phương thức vận chuyển thành công')
    showDeleteModal.value = false
    shippingToDelete.value = null
    await fetchShippings()
  } catch (error) {
    message.error('Không thể xóa phương thức vận chuyển')
    console.error('Error:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    isSubmitting.value = true

    if (editingShipping.value) {
      // Update
      await axios.put(`/api/admin/shippings/${editingShipping.value.id}`, formData)
      message.success('Cập nhật phương thức vận chuyển thành công')
    } else {
      // Create
      await axios.post('/api/admin/shippings', formData)
      message.success('Thêm phương thức vận chuyển thành công')
    }

    showAddModal.value = false
    resetForm()
    await fetchShippings()
  } catch (error) {
    if (error.errors) {
      message.error('Vui lòng kiểm tra lại thông tin')
    } else {
      message.error('Có lỗi xảy ra')
      console.error('Error:', error)
    }
  } finally {
    isSubmitting.value = false
  }
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

// Watch modal close to reset form
watch(showAddModal, (newVal) => {
  if (!newVal) {
    resetForm()
  }
})

onMounted(() => {
  fetchShippings()
})
</script>
