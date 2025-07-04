<template>
  <main class="flex flex-col h-screen">
    <div class="px-4 bg-white shadow">
      <div
        v-if="infoAccount.status === 0 || infoAccount.status === null || infoAccount.status === 1"
        class="max-w-[1440px] mx-auto p-4 flex items-center gap-8"
      >
        <LogoNavbar />
        <h2 class="text-[24px]">Đăng ký</h2>
        <RouterLink to="/" class="ml-auto underline text-blue-900">Bạn cần giúp đỡ?</RouterLink>
      </div>
      <div
        class="max-w-[1440px] mx-auto p-4 flex items-center gap-8"
        v-if="infoAccount.status === 2"
      >
        <LogoNavbar />
        <h2 class="text-[24px]">Kênh bán hàng</h2>
      </div>
    </div>
    <div class="flex-1 p-4">
      <div class="max-w-[1440px] h-full mx-auto flex flex-row justify-center items-center">
        <SellerSignup v-if="infoAccount.status === 0 || infoAccount.status === null" />
        <div
          v-if="infoAccount.status === 1"
          class="flex flex-col items-center justify-center bg-white rounded-lg p-8 max-w-md mx-auto shadow"
        >
          <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mb-4">
            <CircleCheck class="w-8 h-8 text-green-600" />
          </div>
          <h3 class="text-xl font-semibold text-gray-800 mb-2">Đăng ký thành công!</h3>
          <p class="text-gray-600 text-center mb-1">Bạn đã đăng ký kênh bán hàng!</p>
          <p class="text-gray-600 text-center mb-4">Vui lòng chờ duyệt từ hệ thống.</p>
          <p class="text-sm text-gray-600 text-center">Liên hệ với chúng tôi nếu bạn cần hỗ trợ.</p>
        </div>
      </div>
    </div>
    <div class="px-4 bg-white shadow">
      <MainFooter />
    </div>
  </main>
</template>

<script setup>
import MainFooter from '@/components/layouts/MainFooter.vue'
import LogoNavbar from '@/components/navbars/LogoNavbar.vue'
import SellerSignup from '@/components/forms/SellerSignup.vue'
import { onMounted, ref } from 'vue'
import { getInfoAccount } from '@/api/AuthController'
import { CircleCheck } from 'lucide-vue-next'

const infoAccount = ref({})

onMounted(async () => {
  try {
    infoAccount.value = await getInfoAccount()
  } catch (error) {
    console.error('Failed to fetch account info:', error)
  }
})
</script>
