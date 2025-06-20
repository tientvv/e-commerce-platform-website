<script setup>
import { ref } from 'vue'
import LoginForm from './LoginForm.vue'
import RegisterForm from './RegisterForm.vue'

const activeTab = ref('login')

const switchTab = (tab) => {
  activeTab.value = tab
}
</script>

<template>
  <div class="h-full text-sm">
    <div class="flex flex-row items-center h-full">
      <div class="w-[400px] shadow rounded bg-white">
        <div class="flex">
          <button
            :class="
              activeTab === 'login'
                ? 'text-[16px] w-[50%] text-center px-4 py-5 border-b-2 border-blue-500'
                : 'text-[16px] w-[50%] text-center px-4 py-5 border-b-2 border-gray-300'
            "
            @click="switchTab('login')"
          >
            Đăng nhập
          </button>
          <button
            :class="
              activeTab === 'register'
                ? 'text-[16px] w-[50%] text-center px-4 py-5 border-b-2 border-blue-500'
                : 'text-[16px] w-[50%] text-center px-4 py-5 border-b-2 border-gray-300'
            "
            @click="switchTab('register')"
          >
            Đăng ký
          </button>
        </div>
        <div class="p-8">
          <LoginForm v-if="activeTab === 'login'" />
          <RegisterForm v-if="activeTab === 'register'" />
        </div>
      </div>
    </div>
  </div>
</template>
