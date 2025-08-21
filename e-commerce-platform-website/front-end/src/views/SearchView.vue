<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <main class="flex-1 max-w-[1440px] w-full mx-auto px-4 py-6">
      <!-- Search Header -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900 mb-2">
          Kết quả tìm kiếm cho "{{ searchQuery }}"
        </h1>
        <p class="text-gray-600">
          Tìm thấy {{ searchResults.length }} sản phẩm
        </p>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="text-center py-12">
        <p class="text-red-600 text-lg">{{ errorMessage }}</p>
        <button
          @click="performSearch"
          class="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          Thử lại
        </button>
      </div>

      <!-- Search Results -->
      <div v-else-if="searchResults.length > 0" class="space-y-6">
        <!-- Products Grid -->
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
          <div
            v-for="product in displayedProducts"
            :key="product.id"
            @click="goToProduct(product.id)"
            class="product-card cursor-pointer"
          >
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
                    <span v-else>
                      GIẢM GIÁ
                    </span>
                  </div>
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
                    <span class="text-lg font-bold text-green-600 whitespace-nowrap">
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
        <div v-if="searchResults.length > 36" class="text-center">
          <button
            @click="loadMore"
            class="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Xem thêm
          </button>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <div class="mb-4">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">Không tìm thấy sản phẩm</h3>
        <p class="text-gray-600 mb-4">
          Không có sản phẩm nào phù hợp với từ khóa "{{ searchQuery }}"
        </p>
        <div class="space-y-2">
          <p class="text-sm text-gray-500">Thử:</p>
          <ul class="text-sm text-gray-500 list-disc list-inside">
            <li>Kiểm tra lại chính tả</li>
            <li>Sử dụng từ khóa khác</li>
            <li>Tìm kiếm với từ khóa ngắn hơn</li>
          </ul>
        </div>
      </div>
    </main>
    <FooterView />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '~/utils/axios'
import { Star } from 'lucide-vue-next'
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'

const route = useRoute()
const router = useRouter()

// Reactive data
const searchQuery = ref('')
const searchResults = ref([])
const isLoading = ref(false)
const error = ref(false)
const errorMessage = ref('')
const displayedCount = ref(36)

// Computed
// Helper function để so sánh discount (ưu tiên percentage trước)
const compareDiscounts = (product1, product2) => {
  // Ưu tiên 1: Percentage discount luôn tốt hơn Fixed discount
  const hasPercentage1 = product1.discountPercentage && product1.discountPercentage > 0
  const hasPercentage2 = product2.discountPercentage && product2.discountPercentage > 0

  if (hasPercentage1 && !hasPercentage2) {
    return 1 // product1 tốt hơn
  }
  if (!hasPercentage1 && hasPercentage2) {
    return -1 // product2 tốt hơn
  }

  // Nếu cùng loại, so sánh giá trị thực tế
  const value1 = getDiscountValue(product1)
  const value2 = getDiscountValue(product2)

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

// Computed property để lọc và loại bỏ trùng lặp sản phẩm
const filteredSearchResults = computed(() => {
  // Loại bỏ trùng lặp sản phẩm và chỉ giữ lại sản phẩm với discount tốt nhất
  const uniqueProducts = new Map()

  searchResults.value.forEach(product => {
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

  // Chuyển về array
  return Array.from(uniqueProducts.values())
})

const displayedProducts = computed(() => {
  return filteredSearchResults.value.slice(0, displayedCount.value)
})

const hasDiscount = (product) => {
  const hasDiscountValue = product.discountType === 'PERCENTAGE' && product.discountPercentage > 0 ||
                          product.discountType === 'FIXED' && product.discountAmount > 0

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

// Methods
const performSearch = async (query = null) => {
  const searchTerm = query || searchQuery.value.trim()
  if (!searchTerm) return

  console.log('Performing search for:', searchTerm)
  isLoading.value = true
  error.value = false

  // Reset state for new search
  searchResults.value = []
  displayedCount.value = 36

  try {
    console.log('Searching for:', searchTerm)
    const response = await axios.get(`/api/products/search?query=${encodeURIComponent(searchTerm)}`)
    console.log('Search response:', response.data)
    searchResults.value = response.data.products || []
  } catch (err) {
    console.error('Error searching products:', err)
    error.value = true
    errorMessage.value = 'Không thể tìm kiếm sản phẩm. Vui lòng thử lại sau.'
    searchResults.value = []
  } finally {
    isLoading.value = false
  }
}

const goToProduct = (productId) => {
  console.log('Navigating to product:', productId)

  // Force navigation by replacing current route
  router.replace(`/product/${productId}`).then(() => {
    console.log('Navigation successful')
  }).catch(err => {
    console.error('Navigation error:', err)
    // Fallback to window.location if router fails
    window.location.href = `/product/${productId}`
  })
}

const loadMore = () => {
  displayedCount.value += 36
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

onMounted(() => {
  // Lấy query từ URL
  const query = route.query.query
  if (query) {
    searchQuery.value = query
    performSearch(query)
  }
})

// Watch for route query changes to reload search
watch(() => route.query.query, (newQuery, oldQuery) => {
  console.log('Route query changed:', { newQuery, oldQuery })
  if (newQuery && newQuery !== oldQuery) {
    console.log('Query changed, performing new search:', newQuery)
    searchQuery.value = newQuery
    performSearch(newQuery)
  }
}, { immediate: true })
</script>

<style scoped>
/* Product Card */
.product-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  transition: border-color 0.3s ease;
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
