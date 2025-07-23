<template>
  <form @submit.prevent="registerShop">
    <div class="text-xl">Đăng ký bán hàng</div>
    <div v-if="successMessage" class="mt-8 border border-green-500 text-green-500 py-3 px-4 rounded">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="mt-8 border border-red-500 text-red-500 py-3 px-4 rounded">
      {{ errorMessage }}
    </div>
    <div class="mt-4">
      <label for="shopName" class="mb-2 block"> Tên cửa hàng:<span class="text-red-500">*</span> </label>
      <input
        id="shopName"
        v-model="shopName"
        type="text"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập tên cửa hàng"
      />
    </div>
    <div class="mt-4">
      <label for="description" class="mb-2 block"> Mô tả cửa hàng:</label>
      <textarea
        id="description"
        v-model="description"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập mô tả cửa hàng"
      ></textarea>
    </div>
    <div class="mt-4 flex gap-4">
      <div class="w-[50%]">
        <label for="email" class="mb-2 block">Email:<span class="text-red-500">*</span></label>
        <input
          id="email"
          v-model="email"
          type="text"
          class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          placeholder="Nhập email"
          autocomplete="email"
        />
      </div>
      <div class="w-[50%]">
        <label for="phone" class="mb-2 block">Số điện thoại:<span class="text-red-500">*</span></label>
        <input
          id="phone"
          v-model="phone"
          type="text"
          class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          placeholder="Nhập số điện thoại"
          autocomplete="tel"
        />
      </div>
    </div>
    <div class="mt-4">
      <label for="address" class="mb-2 block">Địa chỉ:<span class="text-red-500">*</span></label>
      <input
        id="address"
        v-model="address"
        type="text"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập địa chỉ cửa hàng"
        autocomplete="address"
      />
    </div>
    <div class="mt-4">
      <label for="image" class="mb-2 block">Hình ảnh cửa hàng:</label>
      <input
        id="image"
        type="file"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        ref="imageInput"
        @change="handleImageChange"
      />
    </div>
    <div class="mt-8">
      <button
        :disabled="isLoading"
        class="w-full py-2 px-3 rounded border border-blue-600 hover:bg-blue-600 hover:text-white text-blue-600"
      >
        <span v-if="isLoading">Đang đăng ký...</span>
        <span v-else>Đăng ký</span>
      </button>
    </div>
  </form>
</template>

<script setup>
import axios from 'axios'
import { ref } from 'vue'

const shopName = ref('')
const description = ref('')
const email = ref('')
const phone = ref('')
const address = ref('')
const image = ref(null)
const imageInput = ref(null)

const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const resetForm = () => {
  shopName.value = ''
  description.value = ''
  email.value = ''
  phone.value = ''
  address.value = ''
  image.value = null
  imageInput.value.value = ''
}

const handleImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    image.value = file
  }
}

const registerShop = async () => {
  isLoading.value = true
  const formData = new FormData()
  formData.append('shopName', shopName.value)
  formData.append('description', description.value)
  formData.append('email', email.value)
  formData.append('phone', phone.value)
  formData.append('address', address.value)
  if (image.value) {
    formData.append('image', image.value)
  }
  try {
    const res = await axios.post('/api/register-shop', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    if (res.data.message === 'Đăng ký cửa hàng thành công!') {
      successMessage.value = res.data.message
      resetForm()
      errorMessage.value = ''
      return
    } else {
      successMessage.value = ''
      errorMessage.value = res.data.message
    }
  } catch {
    errorMessage.value = 'Đăng ký thất bại!'
    successMessage.value = ''
    resetForm()
  } finally {
    isLoading.value = false
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
