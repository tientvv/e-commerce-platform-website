import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    // store user information after login
    user: null,
  }),
  actions: {
    setUser(data) {
      this.user = data
    },
    clearUser() {
      this.user = null
    },
  },
})
