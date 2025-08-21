<template>
  <n-space vertical :size="24">
    <!-- Danh sách ảnh -->
    <n-card title="Danh sách Ảnh Sản phẩm" size="small">
      <template #header-extra>
        <n-space>
          <n-text depth="3">Tổng: {{ filteredImages.length }} ảnh</n-text>
        </n-space>
      </template>

      <!-- Filter -->
      <n-space class="mb-6">
        <n-input
          v-model:value="searchKeyword"
          placeholder="Tìm kiếm sản phẩm..."
          class="w-60"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <n-icon><Search /></n-icon>
          </template>
        </n-input>
        <n-select
          v-model:value="imageType"
          placeholder="Loại ảnh"
          :options="imageTypeOptions"
          @update:value="loadImages"
          class="w-40"
          style="min-width: 140px;"
        />
      </n-space>

      <n-spin :show="loading">
        <!-- Grid ảnh -->
        <div v-if="!loading && filteredImages.length > 0" class="image-grid">
          <n-card v-for="image in filteredImages" :key="image.id" size="small" class="image-card">
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
                  {{ getVariantNameComputed(image.productVariantId) }}
                </n-tag>
                <n-tag v-else size="small" type="success"> Ảnh chính </n-tag>
              </div>

            </div>
          </n-card>
        </div>

        <!-- Empty state -->
        <n-empty v-else-if="!loading && filteredImages.length === 0" description="Chưa có ảnh nào được thêm">
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
import { ref, computed, onMounted } from 'vue'
import axios from '../../utils/axios'
import { Edit, Trash2, Image as ImageIcon, Search } from 'lucide-vue-next'
import {
  NSpace,
  NCard,
  NForm,
  NFormItem,
  NSelect,
  NInput,
  NButton,
  NIcon,
  NSpin,
  NEmpty,
  NModal,
  NImage,
  NText,
  NTag,
  NButtonGroup,
  useMessage,
  useDialog,
} from 'naive-ui'

const message = useMessage()
const dialog = useDialog()



const editFormRef = ref(null)

const products = ref([])
const productVariants = ref([])
const editVariants = ref([])
const images = ref([])
const selectedProductId = ref('')
const imageType = ref('all')
const searchKeyword = ref('')
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



const editVariantOptions = computed(() => {
  return editVariants.value.map((variant) => ({
    label: `${variant.variantName}: ${variant.variantValue}`,
    value: variant.id,
  }))
})

// Computed để lọc ảnh theo từ khóa tìm kiếm
const filteredImages = computed(() => {
  let filtered = images.value

  // Lọc theo từ khóa tìm kiếm
  if (searchKeyword.value.trim()) {
    const foundProduct = products.value.find(product =>
      product.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
    if (foundProduct) {
      filtered = filtered.filter(img => img.productId === foundProduct.id)
    } else {
      filtered = []
    }
  }

  return filtered
})

// Computed để hiển thị tên biến thể với reactive updates
const getVariantNameComputed = computed(() => {
  return (variantId) => {
    if (!variantId) return ''

    // Tìm trong editVariants trước (cho modal chỉnh sửa)
    let variant = editVariants.value.find((v) => v.id === variantId)

    // Nếu không tìm thấy, tìm trong productVariants
    if (!variant) {
      variant = productVariants.value.find((v) => v.id === variantId)
    }

    // Nếu vẫn không tìm thấy, tìm trong tất cả biến thể đã load
    if (!variant) {
      const allVariants = [...productVariants.value, ...editVariants.value]
      variant = allVariants.find((v) => v.id === variantId)
    }

    // Nếu tìm thấy biến thể, trả về tên đầy đủ
    if (variant) {
      return `${variant.variantName}: ${variant.variantValue}`
    }

    // Nếu không tìm thấy, trả về tên mặc định
    return 'Biến thể'
  }
})

const imageTypeOptions = [
  { label: 'Tất cả ảnh', value: 'all' },
  { label: 'Ảnh chính', value: 'main' },
  { label: 'Biến thể', value: 'variant' },
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

let searchTimeout = null

const handleSearch = () => {
  // Clear timeout trước đó
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }

  // Đợi 300ms sau khi người dùng ngừng gõ
  searchTimeout = setTimeout(() => {
    // Tìm sản phẩm theo từ khóa
    if (searchKeyword.value.trim()) {
      const foundProduct = products.value.find(product =>
        product.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
      )
      if (foundProduct) {
        selectedProductId.value = foundProduct.id
      } else {
        selectedProductId.value = ''
      }
    } else {
      selectedProductId.value = ''
    }
    // Không gọi loadImages() để tránh nháy màn hình
  }, 300)
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

  // Clear existing variants to avoid duplicates
  editVariants.value = []

  for (const productId of uniqueProductIds) {
    try {
      const response = await axios.get(`/api/product-variants/product/${productId}`)
      const variants = response.data.variants || []
      editVariants.value.push(...variants)
    } catch (error) {
      console.error(`Lỗi khi tải biến thể cho sản phẩm ${productId}:`, error)
    }
  }
}



// Đảm bảo biến thể được load cho một sản phẩm cụ thể
const ensureVariantsLoaded = async (productId) => {
  if (!productId) return

  // Kiểm tra xem biến thể đã được load chưa
  const existingVariants = editVariants.value.filter(v => v.productId === productId)
  if (existingVariants.length === 0) {
    try {
      const response = await axios.get(`/api/product-variants/product/${productId}`)
      const variants = response.data.variants || []
      editVariants.value.push(...variants)
    } catch (error) {
      console.error(`Lỗi khi tải biến thể cho sản phẩm ${productId}:`, error)
    }
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

// Edit functions
const startEditImage = async (image) => {
  editingImage.value = {
    id: image.id,
    imageUrl: image.imageUrl,
    productId: image.productId,
    productVariantId: image.productVariantId || '',
  }

  if (image.productId) {
    await ensureVariantsLoaded(image.productId)
    await loadVariantsForEdit()
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

    // Reload page
    window.location.reload()
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
  return product ? product.name : 'Sản phẩm'
}








// Load data on mount
onMounted(async () => {
  await loadProducts()
  await loadImages()
  await loadAllVariantsForImages()
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
