<template>
  <form @submit.prevent="login" class="w-full">
    <div class="text-xl">Đăng nhập</div>
    <div class="mt-4">
      Bạn chưa có tài khoản đăng nhập? <RouterLink to="/register" class="underline text-blue-600">Đăng ký</RouterLink>
    </div>
    <div v-if="errorMessage" class="mt-8 border border-red-500 text-red-500 py-3 px-4 rounded">
      {{ errorMessage }}
    </div>
    <div class="mt-4">
      <label for="username" class="mb-2 block">Username:</label>
      <input
        id="username"
        v-model="form.username"
        type="text"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập username"
        autocomplete="username"
      />
    </div>
    <div class="mt-4">
      <label for="password" class="mb-2 block">Password:</label>
      <input
        id="password"
        v-model="form.password"
        type="password"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập password"
        autocomplete="current-password"
      />
    </div>
    <div class="mt-8">
      <button class="w-full py-2 px-3 rounded border border-blue-600 hover:bg-blue-600 hover:text-white text-blue-600">
        Đăng nhập
      </button>
    </div>
    <div class="flex items-center my-4">
      <hr class="flex-grow border-t border-gray-300" />
      <span class="mx-4 text-gray-500">hoặc</span>
      <hr class="flex-grow border-t border-gray-300" />
    </div>
    <div>Đăng nhập Google (Chưa phát triển)</div>
  </form>
</template>

<script setup>
import axios from 'axios'
import { ref } from 'vue'
import { RouterLink } from 'vue-router'

const errorMessage = ref('')

const form = ref({
  username: 'tientvv',
  password: '123456',
})

const login = async () => {
  try {
    const res = await axios.post('/api/login', form.value)
    if (res.data.message) {
      errorMessage.value = res.data.message
      return
    }
    window.location.reload()
  } catch {
    errorMessage.value = 'Đã xảy ra lỗi! Vui lòng thử lại sau!'
  }
}
</script>

<style scoped>
button {
  color: #155dfc;
}
button {
  transition: all 0.3s ease;
}
button:hover {
  color: white;
}
</style>
