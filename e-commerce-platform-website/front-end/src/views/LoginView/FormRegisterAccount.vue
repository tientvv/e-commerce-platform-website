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

    <div id="google-register-button" class="w-full"></div>

    <n-alert v-if="googleError" type="error" class="mt-4" closable @close="googleError = ''">
      {{ googleError }}
    </n-alert>
  </n-form>
</template>

<script setup>
import axios from 'axios'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NForm, NFormItem, NInput, NButton, NAlert, NH2, NText, NDivider, NGrid, NGridItem } from 'naive-ui'
import { useGoogleAuth } from '../../composables/useGoogleAuth'

const router = useRouter()

const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)
const formRef = ref(null)
const googleError = ref('')

const { handleGoogleLogin, renderGoogleButton } = useGoogleAuth()

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

const handleGoogleRegisterSuccess = async (response) => {
  const result = await handleGoogleLogin(response)

  if (result.success) {
    successMessage.value = 'Đăng ký Google thành công!'
    errorMessage.value = ''
    resetForm()

    // Sau khi đăng ký Google thành công, kiểm tra thông tin profile
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
    await renderGoogleButton('google-register-button', handleGoogleRegisterSuccess)
  } catch (error) {
    console.error('Không thể khởi tạo Google OAuth:', error)
    googleError.value = 'Không thể khởi tạo đăng ký Google'
  }
})
</script>
