<template>
  <n-layout has-sider class="min-h-screen">
    <!-- Sidebar -->
    <n-layout-sider bordered :width="240" :collapsed="false" :collapsed-width="0" :show-trigger="false" :native-scrollbar="false">
      <n-menu :value="selectedMenuKey" :options="menuOptions" @update:value="handleMenuSelect" />
    </n-layout-sider>

    <n-layout>
      <!-- Header -->
      <n-layout-header bordered class="h-16 flex items-center px-4 sticky top-0 z-50 bg-white">
        <div class="flex items-center justify-between w-full">
          <div class="flex items-center gap-4">
            <n-text strong class="text-xl">Trang quản trị</n-text>
          </div>

          <div class="flex items-center gap-3">
            <!-- Notifications -->
            <n-badge :value="5" :max="99">
              <n-button quaternary circle>
                <template #icon>
                  <Bell class="w-5 h-5" />
                </template>
              </n-button>
            </n-badge>

            <!-- User Menu -->
            <n-dropdown :options="userMenuOptions" @select="handleUserMenuSelect">
              <n-button>
                <span class="hidden sm:inline">Admin</span>
                <ChevronDown class="w-4 h-4 ml-1" />
              </n-button>
            </n-dropdown>
          </div>
        </div>
      </n-layout-header>

      <!-- Main Content -->
      <n-layout-content class="p-6">
        <!-- Email Notification Banner -->
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-blue-800">Hệ thống Email đã được kích hoạt</h3>
              <div class="mt-2 text-sm text-blue-700">
                <p>Email thông báo sẽ được gửi tự động khi:</p>
                <ul class="list-disc list-inside mt-1 space-y-1">
                  <li>Tạo đơn hàng mới</li>
                  <li>Cập nhật trạng thái đơn hàng</li>
                  <li>Hủy đơn hàng</li>
                  <li>Giao hàng thành công</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <RouterView />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { onMounted, h, computed } from 'vue'
import { useRouter, RouterView, useRoute } from 'vue-router'
import axios from '../../utils/axios'
import {
  NLayout,
  NLayoutHeader,
  NLayoutSider,
  NLayoutContent,
  NMenu,
  NButton,
  NDropdown,
  NBadge,
  NText,
  NIcon,
} from 'naive-ui'
import { Bell, ChevronDown, Home, LogOut, Package, Tag, Store, Users, Truck, CreditCard } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()

const selectedMenuKey = computed(() => route.path)

// Menu configuration
const menuOptions = [
  {
    label: 'Dashboard',
    key: '/admin',
    icon: () => h(NIcon, null, { default: () => h(Home) }),
  },
  {
    label: 'Quản lý danh mục',
    key: '/admin/categories',
    icon: () => h(NIcon, null, { default: () => h(Package) }),
  },
  {
    label: 'Quản lý giảm giá',
    key: '/admin/discounts',
    icon: () => h(NIcon, null, { default: () => h(Tag) }),
  },
  {
    label: 'Quản lý vận chuyển',
    key: '/admin/shippings',
    icon: () => h(NIcon, null, { default: () => h(Truck) }),
  },
  {
    label: 'Quản lý thanh toán',
    key: '/admin/payments',
    icon: () => h(NIcon, null, { default: () => h(CreditCard) }),
  },

  {
    label: 'Quản lý cửa hàng',
    key: '/admin/shops',
    icon: () => h(NIcon, null, { default: () => h(Store) }),
  },
  {
    label: 'Quản lý người dùng',
    key: '/admin/users',
    icon: () => h(NIcon, null, { default: () => h(Users) }),
  },
]

// User menu dropdown options
const userMenuOptions = [
  {
    label: 'Về trang chủ',
    key: 'home',
    icon: () => h(NIcon, null, { default: () => h(Home) }),
  },
  {
    type: 'divider',
    key: 'd1',
  },
  {
    label: 'Đăng xuất',
    key: 'logout',
    icon: () => h(NIcon, null, { default: () => h(LogOut) }),
  },
]

const handleMenuSelect = (key) => {
  router.push(key)
}

const handleUserMenuSelect = async (key) => {
  if (key === 'home') {
    router.push('/')
  } else if (key === 'logout') {
    try {
      await axios.post('/api/logout')
      router.push('/login')
    } catch (error) {
      console.error('Logout error:', error)
      router.push('/login')
    }
  }
}

// Check admin access on mount
onMounted(async () => {
  try {
    const response = await axios.get('/api/info-account')
    const user = response.data.account
    if (!user || user.role !== 'ADMIN') {
      console.error('Access denied')
      router.push('/')
    }
  } catch (error) {
    console.error('Auth check error:', error)
    router.push('/login')
  }
})
</script>

<style scoped>
.n-layout-header {
  display: flex;
  align-items: center;
}

/* Đảm bảo sidebar không bị thu nhỏ */
:deep(.n-layout-sider) {
  transition: none !important;
}
</style>
