<template>
  <div class="product-section">
    <!-- Section Header -->
    <div class="section-header">
      <h2 class="text-base font-semibold text-gray-700">Sản phẩm giảm giá</h2>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-grid">
        <div v-for="i in 8" :key="i" class="loading-product-item">
          <div class="loading-image"></div>
          <div class="loading-name"></div>
          <div class="loading-price"></div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">
        <svg fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zM7 8a1 1 0 012 0v4a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v4a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
        </svg>
      </div>
      <h3 class="error-title">Có lỗi xảy ra</h3>
      <p class="error-description">{{ errorMessage }}</p>
      <button @click="loadDiscountedProducts" class="retry-button">
        Thử lại
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="filteredDiscountedProducts.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg fill="currentColor" viewBox="0 0 20 20">
          <path
            fill-rule="evenodd"
            d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"
            clip-rule="evenodd"
          />
        </svg>
      </div>
      <h3 class="empty-title">Chưa có sản phẩm giảm giá</h3>
      <p class="empty-description">Hãy quay lại sau để xem những ưu đãi hấp dẫn!</p>
    </div>

    <!-- Swiper Slider -->
    <div v-else class="swiper-container">
      <swiper
        :modules="[SwiperNavigation, SwiperPagination, SwiperAutoplay]"
        :slides-per-view="1"
        :space-between="20"
        :navigation="true"
        :pagination="{ clickable: true }"
        :autoplay="{ delay: 5000, disableOnInteraction: false }"
        :breakpoints="{
          480: { slidesPerView: 2, spaceBetween: 16 },
          640: { slidesPerView: 2, spaceBetween: 20 },
          768: { slidesPerView: 3, spaceBetween: 24 },
          1024: { slidesPerView: 4, spaceBetween: 24 },
          1280: { slidesPerView: 5, spaceBetween: 24 },
          1536: { slidesPerView: 6, spaceBetween: 24 }
        }"
        class="product-swiper"
      >
        <swiper-slide v-for="product in filteredDiscountedProducts" :key="product.id" class="product-slide">
          <div class="product-card" @click="navigateToProduct(product.id)">
            <!-- Product Image -->
            <div class="product-image-container">
              <img
                v-if="product.productImage"
                :src="product.productImage"
                :alt="product.name"
                class="product-image"
                loading="lazy"
              />
              <div v-else class="product-image-placeholder">
                <svg class="placeholder-icon" fill="currentColor" viewBox="0 0 20 20">
                  <path
                    fill-rule="evenodd"
                    d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
                    clip-rule="evenodd"
                  />
                </svg>
              </div>

              <!-- Discount Badge -->
              <div v-if="hasDiscount(product)" class="absolute top-2 right-2">
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

              <!-- Discount Info -->
              <div v-if="hasDiscount(product) && product.discountName" class="discount-info">
                <div class="discount-details">
                  <span class="discount-name">{{ product.discountName }}</span>
                  <span class="discount-time">{{ formatTimeRemaining(product.discountEndDate) }}</span>
                </div>
              </div>
            </div>

            <!-- Product Info -->
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-brand">{{ product.brand }}</p>

              <!-- Rating -->
              <div class="flex items-center mb-2">
                <div class="flex items-center mr-1">
                  <Star
                    v-for="i in 5"
                    :key="i"
                    :class="i <= (product.rating || 0) ? 'text-yellow-400 fill-current' : 'text-gray-300'"
                    class="w-3 h-3"
                  />
                </div>
                <span class="text-xs text-gray-500">
                  {{ product.rating?.toFixed(1) || '0.0' }} ({{ product.reviewCount || 0 }} đánh giá)
                </span>
              </div>

              <!-- Price -->
              <div class="mb-2">
                <!-- Price Range for products with variants -->
                <div v-if="product.maxPrice && product.maxPrice > product.minPrice" class="flex flex-col">
                  <div class="flex items-center">
                    <span class="text-lg font-bold text-blue-600 whitespace-nowrap">
                      {{ formatPrice(getDiscountedPrice(product)) }} - {{ formatPrice(getDiscountedMaxPrice(product)) }}
                    </span>
                  </div>
                  <div v-if="hasDiscount(product) && getOriginalPrice(product) > 0 && getOriginalPrice(product) > getDiscountedPrice(product)" class="flex items-center">
                    <span class="text-sm text-gray-500 line-through whitespace-nowrap">
                      {{ formatPrice(getOriginalPrice(product)) }} - {{ formatPrice(getOriginalMaxPrice(product)) }}
                    </span>
                  </div>
                </div>

                <!-- Single price for products without variants -->
                <div v-else class="flex flex-col">
                  <div class="flex items-center">
                    <span v-if="hasDiscount(product) && getDiscountedPrice(product) > 0" class="text-lg font-bold text-blue-600">
                      {{ formatPrice(getDiscountedPrice(product)) }}
                    </span>
                    <span v-else-if="product.minPrice && product.minPrice > 0" class="text-lg font-bold text-blue-600">
                      {{ formatPrice(product.minPrice) }}
                    </span>
                    <span v-else class="text-lg font-bold text-gray-500">Liên hệ</span>
                  </div>
                  <div v-if="hasDiscount(product) && getOriginalPrice(product) > 0 && getOriginalPrice(product) > getDiscountedPrice(product)" class="flex items-center">
                    <span class="text-sm text-gray-500 line-through">
                      {{ formatPrice(getOriginalPrice(product)) }}
                    </span>
                  </div>
                </div>
              </div>


            </div>
          </div>
        </swiper-slide>
      </swiper>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Navigation as SwiperNavigation, Pagination as SwiperPagination, Autoplay as SwiperAutoplay } from 'swiper/modules'
import { Star } from 'lucide-vue-next'


// Import Swiper styles
import 'swiper/css'
import 'swiper/css/navigation'
import 'swiper/css/pagination'

// Reactive data
const router = useRouter()
const discountedProducts = ref([])
const isLoading = ref(false)
const error = ref(false)
const errorMessage = ref('')
const countdownTimer = ref(null)
const currentTime = ref(new Date())

// Computed property để lọc sản phẩm đạt điều kiện minOrderValue và loại bỏ trùng lặp
const filteredDiscountedProducts = computed(() => {
  // Lọc sản phẩm có discount và đạt điều kiện minOrderValue
  const validProducts = discountedProducts.value.filter(product => {
    // Kiểm tra xem có discount không
    const hasDiscountValue = (product.discountPercentage && product.discountPercentage > 0) ||
                            (product.discountAmount && product.discountAmount > 0) ||
                            (product.discountType && product.discountType !== '')

    if (!hasDiscountValue) {
      return false
    }

    // Kiểm tra min_order_value nếu có
    if (product.minOrderValue && product.minOrderValue > 0) {
      const productPrice = product.minPrice || 0
      const isValid = productPrice >= product.minOrderValue
      return isValid
    }

    return true
  })

  // Loại bỏ trùng lặp sản phẩm và chỉ giữ lại sản phẩm với discount tốt nhất
  const uniqueProducts = new Map()

  validProducts.forEach(product => {
    const productId = product.id

    if (!uniqueProducts.has(productId)) {
      // Sản phẩm chưa có trong map, thêm vào
      uniqueProducts.set(productId, product)
    } else {
      // Sản phẩm đã có, so sánh discount để giữ lại cái tốt hơn
      const existingProduct = uniqueProducts.get(productId)

      // Sử dụng function so sánh mới
      if (compareDiscounts(product, existingProduct) > 0) {
        uniqueProducts.set(productId, product)
      }
    }
  })

  // Chuyển về array và random thay vì sắp xếp theo giá trị discount
  const result = Array.from(uniqueProducts.values())

  // Random sản phẩm
  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [result[i], result[j]] = [result[j], result[i]];
  }

  return result
})

// Helper function để so sánh discount (ưu tiên percentage trước)
const compareDiscounts = (product1, product2) => {
  // Ưu tiên 1: Percentage discount luôn tốt hơn Fixed discount
  const hasPercentage1 = product1.discountPercentage && product1.discountPercentage > 0
  const hasPercentage2 = product2.discountPercentage && product2.discountPercentage > 0

  console.log(`Comparing discounts for ${product1.name}:`)
  console.log(`  Product1: ${hasPercentage1 ? product1.discountPercentage + '%' : product1.discountAmount + ' VNĐ'}`)
  console.log(`  Product2: ${hasPercentage2 ? product2.discountPercentage + '%' : product2.discountAmount + ' VNĐ'}`)

  if (hasPercentage1 && !hasPercentage2) {
    console.log(`  -> Product1 wins (percentage vs fixed)`)
    return 1 // product1 tốt hơn
  }
  if (!hasPercentage1 && hasPercentage2) {
    console.log(`  -> Product2 wins (fixed vs percentage)`)
    return -1 // product2 tốt hơn
  }

  // Nếu cùng loại, so sánh giá trị thực tế
  const value1 = getDiscountValue(product1)
  const value2 = getDiscountValue(product2)

  console.log(`  -> Same type, comparing values: ${value1} vs ${value2}`)
  return value1 - value2
}

// Helper function để tính giá trị discount
const getDiscountValue = (product) => {
  const productPrice = product.minPrice || 0

  if (product.discountPercentage && product.discountPercentage > 0) {
    // Với discount percentage, tính giá trị thực tế
    return (productPrice * product.discountPercentage) / 100
  } else if (product.discountAmount && product.discountAmount > 0) {
    // Với discount fixed, trả về số tiền giảm trực tiếp
    return product.discountAmount
  }
  return 0
}

// Methods
const loadDiscountedProducts = async () => {
  isLoading.value = true
  error.value = false
  try {
    const response = await axios.get('/api/products/discounted')

    if (response.data && response.data.products) {
      discountedProducts.value = response.data.products
    } else {
      discountedProducts.value = []
    }
  } catch (error) {
    console.error('Error loading discounted products:', error)
    discountedProducts.value = []
    error.value = true
    errorMessage.value = 'Không thể tải sản phẩm giảm giá. Vui lòng thử lại sau.'
  } finally {
    isLoading.value = false
  }
}

const navigateToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const formatPrice = (price) => {
  if (!price || price <= 0) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}



const getOriginalPrice = (product) => {
  if (product.originalPrice && product.originalPrice > 0) {
    return product.originalPrice
  } else {
    return product.minPrice || 0
  }
}

const hasDiscount = (product) => {
  // Kiểm tra xem có discount không
  const hasDiscountValue = (product.discountPercentage && product.discountPercentage > 0) ||
                          (product.discountAmount && product.discountAmount > 0) ||
                          (product.discountType && product.discountType !== '')

  if (!hasDiscountValue) return false

  // Kiểm tra min_order_value nếu có
  if (product.minOrderValue && product.minOrderValue > 0) {
    const productPrice = product.minPrice || 0
    return productPrice >= product.minOrderValue
  }

  return true
}

const getDiscountedPrice = (product) => {
  // Kiểm tra điều kiện áp dụng discount
  if (!hasDiscount(product)) {
    return product.minPrice || 0
  }

  if (product.discountType === 'PERCENTAGE' && product.discountPercentage > 0) {
    return product.originalPrice * (1 - product.discountPercentage / 100)
  } else if (product.discountType === 'FIXED' && product.discountAmount > 0) {
    return Math.max(0, product.originalPrice - product.discountAmount)
  } else {
    return product.minPrice || 0
  }
}

const getDiscountedMaxPrice = (product) => {
  // Kiểm tra điều kiện áp dụng discount
  if (!hasDiscount(product)) {
    return product.maxPrice || product.minPrice || 0
  }

  if (product.maxPrice && product.maxPrice > product.minPrice) {
    if (product.discountType === 'PERCENTAGE' && product.discountPercentage > 0) {
      return product.maxPrice * (1 - product.discountPercentage / 100)
    } else if (product.discountType === 'FIXED' && product.discountAmount > 0) {
      return Math.max(0, product.maxPrice - product.discountAmount)
    } else {
      return product.maxPrice
    }
  } else {
    return product.minPrice || 0
  }
}

const getOriginalMaxPrice = (product) => {
  if (product.maxPrice && product.maxPrice > product.minPrice) {
    return product.maxPrice
  } else {
    return product.minPrice || 0
  }
}

// Format time remaining with realtime updates
const formatTimeRemaining = (endDate) => {
  if (!endDate) return ''

  // Sử dụng currentTime để tính toán realtime
  const now = currentTime.value
  const end = new Date(endDate)
  const diffInMs = end.getTime() - now.getTime()
  const diffInSeconds = Math.max(0, Math.floor(diffInMs / 1000))

  if (diffInSeconds <= 0) {
    return 'Đã hết hạn'
  }

  // Tính giờ, phút, giây
  const hours = Math.floor(diffInSeconds / 3600)
  const minutes = Math.floor((diffInSeconds % 3600) / 60)
  const seconds = diffInSeconds % 60

  // Format: HH:MM:SS
  const formatNumber = (num) => num.toString().padStart(2, '0')

  return `${formatNumber(hours)}:${formatNumber(minutes)}:${formatNumber(seconds)}`
}

// Start countdown timer
const startCountdownTimer = () => {
  countdownTimer.value = setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
}

// Stop countdown timer
const stopCountdownTimer = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

// Lifecycle
onMounted(() => {
  loadDiscountedProducts()
  startCountdownTimer()
})

onUnmounted(() => {
  stopCountdownTimer()
})
</script>

<style scoped>
.product-section {
  width: 100%;
  position: relative;
  background: #ffffff;
  border-radius: 0.375rem;
}

.section-header {
  margin-bottom: 20px;
}

/* Loading State */
.loading-container {
  width: 100%;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  max-width: 100%;
  overflow-x: auto;
}

.loading-product-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.loading-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  background: #e5e7eb;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.loading-name {
  height: 16px;
  width: 80%;
  background: #e5e7eb;
  border-radius: 4px;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.loading-price {
  height: 14px;
  width: 60%;
  background: #e5e7eb;
  border-radius: 4px;
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

/* Swiper Container */
.swiper-container {
  position: relative;
}

/* .product-swiper {
} */

/* Product Card */
.product-card {
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  transition: all 0.2s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #e5e7eb;
}

.product-card:hover {
  border-color: #3b82f6;
}

/* Product Image */
.product-image-container {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
}

.placeholder-icon {
  width: 3rem;
  height: 3rem;
  color: #94a3b8;
}

/* Discount Badge */
.discount-badge {
  position: absolute;
  top: 0.75rem;
  left: 0.75rem;
  background: #ef4444;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-weight: 600;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.discount-badge.fixed {
  background: #10b981;
}

/* Voucher Badge */
.voucher-badge {
  position: relative;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 6px;
  padding: 4px 8px;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 10;
}

.voucher-badge::before {
  content: '';
  position: absolute;
  right: -3px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 0 0 1px #f0f0f0;
}

.voucher-badge-content {
  color: white;
  font-size: 11px;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  white-space: nowrap;
}

/* Discount Info */
.discount-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  padding: 1rem 0.75rem 0.75rem;
  color: white;
}

.discount-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.discount-name {
  font-size: 0.875rem;
  font-weight: 600;
}

.discount-time {
  font-size: 0.75rem;
  opacity: 0.9;
}

/* Product Info */
.product-info {
  padding: 1rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-brand {
  font-size: 0.75rem;
  color: #6b7280;
  margin-bottom: 0.75rem;
}

/* Price Section */
.price-section {
  margin-bottom: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.current-price {
  font-size: 1rem;
  font-weight: 600;
  color: #ef4444;
}

.contact-price {
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;
}

.original-price {
  font-size: 0.75rem;
  color: #9ca3af;
  text-decoration: line-through;
}

.shop-name {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: auto;
}

/* Error State */
.error-state {
  text-align: center;
  padding: 3rem 1rem;
}

.error-icon {
  width: 4rem;
  height: 4rem;
  color: #ef4444;
  margin: 0 auto 1rem;
}

.error-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.error-description {
  color: #6b7280;
  margin-bottom: 1.5rem;
}

.retry-button {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.retry-button:hover {
  background: #2563eb;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
}

.empty-icon {
  width: 4rem;
  height: 4rem;
  color: #9ca3af;
  margin: 0 auto 1rem;
}

.empty-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.empty-description {
  color: #6b7280;
}

/* Swiper Navigation Customization */
:deep(.swiper-button-next),
:deep(.swiper-button-prev) {
  background: rgba(255, 255, 255, 0.9);
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  color: #3b82f6;
  transition: all 0.2s ease;
}

:deep(.swiper-button-next:hover),
:deep(.swiper-button-prev:hover) {
  background: white;
  transform: scale(1.05);
}

:deep(.swiper-button-next::after),
:deep(.swiper-button-prev::after) {
  font-size: 1rem;
  font-weight: bold;
}

/* Swiper Pagination Customization */
:deep(.swiper-pagination-bullet) {
  background: #d1d5db;
  opacity: 0.5;
  transition: all 0.2s ease;
}

:deep(.swiper-pagination-bullet-active) {
  background: #3b82f6;
  opacity: 1;
  transform: scale(1.1);
}

/* Responsive Design */
@media (max-width: 768px) {
  .product-section {
    padding: 16px;
  }

  .section-title {
    font-size: 14px;
  }

  .swiper-container {
    padding: 0 0.5rem;
  }
}

@media (max-width: 480px) {
  .product-section {
    padding: 12px;
  }

  .swiper-container {
    padding: 0 0.25rem;
  }
}

/* Tablet Layout */
@media (min-width: 769px) and (max-width: 1024px) {
  .product-section {
    padding: 24px;
  }
}

/* Desktop Improvements */
@media (min-width: 1025px) {
  .product-section {
    padding: 24px;
  }

  .section-title {
    font-size: 14px;
  }
}
</style>

