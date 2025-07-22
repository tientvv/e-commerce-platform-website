import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '~/views/HomeView.vue'
import LoginAccountView from '~/views/LoginAccountView.vue'
import WishlistView from '~/views/WishlistView.vue'
import RegisterAccountView from '~/views/RegisterAccountView.vue'
import axios from 'axios'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/login', component: LoginAccountView },
    {
      path: '/wishlist',
      component: WishlistView,
      meta: {
        requiresAuth: true,
        roles: ['USER', 'ADMIN'],
      },
    },
    { path: '/register', component: RegisterAccountView },
    { path: '/user', component: RegisterAccountView },
  ],
})

router.beforeEach(async (to) => {
  if (to.path === '/login') {
    try {
      const res = await axios.get('/api/info-account')
      const user = res.data.account
      if (user) {
        return user.role === 'ADMIN' ? '/admin' : '/'
      }
    } catch {
      return true
    }
    return true
  }
  if (!to.meta.requiresAuth) return true
  try {
    const res = await axios.get('/api/info-account')
    const user = res.data.account
    if (!user) return '/login'
    if (to.meta.roles && !to.meta.roles.includes(user.role)) {
      return '/'
    }
    return true
  } catch {
    return '/login'
  }
})

export default router
