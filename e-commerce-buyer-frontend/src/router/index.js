import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/login',
      component: () => import('../views/LoginPages/LoginView.vue'),
    },
    {
      path: '/about',
      component: () => import('../views/AboutView.vue'),
    },
  ],
})

export default router
