import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
// Import LoginView component
import LoginView from '@/views/auth/LoginView.vue'
import DashboardView from '@/views/user/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    { path: '/', component: HomeView },
    { path: '/login', component: LoginView },
    // Seller routes
    { path: '/register-seller', component: () => import('@/views/auth/RegisterSellerView.vue') },
    {
      path: '/seller',
      component: () => import('@/views/seller/DashboardView.vue'),
    },
    // Buyer routes
    {
      path: '/me',
      children: [
        {
          path: '',
          component: DashboardView,
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
