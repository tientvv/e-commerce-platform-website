<template>
  <div class="max-w-4xl mx-auto p-6">
    <div class="bg-white rounded-lg shadow-lg overflow-hidden">
      <!-- Header -->
      <div class="bg-gradient-to-r from-blue-600 to-purple-600 px-6 py-4">
        <h1 class="text-2xl font-bold text-white">Thông tin tài khoản</h1>
        <p class="text-blue-100 mt-1">Quản lý thông tin cá nhân của bạn</p>
      </div>

      <div class="p-6">
        <!-- Loading State -->
        <div v-if="loading" class="flex justify-center items-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">Có lỗi xảy ra</h3>
              <div class="mt-2 text-sm text-red-700">{{ error }}</div>
            </div>
          </div>
        </div>



                        <!-- Profile Update Required Message -->
        <div v-if="(needsProfileUpdate && !loading && userInfo?.role === 'USER') || route.query.message === 'update_required'" class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-blue-800">Cập nhật thông tin bắt buộc</h3>
              <div class="mt-2 text-sm text-blue-700">
                <p class="font-medium">Vui lòng cập nhật đầy đủ thông tin trước khi sử dụng các tính năng.</p>
                <p class="mt-1">Các thông tin bắt buộc: Tên đăng nhập, Họ tên, Email, Số điện thoại, Địa chỉ</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Admin Info Message -->
        <div v-if="userInfo?.role === 'ADMIN' && !loading" class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
  <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-blue-800">👨‍💼 Quản trị viên</h3>
              <div class="mt-2 text-sm text-blue-700">
                <p>Bạn đang đăng nhập với quyền Quản trị viên. Cập nhật thông tin là tùy chọn.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Content -->
        <div v-if="!loading && !error" class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <!-- Thông tin tài khoản -->
          <div class="space-y-6">
            <div class="pb-4">
              <h2 class="text-xl font-semibold text-gray-900 mb-4">Thông tin cá nhân</h2>

              <!-- Avatar Section -->
              <div class="flex items-center space-x-4 mb-6">
                <div class="relative">
                  <div class="w-20 h-20 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white text-2xl font-bold">
                    {{ userInfo?.name?.charAt(0)?.toUpperCase() || 'U' }}
                  </div>
                  <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-green-400 border-2 border-white rounded-full"></div>
                </div>
                <div>
                  <h3 class="text-lg font-medium text-gray-900">{{ userInfo?.name || 'Chưa có tên' }}</h3>
                  <p class="text-sm text-gray-500">{{ userInfo?.role === 'ADMIN' ? 'Quản trị viên' : 'Người dùng' }}</p>
                </div>
              </div>

              <!-- Info Cards -->
              <div class="space-y-4">
                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Tên đăng nhập</p>
                      <p class="text-sm text-gray-900">{{ userInfo?.username }}</p>
                    </div>
                  </div>
                </div>

                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Email</p>
                      <p class="text-sm text-gray-900">{{ userInfo?.email }}</p>
                    </div>
                  </div>
                </div>

                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Số điện thoại</p>
                      <p class="text-sm text-gray-900">{{ userInfo?.phone || 'Chưa cập nhật' }}</p>
                    </div>
                  </div>
                </div>

                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Địa chỉ</p>
                      <p class="text-sm text-gray-900">{{ userInfo?.address || 'Chưa cập nhật' }}</p>
                    </div>
                  </div>
                </div>

                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Ngày tạo tài khoản</p>
                      <p class="text-sm text-gray-900">{{ formatDate(userInfo?.createdAt) }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Form chỉnh sửa -->
          <div class="space-y-6">
            <div class="pb-4">
              <h2 class="text-xl font-semibold text-gray-900 mb-4">Chỉnh sửa thông tin</h2>

                            <n-form ref="formRef" :model="formData" :rules="rules" @submit.prevent="updateProfile">
                <n-form-item label="Tên đăng nhập" path="username">
                  <n-input
                    v-model:value="formData.username"
                    placeholder="Tên đăng nhập"
                    disabled
                    :input-props="{ autocomplete: 'username' }"
                  />
                </n-form-item>

                <n-form-item label="Họ và tên" path="name">
                  <n-input
                    v-model:value="formData.name"
                    placeholder="Nhập họ và tên"
                    :input-props="{ autocomplete: 'name' }"
                  />
                </n-form-item>

                <n-form-item label="Email" path="email">
                  <n-input
                    v-model:value="formData.email"
                    placeholder="Nhập email"
                    :input-props="{ autocomplete: 'email' }"
                  />
                </n-form-item>

                <n-form-item label="Số điện thoại" path="phone">
                  <n-input
                    v-model:value="formData.phone"
                    placeholder="Nhập số điện thoại"
                    :input-props="{ autocomplete: 'tel' }"
                  />
                </n-form-item>

                <n-form-item label="Địa chỉ" path="address">
                  <n-input
                    v-model:value="formData.address"
                    type="textarea"
                    placeholder="Nhập địa chỉ"
                    :autosize="{ minRows: 3, maxRows: 5 }"
                  />
                </n-form-item>

                <div class="flex space-x-3 mt-6">
                  <button
                    type="submit"
                    :disabled="updating"
                    class="flex-1 bg-blue-600 text-white px-3 py-2 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200 font-medium"
                  >
                    <div v-if="updating" class="flex items-center justify-center">
                      <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Đang cập nhật...
                    </div>
                    <span v-else>Cập nhật thông tin</span>
                  </button>

                  <button
                    type="button"
                    @click="resetForm"
                    class="w-24 bg-white text-gray-700 px-3 py-2 rounded-lg border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors duration-200 font-medium flex items-center justify-center whitespace-nowrap"
                  >
                    <svg class="w-4 h-4 mr-1 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                    <span class="text-xs">Làm mới</span>
                  </button>
                </div>
              </n-form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import axios from '../../utils/axios'
import { NForm, NFormItem, NInput, useMessage } from 'naive-ui'

// Reactive data
const loading = ref(true)
const error = ref('')
const updating = ref(false)
const userInfo = ref(null)
const message = useMessage()
const needsProfileUpdate = ref(false)
const route = useRoute()

const formData = reactive({
  username: '',
  name: '',
  email: '',
  phone: '',
  address: ''
})

// Form ref
const formRef = ref(null)

const rules = {
  username: {
    required: true,
    message: 'Tên đăng nhập không được để trống',
    trigger: ['blur', 'input']
  },
  name: {
    required: true,
    message: 'Họ và tên không được để trống',
    trigger: ['blur', 'input'],
    validator: (rule, value) => {
      if (!value || value.trim() === '') {
        return new Error('Họ và tên không được để trống')
      }
      if (value.trim().length < 2) {
        return new Error('Họ và tên phải có ít nhất 2 ký tự')
      }
    }
  },
  email: {
    required: true,
    message: 'Email không được để trống',
    trigger: ['blur', 'input'],
    validator: (rule, value) => {
      if (!value || value.trim() === '') {
        return new Error('Email không được để trống')
      }
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        return new Error('Email không hợp lệ')
      }
    }
  },
  phone: {
    required: true,
    message: 'Số điện thoại không được để trống',
    trigger: ['blur', 'input'],
    validator: (rule, value) => {
      if (!value || value.trim() === '') {
        return new Error('Số điện thoại không được để trống')
      }
      const cleanPhone = value.replace(/\s/g, '')
      if (!/^[0-9]{10,11}$/.test(cleanPhone)) {
        return new Error('Số điện thoại phải có 10-11 số')
      }
    }
  },
  address: {
    required: true,
    message: 'Địa chỉ không được để trống',
    trigger: 'blur',
    validator: (rule, value) => {
      // Kiểm tra nếu value là null, undefined, hoặc empty string
      if (!value || value === '' || value.trim() === '') {
        return new Error('Địa chỉ không được để trống')
      }
      // Kiểm tra độ dài sau khi trim - giảm xuống 3 ký tự
      const trimmedValue = value.trim()
      if (trimmedValue.length < 3) {
        return new Error('Địa chỉ phải có ít nhất 3 ký tự')
      }
      // Nếu tất cả đều OK, không return gì (undefined = valid)
    }
  }
}

// Methods
const fetchUserInfo = async () => {
  try {
    loading.value = true
    error.value = ''
    const response = await axios.get('/api/info-account')
    userInfo.value = response.data.account

    // Populate form data
    formData.username = userInfo.value.username || ''
    formData.name = userInfo.value.name || ''
    formData.email = userInfo.value.email || ''
    formData.phone = userInfo.value.phone || ''
    formData.address = userInfo.value.address || ''


  } catch (err) {
    error.value = 'Không thể tải thông tin tài khoản. Vui lòng thử lại.'
    console.error('Error fetching user info:', err)
  } finally {
    loading.value = false
  }
}

const validateForm = async () => {
  try {
    await formRef.value?.validate()
    return true
  } catch {
    return false
  }
}

const updateProfile = async () => {
  const isValid = await validateForm()
  if (!isValid) {
    return
  }

  try {
    updating.value = true
    error.value = ''

    const response = await axios.put('/api/account/update', {
      username: formData.username.trim(),
      name: formData.name.trim(),
      email: formData.email.trim(),
      phone: formData.phone.trim(),
      address: formData.address.trim()
    })

    if (response.data.success) {
      // Hiển thị thông báo thành công ở góc dưới bên phải
      message.success('Cập nhật thông tin thành công!')

      // Refresh user info
      await fetchUserInfo()
    } else {
      error.value = response.data.message || 'Có lỗi xảy ra khi cập nhật thông tin'
    }
  } catch (err) {
    if (err.response?.data?.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'Có lỗi xảy ra khi cập nhật thông tin. Vui lòng thử lại.'
    }
    console.error('Error updating profile:', err)
  } finally {
    updating.value = false
  }
}

const resetForm = () => {
  // Reset form to original values
  formData.username = userInfo.value?.username || ''
  formData.name = userInfo.value?.name || ''
  formData.email = userInfo.value?.email || ''
  formData.phone = userInfo.value?.phone || ''
  formData.address = userInfo.value?.address || ''

  // Clear form validation
  formRef.value?.restoreValidation()

  // Clear messages
  error.value = ''
}

const formatDate = (dateString) => {
  if (!dateString) return 'Chưa có thông tin'

  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return 'Chưa có thông tin'
  }
}

// Lifecycle
onMounted(() => {
  fetchUserInfo()
})




</script>

<style scoped>
/* Custom styles if needed */
</style>
