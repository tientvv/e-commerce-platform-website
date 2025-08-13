<template>
  <div class="h-full flex flex-col">
    <!-- Header với button thêm sản phẩm -->
    <div class="bg-white mb-4">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Quản lý sản phẩm</h1>
          <p class="text-sm text-gray-600 mt-1">Quản lý danh sách sản phẩm của shop</p>
        </div>
        <div class="flex items-center gap-3">
          <n-button @click="showAddDialog = true" type="primary" class="py-2 px-3 text-sm">
            <template #icon>
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
              </svg>
            </template>
            Thêm sản phẩm mới
          </n-button>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-hidden">
      <RouterView />
    </div>

    <!-- Add Product Dialog -->
    <n-modal v-model:show="showAddDialog" preset="card" style="width: 800px" title="Thêm sản phẩm mới">
      <div class="space-y-6">
        <!-- Success/Error Messages -->
        <div v-if="addSuccessMessage" class="p-4 bg-green-50 border border-green-200 rounded-lg">
          <div class="flex items-center">
            <svg class="w-5 h-5 text-green-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
            </svg>
            <span class="text-green-800 font-medium">{{ addSuccessMessage }}</span>
          </div>
        </div>

        <div v-if="addErrorMessage" class="p-4 bg-red-50 border border-red-200 rounded-lg">
          <div class="flex items-center">
            <svg class="w-5 h-5 text-red-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
            </svg>
            <span class="text-red-800 font-medium">{{ addErrorMessage }}</span>
          </div>
        </div>

        <!-- Form -->
        <form @submit.prevent>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Tên sản phẩm:</label>
              <input
                v-model="addForm.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Thương hiệu:</label>
              <input
                v-model="addForm.brand"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">Danh mục:</label>
            <div class="relative">
              <button
                type="button"
                @click="toggleCategoryDropdown"
                class="w-full px-3 py-2 border border-gray-300 rounded-md text-left bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {{ addForm.selectedCategoryName || 'Chọn danh mục' }}
              </button>
              <div
                v-show="showCategoryDropdown"
                class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-40 overflow-y-auto"
              >
                <div
                  v-for="category in categories"
                  :key="category.id"
                  @click="selectCategory(category)"
                  class="px-3 py-2 hover:bg-gray-100 cursor-pointer"
                >
                  {{ category.name }}
                </div>
              </div>
            </div>
          </div>

          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-2">Mô tả sản phẩm:</label>
            <textarea
              v-model="addForm.description"
              rows="4"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              required
            ></textarea>
          </div>

          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">Hình ảnh sản phẩm:</label>
            <input
              ref="imageInput"
              type="file"
              accept="image/*"
              @change="handleImageChange"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
            <div v-if="addForm.previewImage" class="mt-2">
              <img :src="addForm.previewImage" alt="Preview" class="w-20 h-20 object-cover rounded" />
            </div>
          </div>

          <!-- Dialog Actions -->
          <div class="flex justify-end space-x-3 gap-2">
            <n-button @click="closeDialog" size="medium">Hủy</n-button>
            <n-button
              type="primary"
              :loading="addLoading"
              size="medium"
              class="ms-2"
              @click="createProduct"
            >
              {{ addLoading ? 'Đang tạo...' : 'Thêm sản phẩm' }}
            </n-button>
          </div>
        </form>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '../../utils/axios'
import { NButton, NModal, useMessage } from 'naive-ui'

const message = useMessage()

// Add Product Dialog
const showAddDialog = ref(false)
const addForm = reactive({
  name: '',
  brand: '',
  description: '',
  productImage: null,
  previewImage: null,
  selectedCategoryName: '',
  selectedCategoryId: null,
})
const addSuccessMessage = ref('')
const addErrorMessage = ref('')
const addLoading = ref(false)
const imageInput = ref(null)

// Categories
const categories = ref([])
const showCategoryDropdown = ref(false)

// Methods
const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/categories')
    categories.value = response.data || []
    console.log('Fetched categories:', categories.value)
  } catch (error) {
    console.error('Error fetching categories:', error)
  }
}

const toggleCategoryDropdown = () => {
  showCategoryDropdown.value = !showCategoryDropdown.value
}

const selectCategory = (category) => {
  addForm.selectedCategoryName = category.name
  addForm.selectedCategoryId = category.id
  showCategoryDropdown.value = false
}

const handleImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    addForm.productImage = file
    addForm.previewImage = URL.createObjectURL(file)
  }
}

const createProduct = async () => {
  if (!addForm.name.trim() || !addForm.brand.trim() || !addForm.description.trim() || !addForm.selectedCategoryId) {
    addErrorMessage.value = 'Vui lòng điền đầy đủ thông tin sản phẩm'
    return
  }

  if (!addForm.productImage) {
    addErrorMessage.value = 'Vui lòng chọn hình ảnh sản phẩm'
    return
  }

  addLoading.value = true
  addErrorMessage.value = ''

  const formData = new FormData()
  formData.append('name', addForm.name.trim())
  formData.append('brand', addForm.brand.trim())
  formData.append('description', addForm.description.trim())
  formData.append('categoryId', addForm.selectedCategoryId)
  formData.append('productImage', addForm.productImage)

  try {
    const response = await axios.post('/api/products/add', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    if (response.data.message) {
      message.success(response.data.message)
      closeDialog()
      // Refresh the list view by navigating to it
      window.location.reload()
    } else {
      addErrorMessage.value = 'Thêm sản phẩm thất bại'
    }
  } catch (error) {
    if (error.response?.data?.message) {
      addErrorMessage.value = error.response.data.message
    } else if (error.response?.data?.errorMessage) {
      addErrorMessage.value = error.response.data.errorMessage
    } else {
      addErrorMessage.value = 'Không thể thêm sản phẩm'
    }
  } finally {
    addLoading.value = false
  }
}

const closeDialog = () => {
  showAddDialog.value = false
  addForm.name = ''
  addForm.brand = ''
  addForm.description = ''
  addForm.productImage = null
  addForm.previewImage = null
  addForm.selectedCategoryName = ''
  addForm.selectedCategoryId = null
  addErrorMessage.value = ''
  if (imageInput.value) {
    imageInput.value.value = ''
  }
}

// Load data on mount
onMounted(() => {
  fetchCategories()
})
</script>
