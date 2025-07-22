<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <div class="bg-white py-4 px-2">
      <NavbarUserView />
    </div>
    <main class="flex-1 max-w-[1440px] w-full mx-auto">
      <RouterView />
    </main>
    <FooterView />
  </div>
</template>

<script setup>
import NavbarView from '../components/NavbarView.vue'
import FooterView from '../components/FooterView.vue'
import NavbarUserView from './NavbarUserView.vue'
</script>
