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

    <div id="google-login-button" class="w-full"></div>

    <n-alert v-if="googleError" type="error" class="mt-4" closable @close="googleError = ''">
      {{ googleError }}
    </n-alert>
  </n-form>
</template>

<script setup>
import axios from 'axios'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NForm, NFormItem, NInput, NButton, NAlert, NH2, NText, NDivider } from 'naive-ui'
import { useGoogleAuth } from '../../composables/useGoogleAuth'

const router = useRouter()

const errorMessage = ref('')
const loading = ref(false)
const formRef = ref(null)
const googleError = ref('')

const { handleGoogleLogin, renderGoogleButton } = useGoogleAuth()

const form = ref({
  username: '',
  password: '',
})

const login = async () => {
  loading.value = true
  try {
    const res = await axios.post('/api/login', form.value)
    if (res.data.message) {
      errorMessage.value = res.data.message
      return
    }

    // Sau khi đăng nhập thành công, kiểm tra thông tin profile
    try {
      const userRes = await axios.get('/api/info-account')
      const user = userRes.data.account

      // Nếu là tài khoản Google (có google_id), chỉ cần kiểm tra phone và address
      let isProfileComplete
      if (user.googleId) {
        isProfileComplete = (
          user.phone &&
          user.address &&
          user.phone.trim() !== '' &&
          user.address.trim() !== ''
        )
      } else {
        // Kiểm tra tất cả các trường bắt buộc cho tài khoản thường
        isProfileComplete = (
          user.username &&
          user.name &&
          user.email &&
          user.phone &&
          user.address &&
          user.username.trim() !== '' &&
          user.name.trim() !== '' &&
          user.email.trim() !== '' &&
          user.phone.trim() !== '' &&
          user.address.trim() !== ''
        )
      }

      if (!isProfileComplete && user.role === 'USER') {
        // Nếu thông tin chưa đầy đủ và là USER, chuyển hướng đến trang profile
        router.push('/user/profile')
      } else {
        // Nếu thông tin đầy đủ hoặc là ADMIN, chuyển về trang chủ
        router.push('/')
      }
    } catch {
      // Nếu không thể kiểm tra profile, chuyển về trang chủ
      router.push('/')
    }
  } catch {
    errorMessage.value = 'Đã xảy ra lỗi! Vui lòng thử lại sau!'
  } finally {
    loading.value = false
  }
}

const handleGoogleLoginSuccess = async (response) => {
  const result = await handleGoogleLogin(response)

  if (result.success) {
    // Sau khi đăng nhập Google thành công, kiểm tra thông tin profile
    try {
      const userRes = await axios.get('/api/info-account')
      const user = userRes.data.account

      // Nếu là tài khoản Google (có google_id), chỉ cần kiểm tra phone và address
      let isProfileComplete
      if (user.googleId) {
        isProfileComplete = (
          user.phone &&
          user.address &&
          user.phone.trim() !== '' &&
          user.address.trim() !== ''
        )
      } else {
        // Kiểm tra tất cả các trường bắt buộc cho tài khoản thường
        isProfileComplete = (
          user.username &&
          user.name &&
          user.email &&
          user.phone &&
          user.address &&
          user.username.trim() !== '' &&
          user.name.trim() !== '' &&
          user.email.trim() !== '' &&
          user.phone.trim() !== '' &&
          user.address.trim() !== ''
        )
      }

      if (!isProfileComplete && user.role === 'USER') {
        // Nếu thông tin chưa đầy đủ và là USER, chuyển hướng đến trang profile
        router.push('/user/profile')
      } else {
        // Nếu thông tin đầy đủ hoặc là ADMIN, chuyển về trang chủ
        router.push('/')
      }
    } catch {
      // Nếu không thể kiểm tra profile, chuyển về trang chủ
      router.push('/')
    }
  } else {
    googleError.value = result.error
  }
}

onMounted(async () => {
  try {
    await renderGoogleButton('google-login-button', handleGoogleLoginSuccess)
  } catch (error) {
    console.error('Không thể khởi tạo Google OAuth:', error)
    googleError.value = 'Không thể khởi tạo đăng nhập Google'
  }
})
</script>
