<template>
  <div class="mt-2">
    <!-- Form tạo ảnh mới -->
    <div class="mb-6">
      <h2 class="text-lg font-semibold mb-4">Thêm Ảnh Mới</h2>
      <form @submit.prevent="createImage">
        <div v-if="successMessage" class="mt-4 border border-green-500 text-green-500 py-3 px-4 rounded">
          {{ successMessage }}
        </div>
        <div v-if="errorMessage" class="mt-4 border border-red-500 text-red-500 py-3 px-4 rounded">
          {{ errorMessage }}
        </div>

        <div class="mt-4 flex gap-4">
          <div class="w-[50%]">
            <label class="block mb-2">Sản phẩm:</label>
            <select
              v-model="newImage.productId"
              @change="loadVariantsForProduct"
              class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
              required
            >
              <option value="">Chọn sản phẩm</option>
              <option v-for="product in products" :key="product.id" :value="product.id">
                {{ product.name }}
              </option>
            </select>
          </div>
          <div class="w-[50%]">
            <label class="block mb-2">Biến thể (Tùy chọn):</label>
            <select
              v-model="newImage.productVariantId"
              class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
            >
              <option value="">Không có biến thể</option>
              <option v-for="variant in productVariants" :key="variant.id" :value="variant.id">
                {{ variant.variantName }}: {{ variant.variantValue }}
              </option>
            </select>
          </div>
        </div>

        <div class="mt-4">
          <label class="block mb-2">Chọn file ảnh:</label>
          <input
            type="file"
            @change="handleImageChange"
            accept="image/*"
            class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
            required
          />
        </div>

        <!-- Preview selected file -->
        <div v-if="selectedFile" class="mt-4">
          <label class="block mb-2 text-sm font-medium">File đã chọn:</label>
          <div class="bg-gray-50 p-3 rounded border">
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-700">{{ selectedFile.name }}</span>
              <button type="button" @click="clearFile" class="text-red-500 hover:text-red-700 text-sm">×</button>
            </div>
          </div>
        </div>

        <div class="mt-8">
          <button
            type="submit"
            :disabled="isLoading || !selectedFile"
            class="w-full py-2 px-3 rounded border border-blue-600 hover:bg-blue-600 hover:text-white text-blue-600 disabled:opacity-50"
          >
            <span v-if="isLoading">Đang tạo...</span>
            <span v-else>Thêm Ảnh</span>
          </button>
        </div>
      </form>
    </div>

    <!-- Filter và danh sách ảnh -->
    <div>
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-lg font-semibold">Danh sách Ảnh Sản phẩm</h2>
        <div class="text-sm text-gray-600">
          Tổng số: <span class="font-semibold text-blue-600">{{ images.length }}</span> ảnh
        </div>
      </div>

      <!-- Filter -->
      <div class="mb-4 flex gap-4">
        <div class="w-[50%]">
          <label class="block mb-2">Lọc theo sản phẩm:</label>
          <select
            v-model="selectedProductId"
            @change="loadImages"
            class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          >
            <option value="">Tất cả sản phẩm</option>
            <option v-for="product in products" :key="product.id" :value="product.id">
              {{ product.name }}
            </option>
          </select>
        </div>
        <div class="w-[50%]">
          <label class="block mb-2">Loại ảnh:</label>
          <select
            v-model="imageType"
            @change="loadImages"
            class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          >
            <option value="all">Tất cả ảnh</option>
            <option value="main">Ảnh chính sản phẩm</option>
            <option value="variant">Ảnh biến thể</option>
          </select>
        </div>
      </div>

      <!-- Grid ảnh -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        <div v-for="image in images" :key="image.id" class="border border-gray-200 rounded p-4">
          <div class="relative">
            <img
              :src="image.imageUrl"
              :alt="`Ảnh sản phẩm ${image.id}`"
              class="w-full h-48 object-cover rounded mb-3"
            />
            <div class="absolute top-2 right-2 flex gap-1">
              <button
                @click="startEditImage(image)"
                class="bg-blue-500 text-white rounded-full w-6 h-6 flex items-center justify-center hover:bg-blue-600"
                title="Chỉnh sửa"
              >
                ✏️
              </button>
              <button
                @click="deleteImage(image.id)"
                class="bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center hover:bg-red-600"
                title="Xóa"
              >
                ×
              </button>
            </div>
          </div>
          <div class="text-sm text-gray-600">
            <p class="font-medium text-gray-800 mb-1">{{ getProductName(image.productId) }}</p>
            <p v-if="image.productVariantId" class="text-blue-600 text-xs mb-1">
              <span class="bg-blue-100 px-2 py-1 rounded">{{ getVariantName(image.productVariantId) }}</span>
            </p>
            <p v-else class="text-green-600 text-xs mb-1">
              <span class="bg-green-100 px-2 py-1 rounded">Ảnh chính</span>
            </p>
            <p class="text-xs text-gray-400">ID: {{ image.id.substring(0, 8) }}...</p>
          </div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="images.length === 0" class="text-center py-8">
        <p class="text-gray-500">Chưa có ảnh nào được thêm</p>
      </div>
    </div>

    <!-- Modal chỉnh sửa ảnh -->
    <div v-if="isEditModalOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-md mx-4">
        <h3 class="text-lg font-semibold mb-4">Chỉnh sửa thông tin ảnh</h3>

        <form @submit.prevent="updateImage">
          <div v-if="editSuccessMessage" class="mb-4 border border-green-500 text-green-500 py-3 px-4 rounded">
            {{ editSuccessMessage }}
          </div>
          <div v-if="editErrorMessage" class="mb-4 border border-red-500 text-red-500 py-3 px-4 rounded">
            {{ editErrorMessage }}
          </div>

          <!-- Preview ảnh -->
          <div class="mb-4">
            <img :src="editingImage.imageUrl" :alt="editingImage.id" class="w-full h-48 object-cover rounded border" />
          </div>

          <!-- Chọn sản phẩm -->
          <div class="mb-4">
            <label class="block mb-2 text-sm font-medium">Sản phẩm:</label>
            <select
              v-model="editingImage.productId"
              @change="loadVariantsForEdit"
              class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
              required
            >
              <option value="">Chọn sản phẩm</option>
              <option v-for="product in products" :key="product.id" :value="product.id">
                {{ product.name }}
              </option>
            </select>
          </div>

          <!-- Chọn biến thể -->
          <div class="mb-4">
            <label class="block mb-2 text-sm font-medium">Biến thể (Tùy chọn):</label>
            <select
              v-model="editingImage.productVariantId"
              class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
            >
              <option value="">Không có biến thể</option>
              <option v-for="variant in editVariants" :key="variant.id" :value="variant.id">
                {{ variant.variantName }}: {{ variant.variantValue }}
              </option>
            </select>
          </div>

          <!-- Buttons -->
          <div class="flex gap-3">
            <button
              type="submit"
              :disabled="isEditLoading"
              class="flex-1 py-2 px-4 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
            >
              <span v-if="isEditLoading">Đang lưu...</span>
              <span v-else>Lưu thay đổi</span>
            </button>
            <button
              type="button"
              @click="cancelEdit"
              :disabled="isEditLoading"
              class="flex-1 py-2 px-4 border border-gray-300 text-gray-700 rounded hover:bg-gray-50 disabled:opacity-50"
            >
              Hủy
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const products = ref([])
const productVariants = ref([])
const editVariants = ref([])
const images = ref([])
const selectedProductId = ref('')
const imageType = ref('all')
const selectedFile = ref(null)
const isLoading = ref(false)

// Edit modal refs
const isEditModalOpen = ref(false)
const isEditLoading = ref(false)
const editSuccessMessage = ref('')
const editErrorMessage = ref('')
const editingImage = ref({
  id: null,
  imageUrl: '',
  productId: '',
  productVariantId: '',
})
const successMessage = ref('')
const errorMessage = ref('')

const newImage = ref({
  productId: '',
  productVariantId: '',
})

onMounted(() => {
  loadProducts()
  loadImages()
})

const loadProducts = async () => {
  try {
    const response = await axios.get('/api/products/all')
    products.value = response.data.products || []
  } catch (error) {
    console.error('Lỗi khi tải danh sách sản phẩm:', error)
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
    console.error('Lỗi khi tải biến thể:', error)
  }
}

const loadImages = async () => {
  try {
    let url = '/api/product-images/all' // Lấy tất cả ảnh của shop

    if (selectedProductId.value) {
      if (imageType.value === 'main') {
        url = `/api/product-images/product/${selectedProductId.value}/main`
      } else if (imageType.value === 'variant') {
        url = `/api/product-images/product/${selectedProductId.value}/variants`
      } else {
        url = `/api/product-images/product/${selectedProductId.value}`
      }
    } else {
      // Khi không chọn sản phẩm cụ thể, vẫn có thể lọc theo loại ảnh
      if (imageType.value === 'main' || imageType.value === 'variant') {
        // Cần lọc ở frontend vì không có endpoint lấy tất cả ảnh main/variant của shop
        url = '/api/product-images/all'
      }
    }

    console.log('Loading images from:', url)
    const response = await axios.get(url)
    console.log('Images response:', response.data)

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
    console.log('Loaded images:', images.value.length)

    // Load variants cho tất cả sản phẩm có trong danh sách ảnh
    await loadAllVariantsForImages()
  } catch (error) {
    console.error('Lỗi khi tải danh sách ảnh:', error)
    images.value = []
  }
}

const loadAllVariantsForImages = async () => {
  // Lấy danh sách productId unique từ images
  const uniqueProductIds = [...new Set(images.value.map((img) => img.productId))]

  // Load variants cho từng sản phẩm
  for (const productId of uniqueProductIds) {
    try {
      const response = await axios.get(`/api/product-variants/product/${productId}`)
      const variants = response.data.variants || []

      // Merge variants vào editVariants (để getVariantName có thể tìm thấy)
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

const handleImageChange = (event) => {
  selectedFile.value = event.target.files[0] || null
}

const clearFile = () => {
  selectedFile.value = null
}

const createImage = async () => {
  if (!selectedFile.value) {
    errorMessage.value = 'Vui lòng chọn file ảnh!'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
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

    console.log('Creating product image with data:', imageData)

    await axios.post('/api/product-images/create', imageData)

    successMessage.value = 'Thêm ảnh thành công!'
    resetForm()
    loadImages()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Có lỗi xảy ra khi upload ảnh!'
    console.error(error)
  } finally {
    isLoading.value = false
  }
}

const deleteImage = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa ảnh này?')) {
    try {
      await axios.delete(`/api/product-images/delete/${id}`)
      successMessage.value = 'Xóa ảnh thành công!'
      loadImages()
    } catch (error) {
      errorMessage.value = error.response?.data?.message || 'Có lỗi xảy ra!'
    }
  }
}

const resetForm = () => {
  newImage.value = {
    productId: '',
    productVariantId: '',
  }
  selectedFile.value = null
  productVariants.value = []
}

const getProductName = (productId) => {
  const product = products.value.find((p) => p.id === productId)
  return product ? product.name : `Sản phẩm (${productId.substring(0, 8)}...)`
}

const getVariantName = (variantId) => {
  const allVariants = [...productVariants.value, ...editVariants.value]
  const variant = allVariants.find((v) => v.id === variantId)
  return variant ? `${variant.variantName}: ${variant.variantValue}` : `Biến thể (${variantId.substring(0, 8)}...)`
}

// Edit functions
const startEditImage = (image) => {
  editingImage.value = {
    id: image.id,
    imageUrl: image.imageUrl,
    productId: image.productId,
    productVariantId: image.productVariantId || '',
  }

  // Load variants for the selected product
  if (image.productId) {
    loadVariantsForEdit()
  }

  isEditModalOpen.value = true
  editSuccessMessage.value = ''
  editErrorMessage.value = ''
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
    console.error('Lỗi khi tải biến thể cho edit:', error)
    editVariants.value = []
  }
}

const updateImage = async () => {
  if (!editingImage.value.productId) {
    editErrorMessage.value = 'Vui lòng chọn sản phẩm!'
    return
  }

  isEditLoading.value = true
  editErrorMessage.value = ''
  editSuccessMessage.value = ''

  try {
    const updateData = {
      productId: editingImage.value.productId,
      productVariantId: editingImage.value.productVariantId || null,
    }

    await axios.put(`/api/product-images/update/${editingImage.value.id}`, updateData)

    editSuccessMessage.value = 'Cập nhật thông tin ảnh thành công!'

    // Reload images list
    setTimeout(() => {
      loadImages()
      cancelEdit()
    }, 1500)
  } catch (error) {
    editErrorMessage.value = error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật!'
    console.error('Update image error:', error)
  } finally {
    isEditLoading.value = false
  }
}

const cancelEdit = () => {
  isEditModalOpen.value = false
  editSuccessMessage.value = ''
  editErrorMessage.value = ''
  editingImage.value = {
    id: null,
    imageUrl: '',
    productId: '',
    productVariantId: '',
  }
  editVariants.value = []
}
</script>

<style scoped>
button {
  color: #155dfc;
}
button:hover {
  color: white;
}
</style>
