<script setup>
import { fetchUserFromSession } from '@/stores/user'
import { useUserStore } from '@/stores/useUserStore'
import { onMounted } from 'vue'
// Import HeaderView component
import HeaderView from '@/components/user/HeaderView.vue'

const userStore = useUserStore()

onMounted(async () => {
  fetchUserFromSession()
})
</script>

<template>
  <main>
    <!-- Header View -->
    <HeaderView />
    <!-- Home View -->
    <div class="home">
      <h1>Trang chủ</h1>
      <p>Đây là trang chủ của ứng dụng.</p>
      <div v-if="userStore.user">
        <p>Thông tin user</p>
        <p>Email: {{ userStore.user.email }}</p>
        <p>Vai trò: {{ userStore.user.role }}</p>
      </div>
    </div>
    <!-- FooterView -->
  </main>
</template>
