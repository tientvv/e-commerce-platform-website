<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <main class="flex-1 w-[1440px] mx-auto">Danh sách yêu thích sản phẩm</main>
    <FooterView />
  </div>
</template>

<script setup>
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'
</script>
