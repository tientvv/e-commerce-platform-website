<template>
  <div class="max-w-[1440px] w-full mx-auto flex gap-2 bg-white">
    <RouterLink to="/user/order" class="py-1 px-2 rounded outline-1 flex items-center gap-2 border border-gray-400">
      <ShoppingBasket width="18" />
      Đơn hàng (1) (Đang phát triển)
    </RouterLink>
    <RouterLink
      to="/user/shop/profile"
      class="py-1 px-2 rounded outline-1 flex items-center gap-2 border border-gray-400"
    >
      <Store width="18" />
      Kênh bán hàng
    </RouterLink>
  </div>
</template>

<script setup>
import { Store } from 'lucide-vue-next'
import { ShoppingBasket } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
</script>
