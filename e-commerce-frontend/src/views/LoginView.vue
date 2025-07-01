<template>
  <main class="flex flex-col h-screen">
    <MainNavbar />
    <div class="flex-1 px-4 pb-4">
      <div class="max-w-[1440px] h-full mx-auto flex flex-row justify-center items-center">
        <LoginForm />
      </div>
    </div>
    <div class="px-4 bg-white shadow">
      <MainFooter />
    </div>
  </main>
</template>

<script setup>
import MainNavbar from '@/components/navbars/MainNavbar.vue'
import LoginForm from '@/components/forms/LoginForm.vue'
import MainFooter from '@/components/layouts/MainFooter.vue'
</script>
