<template>
  <div class="p-6">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Quản lý phương thức thanh toán</h1>
      <p class="text-gray-600 mt-2">Quản lý các phương thức thanh toán trong hệ thống</p>
    </div>

    <!-- Add Button -->
    <div class="mb-6">
      <n-button type="primary" @click="showCreateModal = true">
        <template #icon>
          <Plus class="w-4 h-4" />
        </template>
        Thêm phương thức thanh toán
      </n-button>
    </div>

    <!-- Payment Table -->
    <n-card>
      <n-data-table
        :columns="columns"
        :data="payments"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
        striped
      />
    </n-card>

    <!-- Create Modal -->
    <n-modal v-model:show="showCreateModal" preset="card" title="Thêm phương thức thanh toán" style="width: 600px">
      <n-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-placement="left"
        label-width="auto"
        require-mark-placement="right-hanging"
      >
        <n-form-item label="Mã thanh toán" path="paymentCode">
          <n-input v-model:value="createForm.paymentCode" placeholder="VD: COD, BANK_TRANSFER" />
        </n-form-item>

        <n-form-item label="Loại thanh toán" path="paymentType">
          <n-input v-model:value="createForm.paymentType" placeholder="VD: CASH, BANK, CARD, WALLET" />
        </n-form-item>

        <n-form-item label="Tên thanh toán" path="paymentName">
          <n-input v-model:value="createForm.paymentName" placeholder="VD: Thanh toán khi nhận hàng" />
        </n-form-item>

        <n-form-item label="Icon URL" path="icon">
          <n-input v-model:value="createForm.icon" placeholder="https://example.com/icon.png" />
        </n-form-item>

        <n-form-item label="Mô tả" path="description">
          <n-input
            v-model:value="createForm.description"
            type="textarea"
            placeholder="Mô tả chi tiết về phương thức thanh toán"
            :rows="3"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <n-button @click="showCreateModal = false">Hủy</n-button>
          <n-button type="primary" :loading="createLoading" @click="handleCreate">Thêm</n-button>
        </div>
      </template>
    </n-modal>

    <!-- Edit Modal -->
    <n-modal v-model:show="showEditModal" preset="card" title="Sửa phương thức thanh toán" style="width: 600px">
      <n-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-placement="left"
        label-width="auto"
        require-mark-placement="right-hanging"
      >
        <n-form-item label="Mã thanh toán" path="paymentCode">
          <n-input v-model:value="editForm.paymentCode" placeholder="VD: COD, BANK_TRANSFER" />
        </n-form-item>

        <n-form-item label="Loại thanh toán" path="paymentType">
          <n-input v-model:value="editForm.paymentType" placeholder="VD: CASH, BANK, CARD, WALLET" />
        </n-form-item>

        <n-form-item label="Tên thanh toán" path="paymentName">
          <n-input v-model:value="editForm.paymentName" placeholder="VD: Thanh toán khi nhận hàng" />
        </n-form-item>

        <n-form-item label="Icon URL" path="icon">
          <n-input v-model:value="editForm.icon" placeholder="https://example.com/icon.png" />
        </n-form-item>

        <n-form-item label="Mô tả" path="description">
          <n-input
            v-model:value="editForm.description"
            type="textarea"
            placeholder="Mô tả chi tiết về phương thức thanh toán"
            :rows="3"
          />
        </n-form-item>

        <n-form-item label="Trạng thái" path="isActive">
          <n-switch v-model:value="editForm.isActive" />
        </n-form-item>
      </n-form>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <n-button @click="showEditModal = false">Hủy</n-button>
          <n-button type="primary" :loading="editLoading" @click="handleEdit">Cập nhật</n-button>
        </div>
      </template>
    </n-modal>

    <!-- Delete Confirmation Modal -->
    <n-modal v-model:show="showDeleteModal" preset="card" title="Xác nhận xóa" style="width: 400px">
      <div class="text-center">
        <p class="text-gray-700 mb-4">
          Bạn có chắc chắn muốn xóa phương thức thanh toán
          <span class="font-semibold">{{ deletePayment?.paymentName }}</span
          >?
        </p>
        <p class="text-sm text-red-600">Hành động này không thể hoàn tác.</p>
      </div>

      <template #footer>
        <div class="flex justify-end space-x-3">
          <n-button @click="showDeleteModal = false">Hủy</n-button>
          <n-button type="error" :loading="deleteLoading" @click="handleDelete">Xóa</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { NButton, NTag, NSwitch, useMessage } from 'naive-ui'
import { Plus, Edit, Trash2 } from 'lucide-vue-next'
import axios from '../../utils/axios'

const message = useMessage()

// Data
const payments = ref([])
const loading = ref(false)
const createLoading = ref(false)
const editLoading = ref(false)
const deleteLoading = ref(false)

// Modals
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

// Forms
const createFormRef = ref(null)
const editFormRef = ref(null)

const createForm = ref({
  paymentCode: '',
  paymentType: '',
  paymentName: '',
  icon: '',
  description: '',
})

const editForm = ref({
  id: '',
  paymentCode: '',
  paymentType: '',
  paymentName: '',
  icon: '',
  description: '',
  isActive: true,
})

const deletePayment = ref(null)

// Options - removed paymentTypeOptions as it's no longer needed

// Validation rules
const createRules = {
  paymentCode: {
    required: true,
    message: 'Mã thanh toán không được để trống',
    trigger: 'blur',
  },
  paymentType: {
    required: true,
    message: 'Loại thanh toán không được để trống',
    trigger: 'change',
  },
  paymentName: {
    required: true,
    message: 'Tên thanh toán không được để trống',
    trigger: 'blur',
  },
}

const editRules = {
  id: {
    required: true,
    message: 'ID không được để trống',
    trigger: 'blur',
  },
  paymentCode: {
    required: true,
    message: 'Mã thanh toán không được để trống',
    trigger: 'blur',
  },
  paymentType: {
    required: true,
    message: 'Loại thanh toán không được để trống',
    trigger: 'change',
  },
  paymentName: {
    required: true,
    message: 'Tên thanh toán không được để trống',
    trigger: 'blur',
  },
}

// Table columns
const columns = [
  {
    title: 'Mã thanh toán',
    key: 'paymentCode',
    width: 150,
  },
  {
    title: 'Tên thanh toán',
    key: 'paymentName',
    width: 200,
  },
  {
    title: 'Loại thanh toán',
    key: 'paymentType',
    width: 150,
    render(row) {
      const typeMap = {
        CASH: { text: 'Tiền mặt', type: 'default' },
        BANK: { text: 'Ngân hàng', type: 'info' },
        CARD: { text: 'Thẻ tín dụng', type: 'success' },
        WALLET: { text: 'Ví điện tử', type: 'warning' },
        OTHER: { text: 'Khác', type: 'error' },
      }
      const type = typeMap[row.paymentType] || { text: row.paymentType, type: 'default' }
      return h(NTag, { type: type.type }, { default: () => type.text })
    },
  },
  {
    title: 'Mô tả',
    key: 'description',
    width: 250,
    ellipsis: {
      tooltip: true,
    },
  },
  {
    title: 'Trạng thái',
    key: 'isActive',
    width: 100,
    render(row) {
      return h(NSwitch, {
        value: row.isActive,
        disabled: true,
      })
    },
  },
  {
    title: 'Thao tác',
    key: 'actions',
    width: 150,
    render(row) {
      return h('div', { class: 'flex space-x-2' }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            onClick: () => handleEditClick(row),
          },
          { default: () => 'Sửa', icon: () => h(Edit, { class: 'w-3 h-3' }) },
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            onClick: () => handleDeleteClick(row),
          },
          { default: () => 'Xóa', icon: () => h(Trash2, { class: 'w-3 h-3' }) },
        ),
      ])
    },
  },
]

// Pagination
const pagination = {
  pageSize: 10,
}

// Methods
const fetchPayments = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/admin/payments')
    payments.value = response.data.payments || []
  } catch (error) {
    console.error('Error fetching payments:', error)
    message.error('Lỗi khi tải danh sách phương thức thanh toán')
  } finally {
    loading.value = false
  }
}

const resetCreateForm = () => {
  createForm.value = {
    paymentCode: '',
    paymentType: '',
    paymentName: '',
    icon: '',
    description: '',
  }
}

const resetEditForm = () => {
  editForm.value = {
    id: '',
    paymentCode: '',
    paymentType: '',
    paymentName: '',
    icon: '',
    description: '',
    isActive: true,
  }
}

const handleCreate = async () => {
  try {
    await createFormRef.value?.validate()
    createLoading.value = true

    const response = await axios.post('/api/admin/payments', createForm.value)
    message.success(response.data.message || 'Tạo phương thức thanh toán thành công')

    showCreateModal.value = false
    resetCreateForm()
    await fetchPayments()
  } catch (error) {
    console.error('Error creating payment:', error)
    message.error(error.response?.data?.message || 'Lỗi khi tạo phương thức thanh toán')
  } finally {
    createLoading.value = false
  }
}

const handleEditClick = (payment) => {
  editForm.value = {
    id: payment.id,
    paymentCode: payment.paymentCode,
    paymentType: payment.paymentType,
    paymentName: payment.paymentName,
    icon: payment.icon || '',
    description: payment.description || '',
    isActive: payment.isActive,
  }
  showEditModal.value = true
}

const handleEdit = async () => {
  try {
    await editFormRef.value?.validate()
    editLoading.value = true

    const response = await axios.put('/api/admin/payments', editForm.value)
    message.success(response.data.message || 'Cập nhật phương thức thanh toán thành công')

    showEditModal.value = false
    resetEditForm()
    await fetchPayments()
  } catch (error) {
    console.error('Error updating payment:', error)
    message.error(error.response?.data?.message || 'Lỗi khi cập nhật phương thức thanh toán')
  } finally {
    editLoading.value = false
  }
}

const handleDeleteClick = (payment) => {
  deletePayment.value = payment
  showDeleteModal.value = true
}

const handleDelete = async () => {
  try {
    deleteLoading.value = true
    const response = await axios.delete(`/api/admin/payments/${deletePayment.value.id}`)
    message.success(response.data.message || 'Xóa phương thức thanh toán thành công')

    showDeleteModal.value = false
    deletePayment.value = null
    await fetchPayments()
  } catch (error) {
    console.error('Error deleting payment:', error)
    message.error(error.response?.data?.message || 'Lỗi khi xóa phương thức thanh toán')
  } finally {
    deleteLoading.value = false
  }
}

onMounted(() => {
  fetchPayments()
})
</script>
