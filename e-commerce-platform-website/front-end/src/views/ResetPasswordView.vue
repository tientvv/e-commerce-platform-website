<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Đặt lại mật khẩu
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Nhập mã xác nhận và mật khẩu mới
        </p>
      </div>

      <n-form ref="formRef" :model="form" @submit.prevent="handleSubmit">
        <n-alert v-if="message" :type="messageType" class="mb-4" closable @close="message = ''">
          {{ message }}
        </n-alert>

        <n-form-item label="Mã xác nhận" path="token">
          <n-input
            v-model:value="form.token"
            placeholder="Nhập mã 6 số"
            maxlength="6"
            show-count
          />
        </n-form-item>

        <n-form-item label="Mật khẩu mới" path="newPassword">
          <n-input
            v-model:value="form.newPassword"
            type="password"
            placeholder="Nhập mật khẩu mới"
            show-password-on="click"
            :input-props="{ autocomplete: 'new-password' }"
            @input="checkPasswordStrength"
          />
        </n-form-item>

        <!-- Hiển thị mức độ mật khẩu -->
        <div v-if="form.newPassword" class="mb-4">
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

        <n-form-item label="Xác nhận mật khẩu" path="confirmPassword">
          <n-input
            v-model:value="form.confirmPassword"
            type="password"
            placeholder="Nhập lại mật khẩu mới"
            show-password-on="click"
            :input-props="{ autocomplete: 'new-password' }"
          />
        </n-form-item>

        <!-- Hiển thị thông báo khớp mật khẩu -->
        <div v-if="form.confirmPassword && form.newPassword" class="mb-4">
          <div v-if="form.newPassword === form.confirmPassword" class="text-xs text-green-600">
            ✓ Mật khẩu khớp
          </div>
          <div v-else class="text-xs text-red-600">
            ✗ Mật khẩu không khớp
          </div>
        </div>

        <n-form-item>
          <n-button
            type="primary"
            block
            attr-type="submit"
            :loading="loading"
            :disabled="!isFormValid()"
          >
            Đặt lại mật khẩu
          </n-button>
        </n-form-item>

        <div class="flex justify-between">
          <n-button text type="info" @click="$router.push('/forgot-password')">
            Gửi lại mã xác nhận
          </n-button>
          <n-button text type="info" @click="$router.push('/login')">
            Quay lại đăng nhập
          </n-button>
        </div>
      </n-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NForm, NFormItem, NInput, NButton, NAlert } from 'naive-ui'
import axios from '~/utils/axios'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const loading = ref(false)
const message = ref('')
const messageType = ref('info')

const form = ref({
  token: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordStrength = ref({
  strength: 'VERY_WEAK',
  displayName: 'Rất yếu',
  score: 0,
  level: 1,
  message: '',
  isValid: false
})

onMounted(() => {
  // Lấy token từ URL nếu có
  const tokenFromUrl = route.query.token
  if (tokenFromUrl) {
    form.value.token = tokenFromUrl
  }
})

const checkPasswordStrength = async () => {
  if (!form.value.newPassword) {
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
      password: form.value.newPassword
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

const isFormValid = () => {
  return form.value.token &&
         form.value.newPassword &&
         form.value.confirmPassword &&
         form.value.newPassword === form.value.confirmPassword &&
         passwordStrength.value.isValid
}

const handleSubmit = async () => {
  if (!isFormValid()) {
    message.value = 'Vui lòng kiểm tra lại thông tin và đảm bảo mật khẩu đủ mạnh'
    messageType.value = 'error'
    return
  }

  loading.value = true
  message.value = ''

  try {
    const response = await axios.post('/api/auth/reset-password', form.value)

    if (response.data.success) {
      message.value = response.data.message
      messageType.value = 'success'

      // Chuyển hướng đến trang đăng nhập sau 2 giây
      setTimeout(() => {
        router.push('/login')
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
