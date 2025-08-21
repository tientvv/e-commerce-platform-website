<template>
  <n-space vertical :size="24">
    <!-- Danh sách biến thể -->
      <n-card title="Danh sách Biến thể" size="small">
        <template #header-extra>
          <n-input
            v-model:value="searchKeyword"
            placeholder="Tìm kiếm biến thể..."
            class="w-60"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <n-icon><Search /></n-icon>
            </template>
          </n-input>
        </template>

        <n-spin :show="loading">
          <div v-if="!loading && filteredVariants.length > 0" class="swiper-table-container" ref="swiperContainer">
            <div class="swiper swiper-table">
              <div class="swiper-wrapper">
                <div class="swiper-slide">
                  <div class="custom-table">
                    <!-- Table Header -->
                    <div class="table-header">
                      <div class="header-cell">Sản phẩm</div>
                      <div class="header-cell">Tên biến thể</div>
                      <div class="header-cell">Giá trị</div>
                      <div class="header-cell">Số lượng</div>
                      <div class="header-cell">Giá</div>
                      <div class="header-cell">Trạng thái</div>
                      <div class="header-cell">Thêm ảnh</div>
                      <div class="header-cell">Thao tác</div>
                    </div>

                    <!-- Table Body -->
                    <div class="table-body">
                      <div v-for="variant in paginatedVariants" :key="variant.id" class="table-row">
                        <div class="table-cell">
                          <div class="font-medium whitespace-nowrap overflow-hidden text-ellipsis">
                            {{ getProductName(variant.productId) }}
                          </div>
                        </div>
                        <div class="table-cell">{{ variant.variantName }}</div>
                        <div class="table-cell">{{ variant.variantValue }}</div>
                        <div class="table-cell text-center">{{ variant.quantity }}</div>
                        <div class="table-cell">
                          <span class="font-semibold text-blue-600">{{ formatCurrency(variant.price) }}</span>
                        </div>
                        <div class="table-cell">
                          <span :class="getStatusBadgeClass(variant.isActive)">
                            {{ variant.isActive ? 'Hoạt động' : 'Không hoạt động' }}
                          </span>
                        </div>
                        <div class="table-cell">
                          <button @click="openAddImageDialog(variant)" class="action-btn-variant">
                            <ImageIcon class="w-4 h-4" />
                            Thêm ảnh
                          </button>
                        </div>
                        <div class="table-cell">
                          <div class="flex items-center gap-2">
                            <button @click="editVariant(variant)" class="action-btn-icon">
                              <Edit class="w-4 h-4" />
                            </button>
                            <button
                              @click="confirmDelete(variant.id, variant.variantName)"
                              class="action-btn-icon-delete"
                            >
                              <Trash2 class="w-4 h-4" />
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

          <!-- Empty State -->
          <n-empty v-else-if="!loading && filteredVariants.length === 0" description="Chưa có biến thể nào được tạo">
            <template #icon>
              <n-icon size="48" color="#d1d5db">
                <Settings />
              </n-icon>
            </template>
          </n-empty>
        </n-spin>

        <!-- Pagination -->
        <div v-if="!loading && filteredVariants.length > 0" class="pagination-container">
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
              :disabled="pagination.page * pagination.pageSize >= filteredVariants.length"
              class="pagination-btn"
            >
              Sau
            </button>
          </div>
        </div>
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

      <!-- Add Image Dialog -->
      <n-modal v-model:show="showAddImageDialog" preset="card" title="Thêm ảnh cho biến thể" :style="{ width: '500px' }">
        <div class="space-y-6">
          <!-- Success/Error Messages -->
          <div v-if="imageSuccessMessage" class="p-4 bg-green-50 border border-green-200 rounded-lg">
            <div class="flex items-center">
              <svg class="w-5 h-5 text-green-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"></path>
              </svg>
              <span class="text-green-800 font-medium">{{ imageSuccessMessage }}</span>
            </div>
          </div>

          <div v-if="imageErrorMessage" class="p-4 bg-red-50 border border-red-200 rounded-lg">
            <div class="flex items-center">
              <svg class="w-5 h-5 text-red-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"></path>
              </svg>
              <span class="text-red-800 font-medium">{{ imageErrorMessage }}</span>
            </div>
          </div>

          <!-- Variant Info -->
          <div class="bg-gray-50 p-4 rounded-lg">
            <h4 class="font-medium text-gray-900 mb-2">Biến thể: {{ selectedVariant?.variantName }} - {{ selectedVariant?.variantValue }}</h4>
            <p class="text-sm text-gray-600">Sản phẩm: {{ getProductName(selectedVariant?.productId) }}</p>
          </div>

          <!-- Upload Form -->
          <div class="upload-container">
            <n-space vertical :size="12">
              <n-upload
                ref="uploadRef"
                :max="1"
                :default-upload="false"
                accept="image/*"
                @change="handleFileChange"
                :show-file-list="false"
                :disabled="isImageLoading"
              >
                <n-button type="primary" ghost :loading="isImageLoading" style="width: 100%; height: 40px">
                  <template #icon>
                    <n-icon><Upload /></n-icon>
                  </template>
                  {{ isImageLoading ? 'Đang tải lên...' : 'Tải lên ảnh' }}
                </n-button>
              </n-upload>

              <!-- Preview selected file -->
              <div v-if="selectedFile" class="file-preview">
                <div class="preview-card">
                  <div class="file-info">
                    <n-avatar
                      :size="40"
                      :src="selectedFilePreview"
                      fallback-src="/default-image.png"
                      class="file-avatar"
                    />
                    <div class="file-details">
                      <n-text strong class="file-name">{{ selectedFile.name }}</n-text>
                      <n-text depth="3" class="file-size">{{ formatFileSize(selectedFile.size) }}</n-text>
                    </div>
                    <n-button
                      size="small"
                      text
                      type="error"
                      @click="clearSelectedFile"
                      class="remove-btn"
                      :disabled="isImageLoading"
                    >
                      <template #icon>
                        <n-icon><X /></n-icon>
                      </template>
                    </n-button>
                  </div>
                </div>
              </div>
            </n-space>
          </div>

          <!-- Dialog Actions -->
          <n-space justify="end">
            <n-button @click="closeAddImageDialog" size="medium">Hủy</n-button>
            <n-button
              type="primary"
              :loading="isImageLoading"
              size="medium"
              @click="addImage"
            >
              {{ isImageLoading ? 'Đang thêm...' : 'Thêm ảnh' }}
            </n-button>
          </n-space>
        </div>
      </n-modal>
    </n-space>
  </template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import Swiper from 'swiper'
import 'swiper/css'
import axios from '../../utils/axios'
import { Settings, Edit, Trash2, Image as ImageIcon, Upload, X, Search } from 'lucide-vue-next'
import {
  NSpace,
  NCard,
  NForm,
  NFormItem,

  NInput,
  NInputNumber,
  NButton,
  NIcon,
  NSpin,
  NEmpty,
  NModal,
  NSwitch,
  useMessage,
  useDialog,
} from 'naive-ui'

const message = useMessage()
const dialog = useDialog()

// Refs
const editFormRef = ref(null)
const swiperContainer = ref(null)
let swiperInstance = null

// State
const products = ref([])
const variants = ref([])
const selectedProductId = ref('')
const searchKeyword = ref('')
const showEditModal = ref(false)
const editingVariant = ref({})
const loading = ref(false)

// Add Image Dialog State
const showAddImageDialog = ref(false)
const selectedVariant = ref(null)
const selectedFile = ref(null)
const isImageLoading = ref(false)
const imageSuccessMessage = ref('')
const imageErrorMessage = ref('')
const uploadRef = ref(null)


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
    // Nếu đã chọn 1 sản phẩm cụ thể -> lấy theo sản phẩm đó
    if (selectedProductId.value) {
      const response = await axios.get(`/api/product-variants/product/${selectedProductId.value}`)
      variants.value = response.data.variants || []
      return
    }

    // Nếu chưa chọn sản phẩm -> chỉ lấy biến thể thuộc các sản phẩm của tài khoản hiện tại
    if (!products.value || products.value.length === 0) {
      variants.value = []
      return
    }

    const productIds = products.value.map(p => p.id)
    const requests = productIds.map(id => axios.get(`/api/product-variants/product/${id}`))
    const results = await Promise.allSettled(requests)

    const merged = []
    for (const r of results) {
      if (r.status === 'fulfilled') {
        const list = r.value?.data?.variants || []
        merged.push(...list)
      }
    }
    variants.value = merged
  } catch (error) {
    message.error('Không thể tải danh sách biến thể')
    console.error('Error:', error)
  } finally {
    loading.value = false
  }
}

// Add Image Dialog Methods
const openAddImageDialog = (variant) => {
  selectedVariant.value = variant
  showAddImageDialog.value = true
  resetImageForm()
}

const closeAddImageDialog = () => {
  showAddImageDialog.value = false
  selectedVariant.value = null
  resetImageForm()
}

const resetImageForm = () => {
  selectedFile.value = null
  imageSuccessMessage.value = ''
  imageErrorMessage.value = ''
  // Reset upload component
  if (uploadRef.value) {
    uploadRef.value.clear()
  }
}

const handleFileChange = ({ fileList }) => {
  const file = fileList.length > 0 ? fileList[0].file : null

  if (file) {
    // Validate file size (max 5MB)
    const maxSize = 5 * 1024 * 1024 // 5MB
    if (file.size > maxSize) {
      message.error('Kích thước file không được vượt quá 5MB!')
      return
    }
  }

  selectedFile.value = file
}

const clearSelectedFile = () => {
  selectedFile.value = null
  // Reset upload component
  if (uploadRef.value) {
    uploadRef.value.clear()
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const selectedFilePreview = computed(() => {
  if (!selectedFile.value) {
    return null
  }
  return URL.createObjectURL(selectedFile.value)
})

// Hàm chuẩn hóa tiếng Việt để tìm kiếm
const normalizeVietnamese = (str) => {
  return str
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '') // Loại bỏ dấu
    .replace(/[đ]/g, 'd') // Thay đ thành d
    .replace(/[Đ]/g, 'd') // Thay Đ thành d
}

// Computed để lọc biến thể theo từ khóa tìm kiếm
const filteredVariants = computed(() => {
  if (!searchKeyword.value.trim()) {
    return variants.value
  }

  const searchTerm = normalizeVietnamese(searchKeyword.value)

  return variants.value.filter(variant => {
    const productName = normalizeVietnamese(getProductName(variant.productId))
    const variantName = normalizeVietnamese(variant.variantName)
    const variantValue = normalizeVietnamese(variant.variantValue)

    return productName.includes(searchTerm) ||
           variantName.includes(searchTerm) ||
           variantValue.includes(searchTerm)
  })
})

// Computed cho pagination
const paginatedVariants = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredVariants.value.slice(start, end)
})

let searchTimeout = null

const handleSearch = () => {
  // Clear timeout trước đó
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  // Đợi 300ms sau khi người dùng ngừng gõ
  searchTimeout = setTimeout(() => {
    // Tìm kiếm được thực hiện tự động qua computed filteredVariants
  }, 300)
}

const addImage = async () => {
  if (!selectedFile.value) {
    imageErrorMessage.value = 'Vui lòng chọn file ảnh!'
    return
  }

  isImageLoading.value = true
  imageErrorMessage.value = ''

  try {
    // Upload ảnh lên Cloudinary
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const uploadResponse = await axios.post('/api/upload-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    // Tạo product image với URL đã upload
    const imageData = {
      productId: selectedVariant.value.productId,
      productVariantId: selectedVariant.value.id,
      imageUrl: uploadResponse.data.imageUrl,
    }

    await axios.post('/api/product-images/create', imageData)

    imageSuccessMessage.value = 'Thêm ảnh thành công!'
    setTimeout(() => {
      closeAddImageDialog()
    }, 2000)
  } catch (error) {
    if (error.response?.data?.message) {
      imageErrorMessage.value = error.response.data.message
    } else {
      imageErrorMessage.value = 'Có lỗi xảy ra khi upload ảnh!'
    }
  } finally {
    isImageLoading.value = false
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



// Helper functions
const getProductName = (productId) => {
  const product = products.value.find((p) => p.id === productId)
  return product ? product.name : 'Không xác định'
}

const getStatusBadgeClass = (isActive) => {
  return isActive
    ? 'bg-green-100 text-green-800 px-2 py-1 rounded-full text-xs font-medium'
    : 'bg-red-100 text-red-800 px-2 py-1 rounded-full text-xs font-medium'
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
onMounted(async () => {
  await loadProducts()
  await loadVariants()
  await nextTick()
  initSwiper()
})

// Initialize Swiper
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

onBeforeUnmount(() => {
  // Cleanup preview URL objects
  if (selectedFilePreview.value) {
    URL.revokeObjectURL(selectedFilePreview.value)
  }

  // Cleanup Swiper
  if (swiperInstance) {
    swiperInstance.destroy()
    swiperInstance = null
  }
})
</script>

<style scoped>
.upload-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.upload-container .n-button {
  transition: all 0.3s ease !important;
}

.upload-container .n-button:hover {
  border-color: #4f46e5 !important;
  background-color: #f8faff !important;
}

.file-preview {
  margin-top: 16px;
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.preview-card {
  background-color: #fafafa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.preview-card:hover {
  border-color: #d0d0d0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.file-avatar {
  flex-shrink: 0;
}

.file-details {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 2px;
}

.file-name {
  font-size: 14px;
  color: #333;
  word-break: break-all;
  line-height: 1.4;
}

.file-size {
  font-size: 12px;
  color: #666;
  line-height: 1.2;
}

.remove-btn {
  flex-shrink: 0;
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.remove-btn:hover {
  opacity: 1;
}

/* Swiper Table Container */
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

/* Custom Table Styles */
.custom-table {
  min-width: 1200px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  background: white;
}

.table-header {
  display: grid;
  grid-template-columns: 250px 150px 150px 120px 150px 120px 150px 150px;
  background-color: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.header-cell {
  padding: 12px 16px;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
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
  grid-template-columns: 250px 150px 150px 120px 150px 120px 150px 150px;
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
  font-size: 14px;
  color: #374151;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #f3f4f6;
}

.table-cell:first-child {
  justify-content: flex-start;
}

.table-cell:last-child {
  border-right: none;
  justify-content: center;
}

.table-cell.text-center {
  justify-content: center;
}

/* Action Buttons */
.action-btn-variant {
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
  transition: all 0.2s;
  white-space: nowrap;
  min-width: fit-content;
}

.action-btn-variant:hover {
  background-color: #2563eb;
}

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
  transition: all 0.2s;
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
  transition: all 0.2s;
}

.action-btn-icon-delete:hover {
  background-color: #dc2626;
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.pagination-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination-btn {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  background-color: white;
  color: #374151;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #f3f4f6;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn-active {
  padding: 8px 16px;
  border: 1px solid #3b82f6;
  background-color: #3b82f6;
  color: white;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
}
</style>
