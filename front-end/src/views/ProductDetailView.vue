<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <main class="flex-1 max-w-[1440px] w-full mx-auto px-4 py-6">
      <!-- Breadcrumb -->
      <nav class="mb-6">
        <ol class="flex items-center space-x-2 text-sm text-gray-500">
          <li>
            <RouterLink to="/" class="hover:text-blue-600">Trang chủ</RouterLink>
          </li>
          <li class="flex items-center">
            <ChevronRight class="w-4 h-4 mx-2" />
            <RouterLink :to="`/category/${product?.categoryId}`" class="hover:text-blue-600">
              {{ product?.categoryName || 'Danh mục' }}
            </RouterLink>
          </li>
          <li class="flex items-center">
            <ChevronRight class="w-4 h-4 mx-2" />
            <span class="text-gray-800 font-medium">{{ product?.name || 'Sản phẩm' }}</span>
          </li>
        </ol>
      </nav>

      <!-- Loading State -->
      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="text-center py-20">
        <AlertCircle class="w-20 h-20 text-red-500 mx-auto mb-4" />
        <h3 class="text-xl font-medium text-gray-800 mb-2">Không thể tải thông tin sản phẩm</h3>
        <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
        <button
          @click="fetchProduct"
          class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Thử lại
        </button>
      </div>

      <!-- Product Detail -->
      <div v-else-if="product" class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- Product Images -->
        <div class="space-y-4">
          <!-- Main Image -->
          <div class="aspect-square bg-gray-100 rounded-lg overflow-hidden">
            <img
              v-if="product.productImage"
              :src="product.productImage"
              :alt="product.name"
              class="w-full h-full object-cover"
              @error="handleImageError"
            />
            <div
              v-else
              class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200"
            >
              <Package class="w-16 h-16 text-gray-400" />
            </div>
          </div>

          <!-- Additional Images (if any) -->
          <div v-if="productImages.length > 0" class="grid grid-cols-4 gap-2">
            <div
              v-for="image in productImages"
              :key="image.id"
              class="aspect-square bg-gray-100 rounded-lg overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
              @click="selectImage(image.imageUrl)"
            >
              <img :src="image.imageUrl" :alt="product.name" class="w-full h-full object-cover" />
            </div>
          </div>
        </div>

        <!-- Product Info -->
        <div class="space-y-6">
          <!-- Product Header -->
          <div>
            <h1 class="text-3xl font-bold text-gray-800 mb-2">{{ product.name }}</h1>
            <p class="text-lg text-gray-600 mb-4">{{ product.brand }}</p>

            <!-- Rating -->
            <div class="flex items-center mb-4">
              <div class="flex items-center mr-2">
                <Star
                  v-for="i in 5"
                  :key="i"
                  :class="i <= (product.rating || 0) ? 'text-yellow-400 fill-current' : 'text-gray-300'"
                  class="w-5 h-5"
                />
              </div>
              <span class="text-sm text-gray-600">
                {{ product.rating || 0 }}/5 ({{ product.reviewCount || 0 }} đánh giá)
              </span>
            </div>

            <!-- Shop Info -->
            <div class="flex items-center text-sm text-gray-600 mb-4">
              <Store class="w-4 h-4 mr-2" />
              <span>{{ product.shopName }}</span>
            </div>
          </div>

          <!-- Price Section -->
          <div class="border-t border-gray-200 pt-6">
            <div class="flex items-center space-x-4 mb-4">
              <span v-if="product.minPrice && product.minPrice > 0" class="text-3xl font-bold text-blue-600">
                {{ formatPrice(product.minPrice) }}
              </span>
              <span v-else class="text-3xl font-bold text-gray-500">Liên hệ</span>

              <span
                v-if="product.originalPrice && product.originalPrice > product.minPrice"
                class="text-xl text-gray-500 line-through"
              >
                {{ formatPrice(product.originalPrice) }}
              </span>
            </div>

            <!-- Discount Badge -->
            <div v-if="product.discountPercentage && product.discountPercentage > 0" class="mb-4">
              <span class="inline-block bg-red-100 text-red-800 text-sm px-3 py-1 rounded-full font-medium">
                -{{ product.discountPercentage }}%
              </span>
            </div>
          </div>

          <!-- Product Variants -->
          <div v-if="productVariants.length > 0" class="border-t border-gray-200 pt-6">
            <h3 class="text-lg font-semibold text-gray-800 mb-4">Tùy chọn sản phẩm</h3>
            <div class="space-y-3">
              <div
                v-for="variant in productVariants"
                :key="variant.id"
                :class="[
                  'flex items-center justify-between p-4 border rounded-lg transition-colors cursor-pointer',
                  selectedVariant?.id === variant.id
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-blue-300',
                ]"
                @click="selectVariant(variant)"
              >
                <div>
                  <p class="font-medium text-gray-800">{{ variant.variantName }}: {{ variant.variantValue }}</p>
                  <p class="text-sm text-gray-600">Số lượng: {{ variant.quantity }}</p>
                </div>
                <div class="text-right">
                  <p class="font-bold text-blue-600">{{ formatPrice(variant.price) }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Description -->
          <div class="border-t border-gray-200 pt-6">
            <h3 class="text-lg font-semibold text-gray-800 mb-4">Mô tả sản phẩm</h3>
            <div class="prose max-w-none">
              <p class="text-gray-700 leading-relaxed">{{ product.description || 'Chưa có mô tả' }}</p>
            </div>
          </div>

          <!-- Product Stats -->
          <div class="border-t border-gray-200 pt-6">
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div class="flex items-center text-gray-600">
                <Eye class="w-4 h-4 mr-2" />
                <span>{{ product.viewCount || 0 }} lượt xem</span>
              </div>
              <div class="flex items-center text-gray-600">
                <ShoppingCart class="w-4 h-4 mr-2" />
                <span>{{ product.soldCount || 0 }} đã bán</span>
              </div>
            </div>
          </div>

          <!-- Quantity Selector -->
          <div v-if="product.minPrice && product.minPrice > 0" class="border-t border-gray-200 pt-6">
            <div class="flex items-center space-x-4 mb-4">
              <span class="text-sm font-medium text-gray-700">Số lượng:</span>
              <div class="flex items-center space-x-2">
                <button
                  @click="quantity = Math.max(1, quantity - 1)"
                  class="w-8 h-8 flex items-center justify-center border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  <Minus class="w-4 h-4" />
                </button>
                <span class="w-12 text-center font-medium">{{ quantity }}</span>
                <button
                  @click="quantity++"
                  class="w-8 h-8 flex items-center justify-center border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  <Plus class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="border-t border-gray-200 pt-6">
            <div class="flex space-x-4">
              <button
                v-if="product.minPrice && product.minPrice > 0"
                class="flex-1 bg-blue-600 text-white py-3 px-6 rounded-lg hover:bg-blue-700 transition-colors font-medium"
                @click="handleAddToCart"
              >
                Thêm vào giỏ hàng
              </button>
              <button
                v-else
                class="flex-1 bg-gray-600 text-white py-3 px-6 rounded-lg hover:bg-gray-700 transition-colors font-medium"
              >
                Liên hệ mua
              </button>

              <button
                class="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <Heart class="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Toast Notification -->
    <div
      v-if="showToast"
      :class="[
        'fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg transition-all duration-300 transform',
        toastType === 'success' ? 'bg-green-500 text-white' : 'bg-red-500 text-white',
      ]"
    >
      <div class="flex items-center space-x-2">
        <div v-if="toastType === 'success'" class="w-5 h-5">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
          </svg>
        </div>
        <div v-else class="w-5 h-5">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </div>
        <span class="font-medium">{{ toastMessage }}</span>
      </div>
    </div>

    <FooterView />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import axios from 'axios'
import { Package, AlertCircle, ChevronRight, Star, Store, Eye, ShoppingCart, Heart, Plus, Minus } from 'lucide-vue-next'
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'
import { useCart } from '../composables/useCart'

const route = useRoute()
const product = ref(null)
const productVariants = ref([])
const productImages = ref([])
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')
const selectedVariant = ref(null)
const quantity = ref(1)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

const { addItem } = useCart()

const fetchProduct = async () => {
  try {
    loading.value = true
    error.value = false

    const productId = route.params.id

    // Fetch product details
    const productResponse = await axios.get(`/api/products/${productId}`)
    product.value = productResponse.data

    // Fetch product variants
    try {
      const variantsResponse = await axios.get(`/api/product-variants/product/${productId}`)
      productVariants.value = variantsResponse.data.variants || []

      // Mặc định chọn biến thể đầu tiên nếu có
      if (productVariants.value.length > 0) {
        selectedVariant.value = productVariants.value[0]
      }
    } catch {
      console.log('No variants found for this product')
    }

    // Fetch product images
    try {
      const imagesResponse = await axios.get(`/api/product-images/product/${productId}`)
      productImages.value = imagesResponse.data.images || []
    } catch {
      console.log('No additional images found for this product')
    }
  } catch (err) {
    console.error('Error fetching product:', err)
    error.value = true
    errorMessage.value = err.response?.data?.message || 'Đã xảy ra lỗi khi tải thông tin sản phẩm'
  } finally {
    loading.value = false
  }
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
  event.target.parentElement.innerHTML = `
    <div class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
      <svg class="w-16 h-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
      </svg>
    </div>
  `
}

const selectImage = (imageUrl) => {
  if (product.value) {
    product.value.productImage = imageUrl
  }
}

const formatPrice = (price) => {
  if (!price) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

const showToastMessage = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true

  // Tự động ẩn toast sau 3 giây
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

const handleAddToCart = () => {
  if (!product.value) return

  // Nếu có biến thể và chưa chọn, hiển thị thông báo
  if (productVariants.value.length > 0 && !selectedVariant.value) {
    showToastMessage('Vui lòng chọn biến thể sản phẩm trước khi thêm vào giỏ hàng', 'error')
    return
  }

  // Đảm bảo variant có đầy đủ thông tin
  let variantToAdd = selectedVariant.value
  if (variantToAdd) {
    // Tạo object variant với đầy đủ thông tin
    variantToAdd = {
      ...variantToAdd,
      variantName: variantToAdd.variantName || variantToAdd.name,
      variantValue: variantToAdd.variantValue || variantToAdd.value,
      color: variantToAdd.color,
      size: variantToAdd.size,
    }
    console.log('Adding variant to cart:', variantToAdd)
  }

  // Thêm vào giỏ hàng
  addItem(product.value, variantToAdd, quantity.value)
  showToastMessage('Đã thêm sản phẩm vào giỏ hàng!', 'success')
}

const selectVariant = (variant) => {
  selectedVariant.value = variant
}

onMounted(() => {
  fetchProduct()
})
</script>

<style scoped>
.prose {
  color: #374151;
}

.prose p {
  margin-bottom: 1rem;
}
</style>
