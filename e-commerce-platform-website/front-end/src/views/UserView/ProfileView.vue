<template>
  <div class="max-w-4xl mx-auto p-6">
    <div class="bg-white rounded-lg shadow-lg overflow-hidden">
      <!-- Header -->
      <div class="bg-gradient-to-r from-blue-600 to-purple-600 px-6 py-4">
        <h1 class="text-2xl font-bold text-white">Thông tin tài khoản</h1>
        <p class="text-blue-100 mt-1">Quản lý thông tin cá nhân của bạn</p>
      </div>

      <div class="p-6">
        <!-- Loading State -->
        <div v-if="loading" class="flex justify-center items-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">Có lỗi xảy ra</h3>
              <div class="mt-2 text-sm text-red-700">{{ error }}</div>
            </div>
          </div>
        </div>



                        <!-- Profile Update Required Message -->
        <div v-if="(needsProfileUpdate && !loading && userInfo?.role === 'USER') || route.query.message === 'update_required'" class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-blue-800">Cập nhật thông tin bắt buộc</h3>
              <div class="mt-2 text-sm text-blue-700">
                <p class="font-medium">Vui lòng cập nhật đầy đủ thông tin trước khi sử dụng các tính năng.</p>
                <p class="mt-1">Các thông tin bắt buộc: Tên đăng nhập, Họ tên, Email, Số điện thoại, Địa chỉ</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Admin Info Message -->
        <div v-if="userInfo?.role === 'ADMIN' && !loading" class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
  <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-blue-800">👨‍💼 Quản trị viên</h3>
              <div class="mt-2 text-sm text-blue-700">
                <p>Bạn đang đăng nhập với quyền Quản trị viên. Cập nhật thông tin là tùy chọn.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Content -->
        <div v-if="!loading && !error" class="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <!-- Thông tin tài khoản -->
          <div class="space-y-6">
            <div class="pb-4">
              <h2 class="text-xl font-semibold text-gray-900 mb-4">Thông tin cá nhân</h2>

              <!-- Avatar Section -->
              <div class="flex items-center space-x-4 mb-6">
                <div class="relative group">
                  <!-- Avatar Image or Placeholder -->
                  <div class="w-20 h-20 rounded-full overflow-hidden border-4 border-white shadow-lg">
                    <img
                      v-if="userInfo?.accountsImage"
                      :src="userInfo.accountsImage"
                      :alt="userInfo?.name || 'Avatar'"
                      class="w-full h-full object-cover"
                      @error="handleImageError"
                    />
                    <div
                      v-else
                      class="w-full h-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white text-2xl font-bold"
                    >
                      {{ userInfo?.name?.charAt(0)?.toUpperCase() || 'U' }}
                    </div>
                  </div>

                  <!-- Online Status -->
                  <div class="absolute -bottom-1 -right-1 w-6 h-6 bg-green-400 border-2 border-white rounded-full"></div>

                  <!-- Upload Overlay -->
                  <div class="absolute inset-0 bg-black bg-opacity-50 rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-200 flex items-center justify-center cursor-pointer" @click="openImageUpload">
                    <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  </div>
                </div>

                <div class="flex-1">
                  <h3 class="text-lg font-medium text-gray-900">{{ userInfo?.name || 'Chưa có tên' }}</h3>
                  <p class="text-sm text-gray-500">{{ userInfo?.role === 'ADMIN' ? 'Quản trị viên' : 'Người dùng' }}</p>
                  <button
                    @click="openImageUpload"
                    class="mt-2 text-sm text-blue-600 hover:text-blue-700 font-medium transition-colors"
                  >
                    Cập nhật ảnh đại diện
                  </button>
                </div>
              </div>

              <!-- Info Cards -->
              <div class="space-y-4">
                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Tên đăng nhập</p>
                      <p class="text-sm text-gray-900">{{ userInfo?.username }}</p>
                    </div>
                  </div>
                </div>

                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Email</p>
                      <p class="text-sm text-gray-900">{{ userInfo?.email }}</p>
                    </div>
                  </div>
                </div>

                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Số điện thoại</p>
                      <p class="text-sm text-gray-900">{{ userInfo?.phone || 'Chưa cập nhật' }}</p>
                    </div>
                  </div>
                </div>

                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Địa chỉ</p>
                      <p class="text-sm text-gray-900">{{ userInfo?.address || 'Chưa cập nhật' }}</p>
                    </div>
                  </div>
                </div>

                <div class="bg-gray-50 rounded-lg p-4">
                  <div class="flex items-center space-x-3">
                    <div class="flex-shrink-0">
                      <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-500">Ngày tạo tài khoản</p>
                      <p class="text-sm text-gray-900">{{ formatDate(userInfo?.createdAt) }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Form chỉnh sửa -->
          <div class="space-y-6">
            <div class="pb-4">
              <h2 class="text-xl font-semibold text-gray-900 mb-4">Chỉnh sửa thông tin</h2>

                            <n-form ref="formRef" :model="formData" :rules="rules" @submit.prevent="updateProfile">
                <n-form-item label="Tên đăng nhập" path="username">
                  <n-input
                    v-model:value="formData.username"
                    placeholder="Tên đăng nhập"
                    disabled
                    :input-props="{ autocomplete: 'username' }"
                  />
                </n-form-item>

                <n-form-item label="Họ và tên" path="name">
                  <n-input
                    v-model:value="formData.name"
                    placeholder="Nhập họ và tên"
                    :input-props="{ autocomplete: 'name' }"
                  />
                </n-form-item>

                <n-form-item label="Email" path="email">
                  <n-input
                    v-model:value="formData.email"
                    placeholder="Nhập email"
                    :input-props="{ autocomplete: 'email' }"
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
                  <div class="space-y-3">
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
                    />
                    <div class="text-sm text-gray-500">
                      <svg class="w-4 h-4 inline mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      Vui lòng điền thông tin cho chính xác
                    </div>
                  </div>
                </n-form-item>

                <div class="flex space-x-3 mt-6">
                  <button
                    type="submit"
                    :disabled="updating"
                    class="flex-1 bg-blue-600 text-white px-3 py-2 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200 font-medium"
                  >
                    <div v-if="updating" class="flex items-center justify-center">
                      <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Đang cập nhật...
                    </div>
                    <span v-else>Cập nhật thông tin</span>
                  </button>

                  <button
                    type="button"
                    @click="resetForm"
                    class="w-24 bg-white text-gray-700 px-3 py-2 rounded-lg border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors duration-200 font-medium flex items-center justify-center whitespace-nowrap"
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
      </div>
    </div>

    <!-- Image Upload Modal -->
    <n-modal v-model:show="showImageUpload" preset="card" style="width: 500px" title="Cập nhật ảnh đại diện">
      <div class="space-y-4">
        <!-- Current Image Preview -->
        <div v-if="userInfo?.accountsImage || imagePreview" class="flex justify-center">
          <div class="relative">
            <img
              :src="imagePreview || userInfo?.accountsImage"
              alt="Avatar preview"
              class="w-32 h-32 rounded-full object-cover border-4 border-gray-200"
            />
            <button
              v-if="imagePreview || userInfo?.accountsImage"
              @click="removeImage"
              class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 transition-colors"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Upload Area -->
        <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors">
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            class="hidden"
            @change="handleFileChange"
          />
          <div class="space-y-2">
            <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
              <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <div class="text-sm text-gray-600">
              <label for="file-upload" class="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-blue-500">
                <span>Tải lên ảnh</span>
                <input id="file-upload" name="file-upload" type="file" class="sr-only" @change="handleFileChange" />
              </label>
              <p class="pl-1">hoặc kéo thả vào đây</p>
            </div>
            <p class="text-xs text-gray-500">PNG, JPG, GIF tối đa 5MB</p>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="imageError" class="text-red-600 text-sm text-center">
          {{ imageError }}
        </div>

        <!-- Action Buttons -->
        <div class="flex justify-end space-x-3 pt-4">
          <button
            @click="closeImageUpload"
            class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Hủy
          </button>
          <button
            @click="uploadImage"
            :disabled="!imageFile || uploading"
            class="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ uploading ? 'Đang tải lên...' : 'Cập nhật' }}
          </button>
        </div>
      </div>
    </n-modal>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute } from 'vue-router'
import axios from '../../utils/axios'
import { NForm, NFormItem, NInput, NSelect, NModal, useMessage } from 'naive-ui'
import { getProvinces, getDistricts, getWards } from 'vietnam-provinces'

// Reactive data
const loading = ref(true)
const error = ref('')
const updating = ref(false)
const userInfo = ref(null)
const message = useMessage()
const needsProfileUpdate = ref(false)
const route = useRoute()

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

const formData = reactive({
  username: '',
  name: '',
  email: '',
  phone: '',
  address: ''
})

// Form ref
const formRef = ref(null)

// Image upload state
const showImageUpload = ref(false)
const imageFile = ref(null)
const imagePreview = ref('')
const imageError = ref('')
const uploading = ref(false)
const fileInput = ref(null)

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

const rules = {
  username: {
    required: true,
    message: 'Tên đăng nhập không được để trống',
    trigger: ['blur', 'input']
  },
  name: {
    required: true,
    message: 'Họ và tên không được để trống',
    trigger: ['blur', 'input'],
    validator: (rule, value) => {
      if (!value || value.trim() === '') {
        return new Error('Họ và tên không được để trống')
      }
      if (value.trim().length < 2) {
        return new Error('Họ và tên phải có ít nhất 2 ký tự')
      }
    }
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
          return true // Để required rule xử lý
        }

        if (!value.includes('@')) {
          return new Error('Email phải chứa ký tự @')
        }

        const parts = value.split('@')
        if (parts.length !== 2 || !parts[1]) {
          return new Error('Email không đúng định dạng')
        }

        // Kiểm tra có dấu chấm trong domain không
        if (!parts[1].includes('.')) {
          return new Error('Email phải có domain hợp lệ (VD: .com, .vn)')
        }

        // Kiểm tra regex tổng thể
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
          return true // Để required rule xử lý
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
      // Kiểm tra nếu value là null, undefined, hoặc empty string
      if (!value || value === '' || value.trim() === '') {
        return new Error('Địa chỉ không được để trống')
      }
      // Kiểm tra độ dài sau khi trim - giảm xuống 3 ký tự
      const trimmedValue = value.trim()
      if (trimmedValue.length < 3) {
        return new Error('Địa chỉ phải có ít nhất 3 ký tự')
      }
      // Nếu tất cả đều OK, không return gì (undefined = valid)
    }
  }
}

// Methods
const fetchUserInfo = async () => {
  try {
    loading.value = true
    error.value = ''
    const response = await axios.get('/api/info-account')
    userInfo.value = response.data.account

    // Populate form data
    formData.username = userInfo.value.username || ''
    formData.name = userInfo.value.name || ''
    formData.email = userInfo.value.email || ''
    formData.phone = userInfo.value.phone || ''
    formData.address = userInfo.value.address || ''

    // Load address data
    loadAddressData()

  } catch (err) {
    error.value = 'Không thể tải thông tin tài khoản. Vui lòng thử lại.'
    console.error('Error fetching user info:', err)
  } finally {
    loading.value = false
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
  // Bỏ tất cả khoảng trắng và ký tự không phải số
  formData.phone = formData.phone.replace(/\s/g, '').replace(/[^0-9]/g, '')
}

const validateForm = async () => {
  try {
    await formRef.value?.validate()
    return true
  } catch {
    return false
  }
}

const updateProfile = async () => {
  const isValid = await validateForm()
  if (!isValid) {
    return
  }

  try {
    updating.value = true
    error.value = ''

    const response = await axios.put('/api/account/update', {
      username: formData.username.trim(),
      name: formData.name.trim(),
      email: formData.email.trim(),
      phone: formData.phone.trim(),
      address: formData.address.trim()
    })

    if (response.data.success) {
      // Hiển thị thông báo thành công ở góc dưới bên phải
      message.success('Cập nhật thông tin thành công!')

      // Refresh user info
      await fetchUserInfo()
    } else {
      error.value = response.data.message || 'Có lỗi xảy ra khi cập nhật thông tin'
    }
  } catch (err) {
    if (err.response?.data?.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'Có lỗi xảy ra khi cập nhật thông tin. Vui lòng thử lại.'
    }
    console.error('Error updating profile:', err)
  } finally {
    updating.value = false
  }
}

const resetForm = () => {
  // Reset form to original values
  formData.username = userInfo.value?.username || ''
  formData.name = userInfo.value?.name || ''
  formData.email = userInfo.value?.email || ''
  formData.phone = userInfo.value?.phone || ''
  formData.address = userInfo.value?.address || ''

  // Reset address selection
  houseNumber.value = ''
  streetName.value = ''
  selectedProvince.value = null
  selectedDistrict.value = null
  selectedWard.value = null
  districts.value = []
  wards.value = []

  // Clear form validation
  formRef.value?.restoreValidation()

  // Clear messages
  error.value = ''
}

const formatDate = (dateString) => {
  if (!dateString) return 'Chưa có thông tin'

  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return 'Chưa có thông tin'
  }
}

// Image upload functions
const openImageUpload = () => {
  showImageUpload.value = true
  imageError.value = ''
}

const closeImageUpload = () => {
  showImageUpload.value = false
  imageFile.value = null
  imagePreview.value = ''
  imageError.value = ''
}

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // Validate file type
  if (!file.type.startsWith('image/')) {
    imageError.value = 'Vui lòng chọn file ảnh hợp lệ'
    return
  }

  // Validate file size (5MB)
  if (file.size > 5 * 1024 * 1024) {
    imageError.value = 'Kích thước file không được vượt quá 5MB'
    return
  }

  imageFile.value = file
  imageError.value = ''

  // Create preview
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

const removeImage = () => {
  imageFile.value = null
  imagePreview.value = ''
  imageError.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const handleImageError = (event) => {
  event.target.style.display = 'none'
  event.target.nextElementSibling.style.display = 'flex'
}

const uploadImage = async () => {
  if (!imageFile.value) return

  try {
    uploading.value = true
    imageError.value = ''

    const formData = new FormData()
    formData.append('profileImage', imageFile.value)

    const response = await axios.put('/api/account/update-profile-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.success) {
      message.success('Cập nhật ảnh đại diện thành công!')
      await fetchUserInfo()
      closeImageUpload()
    } else {
      imageError.value = response.data.message || 'Có lỗi xảy ra khi cập nhật ảnh'
    }
  } catch (err) {
    console.error('Error uploading image:', err)
    imageError.value = err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật ảnh'
  } finally {
    uploading.value = false
  }
}

// Lifecycle
onMounted(() => {
  fetchUserInfo()
})




</script>

<style scoped>
/* Custom styles for address dropdowns */
:deep(.n-select) {
  min-width: 200px;
}

:deep(.n-select .n-base-selection) {
  min-width: 200px;
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

@media (max-width: 768px) {
  :deep(.n-select) {
    min-width: 100%;
  }

  :deep(.n-select .n-base-selection) {
    min-width: 100%;
  }
}
</style>
