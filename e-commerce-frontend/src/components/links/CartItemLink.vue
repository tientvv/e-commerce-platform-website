<template>
  <RouterLink to="/cart" class="link button-primary flex items-center px-4 py-2 gap-2">
    <CircleUserRound />
    <span class="whitespace-nowrap">Giỏ hàng</span>
  </RouterLink>
</template>
<script setup>
import { CircleUserRound } from 'lucide-vue-next'
</script>
