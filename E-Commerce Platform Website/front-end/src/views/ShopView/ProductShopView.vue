<template>
  <n-space vertical :size="16">
    <n-tabs :value="activeTab" type="segment" @update:value="handleTabChange">
      <n-tab-pane name="list" tab="Danh sách sản phẩm" />
      <n-tab-pane name="add" tab="Thêm sản phẩm" />
    </n-tabs>

    <RouterView />
  </n-space>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NSpace, NTabs, NTabPane } from 'naive-ui'

const route = useRoute()
const router = useRouter()

// Computed để xác định tab hiện tại dựa trên route
const activeTab = computed(() => {
  if (route.path.includes('/add')) return 'add'
  if (route.path.includes('/edit/')) return 'add' // Edit cũng sử dụng tab "add"
  return 'list'
})

// Handle tab change
const handleTabChange = (value) => {
  if (value === 'list') {
    router.push('/user/shop/product/list')
  } else if (value === 'add') {
    router.push('/user/shop/product/add')
  }
}
</script>
