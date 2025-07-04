<template>
  <main class="flex flex-col h-screen bg-gray-100">
    <AdminHeader :pageTitle="activeTabName" />
    <div class="flex-1 p-4 overflow-hidden">
      <div class="max-w-[1440px] h-full mx-auto flex gap-4">
        <aside class="w-64 bg-white p-4 rounded-lg shadow flex-shrink-0">
          <div v-if="infoAccount.email" class="flex flex-col items-center p-4 border-b">
            <img
              src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png"
              alt="Admin Avatar"
              class="w-20 h-20 rounded-full object-cover mb-4 border-2 border-[#004aad]"
            />
            <h3 class="font-bold text-lg text-gray-800">
              {{ infoAccount.name || infoAccount.email }}
            </h3>
            <span class="text-xs text-white bg-[#004aad] font-semibold px-2 py-1 rounded mt-1">{{
              infoAccount.role
            }}</span>
          </div>

          <nav class="mt-4 flex-1">
            <ul>
              <li v-for="tab in tabs" :key="tab.id" class="mb-2">
                <button
                  @click.prevent="activeTab = tab.id"
                  class="block p-2 rounded transition-colors duration-200 w-full text-left"
                  :class="{
                    'bg-[#004aad] text-white font-semibold shadow': activeTab === tab.id,
                    'text-gray-600 hover:bg-gray-200': activeTab !== tab.id,
                  }"
                >
                  <span>{{ tab.name }}</span>
                </button>
              </li>
            </ul>
          </nav>
        </aside>

        <div class="flex-1 bg-white rounded-lg shadow overflow-y-auto custom-scrollbar">
          <div v-if="activeTab === 'dashboard'">
            <h1 class="text-2xl font-bold mb-4">Bảng điều khiển</h1>
            <p>Tổng quan về hệ thống.</p>
          </div>

          <div v-if="activeTab === 'users'">
            <h1 class="text-2xl font-bold mb-4">Quản lý người dùng</h1>
            <p>Nội dung quản lý người dùng sẽ ở đây...</p>
          </div>

          <div v-if="activeTab === 'shops'">
            <h1 class="text-2xl font-bold mb-4">Quản lý cửa hàng</h1>
            <p>Nội dung quản lý cửa hàng sẽ ở đây...</p>
          </div>

          <div v-if="activeTab === 'registrations'">
            <h1 class="text-2xl font-bold mb-4">Duyệt đăng ký bán hàng</h1>
            <SalesRegistration />
          </div>

          <div v-if="activeTab === 'categories'">
            <AdminCategory />
          </div>
        </div>
      </div>
    </div>
    <div class="px-4 bg-blue-950 shadow">
      <div class="max-w-[1440px] mx-auto w-full px-4 py-4 text-white text-center">Admin Footer</div>
    </div>
  </main>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import AdminHeader from './AdminHeader.vue'
import { getInfoAccount } from '@/api/AuthController'
import SalesRegistration from './SalesRegistration.vue'
import AdminCategory from './AdminCategory.vue'

const infoAccount = ref({})

const activeTab = ref('dashboard')

const tabs = [
  { id: 'dashboard', name: 'Bảng điều khiển' },
  { id: 'users', name: 'Quản lý người dùng' },
  { id: 'shops', name: 'Quản lý cửa hàng' },
  { id: 'registrations', name: 'Duyệt đăng ký' },
  { id: 'categories', name: 'Quản lý danh mục' },
]

const activeTabName = computed(() => {
  const currentTab = tabs.find((tab) => tab.id === activeTab.value)
  return currentTab ? currentTab.name : 'Trang chủ'
})

onMounted(async () => {
  try {
    infoAccount.value = await getInfoAccount()
  } catch (error) {
    console.error('Failed to fetch account info:', error)
  }
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #004aad;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #6b7280;
}
</style>
