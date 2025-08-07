<template>
  <n-form ref="formRef" :model="form" @submit.prevent="login">
    <n-h2 class="mb-4">Đăng nhập</n-h2>
    <n-text class="mb-4 block">
      Bạn chưa có tài khoản đăng nhập?
      <n-button text type="info" @click="$router.push('/register')">Đăng ký</n-button>
    </n-text>

    <n-alert v-if="errorMessage" type="error" class="mb-4" closable @close="errorMessage = ''">
      {{ errorMessage }}
    </n-alert>

    <n-form-item label="Username" path="username">
      <n-input v-model:value="form.username" placeholder="Nhập username" :input-props="{ autocomplete: 'username' }" />
    </n-form-item>

    <n-form-item label="Password" path="password">
      <n-input
        v-model:value="form.password"
        type="password"
        placeholder="Nhập password"
        show-password-on="click"
        :input-props="{ autocomplete: 'current-password' }"
      />
    </n-form-item>

    <n-form-item>
      <n-button type="primary" block attr-type="submit" :loading="loading"> Đăng nhập </n-button>
    </n-form-item>

    <n-divider>hoặc</n-divider>

    <n-button block disabled>
      <template #icon>
        <img src="/icon-google.svg" width="20" />
      </template>
      Đăng nhập Google (Chưa phát triển)
    </n-button>
  </n-form>
</template>

<script setup>
import axios from 'axios'
import { ref } from 'vue'
import { NForm, NFormItem, NInput, NButton, NAlert, NH2, NText, NDivider } from 'naive-ui'

const errorMessage = ref('')
const loading = ref(false)
const formRef = ref(null)

const form = ref({
  username: 'tientvv',
  password: '123456',
})

const login = async () => {
  loading.value = true
  try {
    const res = await axios.post('/api/login', form.value)
    if (res.data.message) {
      errorMessage.value = res.data.message
      return
    }
    window.location.reload()
  } catch {
    errorMessage.value = 'Đã xảy ra lỗi! Vui lòng thử lại sau!'
  } finally {
    loading.value = false
  }
}
</script>
