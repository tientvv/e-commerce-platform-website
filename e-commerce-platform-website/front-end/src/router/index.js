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
    { path: '/search', component: () => import('~/views/SearchView.vue') },
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
            {
              path: 'revenue',
              component: () => import('~/views/ShopView/ShopRevenueView.vue'),
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
  console.log('Router guard - navigating to:', to.path)

  // Kiểm tra đăng nhập trước
  let user = null
  try {
    const res = await axios.get('/api/info-account')
    user = res.data.account
    console.log('Router guard - user found:', user ? user.username : 'null')
  } catch (error) {
    console.log('Router guard - not logged in, error:', error.message)
    // Nếu chưa đăng nhập, chỉ cho phép truy cập các route công khai
    const publicRoutes = ['/', '/login', '/register']
    if (publicRoutes.includes(to.path)) {
      console.log('Router guard - allowing access to public route:', to.path)
      return true
    }
    // Nếu route cần auth, redirect to login
    if (to.meta.requiresAuth) {
      console.log('Router guard - redirecting to login (requires auth)')
      return '/login'
    }
    console.log('Router guard - allowing access to non-auth route:', to.path)
    return true
  }

  // Nếu đã đăng nhập, redirect khỏi trang login/register
  if (user && (to.path === '/login' || to.path === '/register')) {
    console.log('Router guard - redirecting logged in user from', to.path, 'to /')
    return '/'
  }

  // Các route không cần kiểm tra thông tin profile (nhưng vẫn cần đăng nhập)
  const exemptRoutes = ['/', '/login', '/register', '/register-shop']
  if (exemptRoutes.includes(to.path)) {
    return true
  }

  // Kiểm tra đăng nhập cho /user/profile
  if (to.path === '/user/profile') {
    if (!user) {
      console.log('Router guard - redirecting to login for profile page')
      return '/login'
    }
    return true
  }

  // Nếu đã đăng nhập, kiểm tra thông tin profile cho USER role
  if (user && user.role === 'USER') {
    console.log('Router guard - checking profile completeness for USER')
    // Kiểm tra xem thông tin có đầy đủ không
    const isProfileComplete = (
      user.username &&
      user.name &&
      user.email &&
      user.phone &&
      user.address &&
      user.username.trim() !== '' &&
      user.name.trim() !== '' &&
      user.email.trim() !== '' &&
      user.phone.trim() !== '' &&
      user.address.trim() !== ''
    )

    console.log('Router guard - profile complete:', isProfileComplete)
    console.log('Router guard - user data:', {
      username: user.username,
      name: user.name,
      email: user.email,
      phone: user.phone,
      address: user.address
    })

    if (!isProfileComplete) {
      console.log('Router guard - redirecting to profile (incomplete)')
      // Nếu thông tin chưa đầy đủ, bắt buộc chuyển hướng đến trang profile với thông báo
      return '/user/profile?message=update_required'
    }
  }

  // Kiểm tra đăng nhập cho các route cần auth
  if (to.meta.requiresAuth) {
    console.log('Router guard - route requires auth')
    if (!user) {
      console.log('Router guard - no user, redirecting to login')
      return '/login'
    }

    // Check role permissions
    if (to.meta.roles && !to.meta.roles.includes(user.role)) {
      console.log('Router guard - user role not allowed:', user.role, 'for roles:', to.meta.roles)
      if (to.path.startsWith('/admin') && user.role !== 'ADMIN') {
        return '/'
      }
      return '/'
    }

    // Kiểm tra xem route có yêu cầu shop không (only for USER routes, not ADMIN)
    if ((to.meta.requiresShop || to.matched.some((record) => record.meta.requiresShop)) && user.role !== 'ADMIN') {
      console.log('Router guard - checking shop requirement')
      try {
        const shopRes = await axios.get('/api/user/shop')
        if (!shopRes.data.shop) {
          console.log('Router guard - no shop, redirecting to register-shop')
          return '/register-shop'
        }
      } catch {
        console.log('Router guard - shop check failed, redirecting to register-shop')
        return '/register-shop'
      }
    }

    console.log('Router guard - auth check passed')
    return true
  }

  console.log('Router guard - allowing access to:', to.path)
  return true
})

export default router

