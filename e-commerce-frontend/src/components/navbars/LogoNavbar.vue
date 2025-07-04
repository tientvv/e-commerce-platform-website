<template>
  <router-link to="/" class="flex items-center gap-2 max-w-[80px]">
    <img class="w-full h-auto" :src="Logo" alt="Logo" />
  </router-link>
</template>

<script setup>
import Logo from '@/assets/logo.svg'
</script>
