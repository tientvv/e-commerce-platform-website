<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <div class="flex-1 max-w-[1440px] mx-auto flex justify-center w-full">
      <FormRegisterShopView />
    </div>
    <FooterView />
  </div>
</template>

<script setup>
import NavbarView from '../components/NavbarView.vue'
import FooterView from '../components/FooterView.vue'
import FormRegisterShopView from './FormRegisterShopView.vue'
</script>
