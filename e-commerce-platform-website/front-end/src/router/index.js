import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '~/views/HomeView.vue'
import LoginAccountView from '~/views/LoginAccountView.vue'
import WishlistView from '~/views/WishlistView.vue'
import RegisterAccountView from '~/views/RegisterAccountView.vue'
import CategoryView from '~/views/CategoryView.vue'
import ProductDetailView from '~/views/ProductDetailView.vue'
import CartView from '~/views/CartView.vue'
import axios from '../utils/axios'
import UserView from '~/views/UserView/UserView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/login', component: LoginAccountView },
    { path: '/category/:id', component: CategoryView },
    { path: '/product/:id', component: ProductDetailView },
    { path: '/cart', component: CartView },
    { path: '/order/:id', component: () => import('~/views/OrderDetailView.vue') },
    { path: '/payment-success', component: () => import('~/views/PaymentSuccessView.vue') },
    { path: '/payment-cancel', component: () => import('~/views/PaymentCancelView.vue') },

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
          meta: { requiresShop: true },
          children: [
            {
              path: 'product-variants/index',
              component: () => import('~/views/ProductVariants/ProductVariantsView.vue'),
              meta: { requiresShop: true },
            },
            {
              path: 'product-images/index',
              component: () => import('~/views/ProductVariants/ProductImageView.vue'),
              meta: { requiresShop: true },
            },
            {
              path: 'profile',
              component: () => import('~/views/ShopView/ProfileShopView.vue'),
              meta: { requiresShop: true },
            },

            {
              path: 'product',
              component: () => import('~/views/ShopView/ProductShopView.vue'),
              meta: { requiresShop: true },
              children: [
                {
                  path: 'list',
                  component: () => import('~/views/ShopView/ListProductView.vue'),
                  meta: { requiresShop: true },
                },
                {
                  path: 'add',
                  component: () => import('~/views/ShopView/CRUDProduct/CreateProductShopView.vue'),
                  meta: { requiresShop: true },
                },
                {
                  path: 'edit/:id',
                  component: () => import('~/views/ShopView/CRUDProduct/EditProductShopView.vue'),
                  meta: { requiresShop: true },
                },
              ],
            },
            {
              path: 'orders',
              component: () => import('~/views/ShopView/ShopOrderView.vue'),
              meta: { requiresShop: true },
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
    // Admin Routes
    {
      path: '/admin',
      component: () => import('~/views/AdminView/AdminView.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
      children: [
        {
          path: '',
          component: () => import('~/views/AdminView/AdminDashboard.vue'),
        },
        {
          path: 'categories',
          component: () => import('~/views/AdminView/AdminCategoryView.vue'),
        },
        {
          path: 'discounts',
          component: () => import('~/views/AdminView/AdminDiscountView.vue'),
        },
        {
          path: 'shippings',
          component: () => import('~/views/AdminView/AdminShippingView.vue'),
        },
        {
          path: 'payments',
          component: () => import('~/views/AdminView/AdminPaymentView.vue'),
        },

        {
          path: 'shops',
          component: () => import('~/views/AdminView/AdminShopView.vue'),
        },
        {
          path: 'users',
          component: () => import('~/views/AdminView/AdminUserView.vue'),
        },
      ],
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

    // Check role permissions
    if (to.meta.roles && !to.meta.roles.includes(user.role)) {
      // If user tries to access admin but is not admin, redirect to home
      if (to.path.startsWith('/admin') && user.role !== 'ADMIN') {
        return '/'
      }
      return '/'
    }

    // Kiểm tra xem route có yêu cầu shop không (only for USER routes, not ADMIN)
    if ((to.meta.requiresShop || to.matched.some((record) => record.meta.requiresShop)) && user.role !== 'ADMIN') {
      try {
        const shopRes = await axios.get('/api/user/shop')
        if (!shopRes.data.shop) {
          // Chưa có shop, chuyển hướng đến trang đăng ký shop
          return '/register-shop'
        }
      } catch {
        // Lỗi khi kiểm tra shop (có thể chưa đăng ký shop)
        return '/register-shop'
      }
    }

    return true
  } catch {
    return '/login'
  }
})

export default router
