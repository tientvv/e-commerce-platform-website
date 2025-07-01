<template>
  <RouterLink to="/" class="link flex items-center px-4 py-2 gap-2">
    <Heart width="24px" height="24p" />
    <span class="whitespace-nowrap">Yêu thích</span>
  </RouterLink>
</template>

<script setup>
import { Heart } from 'lucide-vue-next'
</script>
