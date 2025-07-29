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
                Hiển thị {{ startIndex + 1 }}-{{ endIndex }} của {{ totalProducts }} sản phẩm
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
            <div v-for="n in 8" :key="n" class="animate-pulse">
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
              v-for="product in paginatedProducts"
              :key="product.id"
              :class="
                viewMode === 'grid'
                  ? 'bg-white rounded border border-gray-200 hover:shadow-md transition-shadow duration-200 overflow-hidden group cursor-pointer'
                  : 'bg-white rounded border border-gray-200 hover:shadow-md transition-shadow duration-200 p-4 flex gap-4 cursor-pointer'
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
                      ? 'w-full h-full object-cover group-hover:scale-110 transition-transform duration-300'
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
                  <span class="text-xs text-gray-500">({{ product.reviewCount || 0 }})</span>
                </div>

                <div class="flex items-center justify-between">
                  <div class="flex items-center space-x-2">
                    <span v-if="product.minPrice && product.minPrice > 0" class="text-lg font-bold text-blue-600">
                      {{ formatPrice(product.minPrice) }}
                    </span>
                    <span v-else class="text-lg font-bold text-gray-500">Liên hệ</span>
                    <span
                      v-if="product.originalPrice && product.originalPrice > product.minPrice"
                      class="text-sm text-gray-500 line-through"
                    >
                      {{ formatPrice(product.originalPrice) }}
                    </span>
                  </div>
                  <div class="flex items-center text-sm text-gray-500">
                    <Eye class="w-4 h-4 mr-1" />
                    {{ product.viewCount || 0 }}
                  </div>
                </div>

                <!-- Discount Badge -->
                <div v-if="product.discountPercentage && product.discountPercentage > 0" class="mt-2">
                  <span class="inline-block bg-red-100 text-red-800 text-xs px-2 py-1 rounded-full">
                    -{{ product.discountPercentage }}%
                  </span>
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

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="mt-8 flex justify-center">
            <nav class="flex items-center space-x-2">
              <button
                @click="currentPage = Math.max(1, currentPage - 1)"
                :disabled="currentPage === 1"
                class="px-3 py-2 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Trước
              </button>

              <button
                v-for="page in visiblePages"
                :key="page"
                @click="currentPage = page"
                :class="page === currentPage ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-50'"
                class="px-3 py-2 text-sm border border-gray-300 rounded-md"
              >
                {{ page }}
              </button>

              <button
                @click="currentPage = Math.min(totalPages, currentPage + 1)"
                :disabled="currentPage === totalPages"
                class="px-3 py-2 text-sm border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Sau
              </button>
            </nav>
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

// Pagination
const currentPage = ref(1)
const itemsPerPage = ref(12)

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

const filteredProducts = computed(() => {
  let filtered = [...products.value]

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
    filtered = filtered.filter((product) => product.discountPercentage > 0)
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

const totalPages = computed(() => Math.ceil(totalProducts.value / itemsPerPage.value))

const startIndex = computed(() => (currentPage.value - 1) * itemsPerPage.value)
const endIndex = computed(() => Math.min(startIndex.value + itemsPerPage.value, totalProducts.value))

const paginatedProducts = computed(() => {
  return filteredProducts.value.slice(startIndex.value, endIndex.value)
})

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
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
  currentPage.value = 1
}

const handleProductClick = (product) => {
  router.push(`/product/${product.id}`)
}

// Removed handleAddToCart and handleContactProduct functions as they're no longer needed

const handleImageError = (event) => {
  event.target.style.display = 'none'
  event.target.parentElement.innerHTML = `
    <div class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
      <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
      </svg>
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

// Watchers
watch([priceFilter, selectedBrands, selectedRatings, inStockOnly, hasDiscount, sortBy], () => {
  currentPage.value = 1
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
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
