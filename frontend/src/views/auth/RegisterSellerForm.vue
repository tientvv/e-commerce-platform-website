<script setup>
import { uploadImage } from '@/stores/useImageUpload'
import axios from 'axios'
import { reactive } from 'vue'

const formShop = reactive({
  nameShop: '',
  cccd: '',
  avatarUrl: null,
  cccdFrontUrl: null,
  cccdBackUrl: null,
})

const registerSeller = async () => {
  try {
    const avatarUpload = await uploadImage(formShop.avatarUrl)
    const cccdFrontUpload = await uploadImage(formShop.cccdFrontUrl)
    const cccdBackUpload = await uploadImage(formShop.cccdBackUrl)

    await axios.post('/api/register-seller', {
      nameShop: formShop.nameShop,
      cccd: formShop.cccd,
      avatarUrl: avatarUpload,
      cccdFrontUrl: cccdFrontUpload,
      cccdBackUrl: cccdBackUpload,
    })
  } catch (error) {
    console.error('Error registering seller:', error)
  }
}
</script>
<template>
  <form @submit.prevent="registerSeller">
    <div>
      <input
        type="text"
        v-model="formShop.nameShop"
        placeholder="Nhập tên cửa hàng"
        name="name_shop"
      />
    </div>
    <div>
      <input type="text" v-model="formShop.cccd" placeholder="Nhập CCCD của bạn" name="cccd" />
    </div>
    <input
      type="file"
      @change="(e) => (formShop.avatarUrl = e.target.files[0])"
      name="avatar_url"
    />
    <input
      type="file"
      @change="(e) => (formShop.cccdFrontUrl = e.target.files[0])"
      name="cccd_front_url"
    />
    <input
      type="file"
      @change="(e) => (formShop.cccdBackUrl = e.target.files[0])"
      name="cccd_back_url"
    />
    <button type="submit">Đăng ký</button>
  </form>
</template>
