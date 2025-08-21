<template>
  <div class="w-[750px] p-6">
    <div class="bg-white rounded-lg shadow-lg overflow-hidden w-full">
      <!-- Header -->
      <div class="bg-gradient-to-r from-blue-600 to-purple-600 px-6 py-4">
        <h1 class="text-2xl font-bold text-white">Đăng ký bán hàng</h1>
        <p class="text-blue-100 mt-1">Tạo cửa hàng của bạn để bắt đầu kinh doanh</p>
      </div>

      <div class="p-6">
        <!-- Success Message -->
        <div v-if="successMessage" class="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-green-800">Thành công</h3>
              <div class="mt-2 text-sm text-green-700">{{ successMessage }}</div>
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">Có lỗi xảy ra</h3>
              <div class="mt-2 text-sm text-red-700">{{ errorMessage }}</div>
            </div>
          </div>
        </div>

        <!-- Form -->
        <n-form ref="formRef" :model="formData" :rules="rules" @submit.prevent="registerShop">
          <n-form-item label="Tên cửa hàng" path="shopName">
            <n-input
              v-model:value="formData.shopName"
              placeholder="Nhập tên cửa hàng"
              :input-props="{ autocomplete: 'organization' }"
            />
          </n-form-item>

          <n-form-item label="Mô tả cửa hàng" path="description">
            <n-input
              v-model:value="formData.description"
              type="textarea"
              placeholder="Nhập mô tả cửa hàng"
              :autosize="{ minRows: 3, maxRows: 5 }"
            />
          </n-form-item>

          <n-form-item label="Email" path="email">
            <n-input
              v-model:value="formData.email"
              placeholder="Nhập email"
              :input-props="{ autocomplete: 'email' }"
              @input="formatEmail"
            />
          </n-form-item>

          <n-form-item label="Số điện thoại" path="phone">
            <n-input
              v-model:value="formData.phone"
              placeholder="Nhập số điện thoại"
              :input-props="{ autocomplete: 'tel' }"
              @input="formatPhoneNumber"
            />
          </n-form-item>

          <n-form-item label="Địa chỉ" path="address">
            <div class="space-y-3 w-full">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                <n-input
                  v-model:value="houseNumber"
                  placeholder="Số"
                  @input="updateAddress"
                />
                <n-input
                  v-model:value="streetName"
                  placeholder="Tên Đường"
                  @input="updateAddress"
                />
              </div>
              <div class="space-y-3">
                <n-select
                  v-model:value="selectedProvince"
                  :options="provinceOptions"
                  placeholder="Chọn tỉnh/thành phố"
                  @update:value="onProvinceChange"
                  clearable
                  filterable
                />
                <n-select
                  v-model:value="selectedDistrict"
                  :options="districtOptions"
                  placeholder="Chọn quận/huyện"
                  @update:value="onDistrictChange"
                  :disabled="!selectedProvince"
                  clearable
                  filterable
                />
                <n-select
                  v-model:value="selectedWard"
                  :options="wardOptions"
                  placeholder="Chọn phường/xã"
                  @update:value="onWardChange"
                  :disabled="!selectedDistrict"
                  clearable
                  filterable
                />
              </div>
              <n-input
                v-model:value="formData.address"
                type="textarea"
                placeholder="Địa chỉ sẽ được tự động điền khi chọn số nhà, tỉnh/quận/phường"
                :autosize="{ minRows: 3, maxRows: 5 }"
                :disabled="true"
                class="bg-gray-50"
                :input-props="{ style: 'word-wrap: break-word; overflow-wrap: break-word;' }"
              />
              <div class="text-sm text-gray-500">
                <svg class="w-4 h-4 inline mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Vui lòng điền thông tin cho chính xác
              </div>
            </div>
          </n-form-item>

          <n-form-item label="Hình ảnh cửa hàng" path="image">
            <div class="space-y-3 w-full">
              <div class="flex items-center space-x-3 w-full">
                <n-upload
                  ref="uploadRef"
                  :max="1"
                  :show-file-list="false"
                  accept="image/*"
                  @change="handleImageChange"
                  class="w-full"
                >
                  <n-button class="w-full">Chọn ảnh</n-button>
                </n-upload>
                <button
                  v-if="imagePreview"
                  type="button"
                  @click="removeImage"
                  class="px-3 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 transition-colors duration-200 text-sm whitespace-nowrap flex-shrink-0"
                >
                  Xóa ảnh
                </button>
              </div>
              <div v-if="imagePreview" class="mt-2 w-full">
                <n-image
                  :src="imagePreview"
                  width="100%"
                  height="200"
                  object-fit="cover"
                  class="rounded border border-gray-300 w-full"
                />
              </div>
            </div>
          </n-form-item>

          <div class="flex space-x-3 mt-6 w-full">
            <button
              type="submit"
              :disabled="isLoading"
              class="flex-1 bg-blue-600 text-white px-3 py-2 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200 font-medium"
            >
              <div v-if="isLoading" class="flex items-center justify-center">
                <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Đang đăng ký...
              </div>
              <span v-else>Đăng ký cửa hàng</span>
            </button>

            <button
              type="button"
              @click="resetForm"
              class="w-24 bg-white text-gray-700 px-3 py-2 rounded-lg border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors duration-200 font-medium flex items-center justify-center whitespace-nowrap flex-shrink-0"
            >
              <svg class="w-4 h-4 mr-1 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              <span class="text-xs">Làm mới</span>
            </button>
          </div>
        </n-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NForm, NFormItem, NInput, NSelect, NUpload, NButton, NImage, useMessage } from 'naive-ui'
import { getProvinces, getDistricts, getWards } from 'vietnam-provinces'
import axios from 'axios'

const router = useRouter()
const message = useMessage()

// Form data
const formData = reactive({
  shopName: '',
  description: '',
  email: '',
  phone: '',
  address: ''
})

// Address selection state
const houseNumber = ref('')
const streetName = ref('')
const selectedProvince = ref(null)
const selectedDistrict = ref(null)
const selectedWard = ref(null)

// Address data
const provinces = ref([])
const districts = ref([])
const wards = ref([])

// Image state
const image = ref(null)
const imagePreview = ref('')
const uploadRef = ref(null)

// Form ref
const formRef = ref(null)

// Loading state
const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Computed properties for address options
const provinceOptions = computed(() => {
  return provinces.value.map(province => ({
    label: province.name,
    value: province.code
  }))
})

const districtOptions = computed(() => {
  return districts.value.map(district => ({
    label: district.name,
    value: district.code
  }))
})

const wardOptions = computed(() => {
  return wards.value.map(ward => ({
    label: ward.name,
    value: ward.code
  }))
})

// Validation rules
const rules = {
  shopName: {
    required: true,
    message: 'Tên cửa hàng không được để trống',
    trigger: ['blur', 'input']
  },
  email: [
    {
      required: true,
      message: 'Email không được để trống',
      trigger: ['blur', 'input']
    },
    {
      validator: (rule, value) => {
        if (!value || value.trim() === '') {
          return true
        }
        if (!value.includes('@')) {
          return new Error('Email phải chứa ký tự @')
        }
        const parts = value.split('@')
        if (parts.length !== 2 || !parts[1]) {
          return new Error('Email không đúng định dạng')
        }
        if (!parts[1].includes('.')) {
          return new Error('Email phải có domain hợp lệ (VD: .com, .vn)')
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          return new Error('Email không đúng định dạng')
        }
        return true
      },
      trigger: ['blur', 'input']
    }
  ],
  phone: [
    {
      required: true,
      message: 'Số điện thoại không được để trống',
      trigger: ['blur', 'input']
    },
    {
      validator: (rule, value) => {
        if (!value || value.trim() === '') {
          return true
        }
        const cleanPhone = value.replace(/\s/g, '')
        if (!/^[0-9]+$/.test(cleanPhone)) {
          return new Error('Số điện thoại chỉ được chứa số')
        }
        if (cleanPhone.length < 10 || cleanPhone.length > 11) {
          return new Error('Số điện thoại phải có 10-11 số')
        }
        return true
      },
      trigger: ['blur', 'input']
    }
  ],
  address: {
    required: true,
    message: 'Địa chỉ không được để trống',
    trigger: 'blur',
    validator: (rule, value) => {
      if (!value || value === '' || value.trim() === '') {
        return new Error('Địa chỉ không được để trống')
      }
      const trimmedValue = value.trim()
      if (trimmedValue.length < 3) {
        return new Error('Địa chỉ phải có ít nhất 3 ký tự')
      }
    }
  }
}

// Address methods
const loadAddressData = () => {
  provinces.value = getProvinces()
}

const onProvinceChange = (provinceCode) => {
  selectedDistrict.value = null
  selectedWard.value = null
  districts.value = []
  wards.value = []

  if (provinceCode) {
    districts.value = getDistricts(provinceCode)
  }

  updateAddress()
}

const onDistrictChange = (districtCode) => {
  selectedWard.value = null
  wards.value = []

  if (districtCode) {
    wards.value = getWards(districtCode)
  }

  updateAddress()
}

const onWardChange = () => {
  updateAddress()
}

const updateAddress = () => {
  const addressParts = []

  if (houseNumber.value && houseNumber.value.trim()) {
    addressParts.push(`Số ${houseNumber.value.trim()}`)
  }

  if (streetName.value && streetName.value.trim()) {
    addressParts.push(`Đường ${streetName.value.trim()}`)
  }

  if (selectedWard.value) {
    const ward = wards.value.find(w => w.code === selectedWard.value)
    if (ward) addressParts.push(ward.name)
  }

  if (selectedDistrict.value) {
    const district = districts.value.find(d => d.code === selectedDistrict.value)
    if (district) addressParts.push(district.name)
  }

  if (selectedProvince.value) {
    const province = provinces.value.find(p => p.code === selectedProvince.value)
    if (province) addressParts.push(province.name)
  }

  formData.address = addressParts.join(', ')
}

const formatPhoneNumber = () => {
  formData.phone = formData.phone.replace(/\s/g, '').replace(/[^0-9]/g, '')
}

const formatEmail = () => {
  formData.email = formData.email.trim()
}

const resetForm = () => {
  // Reset form data
  formData.shopName = ''
  formData.description = ''
  formData.email = ''
  formData.phone = ''
  formData.address = ''

  // Reset address selection
  houseNumber.value = ''
  streetName.value = ''
  selectedProvince.value = null
  selectedDistrict.value = null
  selectedWard.value = null
  districts.value = []
  wards.value = []

  // Reset image
  image.value = null
  imagePreview.value = ''
  if (uploadRef.value) {
    uploadRef.value.clear()
  }

  // Clear form validation
  formRef.value?.restoreValidation()

  // Clear messages
  errorMessage.value = ''
  successMessage.value = ''
}

const handleImageChange = (options) => {
  const file = options.file
  if (file) {
    image.value = file.file
    imagePreview.value = URL.createObjectURL(file.file)
  }
}

const removeImage = () => {
  image.value = null
  imagePreview.value = ''
  if (uploadRef.value) {
    uploadRef.value.clear()
  }
}

const validateForm = async () => {
  try {
    await formRef.value?.validate()
    return true
  } catch {
    return false
  }
}

const registerShop = async () => {
  const isValid = await validateForm()
  if (!isValid) {
    return
  }

  isLoading.value = true
  const submitData = new FormData()
  submitData.append('shopName', formData.shopName.trim())
  submitData.append('description', formData.description.trim())
  submitData.append('email', formData.email.trim())
  submitData.append('phone', formData.phone.trim())
  submitData.append('address', formData.address.trim())

  if (image.value) {
    submitData.append('image', image.value)
  }

  try {
    const res = await axios.post('/api/register-shop', submitData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    if (res.data.message === 'Đăng ký cửa hàng thành công!') {
      successMessage.value = res.data.message
      message.success('Đăng ký cửa hàng thành công!')
      resetForm()

      // Redirect đến trang thông tin cửa hàng sau 2 giây
      setTimeout(() => {
        router.push('/user/shop/profile')
      }, 2000)
      return
    } else {
      errorMessage.value = res.data.message || 'Đăng ký cửa hàng thất bại!'
      successMessage.value = ''
    }
  } catch (err) {
    if (err.response?.data?.message) {
      errorMessage.value = err.response.data.message
    } else {
      errorMessage.value = 'Đăng ký thất bại!'
    }
    successMessage.value = ''
  } finally {
    isLoading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadAddressData()
})
</script>

<style scoped>
/* Custom styles for address dropdowns */
:deep(.n-select) {
  min-width: 200px;
  width: 100%;
}

:deep(.n-select .n-base-selection) {
  min-width: 200px;
  width: 100%;
}

:deep(.n-select .n-base-selection-label) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.n-select .n-base-selection-overflow) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.n-form-item) {
  width: 100%;
}

:deep(.n-input) {
  width: 100%;
}

:deep(.n-upload) {
  width: 100%;
}

@media (max-width: 768px) {
  :deep(.n-select) {
    min-width: 100%;
  }

  :deep(.n-select .n-base-selection) {
    min-width: 100%;
  }
}
</style>
