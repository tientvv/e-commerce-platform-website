<template>
  <div class="h-full flex flex-col overflow-hidden">
    <!-- Products Table -->
    <div class="flex-1 overflow-hidden">
      <n-card class="h-full flex flex-col no-border-card">
        <template #header-extra>
          <div class="flex items-center gap-2">
            <n-button @click="fetchProducts" size="small">
            <template #icon>
                <RefreshCw class="w-4 h-4" />
              </template>
              Làm mới
            </n-button>
          </div>
            </template>

        <div class="flex-1">
          <div class="w-full h-full">
            <div class="w-full h-full">
              <div class="w-full">
                <div class="swiper-table-container" ref="swiperContainer">
                  <!-- Loading State -->
                  <div v-if="loading" class="flex items-center justify-center py-8">
                    <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                    <span class="ml-2 text-gray-600">Đang tải...</span>
                  </div>

                  <!-- Swiper Table -->
                  <div v-else class="swiper swiper-table">
                    <div class="swiper-wrapper">
                      <div class="swiper-slide">
                        <div class="custom-table">
                          <!-- Table Header -->
                          <div class="table-header">
                            <div class="header-cell">Tên sản phẩm</div>
                            <div class="header-cell">Thương hiệu</div>
                            <div class="header-cell">Danh mục</div>
                            <div class="header-cell">Trạng thái</div>
                            <div class="header-cell">Mô tả</div>
                            <div class="header-cell">Hình ảnh</div>
                            <div class="header-cell">Thao tác</div>
                          </div>

                          <!-- Table Body -->
                          <div class="table-body">
                            <div v-for="product in paginatedProducts" :key="product.id" class="table-row">
                              <div class="table-cell">
                                <div class="font-medium whitespace-nowrap overflow-hidden text-ellipsis">{{ product.name }}</div>
                              </div>
                              <div class="table-cell text-center">{{ product.brand || 'N/A' }}</div>
                              <div class="table-cell text-center">{{ product.categoryName || 'N/A' }}</div>
                              <div class="table-cell">
                                <span :class="getStatusBadgeClass(product.isActive)">
                                  {{ product.isActive ? 'Đang hiển thị' : 'Đã ẩn' }}
                                </span>
                              </div>
                              <div class="table-cell">
                                <div class="max-w-xs overflow-hidden text-ellipsis whitespace-nowrap">
                                  {{ product.description || 'Không có mô tả' }}
                                </div>
                              </div>
                              <div class="table-cell">
                                <img
                                  :src="product.productImage || '/default-product.png'"
                                  :alt="product.name"
                                  class="w-12 h-12 object-cover rounded"
                                  @error="$event.target.src = '/default-product.png'"
                                />
                              </div>
                              <div class="table-cell">
                                <div class="flex items-center gap-2">
                                  <button @click="editProduct(product)" class="action-btn">
                                    <Edit class="w-4 h-4" />
                                    Sửa
                                  </button>
                                  <button
                                    v-if="product.isActive"
                                    @click="confirmDelete(product.id, product.name)"
                                    class="action-btn-delete"
                                  >
                                    <Trash2 class="w-4 h-4" />
                                    Xóa
                                  </button>
                                  <button
                                    v-else
                                    @click="confirmRestore(product.id, product.name)"
                                    class="action-btn-restore"
                                  >
                                    <RefreshCw class="w-4 h-4" />
                                    Khôi phục
                                  </button>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Pagination -->
                <div class="pagination-container">
                  <div class="pagination-controls">
                    <button
                      @click="pagination.onChange(pagination.page - 1)"
                      :disabled="pagination.page <= 1"
                      class="pagination-btn"
                    >
                      Trước
                    </button>
                    <button class="pagination-btn-active">
                      {{ pagination.page }}
                    </button>
                    <button
                      @click="pagination.onChange(pagination.page + 1)"
                      :disabled="pagination.page * pagination.pageSize >= products.length"
                      class="pagination-btn"
                    >
                      Sau
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- Edit Product Dialog -->
    <n-modal v-model:show="showEditDialog" preset="card" style="width: 800px" title="Chỉnh sửa sản phẩm">
      <div class="space-y-6">
        <!-- Success/Error Messages -->
        <div v-if="editSuccessMessage" class="p-4 bg-green-50 border border-green-200 rounded-lg">
          <div class="flex items-center">
            <svg class="w-5 h-5 text-green-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
            </svg>
            <span class="text-green-800 font-medium">{{ editSuccessMessage }}</span>
          </div>
        </div>

        <div v-if="editErrorMessage" class="p-4 bg-red-50 border border-red-200 rounded-lg">
          <div class="flex items-center">
            <svg class="w-5 h-5 text-red-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
            </svg>
            <span class="text-red-800 font-medium">{{ editErrorMessage }}</span>
          </div>
        </div>

        <!-- Form -->
        <form @submit.prevent>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Tên sản phẩm:</label>
              <input
                v-model="editForm.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">Thương hiệu:</label>
              <input
                v-model="editForm.brand"
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
                @click="toggleEditCategoryDropdown"
                class="w-full px-3 py-2 border border-gray-300 rounded-md text-left bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {{ editForm.selectedCategoryName || 'Chọn danh mục' }}
              </button>
              <div
                v-show="showEditCategoryDropdown"
                class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-40 overflow-y-auto"
              >
                <div
                  v-for="category in categories"
                  :key="category.id"
                  @click="selectEditCategory(category)"
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
              v-model="editForm.description"
              rows="4"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              required
            ></textarea>
          </div>

          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">Hình ảnh sản phẩm:</label>
            <input
              ref="editImageInput"
              type="file"
              accept="image/*"
              @change="handleEditImageChange"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <div v-if="editForm.previewImage" class="mt-2">
              <img :src="editForm.previewImage" alt="Preview" class="w-20 h-20 object-cover rounded" />
            </div>
          </div>

          <!-- Dialog Actions -->
          <div class="flex justify-end space-x-3 gap-2">
            <n-button @click="closeEditDialog" size="medium">Hủy</n-button>
            <n-button
              type="primary"
              :loading="editLoading"
              size="medium"
              @click="updateProduct"
            >
              {{ editLoading ? 'Đang cập nhật...' : 'Cập nhật sản phẩm' }}
            </n-button>
          </div>
        </form>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, reactive } from 'vue'
import axios from '../../utils/axios'
import { Edit, Trash2, RefreshCw } from 'lucide-vue-next'
import { NCard, NButton, NModal, useMessage, useDialog } from 'naive-ui'
import Swiper from 'swiper'
import 'swiper/css'

const message = useMessage()
const dialog = useDialog()

const products = ref([])
const loading = ref(true)

// Swiper refs
const swiperContainer = ref(null)
let swiperInstance = null

// Pagination state
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

// Computed property for paginated products
const paginatedProducts = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return products.value.slice(start, end)
})



// Edit Product Dialog
const showEditDialog = ref(false)
const editForm = reactive({
  id: null,
  name: '',
  brand: '',
  description: '',
  productImage: null,
  previewImage: null,
  selectedCategoryName: '',
  selectedCategoryId: null,
})
const editSuccessMessage = ref('')
const editErrorMessage = ref('')
const editLoading = ref(false)
const editImageInput = ref(null)

// Categories
const categories = ref([])
const showEditCategoryDropdown = ref(false)

// Methods
const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/products/user')
    products.value = response.data.products || []
    // Reinitialize swiper after data loads
    nextTick(() => {
      initSwiper()
    })
  } catch (error) {
    message.error('Không thể tải danh sách sản phẩm')
    console.error('Error:', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const response = await axios.get('/api/categories')
    categories.value = response.data || []
    console.log('Fetched categories for edit:', categories.value)
  } catch (error) {
    console.error('Error fetching categories:', error)
  }
}

const editProduct = (product) => {
  editForm.id = product.id
  editForm.name = product.name
  editForm.brand = product.brand
  editForm.description = product.description
  editForm.selectedCategoryId = product.categoryId
  editForm.selectedCategoryName = product.categoryName
  editForm.productImage = null
  editForm.previewImage = product.productImage
  showEditDialog.value = true
}

const closeEditDialog = () => {
  showEditDialog.value = false
  editForm.id = null
  editForm.name = ''
  editForm.brand = ''
  editForm.description = ''
  editForm.productImage = null
  editForm.previewImage = null
  editForm.selectedCategoryName = ''
  editForm.selectedCategoryId = null
  editErrorMessage.value = ''
}

const updateProduct = async () => {
  if (!editForm.name.trim() || !editForm.brand.trim() || !editForm.description.trim()) {
    editErrorMessage.value = 'Vui lòng điền đầy đủ thông tin sản phẩm'
    return
  }

  editLoading.value = true
  editErrorMessage.value = ''

  const formData = new FormData()
  formData.append('name', editForm.name.trim())
  formData.append('brand', editForm.brand.trim())
  formData.append('description', editForm.description.trim())

  if (editForm.selectedCategoryId) {
    formData.append('categoryId', editForm.selectedCategoryId)
  }

  if (editForm.productImage) {
    formData.append('productImage', editForm.productImage)
  }

  try {
    const response = await axios.put(`/api/products/update/${editForm.id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    if (response.data.message) {
      message.success(response.data.message)
      await fetchProducts()
      closeEditDialog()
    } else {
      editErrorMessage.value = 'Cập nhật sản phẩm thất bại'
    }
  } catch (error) {
    if (error.response?.data?.message) {
      editErrorMessage.value = error.response.data.message
    } else if (error.response?.data?.errorMessage) {
      editErrorMessage.value = error.response.data.errorMessage
    } else {
      editErrorMessage.value = 'Không thể cập nhật sản phẩm'
    }
  } finally {
    editLoading.value = false
  }
}





const toggleEditCategoryDropdown = () => {
  showEditCategoryDropdown.value = !showEditCategoryDropdown.value
}

const selectEditCategory = (category) => {
  editForm.selectedCategoryName = category.name
  editForm.selectedCategoryId = category.id
  showEditCategoryDropdown.value = false
}



const handleEditImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    editForm.productImage = file
    editForm.previewImage = URL.createObjectURL(file)
  }
}

const confirmRestore = (productId, productName) => {
  dialog.warning({
    title: 'Xác nhận khôi phục',
    content: `Bạn có chắc chắn muốn khôi phục sản phẩm "${productName}" không? Sản phẩm sẽ hiển thị lại cho khách hàng.`,
    positiveText: 'Khôi phục',
    negativeText: 'Hủy',
    onPositiveClick: () => restoreProduct(productId),
  })
}

const restoreProduct = async (productId) => {
  try {
    const response = await axios.put(`/api/products/restore/${productId}`)
    if (response.data.message) {
      message.success(response.data.message)
    } else {
      message.success('Đã khôi phục sản phẩm thành công')
    }
    await fetchProducts()
  } catch (error) {
    console.error('Restore error:', error)
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('Không thể khôi phục sản phẩm')
    }
  }
}

const confirmDelete = (productId, productName) => {
  dialog.warning({
    title: 'Xác nhận xóa sản phẩm',
    content: `Bạn có chắc chắn muốn xóa sản phẩm "${productName}" không? Sản phẩm sẽ không thể khôi phục.`,
    positiveText: 'Xóa sản phẩm',
    negativeText: 'Hủy',
    onPositiveClick: () => deleteProduct(productId),
  })
}

const deleteProduct = async (productId) => {
  try {
    const response = await axios.delete(`/api/products/${productId}`)
    if (response.data.message) {
      message.success(response.data.message)
    } else {
      message.success('Đã xóa sản phẩm thành công')
    }
    await fetchProducts()
  } catch (error) {
    console.error('Delete error:', error)
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('Không thể xóa sản phẩm')
    }
  }
}

// Swiper initialization
const initSwiper = () => {
  if (swiperInstance) {
    swiperInstance.destroy()
  }

  nextTick(() => {
    if (swiperContainer.value) {
      swiperInstance = new Swiper('.swiper-table', {
        direction: 'horizontal',
        grabCursor: true,
        scrollbar: {
          hide: true
        },
        mousewheel: false,
        keyboard: false,
        allowTouchMove: true,
        resistance: true,
        resistanceRatio: 0.85,
        slidesPerView: 'auto',
        spaceBetween: 0,
        freeMode: true,
        freeModeSticky: true,
        freeModeMomentum: true,
        freeModeMomentumRatio: 0.25,
        freeModeMomentumVelocityRatio: 0.2,
        freeModeMomentumBounce: true,
        freeModeMomentumBounceRatio: 0.25
      })
    }
  })
}

// Status badge function
const getStatusBadgeClass = (isActive) => {
  return isActive ? 'status-badge badge-success' : 'status-badge badge-error'
}

// Load data on mount
onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<style scoped>
/* Custom scrollbar cho toàn bộ trang */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
}

::-webkit-scrollbar-corner {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

* {
  scrollbar-width: thin;
  scrollbar-color: #3b82f6 #f0f9ff;
}

.no-border-card :deep(.n-card__content) {
  border: none !important;
  box-shadow: none !important;
}

.no-border-card :deep(.n-card) {
  border: none !important;
  box-shadow: none !important;
}

/* Swiper Table Styles */
.swiper-table-container {
  width: 100%;
  height: 100%;
}

.swiper-table {
  width: 100%;
  height: 100%;
}

.swiper-slide {
  width: auto;
  height: auto;
}

.custom-table {
  min-width: 1200px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 250px 150px 150px 120px 300px 100px 200px;
  background-color: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.header-cell {
  padding: 12px 16px;
  font-weight: 600;
  color: #374151;
  text-align: center;
  border-right: 1px solid #e5e7eb;
}

.header-cell:last-child {
  border-right: none;
}

.table-body {
  background-color: white;
}

.table-row {
  display: grid;
  grid-template-columns: 250px 150px 150px 120px 300px 100px 200px;
  border-bottom: 1px solid #f3f4f6;
  transition: background-color 0.2s;
}

.table-row:hover {
  background-color: #f9fafb;
}

.table-row:last-child {
  border-bottom: none;
}

.table-cell {
  padding: 12px 16px;
  border-right: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.table-cell:first-child {
  justify-content: flex-start;
}

.table-cell:last-child {
  border-right: none;
  justify-content: center;
}

/* Status Badges */
.status-badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
}

.badge-success {
  background-color: #d1fae5;
  color: #065f46;
}

.badge-error {
  background-color: #fee2e2;
  color: #991b1b;
}

/* Action Buttons */
.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn:hover {
  background-color: #2563eb;
}

.action-btn-delete {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background-color: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn-delete:hover {
  background-color: #dc2626;
}

.action-btn-restore {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn-restore:hover {
  background-color: #059669;
}

/* Pagination Styles */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  padding: 8px 12px;
  background-color: #f3f4f6;
  color: #6b7280;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #e5e7eb;
}

.pagination-btn:disabled {
  background-color: #f3f4f6;
  cursor: not-allowed;
  color: #9ca3af;
}

.pagination-btn-active {
  padding: 8px 12px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: default;
}
</style>
