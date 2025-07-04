import axios from 'axios'

export async function login(email, password) {
  try {
    await axios.post('/api/auth/login', { email, password })
  } catch (error) {
    console.log('Login failed:', error)
    throw error
  }
}

export async function getInfoAccount() {
  try {
    const response = await axios.get('/api/auth/info-account')
    return response.data
  } catch (error) {
    console.log('Failed to get account info')
    throw error
  }
}

export const logout = async () => {
  try {
    await axios.post('/api/auth/logout')
  } catch (error) {
    console.log('Logout failed:', error)
    throw error
  }
}
