<template>
  <div class="max-w-[1440px] w-full mx-auto flex gap-2 bg-white">
    <!-- Hiển thị link khác nhau dựa trên trạng thái shop -->
    <RouterLink
      v-if="hasShop"
      to="/user/shop/profile"
      class="py-1 px-2 bg-white text-gray-700 rounded-xs border border-gray-300 hover:bg-blue-600 hover:text-white hover:border-transparent transition-colors font-medium flex items-center gap-2"
    >
      <Store width="18" />
      Kênh bán hàng
    </RouterLink>

    <RouterLink
      v-else
      to="/register-shop"
      class="py-1 px-2 bg-white text-gray-700 rounded-xs border border-gray-300 hover:bg-blue-600 hover:text-white hover:border-transparent transition-colors font-medium flex items-center gap-2"
    >
      <Plus width="18" />
      Đăng ký cửa hàng
    </RouterLink>

    <RouterLink to="/user/profile" class="py-1 px-2 bg-white text-gray-700 rounded-xs border border-gray-300 hover:bg-blue-600 hover:text-white hover:border-transparent transition-colors font-medium flex items-center gap-2">
      <User width="18" />
      Thông tin cá nhân
    </RouterLink>

    <RouterLink to="/user/order" class="py-1 px-2 bg-white text-gray-700 rounded-xs border border-gray-300 hover:bg-blue-600 hover:text-white hover:border-transparent transition-colors font-medium flex items-center gap-2">
      <ShoppingBasket width="18" />
      Đơn hàng (1) (Đang phát triển)
    </RouterLink>
  </div>
</template>

<script setup>
import { Store, Plus, User } from 'lucide-vue-next'
import { ShoppingBasket } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import { ref, onMounted } from 'vue'
import axios from 'axios'

const hasShop = ref(false)

onMounted(() => {
  checkShopStatus()
})

const checkShopStatus = async () => {
  try {
    const response = await axios.get('/api/user/shop')
    hasShop.value = !!response.data.shop
  } catch {
    hasShop.value = false
  }
}
</script>
