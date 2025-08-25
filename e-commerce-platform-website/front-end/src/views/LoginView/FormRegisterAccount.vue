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
      <n-input
        v-model:value="form.password"
        type="password"
        placeholder="Nhập mật khẩu"
        show-password-on="click"
        @input="checkPasswordStrength"
      />
    </n-form-item>

    <!-- Hiển thị mức độ mật khẩu -->
    <div v-if="form.password" class="mb-4">
      <div class="flex items-center justify-between text-sm">
        <span>Mức độ mật khẩu:</span>
        <span :class="getStrengthColorClass()">{{ passwordStrength.displayName }}</span>
      </div>

      <!-- Thanh tiến trình mức độ -->
      <div class="mt-1 w-full bg-gray-200 rounded-full h-2">
        <div
          class="h-2 rounded-full transition-all duration-300"
          :class="getStrengthBarColorClass()"
          :style="{ width: getStrengthPercentage() + '%' }"
        ></div>
      </div>

      <!-- Thông báo chi tiết -->
      <div v-if="passwordStrength.message" class="mt-2 text-xs" :class="getStrengthTextColorClass()">
        {{ passwordStrength.message }}
      </div>
    </div>

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

const passwordStrength = ref({
  strength: 'VERY_WEAK',
  displayName: 'Rất yếu',
  score: 0,
  level: 1,
  message: '',
  isValid: false
})

const resetForm = () => {
  form.value = {
    name: '',
    username: '',
    email: '',
    password: '',
    phone: '',
  }
  passwordStrength.value = {
    strength: 'VERY_WEAK',
    displayName: 'Rất yếu',
    score: 0,
    level: 1,
    message: '',
    isValid: false
  }
}

const checkPasswordStrength = async () => {
  if (!form.value.password) {
    passwordStrength.value = {
      strength: 'VERY_WEAK',
      displayName: 'Rất yếu',
      score: 0,
      level: 1,
      message: '',
      isValid: false
    }
    return
  }

  try {
    const response = await axios.post('/api/auth/check-password-strength', {
      password: form.value.password
    })

    if (response.data.success) {
      passwordStrength.value = {
        strength: response.data.strength,
        displayName: response.data.displayName,
        score: response.data.score,
        level: response.data.level,
        message: response.data.message,
        isValid: response.data.isValid
      }
    }
  } catch (error) {
    console.error('Error checking password strength:', error)
  }
}

const getStrengthColorClass = () => {
  switch (passwordStrength.value.strength) {
    case 'VERY_WEAK':
      return 'text-red-600 font-semibold'
    case 'WEAK':
      return 'text-orange-600 font-semibold'
    case 'MEDIUM':
      return 'text-yellow-600 font-semibold'
    case 'STRONG':
      return 'text-blue-600 font-semibold'
    case 'VERY_STRONG':
      return 'text-green-600 font-semibold'
    default:
      return 'text-gray-600'
  }
}

const getStrengthBarColorClass = () => {
  switch (passwordStrength.value.strength) {
    case 'VERY_WEAK':
      return 'bg-red-500'
    case 'WEAK':
      return 'bg-orange-500'
    case 'MEDIUM':
      return 'bg-yellow-500'
    case 'STRONG':
      return 'bg-blue-500'
    case 'VERY_STRONG':
      return 'bg-green-500'
    default:
      return 'bg-gray-500'
  }
}

const getStrengthTextColorClass = () => {
  switch (passwordStrength.value.strength) {
    case 'VERY_WEAK':
    case 'WEAK':
      return 'text-red-600'
    case 'MEDIUM':
      return 'text-yellow-600'
    case 'STRONG':
    case 'VERY_STRONG':
      return 'text-green-600'
    default:
      return 'text-gray-600'
  }
}

const getStrengthPercentage = () => {
  return (passwordStrength.value.score / 6) * 100
}

const registerAccount = async () => {
  // Kiểm tra độ mạnh mật khẩu trước khi đăng ký
  if (!passwordStrength.value.isValid) {
    errorMessage.value = 'Vui lòng chọn mật khẩu mạnh hơn'
    return
  }

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
