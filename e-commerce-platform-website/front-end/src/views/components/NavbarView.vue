<template>
  <n-layout-header bordered class="h-16 p-4 sticky top-0 z-50 bg-white shadow-sm">
    <div class="flex max-w-[1440px] mx-auto items-center justify-between h-full">
      <!-- Logo -->
      <div>
        <RouterLink to="/">
          <img :src="Logo" width="50" alt="Logo" />
        </RouterLink>
      </div>

      <!-- Search -->
      <div class="flex-1 max-w-md mx-8 relative">
        <n-input
          v-model:value="searchQuery"
          placeholder="Tìm kiếm sản phẩm..."
          round
          @input="handleSearchInput"
          @focus="handleSearchFocus"
          @blur="handleSearchBlur"
          @keyup.enter="handleSearchEnter"
        >
          <template #suffix>
            <n-icon :component="Search" @click="handleSearchClick" style="cursor: pointer;" />
          </template>
        </n-input>

        <!-- Search Suggestions -->
        <div
          v-if="showSuggestions && searchResults.length > 0"
          class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-50 max-h-96 overflow-y-auto"
        >
          <div
            v-for="product in searchResults"
            :key="product.id"
            @click="selectProduct(product)"
            class="flex items-center gap-3 p-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0"
          >
            <div class="w-12 h-12 flex-shrink-0">
              <img
                :src="product.productImage"
                :alt="product.name"
                class="w-full h-full object-cover rounded"
                @error="handleImageError"
              />
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-medium text-gray-900 truncate">{{ product.name }}</div>
              <div class="text-sm text-gray-500">{{ product.brand }}</div>
              <div class="text-sm font-medium text-blue-600">
                {{ formatPrice(product.minPrice) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <n-space>
        <!-- Cart Button -->
        <RouterLink to="/cart">
          <n-button secondary>
            <template #icon>
              <n-icon :component="ShoppingCart" />
            </template>
            Giỏ hàng
            <n-badge v-if="cartItemCount > 0" :value="cartItemCount" :max="99" class="ml-2" />
          </n-button>
        </RouterLink>

        <RouterLink to="/wishlist">
          <n-button secondary>
            <template #icon>
              <n-icon :component="Heart" />
            </template>
            Yêu thích
            <n-badge v-if="wishlistCount > 0" :value="wishlistCount" :max="99" class="ml-2" />
          </n-button>
        </RouterLink>

        <RouterLink v-if="!accountInfo" to="/login">
          <n-button>
            <template #icon>
              <n-icon :component="LogIn" />
            </template>
            Đăng nhập
          </n-button>
        </RouterLink>

        <RouterLink v-if="accountInfo && accountInfo.role === 'ADMIN'" to="/admin">
          <n-button type="error">
            <template #icon>
              <n-icon :component="Settings" />
            </template>
            Trang quản trị
          </n-button>
        </RouterLink>

        <!-- User dropdown when logged in -->
        <n-dropdown v-if="accountInfo" :options="userMenuOptions" @select="handleUserMenuSelect" placement="bottom-end">
          <n-button>
            <template #icon>
              <n-icon :component="User" />
            </template>
            Xin chào, {{ accountInfo.username }}
          </n-button>
        </n-dropdown>
      </n-space>
    </div>
  </n-layout-header>
</template>

<script setup>
import { RouterLink, useRouter } from 'vue-router'
import Logo from '../../assets/logo.svg'
import { LogIn, Heart, User, Settings, Search, ShoppingCart, UserCircle, LogOut } from 'lucide-vue-next'
import { onMounted, ref, h } from 'vue'
import axios from '../../utils/axios'
import { NLayoutHeader, NSpace, NButton, NIcon, NInput, useMessage, NDropdown, NBadge } from 'naive-ui'
import { useCart } from '../../composables/useCart'

const router = useRouter()
const message = useMessage()
const { itemCount: cartItemCount } = useCart()

const accountInfo = ref(null)
const wishlistCount = ref(0)

// Search functionality
const searchQuery = ref('')
const searchResults = ref([])
const showSuggestions = ref(false)
const searchTimeout = ref(null)

const handleLogout = async () => {
  try {
    await axios.post('/api/logout')
    accountInfo.value = null
    message.success('Đăng xuất thành công')
    router.push('/')
  } catch {
    message.error('Đăng xuất thất bại')
  }
}

const userMenuOptions = [
  {
    label: () => h('div', { style: { display: 'flex', alignItems: 'center', gap: '8px' } }, [
      h(UserCircle, { size: 16 }),
      'Tổng quan tài khoản'
    ]),
    key: 'profile',
  },
  {
    label: () => h('div', { style: { display: 'flex', alignItems: 'center', gap: '8px' } }, [
      h(LogOut, { size: 16 }),
      'Đăng xuất'
    ]),
    key: 'logout',
  },
]

const handleUserMenuSelect = (key) => {
  if (key === 'logout') {
    handleLogout()
  } else if (key === 'profile') {
    router.push('/user/profile')
  }
}

onMounted(() => {
  fetchAccountInfo()
  fetchWishlistCount()
})

const fetchAccountInfo = async () => {
  try {
    const res = await axios.get('/api/info-account')
    if (res.data) {
      accountInfo.value = res.data.account
    }
  } catch {
    // Không hiển thị lỗi vì người dùng có thể chưa đăng nhập
  }
}

const fetchWishlistCount = async () => {
  try {
    const res = await axios.get('/api/wishlist/count')
    wishlistCount.value = res.data.count || 0
  } catch {
    // Không hiển thị lỗi vì người dùng có thể chưa đăng nhập
    wishlistCount.value = 0
  }
}

// Search methods
const handleSearchInput = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }

  if (searchQuery.value.trim().length < 2) {
    searchResults.value = []
    showSuggestions.value = false
    return
  }

  searchTimeout.value = setTimeout(async () => {
    try {
      console.log('Searching for:', searchQuery.value.trim())
      const response = await axios.get(`/api/products/search?query=${encodeURIComponent(searchQuery.value.trim())}`)
      console.log('Search response:', response.data)
      searchResults.value = response.data.products || []
      showSuggestions.value = searchResults.value.length > 0
    } catch (error) {
      console.error('Error searching products:', error)
      searchResults.value = []
      showSuggestions.value = false
    }
  }, 300)
}

const handleSearchFocus = () => {
  if (searchQuery.value.trim().length >= 2 && searchResults.value.length > 0) {
    showSuggestions.value = true
  }
}

const handleSearchBlur = () => {
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

const handleSearchEnter = () => {
  if (searchQuery.value.trim().length >= 2) {
    // Chuyển đến trang search với query
    router.replace(`/search?query=${encodeURIComponent(searchQuery.value.trim())}`)
  }
}

const handleSearchClick = () => {
  if (searchQuery.value.trim().length >= 2) {
    // Chuyển đến trang search với query
    router.replace(`/search?query=${encodeURIComponent(searchQuery.value.trim())}`)
  }
}

const selectProduct = (product) => {
  // Clear search state
  searchQuery.value = ''
  searchResults.value = []
  showSuggestions.value = false

  // Navigate to product detail
  router.push(`/product/${product.id}`).catch(err => {
    console.error('Navigation error:', err)
    // Fallback to window.location if router fails
    window.location.href = `/product/${product.id}`
  })
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
  event.target.parentElement.innerHTML = `
    <div class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200 rounded">
      <svg class="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clip-rule="evenodd" />
      </svg>
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
</script>

<style scoped>
/* Custom scrollbar cho dropdown search */
.max-h-96::-webkit-scrollbar {
  width: 6px;
}

.max-h-96::-webkit-scrollbar-track {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 3px;
}

.max-h-96::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 3px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.max-h-96::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
}
</style>
