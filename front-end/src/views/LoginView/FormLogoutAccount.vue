<template>
  <form @submit.prevent="logout">
    <button class="py-1 px-2 relative border border-red-500 flex items-center gap-2 rounded">
      <LogOut width="18" />
      Đăng xuất
    </button>
  </form>
</template>

<script setup>
import axios from 'axios'
import { LogOut } from 'lucide-vue-next'

const logout = async () => {
  try {
    await axios.post('/api/logout')
    window.location.href = '/login'
  } catch (error) {
    console.error('Logout failed:', error)
  }
}
</script>

<style scoped>
button {
  color: #fb2c36;
}

button {
  transition: all 0.3s ease;
}

button:hover {
  color: white;
  background-color: #fb2c36;
}
</style>
