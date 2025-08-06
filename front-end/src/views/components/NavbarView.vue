<template>
  <n-layout-header bordered class="h-16 p-4">
    <div class="flex max-w-[1440px] mx-auto items-center justify-between h-full">
      <!-- Logo -->
      <div>
        <RouterLink to="/">
          <img :src="Logo" width="50" alt="Logo" />
        </RouterLink>
      </div>

      <!-- Search -->
      <div class="flex-1 max-w-md mx-8">
        <n-input placeholder="Tìm kiếm sản phẩm (Chưa phát triển)" disabled round>
          <template #suffix>
            <n-icon :component="Search" />
          </template>
        </n-input>
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

        <!-- User dropdown when logged in -->
        <n-dropdown v-if="accountInfo" :options="userMenuOptions" @select="handleUserMenuSelect">
          <n-button>
            <template #icon>
              <n-icon :component="User" />
            </template>
            Xin chào, {{ accountInfo.username }}
          </n-button>
        </n-dropdown>

        <RouterLink v-if="accountInfo && accountInfo.role === 'ADMIN'" to="/admin">
          <n-button type="error">
            <template #icon>
              <n-icon :component="Settings" />
            </template>
            Trang quản trị
          </n-button>
        </RouterLink>
      </n-space>
    </div>
  </n-layout-header>
</template>

<script setup>
import { RouterLink, useRouter } from 'vue-router'
import Logo from '../../assets/logo.svg'
import { LogIn, Heart, User, Settings, Search, ShoppingCart } from 'lucide-vue-next'
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { NLayoutHeader, NSpace, NButton, NIcon, NInput, useMessage, NDropdown, NBadge } from 'naive-ui'
import { useCart } from '../../composables/useCart'

const router = useRouter()
const message = useMessage()
const { itemCount: cartItemCount } = useCart()

const accountInfo = ref(null)

const handleLogout = async () => {
  try {
    await axios.post('/api/logout')
    accountInfo.value = null
    message.success('Đăng xuất thành công')
    router.push('/')
    window.location.reload()
  } catch {
    message.error('Đăng xuất thất bại')
  }
}

const userMenuOptions = [
  {
    label: 'Hồ sơ của tôi',
    key: 'profile',
  },
  {
    label: 'Đăng xuất',
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
</script>
