<script setup>
import { RouterLink } from 'vue-router'
import SearchInput from './SearchInput.vue'
</script>

<template>
  <div class="shadow text-sm px-4 bg-white">
    <nav class="w-[1200px] h-[85px] mx-auto flex items-center">
      <div class="w-[10%]">
        <img src="../../assets/logo.svg" alt="Logo_Brand" width="60" height="60" />
      </div>
      <SearchInput />
      <div class="w-[30%] flex justify-end gap-8">
        <RouterLink to="/">Trang chủ</RouterLink>
        <RouterLink to="/about">Yêu thích (1)</RouterLink>
        <RouterLink to="/login">Đăng nhập</RouterLink>
      </div>
    </nav>
  </div>
</template>
