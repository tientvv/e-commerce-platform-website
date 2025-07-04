import { createRouter, createWebHistory } from 'vue-router'
// Import views
import HomeView from '../views/HomeView.vue'
// Import API function to get account info
import { getInfoAccount } from '@/api/AuthController'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ###################### ADMIN ROUTES ######################
    {
      path: '/admins',
      children: [
        { path: '', redirect: '/' },
        {
          path: 'dashboard',
          component: () => import('../views/admins/AdminDashboard.vue'),
          meta: { requiresAuth: true, requiresRole: ['ADMIN'] },
        },
        {
          path: 'categories',
          component: () => import('../views/admins/AdminCategory.vue'),
          meta: { requiresAuth: true, requiresRole: ['ADMIN'] },
        },
      ],
    },
    // ###################### ADMIN ROUTES ######################

    // ###################### SELLER ROUTES ######################
    {
      path: '/shops',
      children: [
        { path: '', redirect: '/' },
        {
          path: 'register',
          component: () => import('../views/RegisterSeller.vue'),
          meta: { requiresAuth: true, requiresRole: ['USER', 'ADMIN'] },
        },
      ],
    },
    // ###################### SELLER ROUTES ######################
    {
      path: '/cart',
      component: () => import('../views/CartView.vue'),
      meta: { requiresAuth: true, requiresRole: ['USER', 'ADMIN'] },
    },
    { path: '/login', component: () => import('../views/LoginView.vue') },
    { path: '/', component: HomeView },
    { path: '/about', component: () => import('../views/AboutView.vue') },
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
