<script setup>
import NavbarView from '@/components/common/Navbar/NavbarView.vue'
import FooterView from '@/components/common/Footer/FooterView.vue'
</script>

<template>
  <div class="d-flex vh-100 flex-column">
    <NavbarView />
    <main class="flex-grow-1">
      <RouterView />
    </main>
    <FooterView class="flex-shrink-1" />
  </div>
</template>
