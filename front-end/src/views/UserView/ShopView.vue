<template>
  <n-layout has-sider class="h-full">
    <n-layout-sider bordered :width="280" :native-scrollbar="false" show-trigger="false">
      <MenuBarView />
    </n-layout-sider>

    <n-layout-content content-style="padding: 24px;" class="h-full">
      <n-spin :show="loading" class="h-full">
        <div v-if="message" class="h-full flex items-center justify-center">
          <n-result status="warning" :title="message" description="Vui lòng kiểm tra lại thông tin cửa hàng của bạn">
            <template #footer>
              <n-button @click="() => $router.push('/register-shop')"> Đăng ký cửa hàng </n-button>
            </template>
          </n-result>
        </div>
        <RouterView v-else />
      </n-spin>
    </n-layout-content>
  </n-layout>
</template>

<script setup>
import axios from '../../utils/axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MenuBarView from '../ShopView/MenuBarView.vue'
import { NLayout, NLayoutSider, NLayoutContent, NSpin, NResult, NButton } from 'naive-ui'

const router = useRouter()
const shop = ref(null)
const message = ref('')
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await axios.get('/api/user/shop')
    if (res.data.shop) {
      shop.value = res.data.shop
      message.value = ''
    } else {
      message.value = res.data.message
      if (message.value === 'Bạn chưa đăng ký cửa hàng!' || message.value === 'Cửa hàng của bạn đã bị khóa!') {
        router.push('/register-shop')
      }
    }
  } catch {
    message.value = 'Lỗi khi tải thông tin cửa hàng. Vui lòng thử lại sau.'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* Ẩn hoàn toàn nút trigger của sidebar */
:deep(.n-layout-sider-trigger) {
  display: none !important;
}

:deep(.n-layout-sider-trigger-arrow) {
  display: none !important;
}
</style>
