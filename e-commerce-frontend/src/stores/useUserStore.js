import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref(null)

  async function fetchUser() {
    try {
      const { data } = await axios.get('/api/auth/info-account')
      currentUser.value = data
    } catch (error) {
      currentUser.value = null
      console.error('Failed to fetch user:', error)
    }
  }

  function setUser(newUser) {
    currentUser.value = newUser
  }

  function clearUser() {
    currentUser.value = null
  }

  return { currentUser, fetchUser, setUser, clearUser }
})
