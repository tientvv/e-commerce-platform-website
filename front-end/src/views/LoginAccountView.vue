<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <main class="flex-1">
      <div class="max-w-[1440px] mx-auto flex justify-center h-full">
        <div class="flex flex-row justify-center items-center w-[500px] py-4">
          <div class="w-full bg-white p-4 rounded">
            <FormLogin />
          </div>
        </div>
      </div>
    </main>
    <FooterView />
  </div>
</template>

<script setup>
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'
import FormLogin from './LoginView/FormLoginAccount.vue'
</script>
