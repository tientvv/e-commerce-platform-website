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
      <div v-else-if="product" class="bg-white rounded-md p-4 border border-gray-200">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
          <!-- Left Section: Product Images & Promotions (40%) -->
          <div class="lg:col-span-5">
            <div class="space-y-4">
              <!-- Main Product Image -->
              <div class="relative">
                <div class="aspect-square rounded-lg overflow-hidden max-w-md mx-auto">
                  <img
                    v-if="currentImage || getSelectedVariantImage() || product.productImage"
                    :src="currentImage || getSelectedVariantImage() || product.productImage"
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

                <!-- Discount Badge -->
                <div v-if="hasDiscount()" class="absolute top-3 left-3">
                  <div class="voucher-badge">
                    <div class="voucher-badge-content">
                      <span v-if="product.discountType === 'PERCENTAGE' && product.discountPercentage > 0">
                        -{{ product.discountPercentage }}%
                      </span>
                      <span v-else-if="product.discountType === 'FIXED' && product.discountAmount > 0">
                        -{{ formatPrice(product.discountAmount) }}
                      </span>
                      <span v-else>
                        GIẢM GIÁ
                      </span>
                    </div>
                  </div>
                </div>


              </div>

              <!-- Additional Images Thumbnails -->
              <div v-if="getSelectedVariantImages().length > 0 || productImages.length > 0" class="grid grid-cols-4 gap-2">
                <div
                  v-for="image in getSelectedVariantImages().length > 0 ? getSelectedVariantImages() : productImages"
                  :key="image.id"
                  class="aspect-square rounded-lg overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
                  @click="selectImage(image.imageUrl)"
                >
                  <img :src="image.imageUrl" :alt="product.name" class="w-full h-full object-cover" />
                </div>
              </div>

              <!-- Social Sharing -->
              <div class="flex items-center justify-between text-sm text-gray-600">
                <div class="flex items-center space-x-2">
                  <span>Chia sẻ:</span>
                  <div class="flex space-x-1">
                    <button class="p-1 hover:bg-gray-100 rounded">
                      <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                        <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                      </svg>
                    </button>
                    <button class="p-1 hover:bg-gray-100 rounded">
                      <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M3 6a3 3 0 013-3h10a1 1 0 01.8 1.6L14.25 8l2.55 3.4A1 1 0 0116 13H6a1 1 0 00-1 1v3a1 1 0 11-2 0V6z" clip-rule="evenodd" />
                      </svg>
                    </button>
                  </div>
                </div>
                <div class="flex items-center space-x-1">
                  <Heart class="w-4 h-4" />
                  <span>Đã thích ({{ wishlistCount }})</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Section: Product Info & Purchase (60%) -->
          <div class="lg:col-span-7 space-y-6">
            <!-- Product Header -->
            <div>
              <!-- Product Title -->
              <h1 class="text-xl font-bold text-gray-800 mb-2">{{ product.name }}</h1>

              <!-- Rating -->
              <div class="flex items-center mb-4 p-3 bg-gray-50 rounded-lg">
                <div class="flex items-center mr-4">
                  <div class="flex items-center">
                    <Star
                      v-for="i in 5"
                      :key="i"
                      :class="i <= averageRating ? 'text-yellow-400 fill-current' : 'text-gray-300'"
                      class="w-6 h-6"
                    />
                  </div>
                  <div class="ml-3">
                    <div class="text-xl font-bold text-gray-800">
                      {{ averageRating.toFixed(1) }}
                    </div>
                    <div class="text-sm text-gray-500">trên 5 sao</div>
                  </div>
                </div>
                <div class="flex flex-col ml-4">
                  <span class="text-sm font-semibold text-gray-700">
                    {{ reviews.length }} đánh giá
                  </span>
                  <span class="text-xs text-gray-500">
                    {{ product?.soldCount || 0 }} đã bán
                  </span>
                </div>
                <div class="ml-6 flex items-center space-x-4 text-sm text-gray-600">
                  <div class="flex items-center">
                    <Eye class="w-4 h-4 mr-1" />
                    <span>{{ product.viewCount || 0 }} lượt xem</span>
                  </div>
                </div>
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
                <!-- Single price for selected variant or product -->
                <div class="flex items-center space-x-4">
                  <span v-if="getSelectedVariantPrice() > 0" class="text-3xl font-bold text-blue-600">
                    {{ formatPrice(getSelectedVariantPrice()) }}
                  </span>
                  <span v-else-if="product.minPrice && product.minPrice > 0" class="text-3xl font-bold text-blue-600">
                    {{ formatPrice(product.minPrice) }}
                  </span>
                  <span v-else class="text-3xl font-bold text-gray-500">Liên hệ</span>

                  <span
                    v-if="hasDiscount() && getOriginalSelectedPrice() > 0 && getOriginalSelectedPrice() > getSelectedVariantPrice()"
                    class="text-xl text-gray-500 line-through"
                  >
                    {{ formatPrice(getOriginalSelectedPrice()) }}
                  </span>
                </div>
              </div>

              <!-- Discount Voucher Card -->
              <div v-if="hasDiscount()" class="mb-4">
                <div class="voucher-card-shopee cursor-default">
                  <div class="flex">
                    <!-- Left section - Blue stub -->
                    <div class="voucher-left">
                      <div class="logo-section">
                        <div class="shop-logo">E</div>
                        <div class="brand-name">E-COMMERCE</div>
                      </div>
                    </div>

                    <!-- Right section - White details -->
                    <div class="voucher-right">
                      <div class="discount-info">
                        <div class="discount-title">
                          <span>
                            {{ product.discountName || 'Mã giảm giá' }}
                          </span>
                        </div>
                        <div class="discount-condition">
                          <span v-if="product.minOrderValue">
                            Đơn hàng tối thiểu {{ formatPrice(product.minOrderValue) }}
                          </span>
                          <span v-else>
                            Áp dụng cho mọi đơn hàng
                          </span>
                        </div>
                      </div>

                      <div class="flex justify-between items-end">
                        <div class="validity-info">
                          <span v-if="product.discountEndDate">
                            Có hiệu lực đến: {{ formatDate(product.discountEndDate) }}
                          </span>
                          <span v-else>
                            Không giới hạn thời gian
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Purchase Assurance -->
            <div class="border-t border-gray-200 pt-6">
              <h3 class="font-semibold text-gray-800 mb-2">An Tâm Mua Sắm Cùng E-Commerce Platform</h3>
              <div class="text-sm text-gray-600 space-y-1">
                <p>• Xử lý đơn hàng bởi E-Commerce Platform</p>
                <p>• Trả hàng miễn phí 15 ngày</p>
                <p>• Chính hãng 100%</p>
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
                    'flex items-center justify-between p-4 border rounded-lg transition-colors',
                    variant.quantity <= 0 ? 'cursor-not-allowed opacity-60' : 'cursor-pointer',
                    selectedVariant?.id === variant.id
                      ? variant.quantity <= 0
                        ? 'border-red-300 bg-red-50'
                        : 'border-blue-500 bg-blue-50'
                      : variant.quantity <= 0
                        ? 'border-gray-200'
                        : 'border-gray-200 hover:border-blue-300',
                  ]"
                  @click="variant.quantity > 0 && selectVariant(variant)"
                >
                  <div>
                    <p class="font-medium text-gray-800">{{ variant.variantName }}: {{ variant.variantValue }}</p>
                    <p v-if="variant.quantity <= 0" class="text-sm text-red-600 font-medium">
                      Hết hàng
                    </p>
                  </div>
                  <div class="text-right">
                    <p class="font-bold text-blue-600">{{ formatPrice(variant.price) }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Quantity Selector -->
            <div v-if="(getSelectedVariantPrice() > 0 || (product.minPrice && product.minPrice > 0)) && !isOutOfStock" class="border-t border-gray-200 pt-6">
              <div class="flex items-center space-x-4 mb-4">
                <span class="text-sm font-medium text-gray-700">Số lượng:</span>
                <div class="flex items-center space-x-2">
                  <button
                    @click="quantity = Math.max(1, quantity - 1)"
                    :disabled="quantity <= 1"
                    :class="[
                      'w-8 h-8 flex items-center justify-center border rounded-md transition-colors',
                      quantity <= 1
                        ? 'border-gray-200 text-gray-400 cursor-not-allowed'
                        : 'border-gray-300 text-gray-700 hover:bg-gray-50'
                    ]"
                  >
                    <Minus class="w-4 h-4" />
                  </button>
                  <span class="w-12 text-center font-medium">{{ quantity }}</span>
                  <button
                    @click="quantity = Math.min(quantity + 1, getSelectedVariantQuantity())"
                    :disabled="quantity >= getSelectedVariantQuantity()"
                    :class="[
                      'w-8 h-8 flex items-center justify-center border rounded-md transition-colors',
                      quantity >= getSelectedVariantQuantity()
                        ? 'border-gray-200 text-gray-400 cursor-not-allowed'
                        : 'border-gray-300 text-gray-700 hover:bg-gray-50'
                    ]"
                  >
                    <Plus class="w-4 h-4" />
                  </button>
                </div>
                <span class="text-sm text-gray-500">
                  (Còn {{ getSelectedVariantQuantity() }} sản phẩm)
                </span>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="border-t border-gray-200 pt-6">
              <div class="flex space-x-4">
                <button
                  @click="handleAddToCart"
                  :disabled="!canAddToCart"
                  :class="[
                    'flex-1 font-semibold py-3 px-6 rounded-md transition-colors',
                    canAddToCart
                      ? 'bg-blue-600 hover:bg-blue-700 text-white'
                      : 'bg-gray-400 text-gray-600 cursor-not-allowed'
                  ]"
                >
                  {{ addToCartButtonText }}
                </button>

                <!-- Wishlist Button -->
                <button
                  @click="toggleWishlist"
                  :class="[
                    'px-4 py-3 rounded-md transition-colors border-2',
                    isInWishlist
                      ? 'bg-red-50 border-red-300 text-red-600 hover:bg-red-100'
                      : 'bg-white border-gray-300 text-gray-600 hover:bg-gray-50'
                  ]"
                  :title="isInWishlist ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'"
                >
                  <Heart
                    :class="[
                      'w-5 h-5',
                      isInWishlist ? 'fill-current' : ''
                    ]"
                  />
                </button>
              </div>
            </div>

            <!-- Description -->
            <div class="border-t border-gray-200 pt-6">
              <h3 class="text-lg font-semibold text-gray-800 mb-4">Mô tả sản phẩm</h3>
              <div class="prose max-w-none">
                <p class="text-gray-700 leading-relaxed">{{ product.description || 'Chưa có mô tả' }}</p>
              </div>
            </div>

            <!-- Reviews Section -->
            <div class="border-t border-gray-200 pt-6">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-xl font-semibold text-gray-800">Đánh giá sản phẩm</h3>
                <button
                  v-if="canReview"
                  @click="openReviewDialog"
                  class="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
                >
                  <Star class="w-4 h-4" />
                  <span>Viết đánh giá</span>
                </button>
              </div>

              <ReviewList
                :reviews="reviews"
                :is-shop-owner="isShopOwner"
                @review-updated="onReviewUpdated"
              />
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
          <Check class="w-5 h-5" />
        </div>
        <div v-else class="w-5 h-5">
          <X class="w-5 h-5" />
        </div>
        <span class="font-medium">{{ toastMessage }}</span>
      </div>
    </div>

    <!-- Review Dialog -->
    <ReviewDialog
      v-model:show="showReviewDialog"
      :product-id="product?.id"
      :product-name="product?.name"
      :product-image="product?.productImage"
      :variant-name="selectedVariant?.variantName"
      :variant-value="selectedVariant?.variantValue"
      :product-variant-id="selectedVariant?.id"
      @review-submitted="onReviewSubmitted"
    />

    <FooterView />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import axios from 'axios'
import { Package, AlertCircle, ChevronRight, Star, Store, Eye, Heart, Plus, Minus, Check, X } from 'lucide-vue-next'
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'
import ReviewDialog from '../components/ReviewDialog.vue'
import ReviewList from '../components/ReviewList.vue'
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
const currentImage = ref('')
const isInWishlist = ref(false)
const wishlistCount = ref(0)

// Review state
const reviews = ref([])
const showReviewDialog = ref(false)
const canReview = ref(false)

// Check if current user is shop owner
const isShopOwner = computed(() => {
  if (!product.value || !product.value.shopId) return false
  // TODO: Implement proper shop owner check
  // For now, return false - you'll need to implement this based on your auth system
  return false
})

const { addItem } = useCart()

// Computed properties
const averageRating = computed(() => {
  if (reviews.value.length === 0) {
    return product.value?.rating || 0
  }
  const totalRating = reviews.value.reduce((sum, review) => sum + review.rating, 0)
  return totalRating / reviews.value.length
})

// Computed properties for stock status
const isOutOfStock = computed(() => {
  if (selectedVariant.value) {
    return selectedVariant.value.quantity <= 0
  }
  // Nếu không có variant, kiểm tra quantity của product (nếu có)
  return product.value?.quantity <= 0
})

// Kiểm tra shop hoặc sản phẩm có bị khóa không
const isBlocked = computed(() => {
  // Debug: Log thông tin để kiểm tra
  if (product.value) {
    console.log('Product debug:', {
      productId: product.value.id,
      productName: product.value.name,
      productIsActive: product.value.isActive,
      shopIsActive: product.value.shopIsActive,
      shopName: product.value.shopName
    })
  }

  const blocked = product.value?.shopIsActive === false || product.value?.isActive === false
  console.log('Is blocked:', blocked)
  return blocked
})

// Kiểm tra có thể thêm vào giỏ hàng không
const canAddToCart = computed(() => {
  return !isOutOfStock.value && !isBlocked.value
})

// Text hiển thị cho nút thêm vào giỏ hàng
const addToCartButtonText = computed(() => {
  if (isBlocked.value) {
    return 'Sản phẩm này đã bị khóa'
  }
  if (isOutOfStock.value) {
    return 'Hết hàng'
  }
  return 'Thêm vào giỏ hàng'
})



const getSelectedVariantQuantity = () => {
  if (selectedVariant.value) {
    return selectedVariant.value.quantity || 0
  }
  return product.value?.quantity || 0
}

const fetchProduct = async () => {
  try {
    console.log('Fetching product for ID:', route.params.id)
    loading.value = true
    error.value = false

    const productId = route.params.id

    // Fetch product details
    const productResponse = await axios.get(`/api/products/${productId}`)
    product.value = productResponse.data
    console.log('Product loaded:', product.value.name)

    // Fetch product variants
    try {
      const variantsResponse = await axios.get(`/api/product-variants/product/${productId}`)
      productVariants.value = variantsResponse.data.variants || []

      // Mặc định chọn biến thể đầu tiên nếu có
      if (productVariants.value.length > 0) {
        selectedVariant.value = productVariants.value[0]
        // Cập nhật wishlist status cho variant mặc định
        await updateWishlistStatus()
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

    // Check if product is in wishlist (chỉ kiểm tra nếu không có variant được chọn)
    if (productVariants.value.length === 0) {
      try {
        const wishlistResponse = await axios.get(`/api/wishlist/check/${productId}`)
        isInWishlist.value = wishlistResponse.data.isInWishlist || false
      } catch {
        console.log('Could not check wishlist status')
        isInWishlist.value = false
      }
    }

    // Get wishlist count
    try {
      const countResponse = await axios.get(`/api/wishlist/count/${productId}`)
      wishlistCount.value = countResponse.data.count || 0
    } catch {
      console.log('Could not get wishlist count')
      wishlistCount.value = 0
    }

    // Fetch reviews and check review permission
    await fetchReviews()
    await checkCanReview()
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
      <Package class="w-16 h-16 text-gray-400" />
    </div>
  `
}

const selectImage = (imageUrl) => {
  currentImage.value = imageUrl
}

const selectVariant = (variant) => {
  selectedVariant.value = variant
  currentImage.value = '' // Reset current image when selecting new variant
  // Reset quantity về 1 khi chọn variant mới và đảm bảo không vượt quá tồn kho
  quantity.value = Math.min(1, variant.quantity || 0)

  // Cập nhật wishlist status cho variant mới
  updateWishlistStatus()
}

const updateWishlistStatus = async () => {
  if (!product.value?.id) return

  try {
    const variantId = selectedVariant.value?.id || null
    const url = variantId
      ? `/api/wishlist/check/${product.value.id}?variantId=${variantId}`
      : `/api/wishlist/check/${product.value.id}`

    console.log('Checking wishlist status for:', { productId: product.value.id, variantId })
    const response = await axios.get(url)
    isInWishlist.value = response.data.isInWishlist || false
    console.log('Wishlist status updated:', isInWishlist.value, 'for variant:', variantId)
  } catch (error) {
    console.log('Could not check wishlist status for variant:', error)
    isInWishlist.value = false
  }
}

const formatPrice = (price) => {
  if (!price || price === 0) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const hasDiscount = () => {
  if (!product.value) return false

  // Kiểm tra xem có discount không
  const hasDiscountValue = (product.value.discountType === 'PERCENTAGE' && product.value.discountPercentage > 0) ||
                          (product.value.discountType === 'FIXED' && product.value.discountAmount > 0)

  if (!hasDiscountValue) return false

  // Kiểm tra min_order_value nếu có
  if (product.value.minOrderValue && product.value.minOrderValue > 0) {
    const productPrice = product.value.minPrice || 0
    return productPrice >= product.value.minOrderValue
  }

  return true
}

const handleAddToCart = () => {
  // Kiểm tra shop hoặc sản phẩm có bị khóa không
  if (isBlocked.value) {
    showToast.value = true
    toastMessage.value = 'Sản phẩm này đã bị khóa!'
    toastType.value = 'error'

    setTimeout(() => {
      showToast.value = false
    }, 3000)
    return
  }

  // Kiểm tra trạng thái hết hàng
  if (isOutOfStock.value) {
    showToast.value = true
    toastMessage.value = 'Sản phẩm đã hết hàng!'
    toastType.value = 'error'

    setTimeout(() => {
      showToast.value = false
    }, 3000)
    return
  }

  // Kiểm tra số lượng đặt hàng có vượt quá tồn kho không
  const availableQuantity = getSelectedVariantQuantity()
  if (quantity.value > availableQuantity) {
    showToast.value = true
    toastMessage.value = `Số lượng vượt quá tồn kho!`
    toastType.value = 'error'

    setTimeout(() => {
      showToast.value = false
    }, 3000)
    return
  }

  const productData = {
    id: product.value.id,
    name: product.value.name,
    mainImage: product.value.productImage,
    productImage: product.value.productImage,
    brand: product.value.brand,
    shopName: product.value.shopName,
    shopId: product.value.shopId,
    minPrice: product.value.minPrice,
    maxPrice: product.value.maxPrice
  }

  const variantData = selectedVariant.value ? {
    id: selectedVariant.value.id,
    name: selectedVariant.value.name,
    price: selectedVariant.value.price,
    variantName: selectedVariant.value.variantName,
    variantValue: selectedVariant.value.variantValue,
    color: selectedVariant.value.color,
    size: selectedVariant.value.size
  } : null

  addItem(productData, variantData, quantity.value)

  showToast.value = true
  toastMessage.value = 'Đã thêm vào giỏ hàng!'
  toastType.value = 'success'

  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

const toggleWishlist = async () => {
  try {
    // Gửi variant ID nếu có variant được chọn
    const variantId = selectedVariant.value?.id || null
    const url = variantId
      ? `/api/wishlist/toggle/${product.value.id}?variantId=${variantId}`
      : `/api/wishlist/toggle/${product.value.id}`

    const response = await axios.post(url)

    // Cập nhật trạng thái ngay lập tức
    isInWishlist.value = response.data.isInWishlist
    console.log('Wishlist toggled:', isInWishlist.value, 'for variant:', variantId)

    showToast.value = true
    toastMessage.value = response.data.message
    toastType.value = 'success'

    // Cập nhật số lượng wishlist
    try {
      const countResponse = await axios.get(`/api/wishlist/count/${product.value.id}`)
      wishlistCount.value = countResponse.data.count || 0
    } catch {
      console.log('Could not update wishlist count')
    }

    // Hide toast after 3 seconds
    setTimeout(() => {
      showToast.value = false
    }, 3000)
  } catch (error) {
    console.error('Error toggling wishlist:', error)
    showToast.value = true

    // Hiển thị thông báo lỗi từ backend nếu có
    if (error.response && error.response.data && error.response.data.message) {
      toastMessage.value = error.response.data.message
    } else {
      toastMessage.value = 'Có lỗi xảy ra. Vui lòng thử lại!'
    }
    toastType.value = 'error'

    setTimeout(() => {
      showToast.value = false
    }, 3000)
  }
}

const getSelectedVariantPrice = () => {
  let basePrice = 0

  if (selectedVariant.value?.price && selectedVariant.value.price > 0) {
    basePrice = selectedVariant.value.price
  } else {
    basePrice = product.value?.minPrice || 0
  }

  // Kiểm tra điều kiện áp dụng discount
  if (!hasDiscount()) {
    return basePrice
  }

  // Apply discount if available
  const discount = product.value // Use product.value for discount
  if (discount.discountType === 'PERCENTAGE' && discount.discountPercentage > 0) {
    return basePrice * (1 - discount.discountPercentage / 100)
  } else if (discount.discountType === 'FIXED' && discount.discountAmount > 0) {
    return Math.max(0, basePrice - discount.discountAmount)
  }

  return basePrice
}

const getOriginalSelectedPrice = () => {
  if (selectedVariant.value?.price && selectedVariant.value.price > 0) {
    return selectedVariant.value.price
  }
  return product.value?.minPrice || 0
}

const getSelectedVariantImage = () => {
  if (selectedVariant.value?.imageUrl) {
    return selectedVariant.value.imageUrl
  }
  return product.value?.productImage
}

const getSelectedVariantImages = () => {
  if (selectedVariant.value?.images && selectedVariant.value.images.length > 0) {
    return selectedVariant.value.images.map(imageUrl => ({ id: Math.random(), imageUrl }))
  }
  return productImages.value
}

onMounted(() => {
  fetchProduct()
})

// Review functions
const fetchReviews = async () => {
  if (!product.value?.id) return

  try {
    const response = await axios.get(`/api/reviews/product/${product.value.id}`)
    if (response.data.success) {
      reviews.value = response.data.reviews
    }
  } catch (error) {
    console.error('Error fetching reviews:', error)
  }
}

const checkCanReview = async () => {
  if (!product.value?.id) return

  try {
    const response = await axios.get(`/api/reviews/product/${product.value.id}/can-review`)
    if (response.data.success) {
      canReview.value = response.data.canReview
    }
  } catch (error) {
    console.error('Error checking review permission:', error)
  }
}

const openReviewDialog = () => {
  showReviewDialog.value = true
}

const onReviewSubmitted = (review) => {
  // Thêm review mới vào danh sách
  reviews.value.unshift(review)
  canReview.value = false // Không thể đánh giá nữa
}

const onReviewUpdated = (updatedReview) => {
  // Cập nhật review trong danh sách
  const index = reviews.value.findIndex(r => r.id === updatedReview.id)
  if (index !== -1) {
    reviews.value[index] = updatedReview
  }
}

// Watch for route changes to reload product data
watch(() => route.params.id, (newId, oldId) => {
  console.log('Route params changed:', { newId, oldId })
  if (newId && newId !== oldId) {
    console.log('Route changed, fetching new product:', newId)
    // Reset state before fetching new product
    product.value = null
    productVariants.value = []
    productImages.value = []
    selectedVariant.value = null
    currentImage.value = ''
    fetchProduct()
  }
}, { immediate: true })
</script>

<style scoped>
.prose {
  color: #374151;
}

.prose p {
  margin-bottom: 1rem;
}

/* Voucher Badge */
.voucher-badge {
  position: relative;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 8px;
  padding: 8px 12px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.voucher-badge::before {
  content: '';
  position: absolute;
  right: -4px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 0 2px #f0f0f0;
}

.voucher-badge-content {
  color: white;
  font-size: 12px;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  white-space: nowrap;
}

/* Voucher Card Shopee Style */
.voucher-card-shopee {
  position: relative;
  display: flex;
  background: white;
  border-radius: 8px;
  margin-bottom: 12px;
}

.voucher-left {
  position: relative;
  width: 120px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-radius: 8px 0 0 8px;
}

.voucher-left::after {
  content: '';
  position: absolute;
  right: -8px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 0 4px #f0f0f0;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.shop-logo {
  width: 32px;
  height: 32px;
  background: white;
  color: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.brand-name {
  font-size: 12px;
  font-weight: bold;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.voucher-right {
  flex: 1;
  padding: 16px;
  background: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-top: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
  border-radius: 0 8px 8px 0;
  min-width: 0;
}

.discount-info {
  margin-bottom: 8px;
  min-width: 0;
  flex: 1;
}

.discount-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
  line-height: 1.2;
}

.discount-condition {
  font-size: 12px;
  color: #666;
  line-height: 1.3;
}

.validity-info {
  font-size: 11px;
  color: #999;
  line-height: 1.2;
}

/* Voucher Card Shopee Style Compact */
.voucher-card-shopee-compact {
  position: relative;
  display: flex;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 8px;
  max-width: 400px;
}

.voucher-left-compact {
  position: relative;
  width: 80px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  border-radius: 6px 0 0 6px;
}

.voucher-left-compact::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 0 3px #f0f0f0;
}

.logo-section-compact {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.shop-logo-compact {
  width: 24px;
  height: 24px;
  background: white;
  color: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.brand-name-compact {
  font-size: 10px;
  font-weight: bold;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.voucher-right-compact {
  flex: 1;
  padding: 12px;
  background: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border-top: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
  border-radius: 0 6px 6px 0;
  min-width: 0;
}

.discount-info-compact {
  min-width: 0;
  flex: 1;
}

.discount-title-compact {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 2px;
  line-height: 1.2;
}

.discount-condition-compact {
  font-size: 11px;
  color: #666;
  line-height: 1.3;
}
</style>
