import axios from 'axios'

// Cấu hình axios để gửi cookies/session
axios.defaults.withCredentials = true

// Cấu hình base URL
axios.defaults.baseURL = 'http://localhost:8080'

// Cấu hình headers mặc định
axios.defaults.headers.common['Content-Type'] = 'application/json; charset=UTF-8'
axios.defaults.headers.common['Accept'] = 'application/json; charset=UTF-8'
axios.defaults.headers.common['Accept-Charset'] = 'UTF-8'

// Interceptor để xử lý request
axios.interceptors.request.use(
  (config) => {
    // Đảm bảo encoding UTF-8 cho request, nhưng không xử lý FormData
    if (config.data && typeof config.data === 'object' && !(config.data instanceof FormData)) {
      config.data = JSON.stringify(config.data)
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor để xử lý response
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
