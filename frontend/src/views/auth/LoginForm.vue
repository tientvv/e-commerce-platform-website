<script setup>
import axios from 'axios'
import { reactive } from 'vue'

const formLogin = reactive({
  email: '',
  password: '',
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
    <input v-model="formLogin.email" name="email_user" type="text" placeholder="Email" />
    <input v-model="formLogin.password" name="pass_user" type="password" placeholder="Mật khẩu" />
    <button type="submit">Đăng nhập</button>
  </form>
</template>
