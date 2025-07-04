<template>
  <form
    @submit.prevent="registerSeller"
    class="w-full max-w-3xl mx-auto bg-white p-8 shadow rounded-xl"
  >
    <div class="text-[18px] uppercase pb-4 border-b font-bold">Đăng ký bán hàng</div>
    <div class="pt-4">
      <ul>
        <li>
          <label for="nameShop">Tên cửa hàng của bạn:<span class="text-red-500">*</span></label>
          <input
            v-model="formShop.nameShop"
            type="text"
            id="nameShop"
            placeholder="Nhập tên cửa hàng"
            class="w-full p-2 border rounded mt-2"
          />
        </li>
        <li class="mt-4">
          <label for="avatarShop"
            >Tải ảnh đại diện cửa hàng:<span class="text-red-500">*</span></label
          >
        </li>
        <li class="mt-2 flex items-center">
          <input
            type="file"
            id="avatarShop"
            accept="image/*"
            class="hidden"
            @change="handleAvatarChange"
          />
          <label
            for="avatarShop"
            class="cursor-pointer inline-block bg-gray-200 text-gray-600 hover:bg-gray-300 py-1 px-2 rounded"
          >
            Tải ảnh đại diện
          </label>
          <span
            v-if="avatarFileName"
            class="ml-4 text-gray-600 inline-block max-w-1/2 overflow-hidden text-ellipsis whitespace-nowrap align-middle"
            >{{ avatarFileName }}</span
          >
        </li>
        <li>
          <div v-if="avatarPreviewUrl" class="mt-4">
            <img
              :src="avatarPreviewUrl"
              class="w-32 h-32 object-cover rounded-md"
              alt="Avatar preview"
            />
          </div>
        </li>
        <li class="mt-4">
          <label for="cccdID">Căn cước công dân:<span class="text-red-500">*</span></label>
          <input
            v-model="formShop.cccd"
            type="text"
            id="cccdID"
            placeholder="Nhập số CCCD"
            class="w-full p-2 border rounded mt-2"
          />
        </li>
        <li class="flex flex-col md:flex-row gap-4 mt-4">
          <div class="w-full md:w-1/2">
            <label for="cccdFrontUrl">Ảnh mặt trước:<span class="text-red-500">*</span></label>
            <div class="mt-2 flex items-center">
              <input
                type="file"
                id="cccdFrontUrl"
                accept="image/*"
                class="hidden"
                @change="handleFrontImageChange"
              />
              <label
                for="cccdFrontUrl"
                class="cursor-pointer inline-block bg-gray-200 text-gray-600 hover:bg-gray-300 py-1 px-2 rounded"
              >
                Tải ảnh
              </label>
              <span
                v-if="frontImageFileName"
                class="ml-4 text-gray-600 inline-block max-w-1/2 overflow-hidden text-ellipsis whitespace-nowrap align-middle"
                >{{ frontImageFileName }}</span
              >
            </div>
            <div v-if="frontImagePreviewUrl" class="mt-4">
              <img
                :src="frontImagePreviewUrl"
                class="w-full h-50 rounded-md"
                alt="Preview of front image"
              />
            </div>
          </div>
          <div class="w-full md:w-1/2">
            <label for="cccdBackUrl">Ảnh mặt sau:<span class="text-red-500">*</span></label>
            <div class="mt-2 flex items-center">
              <input
                type="file"
                id="cccdBackUrl"
                accept="image/*"
                class="hidden"
                @change="handleBackImageChange"
              />
              <label
                for="cccdBackUrl"
                class="cursor-pointer inline-block bg-gray-200 text-gray-600 hover:bg-gray-300 py-1 px-2 rounded"
              >
                Tải ảnh
              </label>
              <span
                v-if="backImageFileName"
                class="ml-4 text-gray-600 inline-block max-w-1/2 overflow-hidden text-ellipsis whitespace-nowrap align-middle"
                >{{ backImageFileName }}</span
              >
            </div>
            <div v-if="backImagePreviewUrl" class="mt-4">
              <img
                :src="backImagePreviewUrl"
                class="w-full h-50 rounded-md"
                alt="Preview of back image"
              />
            </div>
          </div>
        </li>
        <li class="mt-8 flex justify-end">
          <button
            :disabled="isLoading"
            class="flex items-center justify-center bg-blue-500 hover:bg-[#004aad] text-white rounded py-2 px-4 uppercase min-w-[120px] disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <svg
              v-if="isLoading"
              class="animate-spin h-5 w-5 text-white"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                class="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                stroke-width="4"
              ></circle>
              <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              ></path>
            </svg>
            <span v-else>Đăng ký</span>
          </button>
        </li>
      </ul>
    </div>
  </form>
</template>

<script setup>
import { uploadImage } from '@/stores/useUploadImage'
import { useUserStore } from '@/stores/useUserStore'
import axios from 'axios'
import { storeToRefs } from 'pinia'
import { onMounted, ref } from 'vue'
// Importing toast for notifications
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const userStore = useUserStore()
const { currentUser: infoAccount } = storeToRefs(userStore)

onMounted(() => {
  if (!infoAccount.value) {
    userStore.fetchUser()
  }
})

const isLoading = ref(false)

const formShop = ref({
  nameShop: 'LOGITECH OFFICIAL STORE',
  cccd: '01234567890',
  avatarUrl: null,
  cccdFrontUrl: null,
  cccdBackUrl: null,
})

// Refs for shop avatar
const avatarFileName = ref('')
const avatarPreviewUrl = ref('')

// Refs for front side of ID card
const frontImageFileName = ref('')
const frontImagePreviewUrl = ref('')

// Refs for back side of ID card
const backImageFileName = ref('')
const backImagePreviewUrl = ref('')

const handleAvatarChange = (event) => {
  const file = event.target.files[0]
  formShop.value.avatarUrl = file

  if (file) {
    avatarFileName.value = file.name
    avatarPreviewUrl.value = URL.createObjectURL(file)
  } else {
    avatarFileName.value = ''
    avatarPreviewUrl.value = ''
  }
}

const handleFrontImageChange = (event) => {
  const file = event.target.files[0]
  formShop.value.cccdFrontUrl = file

  if (file) {
    frontImageFileName.value = file.name
    frontImagePreviewUrl.value = URL.createObjectURL(file)
  } else {
    frontImageFileName.value = ''
    frontImagePreviewUrl.value = ''
  }
}

const resetForm = () => {
  formShop.value.nameShop = ''
  formShop.value.cccd = ''
  formShop.value.avatarUrl = null
  formShop.value.cccdFrontUrl = null
  formShop.value.cccdBackUrl = null

  avatarFileName.value = ''
  avatarPreviewUrl.value = ''
  frontImageFileName.value = ''
  frontImagePreviewUrl.value = ''
  backImageFileName.value = ''
  backImagePreviewUrl.value = ''
}

const handleBackImageChange = (event) => {
  const file = event.target.files[0]
  formShop.value.cccdBackUrl = file

  if (file) {
    backImageFileName.value = file.name
    backImagePreviewUrl.value = URL.createObjectURL(file)
  } else {
    backImageFileName.value = ''
    backImagePreviewUrl.value = ''
  }
}

const registerSeller = async () => {
  if (isLoading.value) return

  if (
    !formShop.value.nameShop ||
    !formShop.value.cccd ||
    !formShop.value.avatarUrl ||
    !formShop.value.cccdFrontUrl ||
    !formShop.value.cccdBackUrl
  ) {
    toast.warning('Vui lòng điền đầy đủ thông tin!', {
      position: 'top-right',
      autoClose: 1500,
      theme: 'colored',
      transition: 'zoom',
      pauseOnHover: false,
    })
    return
  }

  isLoading.value = true

  try {
    const avatarShopUpload = await uploadImage(formShop.value.avatarUrl)
    const cccdFrontUpload = await uploadImage(formShop.value.cccdFrontUrl)
    const cccdBackUpload = await uploadImage(formShop.value.cccdBackUrl)

    await axios.post('/api/shops/register', {
      nameShop: formShop.value.nameShop,
      cccd: formShop.value.cccd,
      avatarShop: avatarShopUpload,
      cccdFrontUrl: cccdFrontUpload,
      cccdBackUrl: cccdBackUpload,
    })

    await userStore.fetchUser()

    toast.success('Đăng ký thành công!', {
      position: 'top-right',
      autoClose: 1500,
      theme: 'colored',
      transition: 'zoom',
      pauseOnHover: false,
    })
  } catch {
    toast.error('Đăng ký thất bại!', {
      position: 'top-right',
      autoClose: 1500,
      theme: 'colored',
      transition: 'zoom',
      pauseOnHover: false,
    })
  } finally {
    isLoading.value = false
    resetForm()
  }
}
</script>
