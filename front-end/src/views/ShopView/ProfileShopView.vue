<template>
  <div class="mt-2">
    <h2 class="text-lg font-semibold mb-4">Thông tin Cửa hàng</h2>

    <!-- Loading state -->
    <div v-if="isLoading" class="text-center py-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-2 text-gray-500">Đang tải thông tin...</p>
    </div>

    <!-- Shop info display -->
    <div v-else-if="shopInfo && !isEditing" class="space-y-6">
      <!-- Shop image -->
      <div class="flex justify-center mb-6">
        <div class="relative">
          <img
            :src="shopInfo.shopImage || '/default-shop.png'"
            :alt="shopInfo.shopName"
            class="w-32 h-32 object-cover rounded-full border-4 border-gray-200"
          />
          <div
            class="absolute bottom-0 right-0 bg-green-500 w-6 h-6 rounded-full border-2 border-white flex items-center justify-center"
          >
            <CheckCircle class="w-3 h-3 text-white" />
          </div>
        </div>
      </div>

      <!-- Shop details -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Tên cửa hàng</label>
            <div class="p-3 bg-gray-50 rounded border">{{ shopInfo.shopName }}</div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <div class="p-3 bg-gray-50 rounded border">{{ shopInfo.email }}</div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Số điện thoại</label>
            <div class="p-3 bg-gray-50 rounded border">{{ shopInfo.phone }}</div>
          </div>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Địa chỉ</label>
            <div class="p-3 bg-gray-50 rounded border">{{ shopInfo.address }}</div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
            <div class="p-3 bg-gray-50 rounded border min-h-[80px]">
              {{ shopInfo.description || 'Chưa có mô tả' }}
            </div>
          </div>
        </div>
      </div>

      <!-- Update info -->
      <div class="bg-blue-50 border border-blue-200 rounded p-4">
        <div class="flex items-start gap-3">
          <Info class="w-5 h-5 text-blue-600 mt-0.5" />
          <div class="flex-1">
            <h4 class="font-medium text-blue-800">Thông tin cập nhật</h4>
            <p class="text-sm text-blue-600 mt-1">
              <span v-if="shopInfo.createdAt"> Tạo lúc: {{ formatDateTime(shopInfo.createdAt) }} </span>
            </p>
            <p class="text-sm text-blue-600" v-if="shopInfo.lastUpdated">
              Cập nhật lần cuối: {{ formatDateTime(shopInfo.lastUpdated) }}
            </p>
            <p class="text-sm text-blue-600" v-else>Chưa từng cập nhật</p>
          </div>
        </div>
      </div>

      <!-- Update restriction info -->
      <div v-if="!canUpdate" class="bg-yellow-50 border border-yellow-200 rounded p-4">
        <div class="flex items-start gap-3">
          <Clock class="w-5 h-5 text-yellow-600 mt-0.5" />
          <div>
            <h4 class="font-medium text-yellow-800">Giới hạn cập nhật</h4>
            <p class="text-sm text-yellow-600 mt-1">
              Bạn chỉ có thể cập nhật thông tin cửa hàng 1 lần trong 24 giờ.
              <br />
              Vui lòng đợi thêm <strong>{{ formatTimeRemaining(hoursUntilNextUpdate) }}</strong> nữa để có thể cập nhật.
            </p>
          </div>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="flex gap-4">
        <button
          @click="startEditing"
          :disabled="!canUpdate"
          :class="[
            'px-6 py-2 rounded border transition-colors',
            canUpdate
              ? 'border-blue-600 text-blue-600 hover:bg-blue-600 hover:text-white'
              : 'border-gray-300 text-gray-400 cursor-not-allowed',
          ]"
        >
          <Edit class="w-4 h-4 inline mr-2" />
          Chỉnh sửa thông tin
        </button>

        <button @click="refreshInfo" class="px-6 py-2 rounded border border-gray-300 text-gray-600 hover:bg-gray-50">
          <RefreshCw class="w-4 h-4 inline mr-2" />
          Làm mới
        </button>
      </div>
    </div>

    <!-- Edit form -->
    <div v-else-if="isEditing" class="space-y-6">
      <div class="bg-blue-50 border border-blue-200 rounded p-4 mb-6">
        <div class="flex items-start gap-3">
          <AlertCircle class="w-5 h-5 text-blue-600 mt-0.5" />
          <div>
            <h4 class="font-medium text-blue-800">Lưu ý quan trọng</h4>
            <p class="text-sm text-blue-600 mt-1">
              Bạn chỉ có thể cập nhật thông tin cửa hàng <strong>1 lần trong 24 giờ</strong>. Vui lòng kiểm tra kỹ thông
              tin trước khi lưu.
            </p>
          </div>
        </div>
      </div>

      <form @submit.prevent="updateShopInfo">
        <!-- Success/Error messages -->
        <div v-if="successMessage" class="mb-4 border border-green-500 text-green-500 py-3 px-4 rounded">
          {{ successMessage }}
        </div>
        <div v-if="errorMessage" class="mb-4 border border-red-500 text-red-500 py-3 px-4 rounded">
          {{ errorMessage }}
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Tên cửa hàng <span class="text-red-500">*</span>
              </label>
              <input
                v-model="editForm.shopName"
                type="text"
                required
                class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
                placeholder="Nhập tên cửa hàng"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Email <span class="text-red-500">*</span>
              </label>
              <input
                v-model="editForm.email"
                type="email"
                required
                class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
                placeholder="Nhập email cửa hàng"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Số điện thoại <span class="text-red-500">*</span>
              </label>
              <input
                v-model="editForm.phone"
                type="tel"
                required
                class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
                placeholder="Nhập số điện thoại"
              />
            </div>
          </div>

          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Địa chỉ <span class="text-red-500">*</span>
              </label>
              <input
                v-model="editForm.address"
                type="text"
                required
                class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
                placeholder="Nhập địa chỉ cửa hàng"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
              <textarea
                v-model="editForm.description"
                rows="4"
                class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
                placeholder="Nhập mô tả về cửa hàng (tùy chọn)"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- Action buttons -->
        <div class="flex gap-4 mt-8">
          <button
            type="submit"
            :disabled="isUpdating"
            class="px-6 py-2 rounded border border-green-600 text-green-600 hover:bg-green-600 hover:text-white disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Save class="w-4 h-4 inline mr-2" />
            <span v-if="isUpdating">Đang lưu...</span>
            <span v-else>Lưu thay đổi</span>
          </button>

          <button
            type="button"
            @click="cancelEditing"
            :disabled="isUpdating"
            class="px-6 py-2 rounded border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-50"
          >
            <X class="w-4 h-4 inline mr-2" />
            Hủy bỏ
          </button>
        </div>
      </form>
    </div>

    <!-- No shop found -->
    <div v-else class="text-center py-12">
      <Store class="w-16 h-16 text-gray-300 mx-auto mb-4" />
      <h3 class="text-lg font-medium text-gray-900 mb-2">Chưa có thông tin cửa hàng</h3>
      <p class="text-gray-500 mb-4">Bạn chưa đăng ký cửa hàng hoặc cửa hàng chưa được kích hoạt.</p>
      <RouterLink
        to="/register-shop"
        class="inline-flex items-center px-4 py-2 border border-blue-600 text-blue-600 rounded hover:bg-blue-600 hover:text-white"
      >
        <Plus class="w-4 h-4 mr-2" />
        Đăng ký cửa hàng
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { CheckCircle, Info, Clock, Edit, RefreshCw, AlertCircle, Save, X, Store, Plus } from 'lucide-vue-next'
import axios from 'axios'

const isLoading = ref(true)
const isEditing = ref(false)
const isUpdating = ref(false)
const shopInfo = ref(null)
const canUpdate = ref(true)
const hoursUntilNextUpdate = ref(0)
const successMessage = ref('')
const errorMessage = ref('')
let refreshTimer = null

const editForm = ref({
  shopName: '',
  email: '',
  phone: '',
  address: '',
  description: '',
})

onMounted(() => {
  loadShopInfo()
  // Tự động làm mới thông tin mỗi phút nếu không thể cập nhật
  refreshTimer = setInterval(() => {
    if (!canUpdate.value && !isEditing.value) {
      loadShopInfo()
    }
  }, 60000) // 1 phút
})

// Cleanup timer khi component bị destroy
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

const loadShopInfo = async () => {
  isLoading.value = true
  try {
    const response = await axios.get('/api/user/shop')

    if (response.data.shop) {
      shopInfo.value = response.data.shop
      canUpdate.value = response.data.canUpdate
      hoursUntilNextUpdate.value = response.data.hoursUntilNextUpdate
    } else {
      console.log('No shop found:', response.data.message)
    }
  } catch (error) {
    console.error('Lỗi khi tải thông tin cửa hàng:', error)
    errorMessage.value = 'Có lỗi xảy ra khi tải thông tin cửa hàng'
  } finally {
    isLoading.value = false
  }
}

const startEditing = () => {
  if (!canUpdate.value) return

  isEditing.value = true
  editForm.value = {
    shopName: shopInfo.value.shopName || '',
    email: shopInfo.value.email || '',
    phone: shopInfo.value.phone || '',
    address: shopInfo.value.address || '',
    description: shopInfo.value.description || '',
  }

  // Clear messages
  successMessage.value = ''
  errorMessage.value = ''
}

const cancelEditing = () => {
  isEditing.value = false
  successMessage.value = ''
  errorMessage.value = ''
}

const updateShopInfo = async () => {
  if (isUpdating.value) return

  // Validation cơ bản
  if (!editForm.value.shopName.trim()) {
    errorMessage.value = 'Tên cửa hàng không được để trống!'
    return
  }
  if (!editForm.value.email.trim()) {
    errorMessage.value = 'Email không được để trống!'
    return
  }
  if (!editForm.value.phone.trim()) {
    errorMessage.value = 'Số điện thoại không được để trống!'
    return
  }
  if (!editForm.value.address.trim()) {
    errorMessage.value = 'Địa chỉ không được để trống!'
    return
  }

  isUpdating.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const response = await axios.put('/api/user/shop/update', editForm.value)

    if (response.data.shop) {
      successMessage.value = response.data.message
      shopInfo.value = response.data.shop
      canUpdate.value = response.data.canUpdate
      hoursUntilNextUpdate.value = response.data.hoursUntilNextUpdate

      // Auto close edit form after 2 seconds
      setTimeout(() => {
        isEditing.value = false
        successMessage.value = ''
      }, 2000)
    } else {
      errorMessage.value = response.data.message || 'Có lỗi xảy ra khi cập nhật'
    }
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật thông tin'
    console.error('Update error:', error)
  } finally {
    isUpdating.value = false
  }
}

const refreshInfo = async () => {
  await loadShopInfo()
}

const formatDateTime = (dateString) => {
  if (!dateString) return 'Không xác định'

  try {
    const date = new Date(dateString)
    return date.toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })
  } catch {
    return 'Không xác định'
  }
}

const formatTimeRemaining = (hours) => {
  if (hours <= 0) return '0 giờ'

  if (hours >= 1) {
    const wholeHours = Math.floor(hours)
    const minutes = Math.round((hours - wholeHours) * 60)

    if (minutes === 0) {
      return `${wholeHours} giờ`
    } else {
      return `${wholeHours} giờ ${minutes} phút`
    }
  } else {
    const minutes = Math.round(hours * 60)
    return `${minutes} phút`
  }
}
</script>
