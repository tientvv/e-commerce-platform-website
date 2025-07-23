import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '~/views/HomeView.vue'
import LoginAccountView from '~/views/LoginAccountView.vue'
import WishlistView from '~/views/WishlistView.vue'
import RegisterAccountView from '~/views/RegisterAccountView.vue'
import axios from 'axios'
import UserView from '~/views/UserView/UserView.vue'

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
    {
      path: '/user',
      component: UserView,
      meta: { requiresAuth: true, roles: ['USER', 'ADMIN'] },
      children: [
        { path: 'profile', component: () => import('~/views/UserView/ProfileView.vue') },
        {
          path: 'shop',
          component: () => import('~/views/UserView/ShopView.vue'),
          children: [
            { path: 'profile', component: () => import('~/views/ShopView/ProfileShopView.vue') },
            {
              path: 'product',
              component: () => import('~/views/ShopView/ProductShopView.vue'),
              children: [
                { path: 'list', component: () => import('~/views/ShopView/ListProductView.vue') },
                { path: 'add', component: () => import('~/views/ShopView/CRUDProduct/CreateProductShopView.vue') },
              ],
            },
          ],
        },
        { path: 'order', component: () => import('~/views/UserView/OrderView.vue') },
      ],
    },
    {
      path: '/register-shop',
      component: () => import('~/views/ShopView/RegisterShopView.vue'),
      meta: { requiresAuth: true, roles: ['USER', 'ADMIN'] },
    },
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
