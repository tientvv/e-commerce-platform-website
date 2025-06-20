<script setup>
import { RouterView } from 'vue-router'
import NavbarView from './components/NavbarView/NavbarView.vue'
</script>

<template>
  <div class="flex flex-col h-screen bg-gray-50">
    <NavbarView />
    <div class="flex-1 w-[1200px] mx-auto px-4">
      <RouterView />
    </div>
    <div class="shrink">Footer</div>
  </div>
</template>

<style scoped></style>
