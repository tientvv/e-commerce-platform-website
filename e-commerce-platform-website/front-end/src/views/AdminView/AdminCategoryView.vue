<template>
  <n-space vertical :size="24" class="w-full">
    <!-- Page Header -->
    <div class="flex items-center justify-between">
      <div>
        <n-h1>Quản lý danh mục</n-h1>
        <n-text depth="3">Thêm, sửa, xóa các danh mục sản phẩm</n-text>
      </div>
      <n-button type="primary" @click="showCreateModal">
        <template #icon>
          <n-icon><Plus /></n-icon>
        </template>
        Thêm danh mục
      </n-button>
    </div>

    <!-- Categories Table -->
    <n-card>
      <template #header>
        <div class="flex items-center justify-between">
          <n-h3>Danh sách danh mục</n-h3>
          <n-text depth="3">{{ categories.length }} danh mục</n-text>
        </div>
      </template>

      <!-- Loading State -->
      <n-spin :show="loading">
        <n-data-table
          v-if="!loading && categories.length > 0"
          :columns="columns"
          :data="categories"
          :row-key="(row) => row.id"
          :pagination="pagination"
        />

        <!-- Empty State -->
        <n-empty v-else-if="!loading && categories.length === 0" description="Chưa có danh mục nào">
          <template #icon>
            <n-icon size="48" color="#d1d5db">
              <Package />
            </n-icon>
          </template>
          <template #extra>
            <n-button type="primary" @click="showCreateModal"> Tạo danh mục đầu tiên </n-button>
          </template>
        </n-empty>
      </n-spin>
    </n-card>

    <!-- Create/Edit Modal -->
    <n-modal
      v-model:show="modalVisible"
      :title="isEdit ? 'Sửa danh mục' : 'Thêm danh mục mới'"
      preset="dialog"
      :mask-closable="false"
      :closable="false"
      style="width: 600px"
    >
      <n-form ref="formRef" :model="formData" :rules="rules" :show-label="true">
        <n-form-item path="name" label="Tên danh mục">
          <n-input 
            v-model:value="formData.name" 
            placeholder="Nhập tên danh mục" 
            clearable 
            @blur="handleNameBlur"
          />
        </n-form-item>

        <n-form-item path="image" label="Hình ảnh danh mục">
          <n-space vertical class="w-full">
            <!-- Current Image Display -->
            <div v-if="formData.existingImageUrl && !imagePreview" class="mb-4">
              <n-text depth="3" class="block mb-2">Hình ảnh hiện tại:</n-text>
              <div class="relative inline-block">
                <n-image :src="formData.existingImageUrl" width="120" height="120" object-fit="cover" class="rounded" />
                <n-button
                  v-if="isEdit"
                  size="small"
                  type="error"
                  class="absolute -top-2 -right-2"
                  @click="removeExistingImage"
                >
                  <template #icon>
                    <n-icon><X /></n-icon>
                  </template>
                </n-button>
              </div>
            </div>

            <!-- New Image Preview -->
            <div v-if="imagePreview" class="mb-4">
              <n-text depth="3" class="block mb-2">Hình ảnh mới:</n-text>
              <div class="relative inline-block">
                <n-image :src="imagePreview" width="120" height="120" object-fit="cover" class="rounded" />
                <n-button size="small" type="error" class="absolute -top-2 -right-2" @click="removeImage">
                  <template #icon>
                    <n-icon><X /></n-icon>
                  </template>
                </n-button>
              </div>
            </div>

            <!-- Upload Button -->
            <n-upload
              :max="1"
              accept="image/*"
              :show-file-list="false"
              @change="handleFileChange"
              :disabled="uploading"
              :key="uploadKey"
            >
              <n-button :loading="uploading">
                <template #icon>
                  <n-icon><Upload /></n-icon>
                </template>
                {{ getUploadButtonText() }}
              </n-button>
            </n-upload>

            <n-text depth="3" class="text-sm"> Kích thước tối đa: 5MB </n-text>
          </n-space>
        </n-form-item>
      </n-form>

      <template #action>
        <n-space>
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
      :content="`Bạn có chắc chắn muốn xóa danh mục '${categoryToDelete?.name}' không? Hành động này không thể hoàn tác.`"
      positive-text="Xóa"
      negative-text="Hủy"
      type="error"
      @positive-click="handleDelete"
    />
  </n-space>
</template>

<script setup>
import { ref, reactive, onMounted, h, nextTick } from 'vue'
import axios from '../../utils/axios'
import { Plus, Edit, Trash2, Package, Upload, X } from 'lucide-vue-next'
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
  NFormItem,
  NInput,
  NUpload,
  NImage,
  useMessage,
  NTag,
} from 'naive-ui'

const message = useMessage()

// State
const categories = ref([])
const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const modalVisible = ref(false)
const deleteModalVisible = ref(false)
const isEdit = ref(false)
const currentCategoryId = ref(null)
const formRef = ref(null)

// Form data
const formData = reactive({
  name: '',
  image: null,
  existingImageUrl: '',
})

const imagePreview = ref('')
const categoryToDelete = ref(null)
const removeExistingImageFlag = ref(false)
const uploadKey = ref(0)

// Form validation rules
const rules = {
  name: {
    trigger: [], // Don't trigger automatically
    validator: (rule, value) => {
      console.log('Validating name:', value)
      console.log('Categories:', categories.value)
      console.log('Is edit:', isEdit.value)
      console.log('Current category ID:', currentCategoryId.value)

      if (!value || value.trim() === '') {
        console.log('Validation failed: empty name')
        return new Error('Vui lòng nhập tên danh mục')
      }

      // Check for duplicate names (case insensitive)
      const trimmedValue = value.trim()
      console.log('Checking for duplicate name:', trimmedValue)

      const existingCategory = categories.value.find(cat => {
        const isDuplicate = cat.name.toLowerCase() === trimmedValue.toLowerCase() &&
          (!isEdit.value || cat.id !== currentCategoryId.value)
        console.log(`Checking category "${cat.name}" (ID: ${cat.id}): ${isDuplicate}`)
        return isDuplicate
      })

      if (existingCategory) {
        console.log('Validation failed: duplicate name found:', existingCategory)
        return new Error('Tên danh mục đã tồn tại')
      }

      console.log('Validation passed')
      return true
    }
  },
}

// Table columns
const columns = [
  {
    title: 'Hình ảnh',
    key: 'categoryImage',
    width: 100,
    render(row) {
      if (row.categoryImage && row.categoryImage.trim() !== '') {
        return h(NImage, {
          src: row.categoryImage,
          width: 60,
          height: 60,
          objectFit: 'cover',
          class: 'rounded',
        })
      } else {
        return h(
          'div',
          {
            class: 'w-[60px] h-[60px] bg-gray-100 rounded flex items-center justify-center',
          },
          h(NIcon, { size: 24, color: '#9ca3af' }, { default: () => h(Package) }),
        )
      }
    },
  },
  {
    title: 'Tên danh mục',
    key: 'name',
    width: 200,
  },
  {
    title: 'Thao tác',
    key: 'actions',
    width: 200,
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

// Fetch categories
const fetchCategories = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/categories')
    console.log('Fetched categories:', response.data)
    if (response.data.categories) {
      categories.value = response.data.categories
    } else {
      categories.value = response.data
    }
  } catch (error) {
    console.error('Fetch categories error:', error)
    message.error('Không thể tải danh sách danh mục')
  } finally {
    loading.value = false
  }
}

// Show create modal
const showCreateModal = () => {
  console.log('Opening create modal')
  isEdit.value = false
  resetForm()
  modalVisible.value = true

  // Force validation reset after modal opens
  nextTick(() => {
    if (formRef.value) {
      formRef.value.restoreValidation()
      // Clear any validation errors
      formRef.value.clearValidate()
      // Force clear all validation errors
      formRef.value.clearValidate(['name'])
    }
  })
}

// Show edit modal
const showEditModal = (category) => {
  isEdit.value = true
  currentCategoryId.value = category.id
  formData.name = category.name
  formData.existingImageUrl = category.categoryImage || ''
  removeExistingImageFlag.value = false
  modalVisible.value = true
}

// Close modal
const closeModal = () => {
  modalVisible.value = false
  resetForm()
}

// Reset form
const resetForm = () => {
  formData.name = ''
  formData.image = null
  formData.existingImageUrl = ''
  imagePreview.value = ''
  currentCategoryId.value = null
  removeExistingImageFlag.value = false
  uploadKey.value = 0

  // Clear validation errors immediately
  if (formRef.value) {
    formRef.value.restoreValidation()
    formRef.value.clearValidate()
  }

  // Force re-render form to clear any cached validation state
  nextTick(() => {
    if (formRef.value) {
      formRef.value.restoreValidation()
      formRef.value.clearValidate()
      // Force clear all validation errors
      formRef.value.clearValidate(['name'])
    }
  })
}

// Handle name blur for validation
const handleNameBlur = () => {
  if (formData.name && formData.name.trim()) {
    formRef.value?.validateField('name')
  }
}

// Get upload button text
const getUploadButtonText = () => {
  if (uploading.value) return 'Đang tải...'
  if (imagePreview.value) return 'Thay đổi hình ảnh'
  if (formData.existingImageUrl && !removeExistingImageFlag.value) return 'Thay đổi hình ảnh'
  return 'Tải lên hình ảnh'
}

// Handle file change
const handleFileChange = ({ file }) => {
  if (!file) return

  // Validate file size (5MB)
  if (file.file.size > 5 * 1024 * 1024) {
    message.error('Kích thước file không được vượt quá 5MB')
    return
  }

  formData.image = file.file
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file.file)
}

// Remove image
const removeImage = () => {
  formData.image = null
  imagePreview.value = ''
  // Force re-render upload component
  uploadKey.value++
}

// Remove existing image
const removeExistingImage = () => {
  removeExistingImageFlag.value = true
  formData.existingImageUrl = ''
  // Reset file input để có thể chọn lại
  formData.image = null
  imagePreview.value = ''
  // Force re-render upload component
  uploadKey.value++
}

// Handle submit
const handleSubmit = async () => {
  try {
    console.log('Starting form submission...')
    console.log('Form data:', formData)
    console.log('Form ref:', formRef.value)
    console.log('Form data name:', formData.name)
    console.log('Form data name trimmed:', formData.name?.trim())

    // Check if name is empty before validation
    if (!formData.name || formData.name.trim() === '') {
      message.error('Vui lòng nhập tên danh mục')
      return
    }

    // Validate form first
    if (!formRef.value) {
      console.error('Form ref is null')
      message.error('Lỗi form validation')
      return
    }

    // Only validate if we have data to validate and it's not empty
    if (formData.name && formData.name.trim()) {
      try {
        await formRef.value.validate()
        console.log('Form validation passed')
      } catch (validationError) {
        console.log('Validation failed:', validationError)
        // Don't show additional error message, validation error is already shown
        return
      }
    }

    submitting.value = true

    const submitData = new FormData()
    const trimmedName = formData.name.trim()
    submitData.append('name', trimmedName)
    console.log('Submitting name:', trimmedName)

    if (formData.image) {
      submitData.append('categoryImage', formData.image)
      console.log('Uploading image:', formData.image.name, 'Size:', formData.image.size)
    }

    if (removeExistingImageFlag.value) {
      submitData.append('removeImage', 'true')
      console.log('Removing existing image')
    }

    console.log('Submitting form data:', {
      name: formData.name,
      hasImage: !!formData.image,
      removeImage: removeExistingImageFlag.value,
      isEdit: isEdit.value,
    })

    if (isEdit.value) {
      const response = await axios.put(`/api/admin/categories/${currentCategoryId.value}`, submitData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      console.log('Update response:', response.data)
      message.success('Cập nhật danh mục thành công')
    } else {
      const response = await axios.post('/api/admin/categories', submitData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      console.log('Create response:', response.data)
      console.log('Response status:', response.status)
      if (response.data.message) {
        message.success(response.data.message)
      } else {
        message.success('Tạo danh mục thành công')
      }
    }

    closeModal()
    await fetchCategories()
  } catch (error) {
    console.error('Submit error:', error)
    console.error('Error response:', error.response?.data)
    console.error('Error status:', error.response?.status)
    console.error('Error message:', error.message)
    console.error('Error message type:', typeof error.message)
    console.error('Error message is object:', typeof error.message === 'object')
    console.error('Error type:', typeof error)
    console.error('Error keys:', Object.keys(error))
    console.error('Full error object:', JSON.stringify(error, null, 2))

    // Handle validation errors
    if (error.name === 'ValidationError') {
      // Form validation error - already handled by form component
      console.log('Validation error caught, not showing additional message')
      return
    }

        // Handle server errors
    if (error.response?.data?.message) {
      console.log('Using server error message:', error.response.data.message)
      const serverMessage = error.response.data.message
      if (serverMessage.includes('đã tồn tại') || serverMessage.includes('duplicate')) {
        message.error('Tên danh mục đã tồn tại')
      } else {
        message.error(serverMessage)
      }
    } else if (error.response?.status === 409) {
      console.log('Conflict status detected, showing duplicate name error')
      message.error('Tên danh mục đã tồn tại')
    } else if (error.response?.status === 400) {
      // Handle validation errors from server
      if (error.response?.data?.message) {
        message.error(error.response.data.message)
      } else {
        message.error('Dữ liệu không hợp lệ')
      }
    } else if (error.response?.status === 401) {
      message.error('Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn')
    } else if (error.response?.status === 403) {
      message.error('Bạn không có quyền truy cập')
    } else if (error.response?.status === 500) {
      message.error('Lỗi server: ' + (error.response.data?.message || 'Lỗi không xác định'))
    } else if (error.response?.status) {
      // Handle other HTTP status codes
      message.error(`Lỗi ${error.response.status}: ${error.response.data?.message || 'Lỗi không xác định'}`)
    } else {
      // Handle case where error.message might be undefined or an object
      let errorMessage = 'Lỗi không xác định'
      if (error.message) {
        if (typeof error.message === 'string') {
          errorMessage = error.message
        } else if (typeof error.message === 'object') {
          errorMessage = JSON.stringify(error.message)
        } else {
          errorMessage = String(error.message)
        }
      } else if (error.toString) {
        errorMessage = error.toString()
      }
      
      // Don't show the generic error message for duplicate names
      if (errorMessage.includes('đã tồn tại') || errorMessage.includes('duplicate')) {
        message.error('Tên danh mục đã tồn tại')
      } else {
        // Don't show generic error message at all
        console.error('Silent error handling:', errorMessage)
      }
    }
  } finally {
    submitting.value = false
  }
}

// Confirm delete
const confirmDelete = (category) => {
  categoryToDelete.value = category
  deleteModalVisible.value = true
}

// Handle delete
const handleDelete = async () => {
  try {
    await axios.delete(`/api/admin/categories/${categoryToDelete.value.id}`)
    message.success('Xóa danh mục thành công')
    await fetchCategories()
  } catch (error) {
    console.error('Delete error:', error)
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('Không thể xóa danh mục')
    }
  }
}

// Load data on mount
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
