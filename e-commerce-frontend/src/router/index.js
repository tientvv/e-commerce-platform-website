import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { getInfoAccount } from '@/api/AuthController'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/cart',
      component: () => import('../views/CartView.vue'),
      meta: { requiresAuth: true, requiresRole: ['USER'] },
    },
    {
      path: '/login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/',
      component: HomeView,
    },
    {
      path: '/about',
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

router.beforeEach(async (to, from, next) => {
  if (to.path === '/login') {
    try {
      const infoAccount = await getInfoAccount()
      if (infoAccount) return next('/')
    } catch {
      return next('/login')
    }
    return next()
  }

  if (!to.meta.requiresAuth) return next()
  try {
    const infoAccount = await getInfoAccount()
    if (to.meta.requiresRole && !to.meta.requiresRole.includes(infoAccount.role)) {
      return next('/login')
    }
    if (to.meta.requiresSeller && !infoAccount.isSeller) {
      return next('/login')
    }
    next()
  } catch {
    next('/login')
  }
})

export default router
