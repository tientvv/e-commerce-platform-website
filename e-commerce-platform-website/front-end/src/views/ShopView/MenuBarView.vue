<template>
  <div class="p-4">
    <n-h3 class="mb-4">Danh mục</n-h3>
    <n-menu :value="currentRoute" :options="menuOptions" @update:value="handleMenuSelect" />
  </div>
</template>

<script setup>
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Info, Package, Settings, Image, ShoppingCart } from 'lucide-vue-next'
import { NH3, NMenu, NIcon } from 'naive-ui'

const route = useRoute()
const router = useRouter()

// Computed để theo dõi route hiện tại
const currentRoute = computed(() => route.path)

// Menu options
const menuOptions = [
  {
    label: 'Thông tin cửa hàng',
    key: '/user/shop/profile',
    icon: () => h(NIcon, null, { default: () => h(Info) }),
  },
  {
    label: 'Quản lý sản phẩm',
    key: '/user/shop/product/list',
    icon: () => h(NIcon, null, { default: () => h(Package) }),
  },
  {
    label: 'Quản lý SP biến thể',
    key: '/user/shop/product-variants/index',
    icon: () => h(NIcon, null, { default: () => h(Settings) }),
  },
  {
    label: 'Quản lý ảnh SP',
    key: '/user/shop/product-images/index',
    icon: () => h(NIcon, null, { default: () => h(Image) }),
  },
  {
    label: 'Quản lý đơn hàng',
    key: '/user/shop/orders',
    icon: () => h(NIcon, null, { default: () => h(ShoppingCart) }),
  },
]

// Handle menu selection
const handleMenuSelect = (key) => {
  router.push(key)
}
</script>
