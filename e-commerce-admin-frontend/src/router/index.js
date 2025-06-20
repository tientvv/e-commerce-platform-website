import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../views/LoginPages/LoginView.vue'),
    },
    {
      path: '/login',
      component: () => import('../views/LoginPages/LoginView.vue'),
    },
    {
      path: '/dashboard',
      component: () => import('../views/DashboardView.vue'),
    },
  ],
})

export default router
