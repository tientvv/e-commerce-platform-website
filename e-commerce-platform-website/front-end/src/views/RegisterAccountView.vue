<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <main class="flex-1">
      <div class="max-w-[1440px] mx-auto flex justify-center h-full">
        <div class="flex flex-row justify-center items-center py-4 w-[500px]">
          <div class="w-full bg-white p-4 rounded">
            <FormRegister />
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
import FormRegister from './LoginView/FormRegisterAccount.vue'
</script>
