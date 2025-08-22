import { ref, computed } from 'vue'
import axios from '../utils/axios'

export function useUserInfo() {
  const userInfo = ref(null)
  const loading = ref(false)
  const error = ref('')

    // Kiểm tra xem thông tin người dùng có đầy đủ không
  const isProfileComplete = computed(() => {
    if (!userInfo.value) return false

    // Nếu là tài khoản Google (có google_id), chỉ cần kiểm tra phone và address
    if (userInfo.value.googleId) {
      return (
        userInfo.value.phone &&
        userInfo.value.address &&
        userInfo.value.phone.trim() !== '' &&
        userInfo.value.address.trim() !== ''
      )
    }

    // Kiểm tra tất cả các trường bắt buộc cho tài khoản thường
    return (
      userInfo.value.username &&
      userInfo.value.name &&
      userInfo.value.email &&
      userInfo.value.phone &&
      userInfo.value.address &&
      userInfo.value.username.trim() !== '' &&
      userInfo.value.name.trim() !== '' &&
      userInfo.value.email.trim() !== '' &&
      userInfo.value.phone.trim() !== '' &&
      userInfo.value.address.trim() !== ''
    )
  })

  // Kiểm tra xem có cần cập nhật thông tin không
  const needsProfileUpdate = computed(() => {
    return !isProfileComplete.value
  })

  // Lấy thông tin người dùng
  const fetchUserInfo = async () => {
    try {
      loading.value = true
      error.value = ''
      const response = await axios.get('/api/info-account')
      userInfo.value = response.data.account
      return response.data.account
    } catch (err) {
      error.value = 'Không thể tải thông tin tài khoản'
      console.error('Error fetching user info:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  // Kiểm tra và redirect nếu cần
  const checkAndRedirectIfNeeded = async (router) => {
    const user = await fetchUserInfo()
    if (user && user.role === 'USER' && needsProfileUpdate.value) {
      // Chỉ kiểm tra cho USER, không kiểm tra cho ADMIN
      if (router.currentRoute.value.path !== '/user/profile') {
        router.push('/user/profile')
        return true // Đã redirect
      }
    }
    return false // Không cần redirect
  }

  return {
    userInfo,
    loading,
    error,
    isProfileComplete,
    needsProfileUpdate,
    fetchUserInfo,
    checkAndRedirectIfNeeded
  }
}
