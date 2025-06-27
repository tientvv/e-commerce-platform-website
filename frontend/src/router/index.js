import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
// Import LoginView component
import LoginView from '@/views/auth/LoginView.vue'
import BuyerDashboardView from '@/views/buyer/BuyerDashboardView.vue'
import ProductList from '@/views/buyer/ProductList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    { path: '/', component: HomeView },
    { path: '/login', component: LoginView },
    // Seller routes
    { path: '/register-seller', component: () => import('@/views/auth/RegisterSellerView.vue') },
    // Buyer routes
    {
      path: '/buyer',
      children: [
        {
          path: '',
          component: BuyerDashboardView,
        },
        {
          path: 'products',
          component: ProductList,
        },
      ],
    },
    // Admin routes
    {
      path: '/admin',
      children: [
        {
          path: '',
          component: AdminDashboardView,
        },
      ],
    },
  ],
})

export default router
