<template>
  <RouterLink to="/cart" class="link button-primary flex items-center px-4 py-2 gap-2">
    <ShoppingCart width="24px" height="24px" />
    <span class="whitespace-nowrap">Giỏ hàng</span>
  </RouterLink>
</template>
<script setup>
import { ShoppingCart } from 'lucide-vue-next'
</script>
