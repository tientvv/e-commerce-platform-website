<template>
  <div class="h-full flex flex-col">
    <!-- Products Table -->
    <div class="flex-1">
      <!-- Header with Search and Filters -->
      <div class="mb-6">

        <!-- Search and Filter Bar -->
        <div class="bg-white p-4 rounded-lg border border-gray-200">
          <div class="flex items-center gap-4 flex-wrap">
            <!-- Search Input -->
            <div class="flex items-center gap-2">
              <n-input
                v-model:value="searchTerm"
                placeholder="Tìm kiếm sản phẩm..."
                style="width: 250px"
                clearable
                @keyup.enter="handleSearch"
              >
                <template #prefix>
                  <Search class="w-4 h-4" />
                </template>
              </n-input>
              <n-button @click="handleSearch" type="primary" size="small">
                Tìm kiếm
              </n-button>
            </div>

            <!-- Category Filter -->
            <n-select
              v-model:value="selectedCategory"
              placeholder="Lọc theo danh mục"
              style="width: 220px"
              clearable
              :options="categoryOptions"
              @update:value="handleCategoryFilter"
              filterable
            />

            <!-- Refresh Button -->
            <n-button @click="refreshAndClearFilters" size="small">
              <template #icon>
                <RefreshCw class="w-4 h-4" />
              </template>
              Làm mới
            </n-button>

            <!-- Clear Filters Button -->
            <n-button @click="clearSearch" v-if="searchTerm || selectedCategory" size="small" type="default">
              Xóa lọc
            </n-button>
          </div>
        </div>
      </div>

      <n-card class="h-full flex flex-col no-border-card">

        <div class="flex-1">
          <div class="w-full h-full">
            <div class="w-full h-full">
              <div class="w-full">
                <!-- Search Results Info -->
                <div v-if="!loading && (searchTerm || selectedCategory) && products.length > 0" class="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
                  <div class="flex items-center">
                    <svg class="w-5 h-5 text-blue-500 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path>
                    </svg>
                    <span class="text-blue-800">
                      Tìm thấy {{ products.length }} sản phẩm
                      <span v-if="searchTerm"> với từ khóa "{{ searchTerm }}"</span>
                      <span v-if="selectedCategory"> trong danh mục "{{ getCategoryName(selectedCategory) }}"</span>
                    </span>
                  </div>
                </div>

                <div class="swiper-table-container" ref="swiperContainer">
                  <!-- Loading State -->
                  <div v-if="loading" class="flex items-center justify-center py-8">
                    <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                    <span class="ml-2 text-gray-600">Đang tải...</span>
                  </div>

                  <!-- Swiper Table -->
                  <div v-else-if="products.length > 0" class="swiper swiper-table">
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
                            <div class="header-cell">Thêm biến thể</div>
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
                                <button @click="openAddVariantDialog(product)" class="action-btn-variant">
                                  <Plus class="w-4 h-4" />
                                  Thêm biến thể
                                </button>
                              </div>
                              <div class="table-cell">
                                <div class="flex items-center gap-2">
                                  <button @click="editProduct(product)" class="action-btn-icon">
                                    <Edit class="w-4 h-4" />
                                  </button>
                                  <button
                                    v-if="product.isActive"
                                    @click="confirmDelete(product.id, product.name)"
                                    class="action-btn-icon-delete"
                                  >
                                    <Trash2 class="w-4 h-4" />
                                  </button>
                                  <button
                                    v-else
                                    @click="confirmRestore(product.id, product.name)"
                                    class="action-btn-icon-restore"
                                  >
                                    <RefreshCw class="w-4 h-4" />
                                  </button>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Empty State -->
                  <div v-else class="flex flex-col items-center justify-center py-12">
                    <div class="text-gray-400 mb-4">
                      <svg class="w-16 h-16" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z" clip-rule="evenodd"></path>
                      </svg>
                    </div>
                    <h3 class="text-lg font-medium text-gray-900 mb-2">
                      {{ getEmptyStateTitle() }}
                    </h3>
                    <p class="text-gray-500 text-center max-w-md">
                      {{ getEmptyStateDescription() }}
                    </p>
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

    <!-- Add Variant Dialog -->
    <n-modal v-model:show="showAddVariantDialog" preset="card" style="width: 600px" title="Thêm biến thể mới">
      <div class="space-y-6">
        <!-- Success/Error Messages -->
        <div v-if="variantSuccessMessage" class="p-4 bg-green-50 border border-green-200 rounded-lg">
          <div class="flex items-center">
            <svg class="w-5 h-5 text-green-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
            </svg>
            <span class="text-green-800 font-medium">{{ variantSuccessMessage }}</span>
          </div>
        </div>

        <div v-if="variantErrorMessage" class="p-4 bg-red-50 border border-red-200 rounded-lg">
          <div class="flex items-center">
            <svg class="w-5 h-5 text-red-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
            </svg>
            <span class="text-red-800 font-medium">{{ variantErrorMessage }}</span>
          </div>
        </div>

        <!-- Product Info -->
        <div class="bg-gray-50 p-4 rounded-lg">
          <h4 class="font-medium text-gray-900 mb-2">Sản phẩm: {{ selectedProduct?.name }}</h4>
          <p class="text-sm text-gray-600">{{ selectedProduct?.description }}</p>
        </div>

                <!-- Form -->
        <n-form
          ref="variantFormRef"
          :model="variantForm"
          :rules="variantFormRules"
          label-placement="top"
          @submit.prevent="addVariant"
        >
          <n-grid :cols="2" :x-gap="16" :y-gap="16">
            <n-form-item-gi label="Tên biến thể" path="variantName">
              <n-input
                v-model:value="variantForm.variantName"
                placeholder="VD: Màu sắc, Kích thước"
              />
            </n-form-item-gi>

            <n-form-item-gi label="Giá trị biến thể" path="variantValue">
              <n-input
                v-model:value="variantForm.variantValue"
                placeholder="VD: Đỏ, XL"
              />
            </n-form-item-gi>

            <n-form-item-gi label="Số lượng" path="quantity">
              <n-input-number
                v-model:value="variantForm.quantity"
                :min="0"
                placeholder="Nhập số lượng"
                class="w-full"
              />
            </n-form-item-gi>

            <n-form-item-gi label="Trạng thái" path="isActive">
              <n-select
                v-model:value="variantForm.isActive"
                :options="[
                  { label: 'Hoạt động', value: true },
                  { label: 'Không hoạt động', value: false }
                ]"
              />
            </n-form-item-gi>

            <n-form-item-gi label="Giá (VNĐ)" path="price" :span="2">
              <n-input-number
                v-model:value="variantForm.price"
                :min="0"
                :step="1000"
                placeholder="Nhập giá sản phẩm"
                class="w-full"
                :format="formatPrice"
                :parse="parsePrice"
              />
            </n-form-item-gi>
          </n-grid>

          <!-- Dialog Actions -->
          <n-space class="mt-6" justify="end">
            <n-button @click="closeAddVariantDialog" size="medium">Hủy</n-button>
            <n-button
              type="primary"
              :loading="variantLoading"
              size="medium"
              @click="addVariant"
            >
              {{ variantLoading ? 'Đang thêm...' : 'Thêm biến thể' }}
            </n-button>
          </n-space>
        </n-form>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, reactive } from 'vue'
import axios from '../../utils/axios'
import { Edit, Trash2, RefreshCw, Plus, Search } from 'lucide-vue-next'
import { NCard, NButton, NModal, NForm, NFormItemGi, NGrid, NInput, NInputNumber, NSelect, NSpace, useMessage, useDialog } from 'naive-ui'
import Swiper from 'swiper'
import 'swiper/css'

const message = useMessage()
const dialog = useDialog()

const products = ref([])
const loading = ref(true)

// Search and filter state
const searchTerm = ref('')
const selectedCategory = ref(null)

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

// Computed properties for filter options
const categoryOptions = computed(() => {
  return categories.value.map(category => ({
    label: category.name,
    value: category.id
  }))
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

// Add Variant Dialog
const showAddVariantDialog = ref(false)
const selectedProduct = ref(null)
const variantForm = reactive({
  variantName: '',
  variantValue: '',
  quantity: 0,
  price: 0,
  isActive: true,
})
const variantSuccessMessage = ref('')
const variantErrorMessage = ref('')
const variantLoading = ref(false)
const variantFormRef = ref(null)

// Validation rules
const variantFormRules = {
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

// Helper functions
const formatPrice = (value) => {
  if (!value) return '0'
  return new Intl.NumberFormat('vi-VN').format(value)
}

const parsePrice = (input) => {
  return parseInt(input.replace(/\D/g, '')) || 0
}

const fetchProducts = async (search = '', categoryId = '') => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (search && typeof search === 'string' && search.trim()) {
      params.append('search', search.trim())
    }
    if (categoryId) {
      params.append('categoryId', categoryId)
    }

    const url = `/api/products/user${params.toString() ? '?' + params.toString() : ''}`
    const response = await axios.get(url)
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

// Search and filter methods
const handleSearch = () => {
  fetchProducts(searchTerm.value, selectedCategory.value)
}

const handleCategoryFilter = () => {
  fetchProducts(searchTerm.value, selectedCategory.value)
}

const clearSearch = () => {
  searchTerm.value = ''
  selectedCategory.value = null
  fetchProducts()
}

const refreshAndClearFilters = () => {
  searchTerm.value = ''
  selectedCategory.value = null
  fetchProducts()
}

const getCategoryName = (categoryId) => {
  const category = categories.value.find(cat => cat.id === categoryId)
  return category ? category.name : 'Không xác định'
}



const getEmptyStateTitle = () => {
  if (searchTerm.value || selectedCategory.value) {
    return 'Không tìm thấy sản phẩm'
  }
  return 'Chưa có sản phẩm nào'
}

const getEmptyStateDescription = () => {
  if (searchTerm.value || selectedCategory.value) {
    return 'Không có sản phẩm nào phù hợp với bộ lọc hiện tại. Hãy thử thay đổi điều kiện tìm kiếm.'
  }
  return 'Bắt đầu thêm sản phẩm đầu tiên vào cửa hàng của bạn.'
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

// Variant methods
const openAddVariantDialog = (product) => {
  selectedProduct.value = product
  showAddVariantDialog.value = true
  resetVariantForm()
}

const closeAddVariantDialog = () => {
  showAddVariantDialog.value = false
  selectedProduct.value = null
  resetVariantForm()
}

const resetVariantForm = () => {
  variantForm.variantName = ''
  variantForm.variantValue = ''
  variantForm.quantity = 0
  variantForm.price = 0
  variantForm.isActive = true
  variantSuccessMessage.value = ''
  variantErrorMessage.value = ''
}



const addVariant = async () => {
  if (!variantFormRef.value) return

  try {
    await variantFormRef.value.validate()
    variantLoading.value = true
    variantErrorMessage.value = ''

    const variantData = {
      variantName: variantForm.variantName.trim(),
      variantValue: variantForm.variantValue.trim(),
      quantity: variantForm.quantity,
      price: variantForm.price,
      isActive: variantForm.isActive,
      productId: selectedProduct.value.id
    }

    const response = await axios.post('/api/product-variants/create', variantData, {
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (response.data.message) {
      variantSuccessMessage.value = response.data.message
      setTimeout(() => {
        closeAddVariantDialog()
      }, 2000)
    } else {
      variantErrorMessage.value = 'Thêm biến thể thất bại'
    }
  } catch (error) {
    if (error.errors) {
      variantErrorMessage.value = 'Vui lòng kiểm tra lại thông tin'
    } else if (error.response?.data?.message) {
      variantErrorMessage.value = error.response.data.message
    } else if (error.response?.data?.errorMessage) {
      variantErrorMessage.value = error.response.data.errorMessage
    } else {
      variantErrorMessage.value = 'Không thể thêm biến thể'
    }
  } finally {
    variantLoading.value = false
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
}

.table-header {
  display: grid;
  grid-template-columns: 250px 150px 150px 120px 300px 100px 150px 150px;
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
  grid-template-columns: 250px 150px 150px 120px 300px 100px 150px 150px;
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

.action-btn-variant {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background-color: #8b5cf6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
  min-width: fit-content;
}

.action-btn-variant:hover {
  background-color: #7c3aed;
}

/* Icon Action Buttons */
.action-btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn-icon:hover {
  background-color: #2563eb;
}

.action-btn-icon-delete {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background-color: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn-icon-delete:hover {
  background-color: #dc2626;
}

.action-btn-icon-restore {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background-color: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn-icon-restore:hover {
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
