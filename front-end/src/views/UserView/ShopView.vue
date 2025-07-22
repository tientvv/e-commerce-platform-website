<template>
  <div>
    Kênh đã đăng ký bán hàng (Đang phát triển)
    <RouterView />
  </div>
</template>

<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'
import router from '~/router'

const shop = ref(null)
const message = ref('')

onMounted(async () => {
  try {
    const res = await axios.get('/api/user/shop')
    if (res.data.shop) {
      shop.value = res.data.shop
    } else {
      message.value = res.data.message
      if (message.value === 'Bạn chưa đăng ký cửa hàng!' || message.value === 'Cửa hàng của bạn đã bị khóa!') {
        router.push('/register-shop')
      }
    }
  } catch {
    message.value = 'Lỗi khi tải thông tin cửa hàng. Vui lòng thử lại sau.'
  }
})
</script>
