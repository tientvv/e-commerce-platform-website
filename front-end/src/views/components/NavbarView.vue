<template>
  <div class="p-2 bg-white border-b border-gray-200">
    <div class="flex max-w-[1440px] mx-auto items-center justify-between">
      <div>
        <RouterLink to="/"><img :src="Logo" width="50" /></RouterLink>
      </div>
      <div>
        <div>Tìm kiếm sản phẩm (Chưa phát triển)</div>
      </div>
      <div>
        <div class="flex items-center gap-2">
          <RouterLink to="/wishlist" class="py-1 px-2 rounded outline-1 flex items-center gap-2 border border-gray-400">
            <Heart width="18" />
            Yêu thích
          </RouterLink>
          <RouterLink
            v-if="!accountInfo"
            to="/login"
            class="py-1 px-2 rounded outline-1 flex items-center gap-2 border border-gray-400"
          >
            <LogIn width="18" />
            Đăng nhập
          </RouterLink>
          <button v-if="accountInfo" class="py-1 px-2 relative border border-gray-400 flex items-center gap-2 rounded">
            <User width="18" />
            Xin chào, {{ accountInfo.username }}
          </button>
          <FormLogoutAccount v-if="accountInfo" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import Logo from '~/assets/logo.svg'
import { LogIn } from 'lucide-vue-next'
import { Heart } from 'lucide-vue-next'
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { User } from 'lucide-vue-next'
import FormLogoutAccount from '../LoginView/FormLogoutAccount.vue'

const message = ref('')

const accountInfo = ref({
  username: 'Không có dữ liệu',
  email: '',
  phone: '',
})

onMounted(() => {
  fetchAccountInfo()
})

const fetchAccountInfo = async () => {
  try {
    const res = await axios.get('/api/info-account', accountInfo.value)
    if (res.data) {
      accountInfo.value = res.data.account
    }
  } catch {
    message.value = 'Không thể lấy thông tin tài khoản!'
  }
}
</script>
