<template>
  <n-space vertical :size="24">
    <!-- Form tạo ảnh mới -->
    <n-card title="Thêm Ảnh Mới" size="small">
      <n-form ref="formRef" :model="newImage" :rules="formRules" label-placement="top" @submit.prevent="createImage">
        <n-grid :cols="2" :x-gap="16" :y-gap="16">
          <n-form-item-gi label="Sản phẩm" path="productId">
            <n-select
              v-model:value="newImage.productId"
              placeholder="Chọn sản phẩm"
              :options="productOptions"
              @update:value="loadVariantsForProduct"
              filterable
            />
          </n-form-item-gi>

          <n-form-item-gi label="Biến thể (Tùy chọn)" path="productVariantId">
            <n-select
              v-model:value="newImage.productVariantId"
              placeholder="Không có biến thể"
              :options="variantOptions"
              clearable
              filterable
              :disabled="!newImage.productId"
            />
          </n-form-item-gi>

          <n-form-item-gi label="Chọn file ảnh" path="file" :span="2">
            <div class="upload-container">
              <n-space vertical :size="12">
                <n-upload
                  ref="uploadRef"
                  :max="1"
                  :default-upload="false"
                  accept="image/*"
                  @change="handleFileChange"
                  :show-file-list="false"
                  :disabled="isLoading"
                >
                  <n-button type="primary" ghost :loading="isLoading" style="width: 100%; height: 40px">
                    <template #icon>
                      <n-icon><Upload /></n-icon>
                    </template>
                    {{ isLoading ? 'Đang tải lên...' : 'Tải lên' }}
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
                        :disabled="isLoading"
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
          </n-form-item-gi>
        </n-grid>

        <n-space>
          <n-button
            type="primary"
            attr-type="submit"
            :loading="isLoading"
            :disabled="!selectedFile || !newImage.productId"
          >
            <template #icon>
              <n-icon><Plus /></n-icon>
            </template>
            Thêm Ảnh
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

    <!-- Danh sách ảnh -->
    <n-card title="Danh sách Ảnh Sản phẩm" size="small">
      <template #header-extra>
        <n-space>
          <n-text depth="3">Tổng: {{ images.length }} ảnh</n-text>
        </n-space>
      </template>

      <!-- Filter -->
      <n-space class="mb-6">
        <n-select
          v-model:value="selectedProductId"
          placeholder="Lọc theo sản phẩm"
          :options="[{ label: 'Tất cả sản phẩm', value: '' }, ...productOptions]"
          @update:value="loadImages"
          class="w-60"
          clearable
        />
        <n-select
          v-model:value="imageType"
          placeholder="Loại ảnh"
          :options="imageTypeOptions"
          @update:value="loadImages"
          class="w-40"
        />
      </n-space>

      <n-spin :show="loading">
        <!-- Grid ảnh -->
        <div v-if="!loading && images.length > 0" class="image-grid">
          <n-card v-for="image in images" :key="image.id" size="small" class="image-card">
            <template #cover>
              <div class="image-container">
                <n-image
                  :src="image.imageUrl"
                  :alt="`Ảnh sản phẩm ${image.id}`"
                  object-fit="cover"
                  class="product-image"
                />
                <div class="image-actions">
                  <n-button-group>
                    <n-button size="small" type="primary" @click="startEditImage(image)">
                      <template #icon>
                        <n-icon><Edit /></n-icon>
                      </template>
                    </n-button>
                    <n-button size="small" type="error" @click="confirmDelete(image.id, image.productId)">
                      <template #icon>
                        <n-icon><Trash2 /></n-icon>
                      </template>
                    </n-button>
                  </n-button-group>
                </div>
              </div>
            </template>

            <div class="image-info">
              <n-text strong class="product-name">
                {{ getProductName(image.productId) }}
              </n-text>
              <div class="mt-2">
                <n-tag v-if="image.productVariantId" size="small" type="info">
                  {{ getVariantName(image.productVariantId) }}
                </n-tag>
                <n-tag v-else size="small" type="success"> Ảnh chính </n-tag>
              </div>
              <n-text depth="3" class="text-xs mt-1"> ID: {{ image.id.substring(0, 8) }}... </n-text>
            </div>
          </n-card>
        </div>

        <!-- Empty state -->
        <n-empty v-else-if="!loading && images.length === 0" description="Chưa có ảnh nào được thêm">
          <template #icon>
            <n-icon size="48" color="#d1d5db">
              <ImageIcon />
            </n-icon>
          </template>
        </n-empty>
      </n-spin>
    </n-card>

    <!-- Edit Modal -->
    <n-modal v-model:show="isEditModalOpen" preset="card" title="Chỉnh sửa thông tin ảnh" :style="{ width: '450px' }">
      <n-form
        ref="editFormRef"
        :model="editingImage"
        :rules="editFormRules"
        label-placement="top"
        @submit.prevent="updateImage"
      >
        <!-- Preview ảnh -->
        <n-form-item label="Ảnh hiện tại">
          <n-image
            :src="editingImage.imageUrl"
            :alt="editingImage.id"
            width="100%"
            height="150"
            object-fit="cover"
            class="rounded"
          />
        </n-form-item>

        <!-- Chọn sản phẩm -->
        <n-form-item label="Sản phẩm" path="productId">
          <n-select
            v-model:value="editingImage.productId"
            placeholder="Chọn sản phẩm"
            :options="productOptions"
            @update:value="loadVariantsForEdit"
            filterable
          />
        </n-form-item>

        <!-- Chọn biến thể -->
        <n-form-item label="Biến thể (Tùy chọn)" path="productVariantId">
          <n-select
            v-model:value="editingImage.productVariantId"
            placeholder="Không có biến thể"
            :options="editVariantOptions"
            clearable
            filterable
            :disabled="!editingImage.productId"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="cancelEdit"> Hủy </n-button>
          <n-button type="primary" :loading="isEditLoading" @click="updateImage"> Lưu thay đổi </n-button>
        </n-space>
      </template>
    </n-modal>
  </n-space>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import axios from '../../utils/axios'
import { Plus, RotateCcw, Upload, Edit, Trash2, Image as ImageIcon, X } from 'lucide-vue-next'
import {
  NSpace,
  NCard,
  NForm,
  NFormItem,
  NFormItemGi,
  NGrid,
  NSelect,
  NUpload,
  NButton,
  NIcon,
  NSpin,
  NEmpty,
  NModal,
  NImage,
  NText,
  NTag,
  NButtonGroup,
  NAvatar,
  useMessage,
  useDialog,
} from 'naive-ui'

const message = useMessage()
const dialog = useDialog()

// Refs
const formRef = ref(null)
const editFormRef = ref(null)
const uploadRef = ref(null)

// State
const products = ref([])
const productVariants = ref([])
const editVariants = ref([])
const images = ref([])
const selectedProductId = ref('')
const imageType = ref('all')
const selectedFile = ref(null)
const isLoading = ref(false)
const loading = ref(false)

// Edit modal state
const isEditModalOpen = ref(false)
const isEditLoading = ref(false)
const editingImage = ref({
  id: null,
  imageUrl: '',
  productId: '',
  productVariantId: '',
})

const newImage = ref({
  productId: '',
  productVariantId: '',
})

// Form validation rules
const formRules = {
  productId: {
    required: true,
    message: 'Vui lòng chọn sản phẩm',
    trigger: ['blur', 'change'],
  },
}

const editFormRules = {
  productId: {
    required: true,
    message: 'Vui lòng chọn sản phẩm',
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

const variantOptions = computed(() => {
  return productVariants.value.map((variant) => ({
    label: `${variant.variantName}: ${variant.variantValue}`,
    value: variant.id,
  }))
})

const editVariantOptions = computed(() => {
  return editVariants.value.map((variant) => ({
    label: `${variant.variantName}: ${variant.variantValue}`,
    value: variant.id,
  }))
})

const imageTypeOptions = [
  { label: 'Tất cả ảnh', value: 'all' },
  { label: 'Ảnh chính sản phẩm', value: 'main' },
  { label: 'Ảnh biến thể', value: 'variant' },
]

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

const loadVariantsForProduct = async () => {
  if (!newImage.value.productId) {
    productVariants.value = []
    return
  }
  try {
    const response = await axios.get(`/api/product-variants/product/${newImage.value.productId}`)
    productVariants.value = response.data.variants || []
  } catch (error) {
    message.error('Không thể tải danh sách biến thể')
    console.error('Error:', error)
  }
}

const loadImages = async () => {
  loading.value = true
  try {
    let url = '/api/product-images/all'

    if (selectedProductId.value) {
      if (imageType.value === 'main') {
        url = `/api/product-images/product/${selectedProductId.value}/main`
      } else if (imageType.value === 'variant') {
        url = `/api/product-images/product/${selectedProductId.value}/variants`
      } else {
        url = `/api/product-images/product/${selectedProductId.value}`
      }
    } else {
      if (imageType.value === 'main' || imageType.value === 'variant') {
        url = '/api/product-images/all'
      }
    }

    const response = await axios.get(url)
    let allImages = response.data.images || []

    // Lọc theo loại ảnh nếu không chọn sản phẩm cụ thể
    if (!selectedProductId.value && imageType.value !== 'all') {
      if (imageType.value === 'main') {
        allImages = allImages.filter((img) => !img.productVariantId)
      } else if (imageType.value === 'variant') {
        allImages = allImages.filter((img) => img.productVariantId)
      }
    }

    images.value = allImages
    await loadAllVariantsForImages()
  } catch (error) {
    message.error('Không thể tải danh sách ảnh')
    console.error('Error:', error)
    images.value = []
  } finally {
    loading.value = false
  }
}

const loadAllVariantsForImages = async () => {
  const uniqueProductIds = [...new Set(images.value.map((img) => img.productId))]

  for (const productId of uniqueProductIds) {
    try {
      const response = await axios.get(`/api/product-variants/product/${productId}`)
      const variants = response.data.variants || []

      variants.forEach((variant) => {
        if (!editVariants.value.find((v) => v.id === variant.id)) {
          editVariants.value.push(variant)
        }
      })
    } catch (error) {
      console.error(`Lỗi khi tải biến thể cho sản phẩm ${productId}:`, error)
    }
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
  // Cleanup preview URL if exists
  if (selectedFilePreview.value) {
    URL.revokeObjectURL(selectedFilePreview.value)
  }

  selectedFile.value = null
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

const createImage = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (!selectedFile.value) {
      message.error('Vui lòng chọn file ảnh!')
      return
    }

    isLoading.value = true

    // Upload ảnh lên Cloudinary
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const uploadResponse = await axios.post('/api/upload-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    // Tạo product image với URL đã upload
    const imageData = {
      productId: newImage.value.productId,
      productVariantId: newImage.value.productVariantId || null,
      imageUrl: uploadResponse.data.imageUrl,
    }

    await axios.post('/api/product-images/create', imageData)

    message.success('Thêm ảnh thành công!')
    resetForm()
    await loadImages()
  } catch (error) {
    if (error.errors) {
      message.error('Vui lòng kiểm tra lại thông tin')
    } else {
      message.error(error.response?.data?.message || 'Có lỗi xảy ra khi upload ảnh!')
    }
  } finally {
    isLoading.value = false
  }
}

const confirmDelete = (id, productId) => {
  const productName = getProductName(productId)
  dialog.warning({
    title: 'Xác nhận xóa',
    content: `Bạn có chắc chắn muốn xóa ảnh của sản phẩm "${productName}" không?`,
    positiveText: 'Xóa',
    negativeText: 'Hủy',
    onPositiveClick: () => deleteImage(id),
  })
}

const deleteImage = async (id) => {
  try {
    await axios.delete(`/api/product-images/delete/${id}`)
    message.success('Xóa ảnh thành công!')
    await loadImages()
  } catch (error) {
    message.error(error.response?.data?.message || 'Có lỗi xảy ra!')
  }
}

const resetForm = () => {
  // Cleanup previous preview URL if exists
  if (selectedFilePreview.value) {
    URL.revokeObjectURL(selectedFilePreview.value)
  }

  newImage.value = {
    productId: '',
    productVariantId: '',
  }
  selectedFile.value = null
  productVariants.value = []

  if (uploadRef.value) {
    uploadRef.value.clear()
  }
  if (formRef.value) {
    formRef.value.restoreValidation()
  }
}

// Edit functions
const startEditImage = (image) => {
  editingImage.value = {
    id: image.id,
    imageUrl: image.imageUrl,
    productId: image.productId,
    productVariantId: image.productVariantId || '',
  }

  if (image.productId) {
    loadVariantsForEdit()
  }

  isEditModalOpen.value = true
}

const loadVariantsForEdit = async () => {
  if (!editingImage.value.productId) {
    editVariants.value = []
    return
  }

  try {
    const response = await axios.get(`/api/product-variants/product/${editingImage.value.productId}`)
    editVariants.value = response.data.variants || []
  } catch (error) {
    message.error('Không thể tải danh sách biến thể')
    console.error('Error:', error)
    editVariants.value = []
  }
}

const updateImage = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()

    isEditLoading.value = true

    const updateData = {
      productId: editingImage.value.productId,
      productVariantId: editingImage.value.productVariantId || null,
    }

    await axios.put(`/api/product-images/update/${editingImage.value.id}`, updateData)

    message.success('Cập nhật thông tin ảnh thành công!')
    await loadImages()
    cancelEdit()
  } catch (error) {
    if (error.errors) {
      message.error('Vui lòng kiểm tra lại thông tin')
    } else {
      message.error(error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật!')
    }
  } finally {
    isEditLoading.value = false
  }
}

const cancelEdit = () => {
  isEditModalOpen.value = false
  editingImage.value = {
    id: null,
    imageUrl: '',
    productId: '',
    productVariantId: '',
  }
  editVariants.value = []
}

// Helper functions
const getProductName = (productId) => {
  const product = products.value.find((p) => p.id === productId)
  return product ? product.name : `Sản phẩm (${productId.substring(0, 8)}...)`
}

const getVariantName = (variantId) => {
  const allVariants = [...productVariants.value, ...editVariants.value]
  const variant = allVariants.find((v) => v.id === variantId)
  return variant ? `${variant.variantName}: ${variant.variantValue}` : `Biến thể (${variantId.substring(0, 8)}...)`
}

// Load data on mount
onMounted(() => {
  loadProducts()
  loadImages()
})

onBeforeUnmount(() => {
  // Cleanup preview URL objects
  if (selectedFilePreview.value) {
    URL.revokeObjectURL(selectedFilePreview.value)
  }
})
</script>

<style scoped>
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.image-card {
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.image-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-container {
  position: relative;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 200px;
  transition: transform 0.3s;
}

.image-container:hover .product-image {
  transform: scale(1.05);
}

.image-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-container:hover .image-actions {
  opacity: 1;
}

.image-info {
  padding: 8px 0;
  line-height: 1.4;
}

.product-name {
  display: block;
  font-size: 14px;
  line-height: 1.4;
}

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

/* Mobile responsive */
@media (max-width: 768px) {
  .upload-container .n-button {
    height: 44px !important;
    font-size: 14px;
  }

  .preview-card {
    padding: 12px;
  }

  .file-name {
    font-size: 13px;
  }

  .file-size {
    font-size: 11px;
  }
}
</style>
