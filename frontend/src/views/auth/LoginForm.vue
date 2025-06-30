<script setup>
import axios from 'axios'
import { reactive } from 'vue'

const formLogin = reactive({
  email: 'user',
  password: 'user123',
})

const handleLogin = async () => {
  try {
    await axios.post('/api/login', formLogin)
  } catch (error) {
    console.error('Login failed:', error)
  }
}
</script>

<template>
  <form @submit.prevent="handleLogin">
    <div class="flex flex-col">
      <input
        class="p-3 border border-[#00000042] text-[#000c] mb-7"
        v-model="formLogin.email"
        name="email_user"
        type="text"
        placeholder="Email"
      />
      <input
        class="p-3 border border-[#00000042] text-[#000c] mb-10"
        v-model="formLogin.password"
        name="pass_user"
        type="password"
        placeholder="Mật khẩu"
      />
      <button class="p-2.5 bg-emerald-600 cursor-pointer" type="submit">Đăng nhập</button>
    </div>
  </form>
</template>
