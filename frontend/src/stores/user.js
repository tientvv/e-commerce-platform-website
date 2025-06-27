import axios from 'axios'
import { useUserStore } from './useUserStore'

export const fetchUserFromSession = async () => {
  const userStore = useUserStore()

  try {
    const res = await axios.get('/api/info-user')
    userStore.setUser(res.data.user)
  } catch (error) {
    console.error('Error fetching user from session:', error)
  }
}
