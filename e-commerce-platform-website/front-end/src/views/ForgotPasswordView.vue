<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Quên mật khẩu
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Nhập email của bạn để nhận mã xác nhận đặt lại mật khẩu
        </p>
      </div>

      <n-form ref="formRef" :model="form" @submit.prevent="handleSubmit">
        <n-alert v-if="message" :type="messageType" class="mb-4" closable @close="message = ''">
          {{ message }}
        </n-alert>

        <n-form-item label="Email" path="email">
          <n-input
            v-model:value="form.email"
            type="email"
            placeholder="Nhập email của bạn"
            :input-props="{ autocomplete: 'email' }"
          />
        </n-form-item>

        <n-form-item>
          <n-button type="primary" block attr-type="submit" :loading="loading">
            Gửi mã xác nhận
          </n-button>
        </n-form-item>

        <div class="text-center">
          <n-button text type="info" @click="$router.push('/login')">
            Quay lại đăng nhập
          </n-button>
        </div>
      </n-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NForm, NFormItem, NInput, NButton, NAlert } from 'naive-ui'
import axios from '~/utils/axios'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)
const message = ref('')
const messageType = ref('info')

const form = ref({
  email: ''
})

const handleSubmit = async () => {
  loading.value = true
  message.value = ''

  try {
    const response = await axios.post('/api/auth/forgot-password', form.value)

    if (response.data.success) {
      message.value = response.data.message
      messageType.value = 'success'

      // Chuyển hướng đến trang nhập mã sau 2 giây
      setTimeout(() => {
        router.push({
          path: '/reset-password',
          query: { email: form.value.email }
        })
      }, 2000)
    } else {
      message.value = response.data.message
      messageType.value = 'error'
    }
  } catch (error) {
    console.error('Error:', error)
    message.value = error.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại sau.'
    messageType.value = 'error'
  } finally {
    loading.value = false
  }
}
</script>
