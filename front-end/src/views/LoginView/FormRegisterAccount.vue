<template>
  <n-form ref="formRef" :model="form" @submit.prevent="registerAccount">
    <n-h2 class="mb-4">Tạo tài khoản</n-h2>
    <n-text class="mb-4 block">
      Bạn có tài khoản rồi?
      <n-button text type="info" @click="$router.push('/login')">Đăng nhập</n-button>
    </n-text>

    <n-alert v-if="successMessage" type="success" class="mb-4" closable @close="successMessage = ''">
      {{ successMessage }}
    </n-alert>
    <n-alert v-if="errorMessage" type="error" class="mb-4" closable @close="errorMessage = ''">
      {{ errorMessage }}
    </n-alert>

    <n-grid :cols="2" :x-gap="12">
      <n-grid-item>
        <n-form-item label="Họ và tên" path="name">
          <n-input v-model:value="form.name" placeholder="Nhập họ và tên" :input-props="{ autocomplete: 'name' }" />
        </n-form-item>
      </n-grid-item>
      <n-grid-item>
        <n-form-item label="Username" path="username">
          <n-input
            v-model:value="form.username"
            placeholder="Nhập username"
            :input-props="{ autocomplete: 'username' }"
          />
        </n-form-item>
      </n-grid-item>
    </n-grid>

    <n-form-item label="Email" path="email">
      <n-input v-model:value="form.email" placeholder="Nhập email" :input-props="{ autocomplete: 'email' }" />
    </n-form-item>

    <n-form-item label="Mật khẩu" path="password">
      <n-input v-model:value="form.password" type="password" placeholder="Nhập mật khẩu" show-password-on="click" />
    </n-form-item>

    <n-form-item label="Số điện thoại" path="phone">
      <n-input v-model:value="form.phone" placeholder="Nhập số điện thoại" :input-props="{ autocomplete: 'tel' }" />
    </n-form-item>

    <n-form-item>
      <n-button type="primary" block attr-type="submit" :loading="loading"> Đăng ký </n-button>
    </n-form-item>

    <n-divider>hoặc</n-divider>

    <n-button block disabled>
      <template #icon>
        <img src="/icon-google.svg" width="20" />
      </template>
      Đăng ký Google (Chưa phát triển)
    </n-button>
  </n-form>
</template>

<script setup>
import axios from 'axios'
import { ref } from 'vue'
import { NForm, NFormItem, NInput, NButton, NAlert, NH2, NText, NDivider, NGrid, NGridItem } from 'naive-ui'

const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)
const formRef = ref(null)

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
  loading.value = true
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
  } finally {
    loading.value = false
  }
}
</script>
