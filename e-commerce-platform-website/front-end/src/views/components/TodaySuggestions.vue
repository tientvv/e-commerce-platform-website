<template>
  <div class="mb-8 bg-white rounded-md border border-gray-200 p-6">
    <h2 class="text-base font-semibold text-gray-700 mb-4">Gợi ý hôm nay</h2>

    <!-- Loading State -->
    <div v-if="isLoading" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
      <div v-for="i in 18" :key="i" class="animate-pulse">
        <div class="bg-gray-200 rounded-lg aspect-square mb-2"></div>
        <div class="h-4 bg-gray-200 rounded mb-2"></div>
        <div class="h-3 bg-gray-200 rounded w-3/4"></div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="text-center py-8">
      <p class="text-gray-500">{{ errorMessage }}</p>
    </div>

    <!-- Products -->
    <div v-else-if="suggestedProducts.length > 0" class="relative">
      <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
        <div v-for="product in displayedProducts" :key="product.id" class="product-card cursor-pointer" @click="goToProduct(product.id)">
          <!-- Product Image -->
          <div class="product-image-container">
            <img
              v-if="product.productImage"
              :src="product.productImage"
              :alt="product.name"
              class="w-full h-full object-cover"
              @error="handleImageError"
            />
            <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
              <svg class="w-8 h-8 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
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
                  <span v-else-if="product.discountPercentage > 0">
                    -{{ product.discountPercentage }}%
                  </span>
                  <span v-else-if="product.discountAmount > 0">
                    -{{ formatPrice(product.discountAmount) }}
                  </span>
                  <span v-else>
                    GIẢM GIÁ
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Product Info -->
          <div class="product-info">
            <h3 class="product-name">{{ product.name || 'Sản phẩm' }}</h3>
            <p class="product-brand">{{ product.brand || 'Thương hiệu' }}</p>

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
                  <span class="text-sm font-bold text-blue-600 whitespace-nowrap">
                    {{ formatPrice(getDiscountedPrice(product)) }} - {{ formatPrice(getDiscountedMaxPrice(product)) }}
                  </span>
                </div>
                <div class="flex items-center">
                  <span v-if="hasDiscount(product) && getOriginalPrice(product) > 0 && getOriginalPrice(product) > getDiscountedPrice(product)" class="text-sm text-gray-500 line-through whitespace-nowrap">
                    {{ formatPrice(getOriginalPrice(product)) }} - {{ formatPrice(getOriginalMaxPrice(product)) }}
                  </span>
                  <span v-else class="text-sm text-gray-500" style="height: 1.25rem; display: inline-block;">&nbsp;</span>
                </div>
              </div>

              <!-- Single price for products without variants -->
              <div v-else class="flex flex-col">
                <div class="flex items-center">
                  <span v-if="hasDiscount(product) && getDiscountedPrice(product) > 0" class="text-sm font-bold text-blue-600">
                    {{ formatPrice(getDiscountedPrice(product)) }}
                  </span>
                  <span v-else-if="product.minPrice && product.minPrice > 0" class="text-sm font-bold text-blue-600">
                    {{ formatPrice(product.minPrice) }}
                  </span>
                  <span v-else class="text-sm font-bold text-gray-500">Liên hệ</span>
                </div>
                <div class="flex items-center">
                  <span v-if="hasDiscount(product) && getOriginalPrice(product) > 0 && getOriginalPrice(product) > getDiscountedPrice(product)" class="text-sm text-gray-500 line-through">
                    {{ formatPrice(getOriginalPrice(product)) }}
                  </span>
                  <span v-else class="text-sm text-gray-500" style="height: 1.25rem; display: inline-block;">&nbsp;</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Load More Button -->
      <div v-if="suggestedProducts.length > displayedProducts.length" class="text-center mt-6">
        <button
          @click="loadMore"
          class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
        >
          Xem thêm
        </button>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-8">
      <p class="text-gray-500">Chưa có gợi ý sản phẩm</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { Star } from 'lucide-vue-next'

// Reactive data
const router = useRouter()
const suggestedProducts = ref([])
const isLoading = ref(false)
const error = ref(false)
const errorMessage = ref('')
const currentDisplayCount = ref(18)
const lastFetchTime = ref(0)
const CACHE_DURATION = 30 * 60 * 1000

const displayedProducts = computed(() => {
  return suggestedProducts.value.slice(0, currentDisplayCount.value)
})

const fetchSuggestedProducts = async () => {
  const now = Date.now()

  if (suggestedProducts.value.length > 0 && (now - lastFetchTime.value) < CACHE_DURATION) {
    return
  }

  isLoading.value = true
  error.value = false

  try {
    const response = await axios.get('/api/products/suggestions')
    suggestedProducts.value = response.data.products || []
    lastFetchTime.value = now

    console.log('Suggested products discount data:', suggestedProducts.value.map(p => ({
      name: p.name,
      discountType: p.discountType,
      discountPercentage: p.discountPercentage,
      discountAmount: p.discountAmount,
      discountName: p.discountName
    })))
  } catch (err) {
    error.value = true
    errorMessage.value = 'Không thể tải gợi ý sản phẩm'
    console.error('Error fetching suggested products:', err)
  } finally {
    isLoading.value = false
  }
}

const loadMore = () => {
  currentDisplayCount.value = Math.min(currentDisplayCount.value + 18, suggestedProducts.value.length)
}

const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
  event.target.nextElementSibling.style.display = 'flex'
}

const formatPrice = (price) => {
  if (!price || price <= 0) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const hasDiscount = (product) => {
  if (product.name && product.name.includes('G304')) {
    console.log('G304 product discount data:', {
      name: product.name,
      discountType: product.discountType,
      discountPercentage: product.discountPercentage,
      discountAmount: product.discountAmount,
      discountName: product.discountName,
      minOrderValue: product.minOrderValue,
      minPrice: product.minPrice
    })
  }

  const hasDiscountValue = product.discountType === 'PERCENTAGE' && product.discountPercentage > 0 ||
                          product.discountType === 'FIXED' && product.discountAmount > 0 ||
                          product.discountPercentage > 0 ||
                          product.discountAmount > 0

  if (!hasDiscountValue) return false

  if (product.minOrderValue && product.minOrderValue > 0) {
    const productPrice = product.minPrice || 0
    return productPrice >= product.minOrderValue
  }

  return true
}

const getDiscountedPrice = (product) => {
  if (!hasDiscount(product)) return product.minPrice || 0

  const basePrice = product.minPrice || 0
  if (product.discountType === 'PERCENTAGE' && product.discountPercentage > 0) {
    return basePrice * (1 - product.discountPercentage / 100)
  } else if (product.discountType === 'FIXED' && product.discountAmount > 0) {
    return Math.max(0, basePrice - product.discountAmount)
  }
  return basePrice
}

const getDiscountedMaxPrice = (product) => {
  if (!hasDiscount(product)) return product.maxPrice || product.minPrice || 0

  const basePrice = product.maxPrice || product.minPrice || 0
  if (product.discountType === 'PERCENTAGE' && product.discountPercentage > 0) {
    return basePrice * (1 - product.discountPercentage / 100)
  } else if (product.discountType === 'FIXED' && product.discountAmount > 0) {
    return Math.max(0, basePrice - product.discountAmount)
  }
  return basePrice
}

const getOriginalPrice = (product) => {
  return product.minPrice || 0
}

const getOriginalMaxPrice = (product) => {
  return product.maxPrice || product.minPrice || 0
}



onMounted(() => {
  fetchSuggestedProducts()
})
</script>

<style scoped>
/* Product Card */
.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  transition: border-color 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  border-color: #3b82f6;
}

.product-image-container {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

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

.shop-name {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: auto;
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


</style>
