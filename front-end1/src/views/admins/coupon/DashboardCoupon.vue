<script setup lang="ts">
import Navbar from '@/components/admins/Navbar.vue'
import AdminFooter from '@/components/admins/Footer.vue'
import FormCoupon from './FormCoupon.vue'
import { isCancel } from 'axios'
</script>

<template>
  <div class="h-screen flex flex-col">
    <Navbar />
    <div class="flex-1 p-4">
      <div class="max-w-[1200px] mx-auto h-full flex flex-col gap-4 bg-white shadow rounded-md p-4">
        <div class="text-xl uppercase font-bold">Quản lý mã giảm giá</div>
        <div class="w-[50%]">
          <FormCoupon />
        </div>
      </div>
    </div>
    <AdminFooter />
  </div>
</template>
