import { ref } from 'vue'
import axios from '../utils/axios'

export function useGoogleAuth() {
  const loading = ref(false)
  const error = ref('')

  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || 'YOUR_GOOGLE_CLIENT_ID_HERE'

  const initializeGoogleAuth = () => {
    return new Promise((resolve, reject) => {
      if (window.google) {
        resolve(window.google)
        return
      }

      const script = document.createElement('script')
      script.src = 'https://accounts.google.com/gsi/client'
      script.async = true
      script.defer = true
      script.onload = () => {
        if (window.google) {
          resolve(window.google)
        } else {
          reject(new Error('Google API không tải được'))
        }
      }
      script.onerror = () => {
        reject(new Error('Không thể tải Google API'))
      }
      document.head.appendChild(script)
    })
  }

  const handleGoogleLogin = async (response) => {
    try {
      loading.value = true
      error.value = ''

      console.log('Google OAuth response:', response)

      // Decode JWT token to get user info
      const payload = JSON.parse(atob(response.credential.split('.')[1]))
      console.log('Decoded JWT payload:', payload)

      const googleData = {
        googleId: payload.sub, // Use 'sub' (subject) instead of full JWT token
        email: payload.email,
        name: payload.name,
        picture: payload.picture
      }

      console.log('Sending Google data to backend:', googleData)

      const res = await axios.post('/api/google-login', googleData)

      if (res.data.message && res.data.message.includes('thất bại')) {
        error.value = res.data.message
        return { success: false, error: res.data.message }
      }

      return { success: true, data: res.data }
    } catch (err) {
      error.value = 'Đăng nhập Google thất bại: ' + (err.response?.data?.message || err.message)
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

      const renderGoogleButton = async (elementId, callback) => {
    const google = await initializeGoogleAuth()

    google.accounts.id.initialize({
      client_id: googleClientId,
      callback: callback
    })

    google.accounts.id.renderButton(
      document.getElementById(elementId),
      {
        theme: 'outline',
        size: 'large',
        text: 'continue_with',
        shape: 'rectangular',
        width: '100%'
      }
    )
  }

  return {
    loading,
    error,
    handleGoogleLogin,
    renderGoogleButton,
    initializeGoogleAuth
  }
}
