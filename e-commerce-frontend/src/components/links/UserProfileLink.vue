<template>
  <main class="relative" ref="dropdownContainer">
    <button
      @click="toggleDropdown"
      class="flex w-[125px] items-center bg-gray-200 rounded text-gray-600 hover:bg-gray-300 link button-primary px-4 py-2"
    >
      <img
        class="w-[24px] h-[24px] rounded-full"
        src="https://i.pravatar.cc/150?u=jamesaldrino"
        alt="User avatar"
      />
      <span class="whitespace-nowrap w-full text-right">Tài khoản</span>
    </button>
    <div
      v-if="isOpen"
      class="absolute right-0 mt-2 w-[200px] bg-white rounded border border-gray-200"
    >
      <ul class="py-2">
        <li>
          <router-link to="/" class="px-4 py-2 hover:bg-gray-100 flex gap-2 items-center"
            ><UserPen width="20" height="20" /> Thông tin cá nhân</router-link
          >
        </li>
        <li>
          <router-link to="/" class="flex gap-2 items-center px-4 py-2 hover:bg-gray-100">
            <Package width="20" height="20" /> Đơn hàng của tôi</router-link
          >
        </li>
        <li>
          <router-link to="/shops/register" class="flex gap-2 px-4 py-2 hover:bg-gray-100"
            ><Store width="20" height="20" /> Kênh bán hàng</router-link
          >
        </li>
        <li>
          <form @submit.prevent="handleLogout">
            <button class="flex gap-2 w-full text-left px-4 py-2 hover:bg-gray-100">
              <LogOut width="20" height="20" /> Đăng xuất
            </button>
          </form>
        </li>
      </ul>
    </div>
  </main>
</template>
<script setup>
import { getInfoAccount, logout } from '@/api/AuthController'
import { onMounted, ref } from 'vue'
import { UserPen } from 'lucide-vue-next'
import { Package } from 'lucide-vue-next'
import { Store } from 'lucide-vue-next'
import { LogOut } from 'lucide-vue-next'
import { onClickOutside } from '@vueuse/core'

const infoAccount = ref({})

onMounted(async () => {
  try {
    infoAccount.value = await getInfoAccount()
  } catch (error) {
    console.error('Failed to fetch account info:', error)
  }
})

const isOpen = ref(false)

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const handleLogout = async () => {
  try {
    await logout()
    window.location.href = '/'
  } catch (error) {
    console.error('Logout failed:', error)
  }
}

const dropdownContainer = ref(null)

onClickOutside(dropdownContainer, () => {
  isOpen.value = false
})
</script>
