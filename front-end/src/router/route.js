import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),

  routes: [
    {
      path: '/',
      redirect: '/home',
      children: [
        {
          path: '/home',
          component: () => import('@/views/Home.vue'),
        },
      ],
    },

    {
      path: '/admins',
      redirect: '/admins/dashboard',
      children: [
        {
          path: '/admins/dashboard',
          component: () => import('../views/admins/DashboardAdmin.vue'),
        },
        {
          path: '/admins/categories',
          component: () => import('../views/admins/category/DashboardCategory.vue'),
        },
        { path: '/admins/sellers', component: () => import('../views/admins/sellers.vue') },
      ],
    },
  ],
})

export default router
