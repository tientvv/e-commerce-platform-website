<template>
  <RouterLink
    to="/login"
    class="rounded bg-gray-200 text-gray-600 hover:bg-gray-300 link button-primary flex items-center px-4 py-2 gap-2"
  >
    <CircleUserRound width="24px" height="24px" />
    <span class="whitespace-nowrap">Tài khoản</span>
  </RouterLink>
</template>

<script setup>
import { CircleUserRound } from 'lucide-vue-next'
</script>
