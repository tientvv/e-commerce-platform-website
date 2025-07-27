<template>
  <div class="max-w-[1440px] w-full mx-auto flex gap-2 bg-white">
    <RouterLink to="/user/order" class="py-1 px-2 rounded outline-1 flex items-center gap-2 border border-gray-400">
      <ShoppingBasket width="18" />
      Đơn hàng (1) (Đang phát triển)
    </RouterLink>

    <!-- Hiển thị link khác nhau dựa trên trạng thái shop -->
    <RouterLink
      v-if="hasShop"
      to="/user/shop/profile"
      class="py-1 px-2 rounded outline-1 flex items-center gap-2 border border-gray-400"
    >
      <Store width="18" />
      Kênh bán hàng
    </RouterLink>

    <RouterLink
      v-else
      to="/register-shop"
      class="py-1 px-2 rounded outline-1 flex items-center gap-2 border border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white"
    >
      <Plus width="18" />
      Đăng ký cửa hàng
    </RouterLink>
  </div>
</template>

<script setup>
import { Store, Plus } from 'lucide-vue-next'
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
