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
            <span class="text-gray-800 font-medium">{{ categoryName || 'Danh mục' }}</span>
          </li>
        </ol>
      </nav>

      <!-- Category Header -->
      <div class="mb-6">
        <h1 class="text-3xl font-bold text-gray-800 mb-2">{{ categoryName || 'Sản phẩm theo danh mục' }}</h1>
        <p class="text-gray-600">{{ totalProducts }} sản phẩm</p>
      </div>

      <!-- Main Content -->
      <div class="flex gap-6">
        <!-- Sidebar Filters -->
        <div class="w-64 flex-shrink-0">
          <div class="bg-white rounded border border-gray-200 p-6 sticky top-6">
            <!-- Filter Header -->
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold text-gray-800">Bộ lọc</h3>
              <button
                @click="clearAllFilters"
                class="text-sm text-blue-600 hover:text-blue-700"
                v-if="hasActiveFilters"
              >
                Xóa tất cả
              </button>
            </div>

            <!-- Price Range Filter -->
            <div class="mb-6">
              <h4 class="font-medium text-gray-700 mb-3">Khoảng giá</h4>
              <div class="space-y-2">
                <label class="flex items-center">
                  <input type="radio" v-model="priceFilter" value="" class="mr-2" />
                  <span class="text-sm">Tất cả</span>
                </label>
                <label class="flex items-center">
                  <input type="radio" v-model="priceFilter" value="0-100000" class="mr-2" />
                  <span class="text-sm">Dưới 100.000đ</span>
                </label>
                <label class="flex items-center">
                  <input type="radio" v-model="priceFilter" value="100000-500000" class="mr-2" />
                  <span class="text-sm">100.000đ - 500.000đ</span>
                </label>
                <label class="flex items-center">
                  <input type="radio" v-model="priceFilter" value="500000-1000000" class="mr-2" />
                  <span class="text-sm">500.000đ - 1.000.000đ</span>
                </label>
                <label class="flex items-center">
                  <input type="radio" v-model="priceFilter" value="1000000+" class="mr-2" />
                  <span class="text-sm">Trên 1.000.000đ</span>
                </label>
              </div>
            </div>

            <!-- Brand Filter -->
            <div class="mb-6">
              <h4 class="font-medium text-gray-700 mb-3">Thương hiệu</h4>
              <div class="space-y-2 max-h-40 overflow-y-auto">
                <label v-for="brand in availableBrands" :key="brand" class="flex items-center">
                  <input type="checkbox" v-model="selectedBrands" :value="brand" class="mr-2" />
                  <span class="text-sm">{{ brand }}</span>
                </label>
              </div>
            </div>

            <!-- Rating Filter -->
            <div class="mb-6">
              <h4 class="font-medium text-gray-700 mb-3">Đánh giá</h4>
              <div class="space-y-2">
                <label v-for="rating in [5, 4, 3, 2, 1]" :key="rating" class="flex items-center">
                  <input type="checkbox" v-model="selectedRatings" :value="rating" class="mr-2" />
                  <div class="flex items-center mr-2">
                    <Star
                      v-for="i in 5"
                      :key="i"
                      :class="i <= rating ? 'text-yellow-400 fill-current' : 'text-gray-300'"
                      class="w-4 h-4"
                    />
                  </div>
                  <span class="text-sm">trở lên</span>
                </label>
              </div>
            </div>

            <!-- Availability Filter -->
            <div class="mb-6">
              <h4 class="font-medium text-gray-700 mb-3">Tình trạng</h4>
              <div class="space-y-2">
                <label class="flex items-center">
                  <input type="checkbox" v-model="inStockOnly" class="mr-2" />
                  <span class="text-sm">Còn hàng</span>
                </label>
                <label class="flex items-center">
                  <input type="checkbox" v-model="hasDiscount" class="mr-2" />
                  <span class="text-sm">Có giảm giá</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Products Section -->
        <div class="flex-1">
          <!-- Toolbar -->
          <div class="bg-white rounded border border-gray-200 p-4 mb-6">
            <div class="flex items-center justify-between">
              <!-- Results Info -->
              <div class="text-sm text-gray-600">
                Hiển thị {{ displayedProducts.length }} của {{ totalProducts }} sản phẩm
              </div>

              <!-- Sort Options -->
              <div class="flex items-center space-x-4">
                <span class="text-sm text-gray-600">Sắp xếp theo:</span>
                <select
                  v-model="sortBy"
                  class="border border-gray-300 rounded-md px-3 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="newest">Mới nhất</option>
                  <option value="price-asc">Giá tăng dần</option>
                  <option value="price-desc">Giá giảm dần</option>
                  <option value="name-asc">Tên A-Z</option>
                  <option value="name-desc">Tên Z-A</option>
                  <option value="popular">Phổ biến</option>
                </select>

                <!-- View Toggle -->
                <div class="flex items-center space-x-2">
                  <button
                    @click="viewMode = 'grid'"
                    :class="viewMode === 'grid' ? 'text-blue-600 bg-blue-50' : 'text-gray-400'"
                    class="p-2 rounded-md hover:bg-gray-100 transition-colors"
                  >
                    <Grid class="w-4 h-4" />
                  </button>
                  <button
                    @click="viewMode = 'list'"
                    :class="viewMode === 'list' ? 'text-blue-600 bg-blue-50' : 'text-gray-400'"
                    class="p-2 rounded-md hover:bg-gray-100 transition-colors"
                  >
                    <List class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Loading state -->
          <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            <div v-for="n in 36" :key="n" class="animate-pulse">
              <div class="bg-gray-200 rounded-lg aspect-square mb-4"></div>
              <div class="bg-gray-200 h-4 rounded mb-2"></div>
              <div class="bg-gray-200 h-4 rounded w-2/3"></div>
            </div>
          </div>

          <!-- Products Grid -->
          <div
            v-else-if="filteredProducts.length > 0"
            :class="
              viewMode === 'grid' ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6' : 'space-y-4'
            "
          >
            <div
              v-for="product in displayedProducts"
              :key="product.id"
              :class="
                viewMode === 'grid'
                  ? 'bg-white rounded border border-gray-200 hover:border-blue-300 transition-colors duration-200 overflow-hidden group cursor-pointer'
                  : 'bg-white rounded border border-gray-200 hover:border-blue-300 transition-colors duration-200 p-4 flex gap-4 cursor-pointer'
              "
              @click="handleProductClick(product)"
            >
              <!-- Product Image -->
              <div
                :class="
                  viewMode === 'grid'
                    ? 'aspect-square bg-gray-100 relative overflow-hidden'
                    : 'w-24 h-24 bg-gray-100 rounded-lg relative overflow-hidden flex-shrink-0'
                "
              >
                <img
                  v-if="product.productImage"
                  :src="product.productImage"
                  :alt="product.name"
                  :class="
                    viewMode === 'grid'
                      ? 'w-full h-full object-cover'
                      : 'w-full h-full object-cover'
                  "
                  @error="handleImageError"
                />
                <div
                  v-else
                  :class="
                    viewMode === 'grid'
                      ? 'w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200'
                      : 'w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200'
                  "
                >
                  <Package class="w-8 h-8 text-gray-400" />
                </div>

                <!-- Discount Badge - Top Right -->
                <div v-if="hasProductDiscount(product)" class="absolute top-2 right-2">
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
              <div :class="viewMode === 'grid' ? 'p-4' : 'flex-1'">
                <h3
                  :class="
                    viewMode === 'grid'
                      ? 'font-medium text-gray-800 mb-2 line-clamp-2 group-hover:text-blue-600 transition-colors duration-200'
                      : 'font-medium text-gray-800 mb-2 group-hover:text-blue-600 transition-colors duration-200'
                  "
                >
                  {{ product.name }}
                </h3>
                <p class="text-sm text-gray-500 mb-2">{{ product.brand }}</p>

                <!-- Rating -->
                <div class="flex items-center mb-2">
                  <div class="flex items-center mr-2">
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

                <div class="flex items-center justify-between">
                  <div class="flex flex-col">
                    <!-- Price Range for products with variants -->
                    <div v-if="product.maxPrice && product.maxPrice > product.minPrice" class="flex flex-col">
                      <div class="flex items-center">
                        <span class="text-lg font-bold text-blue-600 whitespace-nowrap">
                          {{ formatPrice(getDiscountedPrice(product)) }} - {{ formatPrice(getDiscountedMaxPrice(product)) }}
                        </span>
                      </div>
                      <div v-if="getOriginalPrice(product) > 0 && getOriginalPrice(product) > getDiscountedPrice(product)" class="flex items-center">
                        <span class="text-sm text-gray-500 line-through whitespace-nowrap">
                          {{ formatPrice(getOriginalPrice(product)) }} - {{ formatPrice(getOriginalMaxPrice(product)) }}
                        </span>
                      </div>
                    </div>

                    <!-- Single price for products without variants -->
                    <div v-else class="flex flex-col">
                      <div class="flex items-center">
                        <span v-if="getDiscountedPrice(product) > 0" class="text-lg font-bold text-blue-600">
                          {{ formatPrice(getDiscountedPrice(product)) }}
                        </span>
                        <span v-else-if="product.minPrice && product.minPrice > 0" class="text-lg font-bold text-blue-600">
                          {{ formatPrice(product.minPrice) }}
                        </span>
                        <span v-else class="text-lg font-bold text-gray-500">Liên hệ</span>
                      </div>
                      <div v-if="getOriginalPrice(product) > 0 && getOriginalPrice(product) > getDiscountedPrice(product)" class="flex items-center">
                        <span class="text-sm text-gray-500 line-through">
                          {{ formatPrice(getOriginalPrice(product)) }}
                        </span>
                      </div>
                      <div v-else-if="!getDiscountedPrice(product) && !product.minPrice" class="flex items-center">
                        <span class="text-sm text-gray-500" style="height: 1.25rem; display: inline-block;">&nbsp;</span>
                      </div>
                    </div>
                  </div>
                  <div class="flex items-center text-sm text-gray-500">
                    <Eye class="w-4 h-4 mr-1" />
                    {{ product.viewCount || 0 }}
                  </div>
                </div>

                <!-- Removed Add to Cart Button - users can click on product to view details -->
              </div>
            </div>
          </div>

          <!-- Empty state -->
          <div v-else-if="!loading" class="text-center py-16">
            <Package class="w-20 h-20 text-gray-300 mx-auto mb-4" />
            <h3 class="text-xl font-medium text-gray-600 mb-2">Không tìm thấy sản phẩm</h3>
            <p class="text-gray-500 mb-6">Thử điều chỉnh bộ lọc hoặc tìm kiếm với từ khóa khác.</p>
            <button
              @click="clearAllFilters"
              class="inline-flex items-center px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors duration-200"
            >
              Xóa bộ lọc
            </button>
          </div>

          <!-- Error state -->
          <div v-if="error" class="text-center py-16">
            <div class="text-red-500 mb-4">
              <AlertCircle class="w-20 h-20 mx-auto mb-4" />
              <h3 class="text-xl font-medium mb-2">Không thể tải sản phẩm</h3>
              <p class="text-gray-600 mb-6">Đã xảy ra lỗi khi tải danh sách sản phẩm.</p>
            </div>
            <button
              @click="fetchProducts"
              class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors duration-200"
            >
              Thử lại
            </button>
          </div>

                    <!-- Load More Button -->
          <div v-if="hasMoreProducts" class="mt-8 flex justify-center">
            <button
              @click="loadMoreProducts"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 font-medium"
            >
              Xem thêm
            </button>
          </div>
        </div>
      </div>
    </main>
    <FooterView />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import axios from 'axios'
import { Package, AlertCircle, Eye, ChevronRight, Star, Grid, List } from 'lucide-vue-next'
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'
// Removed useCart import as it's no longer needed

const route = useRoute()
const router = useRouter()
// Removed addItem as it's no longer needed

// Data
const products = ref([])
const categoryName = ref('')
const loading = ref(true)
const error = ref(false)

// Filters
const priceFilter = ref('')
const selectedBrands = ref([])
const selectedRatings = ref([])
const inStockOnly = ref(false)
const hasDiscount = ref(false)

// View and Sort
const viewMode = ref('grid')
const sortBy = ref('newest')

// Load More
const displayedCount = ref(36) // Số sản phẩm hiển thị ban đầu

// Computed
const totalProducts = computed(() => filteredProducts.value.length)

const availableBrands = computed(() => {
  const brands = [...new Set(products.value.map((p) => p.brand).filter(Boolean))]
  return brands.sort()
})

const hasActiveFilters = computed(() => {
  return (
    priceFilter.value ||
    selectedBrands.value.length > 0 ||
    selectedRatings.value.length > 0 ||
    inStockOnly.value ||
    hasDiscount.value
  )
})

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
const filteredProducts = computed(() => {
  let filtered = [...products.value]

  // Loại bỏ trùng lặp sản phẩm và chỉ giữ lại sản phẩm với discount tốt nhất
  const uniqueProducts = new Map()

  filtered.forEach(product => {
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
  filtered = Array.from(uniqueProducts.values())

  // Price filter
  if (priceFilter.value) {
    const [min, max] = priceFilter.value.split('-').map(Number)
    filtered = filtered.filter((product) => {
      const price = product.minPrice || 0
      if (max) {
        return price >= min && price <= max
      } else {
        return price >= min
      }
    })
  }

  // Brand filter
  if (selectedBrands.value.length > 0) {
    filtered = filtered.filter((product) => selectedBrands.value.includes(product.brand))
  }

  // Rating filter
  if (selectedRatings.value.length > 0) {
    filtered = filtered.filter((product) => {
      const rating = product.rating || 0
      return selectedRatings.value.some((r) => rating >= r)
    })
  }

  // Stock filter
  if (inStockOnly.value) {
    filtered = filtered.filter((product) => product.stockQuantity > 0)
  }

  // Discount filter
  if (hasDiscount.value) {
    filtered = filtered.filter((product) => hasProductDiscount(product))
  }

  // Sort
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'price-asc':
        return (a.minPrice || 0) - (b.minPrice || 0)
      case 'price-desc':
        return (b.minPrice || 0) - (a.minPrice || 0)
      case 'name-asc':
        return a.name.localeCompare(b.name)
      case 'name-desc':
        return b.name.localeCompare(a.name)
      case 'popular':
        return (b.viewCount || 0) - (a.viewCount || 0)
      default: // newest
        return new Date(b.createdAt || 0) - new Date(a.createdAt || 0)
    }
  })

  return filtered
})

const displayedProducts = computed(() => {
  return filteredProducts.value.slice(0, displayedCount.value)
})

const hasMoreProducts = computed(() => {
  return displayedCount.value < filteredProducts.value.length
})

// Methods
const fetchProducts = async () => {
  try {
    loading.value = true
    error.value = false

    const categoryId = route.params.id

    // Fetch category info
    const categoriesResponse = await axios.get('/api/categories')
    const category = categoriesResponse.data.find((cat) => cat.id === categoryId)
    if (category) {
      categoryName.value = category.name
    }

    // Fetch products by category
    const productsResponse = await axios.get(`/api/products/all?categoryId=${categoryId}`)
    products.value = productsResponse.data.products || []
  } catch (err) {
    console.error('Error fetching products:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

const clearAllFilters = () => {
  priceFilter.value = ''
  selectedBrands.value = []
  selectedRatings.value = []
  inStockOnly.value = false
  hasDiscount.value = false
  displayedCount.value = 36 // Reset về 36 sản phẩm
}

const loadMoreProducts = () => {
  displayedCount.value += 36 // Thêm 36 sản phẩm nữa
}

const handleProductClick = (product) => {
  router.push(`/product/${product.id}`)
}

// Removed handleAddToCart and handleContactProduct functions as they're no longer needed

const handleImageError = (event) => {
  event.target.style.display = 'none'
  event.target.parentElement.innerHTML = `
    <div class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
      <Package class="w-8 h-8 text-gray-400" />
    </div>
  `
}

const formatPrice = (price) => {
  if (!price) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

const getDiscountedPrice = (product) => {
  // Kiểm tra điều kiện áp dụng discount
  if (!hasProductDiscount(product)) {
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
  if (!hasProductDiscount(product)) {
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

const getOriginalPrice = (product) => {
  if (product.originalPrice && product.originalPrice > 0) {
    return product.originalPrice
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

const hasProductDiscount = (product) => {
  // Kiểm tra xem có discount không
  const hasDiscountValue = product.discountType === 'PERCENTAGE' && product.discountPercentage > 0 ||
                          product.discountType === 'FIXED' && product.discountAmount > 0

  if (!hasDiscountValue) return false

  // Kiểm tra min_order_value nếu có
  if (product.minOrderValue && product.minOrderValue > 0) {
    const productPrice = product.minPrice || 0
    return productPrice >= product.minOrderValue
  }

  return true
}

// Watchers
watch([priceFilter, selectedBrands, selectedRatings, inStockOnly, hasDiscount, sortBy], () => {
  displayedCount.value = 36 // Reset về 36 sản phẩm khi thay đổi filter
})

// Lifecycle
onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.animate-pulse {
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

/* Custom scrollbar for brand filter */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 3px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
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
