<script setup>
import Navbar from '@/components/admins/Navbar.vue'
import AdminFooter from '@/components/admins/Footer.vue'

import { useRoute } from 'vue-router'

const route = useRoute()
const isActive = (path) => {
  if (route.path === path) {
    return 'py-2 px-3 rounded bg-blue-950 text-white'
  } else {
    return 'py-2 px-3 rounded hover:bg-gray-200 transition-colors duration-200'
  }
}
</script>

<template>
  <div class="h-screen flex flex-col">
    <Navbar />
    <div class="flex-1 p-4">
      <div class="max-w-[1200px] mx-auto h-full flex flex-row gap-4">
        <div class="w-[20%] bg-white rounded shadow-sm p-4">
          <div class="flex flex-col gap-2">
            <RouterLink to="/admins/dashboard" :class="isActive('/admins/dashboard')">
              Bảng điều khiển
            </RouterLink>
            <RouterLink to="/admins/categories" :class="isActive('/admins/categories')">
              Quản lý danh mục
            </RouterLink>
            <RouterLink to="/admins/sellers" :class="isActive('/admins/sellers')">
              Quản lý người bán
            </RouterLink>
            <RouterLink to="/admins/coupon" :class="isActive('/admins/coupon')">
              Quản lý Mã giảm giá
            </RouterLink>
          </div>
        </div>
        <div class="w-[80%] bg-white rounded shadow-sm">
          <div>Bảng điều khiển</div>
        </div>
      </div>
    </div>
    <AdminFooter />
  </div>
</template>
