import axios from 'axios'

// Cấu hình axios để gửi cookies/session
axios.defaults.withCredentials = true

// Cấu hình base URL
axios.defaults.baseURL = 'http://localhost:8080'

// Cấu hình headers mặc định
axios.defaults.headers.common['Content-Type'] = 'application/json'
axios.defaults.headers.common['Accept'] = 'application/json'

// Interceptor để xử lý lỗi
axios.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    // Nếu lỗi 401 (Unauthorized), redirect về login
    if (error.response && error.response.status === 401) {
      window.location.href = '/login'
    }
    return Promise.reject(error)
  },
)

export default axios
