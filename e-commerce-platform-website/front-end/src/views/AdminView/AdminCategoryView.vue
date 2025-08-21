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
      <n-form ref="formRef" :model="formData" :rules="rules">
        <n-form-item path="name" label="Tên danh mục">
          <n-input v-model:value="formData.name" placeholder="Nhập tên danh mục" clearable />
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
import { ref, reactive, onMounted, h } from 'vue'
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
    required: true,
    message: 'Vui lòng nhập tên danh mục',
    trigger: ['input', 'blur'],
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
    const response = await axios.get('/api/categories')
    console.log('Fetched categories:', response.data)
    categories.value = response.data
  } catch (error) {
    console.error('Fetch categories error:', error)
    message.error('Không thể tải danh sách danh mục')
  } finally {
    loading.value = false
  }
}

// Show create modal
const showCreateModal = () => {
  isEdit.value = false
  resetForm()
  modalVisible.value = true
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
    await formRef.value?.validate()
    submitting.value = true

    const submitData = new FormData()
    submitData.append('name', formData.name)

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
      message.success('Tạo danh mục thành công')
    }

    closeModal()
    await fetchCategories()
  } catch (error) {
    console.error('Submit error:', error)
    console.error('Error response:', error.response?.data)
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('Có lỗi xảy ra khi lưu danh mục')
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
