<template>
  <form @submit.prevent="handleLogin" class="w-[400px] bg-white p-8 shadow rounded-xl">
    <div class="text-[18px] uppercase pb-4 border-b font-bold">Đăng nhập</div>
    <div class="pt-4">
      <ul>
        <li>
          <label for="emailUser">Email:</label>
          <input
            v-model="email"
            type="text"
            id="emailUser"
            placeholder="Nhâp email"
            class="w-full p-2 border rounded mt-2"
          />
        </li>
        <li class="mt-4">
          <label for="passwordUser">Mật khẩu:</label>
          <input
            v-model="password"
            type="password"
            id="passwordUser"
            placeholder="Nhập mật khẩu"
            class="w-full p-2 border rounded mt-2"
          />
        </li>
      </ul>
    </div>
    <div class="flex pt-8 gap-4">
      <LoginButton class="w-1/2 px-2 py-3 border rounded" />
      <RegisterUser class="w-1/2 px-2 py-3 border rounded" />
    </div>
  </form>
</template>

<script setup>
import { ref } from 'vue'
import LoginButton from '../buttons/LoginButton.vue'
import RegisterUser from '../links/RegisterUser.vue'
import { login } from '@/api/AuthController'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'
import router from '@/router'

const email = ref('admin')
const password = ref('admin123')

async function handleLogin() {
  try {
    await login(email.value, password.value)
    toast.success('Đăng nhập thành công', {
      position: 'top-right',
      autoClose: 750,
      theme: 'colored',
      onClose: () => router.push('/'),
    })
  } catch {
    toast.error('Email hoặc mật khẩu không chính xác', {
      position: 'top-right',
      autoClose: 3000,
      theme: 'colored',
    })
  }
}
</script>
