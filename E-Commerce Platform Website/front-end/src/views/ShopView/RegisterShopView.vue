<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <div class="flex-1 max-w-[1440px] mx-auto flex justify-center w-full">
      <div class="flex flex-row justify-center items-center w-[700px] p-4">
        <div class="w-full bg-white p-4 rounded">
          <FormRegisterShopView />
        </div>
      </div>
    </div>
    <FooterView />
  </div>
</template>

<script setup>
import NavbarView from '../components/NavbarView.vue'
import FooterView from '../components/FooterView.vue'
import FormRegisterShopView from './FormRegisterShopView.vue'
</script>
