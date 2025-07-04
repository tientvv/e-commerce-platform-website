<template>
  <RouterLink
    to="/"
    class="bg-gray-200 rounded text-gray-600 hover:bg-gray-300 link flex items-center px-4 py-2 gap-2"
  >
    <Heart width="24px" height="24px" />
    <span class="whitespace-nowrap">Yêu thích</span>
  </RouterLink>
</template>

<script setup>
import { Heart } from 'lucide-vue-next'
</script>
