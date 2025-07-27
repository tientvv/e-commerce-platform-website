<template>
  <div class="mt-2">
    <h2 class="text-lg font-semibold mb-4">Thống kê Cửa hàng</h2>

    <!-- Thông tin tổng quan -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-blue-50 border border-blue-200 rounded p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-blue-600">Tổng sản phẩm</p>
            <p class="text-2xl font-bold text-blue-800">{{ statistics.totalProducts }}</p>
          </div>
          <Package class="w-8 h-8 text-blue-600" />
        </div>
      </div>

      <div class="bg-green-50 border border-green-200 rounded p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-green-600">Sản phẩm hoạt động</p>
            <p class="text-2xl font-bold text-green-800">{{ statistics.activeProducts }}</p>
          </div>
          <CheckCircle class="w-8 h-8 text-green-600" />
        </div>
      </div>

      <div class="bg-purple-50 border border-purple-200 rounded p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-purple-600">Biến thể sản phẩm</p>
            <p class="text-2xl font-bold text-purple-800">{{ statistics.totalVariants }}</p>
          </div>
          <Settings class="w-8 h-8 text-purple-600" />
        </div>
      </div>

      <div class="bg-orange-50 border border-orange-200 rounded p-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-orange-600">Tổng ảnh</p>
            <p class="text-2xl font-bold text-orange-800">{{ statistics.totalImages }}</p>
          </div>
          <Image class="w-8 h-8 text-orange-600" />
        </div>
      </div>
    </div>

    <!-- Biểu đồ và thống kê chi tiết -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Sản phẩm theo danh mục -->
      <div class="bg-white border border-gray-200 rounded p-4">
        <h3 class="text-lg font-semibold mb-4">Sản phẩm theo danh mục</h3>
        <div v-if="categoryStats.length === 0" class="text-center py-8">
          <p class="text-gray-500">Chưa có dữ liệu</p>
        </div>
        <div v-else class="space-y-3">
          <div v-for="stat in categoryStats" :key="stat.categoryName" class="flex items-center justify-between">
            <span class="text-sm">{{ stat.categoryName }}</span>
            <div class="flex items-center gap-2">
              <div class="w-24 bg-gray-200 rounded-full h-2">
                <div class="bg-blue-600 h-2 rounded-full" :style="{ width: stat.percentage + '%' }"></div>
              </div>
              <span class="text-sm font-medium">{{ stat.count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Sản phẩm mới nhất -->
      <div class="bg-white border border-gray-200 rounded p-4">
        <h3 class="text-lg font-semibold mb-4">Danh sách sản phẩm</h3>
        <div v-if="recentProducts.length === 0" class="text-center py-8">
          <p class="text-gray-500">Chưa có sản phẩm nào</p>
        </div>
        <div v-else class="space-y-3">
          <div
            v-for="product in recentProducts"
            :key="product.id"
            class="flex items-center gap-3 p-2 hover:bg-gray-50 rounded"
          >
            <div class="w-12 h-12 bg-gray-200 rounded flex items-center justify-center">
              <img
                v-if="product.productImage"
                :src="product.productImage"
                :alt="product.name"
                class="w-12 h-12 object-cover rounded"
                @error="handleImageError"
              />
              <Package v-else class="w-6 h-6 text-gray-400" />
            </div>
            <div class="flex-1">
              <p class="font-medium text-sm">{{ product.name }}</p>
            </div>
            <span :class="product.isActive ? 'text-green-600' : 'text-red-600'" class="text-xs">
              {{ product.isActive ? 'Hoạt động' : 'Không hoạt động' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Package, CheckCircle, Settings, Image } from 'lucide-vue-next'
import axios from 'axios'

const statistics = ref({
  totalProducts: 0,
  activeProducts: 0,
  totalVariants: 0,
  totalImages: 0,
})

const categoryStats = ref([])
const recentProducts = ref([])

onMounted(() => {
  loadStatistics()
})

const loadStatistics = async () => {
  try {
    // Load tổng quan
    const productsResponse = await axios.get('/api/products/all')
    const products = productsResponse.data.products || []

    statistics.value.totalProducts = products.length
    statistics.value.activeProducts = products.filter((p) => p.isActive).length

    // Load biến thể
    const variantsResponse = await axios.get('/api/product-variants/all')
    statistics.value.totalVariants = variantsResponse.data.variants?.length || 0

    // Load ảnh
    const imagesResponse = await axios.get('/api/product-images/all')
    statistics.value.totalImages = imagesResponse.data.images?.length || 0

    // Load thống kê theo danh mục
    loadCategoryStats(products)

    // Load sản phẩm mới nhất
    loadRecentProducts(products)
  } catch (error) {
    console.error('Lỗi khi tải thống kê:', error)
  }
}

const loadCategoryStats = (products) => {
  const categoryMap = {}

  products.forEach((product) => {
    const categoryName = product.categoryName || 'Chưa phân loại'
    if (!categoryMap[categoryName]) {
      categoryMap[categoryName] = 0
    }
    categoryMap[categoryName]++
  })

  const total = products.length
  categoryStats.value = Object.entries(categoryMap)
    .map(([name, count]) => ({
      categoryName: name,
      count,
      percentage: total > 0 ? Math.round((count / total) * 100) : 0,
    }))
    .sort((a, b) => b.count - a.count)
}

const loadRecentProducts = (products) => {
  // Vì không có ngày tạo, chỉ lấy 5 sản phẩm đầu tiên
  recentProducts.value = products.slice(0, 5)
}

const handleImageError = (event) => {
  // Ẩn ảnh bị lỗi và hiển thị icon mặc định
  event.target.style.display = 'none'
  const parent = event.target.parentElement
  if (parent && !parent.querySelector('.fallback-icon')) {
    const icon = document.createElement('div')
    icon.className = 'fallback-icon w-6 h-6 text-gray-400'
    icon.innerHTML = '📦'
    parent.appendChild(icon)
  }
}
</script>
