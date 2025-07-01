import { createRouter, createWebHistory } from 'vue-router'
// Import views
import HomeView from '../views/HomeView.vue'
// Import API function to get account info
import { getInfoAccount } from '@/api/AuthController'
// Import toastify for notifications
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ################## SELLER ROUTES ##################
    {
      path: '/cart',
      component: () => import('../views/CartView.vue'),
      meta: { requiresAuth: true, requiresRole: ['USER', 'ADMIN'] },
    },
    // ################## SELLER ROUTES ##################
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
      return next()
    }
    return next()
  }

  if (!to.meta.requiresAuth) return next()

  try {
    const infoAccount = await getInfoAccount()

    if (to.meta.requiresRole && !to.meta.requiresRole.includes(infoAccount.role)) {
      toast.error('Vui lòng đăng nhập để tiếp tục', {
        position: 'top-right',
        autoClose: 2000,
        theme: 'colored',
      })
      return next('/login')
    }

    if (to.meta.requiresSeller && !infoAccount.isSeller) {
      toast.error('Bạn cần đăng ký tài khoản bán hàng để truy cập trang này', {
        position: 'top-right',
        autoClose: 2000,
        theme: 'colored',
      })
      return next('/login')
    }

    next()
  } catch {
    toast.error('Vui lòng đăng nhập để tiếp tục', {
      position: 'top-right',
      autoClose: 2000,
      theme: 'colored',
    })
    next('/login')
  }
})

export default router
