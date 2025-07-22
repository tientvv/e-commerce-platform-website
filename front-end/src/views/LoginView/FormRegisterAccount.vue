<template>
  <form @submit.prevent="registerAccount">
    <div class="text-xl">Tạo tài khoản</div>
    <div class="mt-4">
      Bạn có tài khoản rồi? <RouterLink to="/login" class="underline text-blue-600">Đăng nhập</RouterLink>
    </div>
    <div v-if="successMessage" class="mt-8 border border-green-500 text-green-500 py-2 px-3 rounded">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="mt-8 border border-red-500 text-red-500 py-3 px-4 rounded">
      {{ errorMessage }}
    </div>
    <div class="mt-4 flex gap-4">
      <div class="w-[50%]">
        <label for="name" class="mb-2 block">Họ và tên:</label>
        <input
          id="name"
          v-model="form.name"
          type="text"
          class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          placeholder="Nhập họ và tên"
          autocomplete="name"
        />
      </div>
      <div class="w-[50%]">
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
    </div>
    <div class="mt-4">
      <label for="email" class="mb-2 block">Email:</label>
      <input
        id="email"
        v-model="form.email"
        type="text"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập email"
        autocomplete="email"
      />
    </div>
    <div class="mt-4">
      <label for="password" class="mb-2 block">Mật khẩu:</label>
      <input
        id="password"
        v-model="form.password"
        type="password"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập mật khẩu"
      />
    </div>
    <div class="mt-4">
      <label for="phone" class="mb-2 block">Số điện thoại:</label>
      <input
        id="phone"
        v-model="form.phone"
        type="text"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập số điện thoại"
        autocomplete="tel"
      />
    </div>
    <div class="mt-8">
      <button class="w-full py-2 px-3 rounded border border-blue-600 hover:bg-blue-600 hover:text-white text-blue-600">
        Đăng ký
      </button>
    </div>
    <div class="flex items-center my-4">
      <hr class="flex-grow border-t border-gray-300" />
      <span class="mx-4 text-gray-500">hoặc</span>
      <hr class="flex-grow border-t border-gray-300" />
    </div>
    <div>Đăng ký Google (Chưa phát triển)</div>
  </form>
</template>

<script setup>
import axios from 'axios'
import { ref } from 'vue'

const errorMessage = ref('')
const successMessage = ref('')

const form = ref({
  name: '',
  username: '',
  email: '',
  password: '',
  phone: '',
})

const resetForm = () => {
  form.value = {
    name: '',
    username: '',
    email: '',
    password: '',
    phone: '',
  }
}

const registerAccount = async () => {
  try {
    const res = await axios.post('/api/register', form.value)
    if (res.data.message === 'Đăng ký tài khoản thành công!') {
      successMessage.value = res.data.message
      resetForm()
      errorMessage.value = ''
    } else {
      errorMessage.value = res.data.message
    }
  } catch {
    errorMessage.value = 'Đã xảy ra lỗi! Vui lòng thử lại sau!'
    successMessage.value = ''
    resetForm()
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
