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
            <span class="text-gray-800 font-medium">Danh sách yêu thích</span>
          </li>
        </ol>
      </nav>

      <!-- Page Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Danh sách yêu thích</h1>
        <p class="mt-2 text-gray-600">Các sản phẩm bạn đã yêu thích</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="text-center py-20">
        <AlertCircle class="w-20 h-20 text-red-500 mx-auto mb-4" />
        <h3 class="text-xl font-medium text-gray-800 mb-2">Không thể tải danh sách yêu thích</h3>
        <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
        <button
          @click="fetchWishlist"
          class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Thử lại
        </button>
      </div>

      <!-- Empty State -->
      <div v-else-if="wishlistItems.length === 0" class="text-center py-20">
        <Heart class="w-20 h-20 text-gray-400 mx-auto mb-4" />
        <h3 class="text-xl font-medium text-gray-800 mb-2">Danh sách yêu thích trống</h3>
        <p class="text-gray-600 mb-6">Bạn chưa có sản phẩm nào trong danh sách yêu thích.</p>
        <RouterLink
          to="/"
          class="inline-flex items-center px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          Tiếp tục mua sắm
        </RouterLink>
      </div>

      <!-- Wishlist Items -->
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
        <div
          v-for="item in wishlistItems"
          :key="item.id"
          class="bg-white rounded-lg border border-gray-200 overflow-hidden hover:shadow-lg transition-shadow"
        >
          <!-- Product Image -->
          <div class="relative aspect-square">
            <img
              v-if="item.productImage"
              :src="item.productImage"
              :alt="item.name"
              class="w-full h-full object-cover"
              @error="handleImageError"
            />
            <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
              <Package class="w-12 h-12 text-gray-400" />
            </div>

            <!-- Discount Badge -->
            <div v-if="hasDiscount(item)" class="absolute top-2 left-2">
              <div class="voucher-badge">
                <div class="voucher-badge-content">
                  <span v-if="item.discountType === 'PERCENTAGE' && item.discountPercentage > 0">
                    -{{ item.discountPercentage }}%
                  </span>
                  <span v-else-if="item.discountType === 'FIXED' && item.discountAmount > 0">
                    -{{ formatPrice(item.discountAmount) }}
                  </span>
                  <span v-else>
                    GIẢM GIÁ
                  </span>
                </div>
              </div>
            </div>

            <!-- Remove from Wishlist Button -->
            <button
              @click="removeFromWishlist(item.id, item.variantId)"
              class="absolute top-2 right-2 p-2 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors"
              title="Bỏ yêu thích"
            >
              <X class="w-4 h-4" />
            </button>
          </div>

          <!-- Product Info -->
          <div class="p-4">
            <h3 class="font-medium text-gray-900 mb-2 line-clamp-2">
              <RouterLink :to="`/product/${item.id}`" class="hover:text-blue-600">
                {{ item.name }}
              </RouterLink>
            </h3>
            <p class="text-sm text-gray-500 mb-2">{{ item.brand }}</p>

            <!-- Price -->
            <div class="flex items-center justify-between">
              <div class="flex items-center space-x-2">
                <span v-if="item.minPrice && item.minPrice > 0" class="text-lg font-bold text-blue-600">
                  {{ formatPrice(item.minPrice) }}
                </span>
                <span v-else class="text-lg font-bold text-gray-500">Liên hệ</span>
              </div>
            </div>

            <!-- Variant Info -->
            <div v-if="item.variantId && item.variantName" class="mt-1">
              <span class="text-sm text-gray-600">{{ item.variantName }}: {{ item.variantValue }}</span>
            </div>

            <!-- Shop Name -->
            <p class="text-sm text-gray-500 mt-2">{{ item.shopName }}</p>
          </div>
        </div>
      </div>
    </main>
    <FooterView />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import axios from 'axios'
import { Heart, ChevronRight, AlertCircle, Package, X } from 'lucide-vue-next'
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'

const wishlistItems = ref([])
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')

const fetchWishlist = async () => {
  try {
    loading.value = true
    error.value = false

    const response = await axios.get('/api/wishlist')
    wishlistItems.value = response.data.items || []
  } catch (err) {
    console.error('Error fetching wishlist:', err)
    error.value = true
    errorMessage.value = err.response?.data?.message || 'Đã xảy ra lỗi khi tải danh sách yêu thích'
  } finally {
    loading.value = false
  }
}

const removeFromWishlist = async (productId, variantId = null) => {
  try {
    const url = variantId
      ? `/api/wishlist/${productId}?variantId=${variantId}`
      : `/api/wishlist/${productId}`

    await axios.delete(url)
    wishlistItems.value = wishlistItems.value.filter(item => {
      if (variantId) {
        return !(item.id === productId && item.variantId === variantId)
      }
      return item.id !== productId
    })
  } catch (error) {
    console.error('Error removing from wishlist:', error)
    // Hiển thị thông báo lỗi từ backend nếu có
    if (error.response && error.response.data && error.response.data.message) {
      alert(error.response.data.message)
    } else {
      alert('Có lỗi xảy ra khi bỏ yêu thích sản phẩm')
    }
  }
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
  event.target.parentElement.innerHTML = `
    <div class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200">
      <Package class="w-12 h-12 text-gray-400" />
    </div>
  `
}

const formatPrice = (price) => {
  if (!price || price <= 0) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const hasDiscount = (item) => {
  // Kiểm tra xem có discount không
  const hasDiscountValue = (item?.discountPercentage && item.discountPercentage > 0) ||
                          (item?.discountAmount && item.discountAmount > 0) ||
                          (item?.discountType && item.discountType !== '')

  if (!hasDiscountValue) return false

  // Kiểm tra min_order_value nếu có
  if (item?.minOrderValue && item.minOrderValue > 0) {
    const productPrice = item.minPrice || 0
    return productPrice >= item.minOrderValue
  }

  return true
}

onMounted(() => {
  fetchWishlist()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.voucher-badge {
  position: relative;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 6px;
  padding: 4px 8px;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 10;
  margin-top: 4px;
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
