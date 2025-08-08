<template>
  <n-space vertical :size="24">
    <n-h2>Thông tin Cửa hàng</n-h2>

      <!-- Loading state -->
      <n-spin :show="isLoading" class="w-full">
        <!-- Shop info display -->
        <div v-if="shopInfo && !isEditing">
          <n-space vertical :size="24">
            <!-- Shop image card -->
            <n-card size="small">
              <div class="flex justify-center">
                <div class="relative">
                  <n-avatar
                    :size="120"
                    :src="shopInfo.shopImage || '/default-shop.png'"
                    fallback-src="/default-shop.png"
                    object-fit="cover"
                  />
                  <n-badge dot processing color="#52c41a" class="absolute bottom-2 right-2" />
                </div>
              </div>
            </n-card>

            <!-- Shop details -->
            <n-card title="Chi tiết cửa hàng" size="small">
              <n-descriptions :column="2" bordered>
                <n-descriptions-item label="Tên cửa hàng">
                  {{ shopInfo.shopName }}
                </n-descriptions-item>
                <n-descriptions-item label="Email">
                  {{ shopInfo.email }}
                </n-descriptions-item>
                <n-descriptions-item label="Số điện thoại">
                  {{ shopInfo.phone }}
                </n-descriptions-item>
                <n-descriptions-item label="Địa chỉ">
                  {{ shopInfo.address }}
                </n-descriptions-item>
                <n-descriptions-item label="Mô tả" :span="2">
                  {{ shopInfo.description || 'Chưa có mô tả' }}
                </n-descriptions-item>
              </n-descriptions>
            </n-card>

            <!-- Update info -->
            <n-alert type="info" title="Thông tin cập nhật" show-icon>
              <div>
                <p v-if="shopInfo.createdAt"><strong>Tạo lúc:</strong> {{ formatDateTime(shopInfo.createdAt) }}</p>
                <p v-if="shopInfo.lastUpdated">
                  <strong>Cập nhật lần cuối:</strong> {{ formatDateTime(shopInfo.lastUpdated) }}
                </p>
                <p v-else><strong>Trạng thái:</strong> Chưa từng cập nhật</p>
              </div>
            </n-alert>

            <!-- Update restriction info -->
            <n-alert v-if="!canUpdate" type="warning" title="Giới hạn cập nhật" show-icon>
              Bạn chỉ có thể cập nhật thông tin cửa hàng 1 lần trong 24 giờ. Vui lòng đợi thêm
              <strong>{{ formatTimeRemaining(hoursUntilNextUpdate) }}</strong> nữa để có thể cập nhật.
            </n-alert>

            <!-- Action buttons -->
            <n-space>
              <n-button type="primary" @click="startEditing" :disabled="!canUpdate">
                <template #icon>
                  <n-icon><Edit /></n-icon>
                </template>
                Chỉnh sửa thông tin
              </n-button>

              <n-button @click="refreshInfo">
                <template #icon>
                  <n-icon><RefreshCw /></n-icon>
                </template>
                Làm mới
              </n-button>
            </n-space>
          </n-space>
        </div>

        <!-- Edit form -->
        <div v-else-if="isEditing">
          <n-space vertical :size="24">
            <n-alert type="warning" title="Lưu ý quan trọng" show-icon>
              Bạn chỉ có thể cập nhật thông tin cửa hàng <strong>1 lần trong 24 giờ</strong>. Vui lòng kiểm tra kỹ thông
              tin trước khi lưu.
            </n-alert>

            <n-card title="Cập nhật thông tin cửa hàng" size="small">
              <n-form
                ref="formRef"
                :model="editForm"
                :rules="formRules"
                label-placement="top"
                @submit.prevent="updateShopInfo"
              >
                <n-grid :cols="2" :x-gap="24" :y-gap="16">
                  <n-form-item-gi label="Tên cửa hàng" path="shopName">
                    <n-input v-model:value="editForm.shopName" placeholder="Nhập tên cửa hàng" />
                  </n-form-item-gi>

                  <n-form-item-gi label="Email" path="email">
                    <n-input v-model:value="editForm.email" placeholder="Nhập email" />
                  </n-form-item-gi>

                  <n-form-item-gi label="Số điện thoại" path="phone">
                    <n-input v-model:value="editForm.phone" placeholder="Nhập số điện thoại" />
                  </n-form-item-gi>

                  <n-form-item-gi label="Địa chỉ" path="address">
                    <n-input v-model:value="editForm.address" placeholder="Nhập địa chỉ" />
                  </n-form-item-gi>

                  <n-form-item-gi label="Mô tả" :span="2" path="description">
                    <n-input
                      v-model:value="editForm.description"
                      type="textarea"
                      placeholder="Nhập mô tả cửa hàng"
                      :autosize="{ minRows: 3, maxRows: 6 }"
                    />
                  </n-form-item-gi>
                </n-grid>

                <n-space class="mt-6">
                  <n-button type="primary" attr-type="submit" :loading="isUpdating" :disabled="!isFormValid">
                    <template #icon>
                      <n-icon><Save /></n-icon>
                    </template>
                    Lưu thay đổi
                  </n-button>

                  <n-button @click="cancelEditing">
                    <template #icon>
                      <n-icon><X /></n-icon>
                    </template>
                    Hủy
                  </n-button>
                </n-space>
              </n-form>
            </n-card>
          </n-space>
        </div>

        <!-- Error state -->
        <n-result
          v-else-if="!isLoading && !shopInfo"
          status="error"
          title="Không thể tải thông tin"
          description="Có lỗi xảy ra khi tải thông tin cửa hàng"
        >
          <template #footer>
            <n-button @click="loadShopInfo"> Thử lại </n-button>
          </template>
        </n-result>
      </n-spin>
    </n-space>
  </template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '../../utils/axios'
import { Edit, RefreshCw, Save, X } from 'lucide-vue-next'
import {
  NH2,
  NSpace,
  NSpin,
  NCard,
  NAvatar,
  NBadge,
  NDescriptions,
  NDescriptionsItem,
  NAlert,
  NButton,
  NIcon,
  NForm,
  NFormItemGi,
  NGrid,
  NInput,
  NResult,
  useMessage,
} from 'naive-ui'

const message = useMessage()

// State
const isLoading = ref(true)
const isEditing = ref(false)
const isUpdating = ref(false)
const shopInfo = ref(null)
const canUpdate = ref(true)
const hoursUntilNextUpdate = ref(0)

// Form
const formRef = ref(null)
const editForm = ref({
  shopName: '',
  email: '',
  phone: '',
  address: '',
  description: '',
})

// Form validation rules
const formRules = {
  shopName: {
    required: true,
    message: 'Tên cửa hàng không được để trống',
    trigger: ['blur', 'input'],
  },
  email: [
    {
      required: true,
      message: 'Email không được để trống',
      trigger: ['blur', 'input'],
    },
    {
      type: 'email',
      message: 'Email không đúng định dạng',
      trigger: ['blur', 'input'],
    },
  ],
  phone: {
    required: true,
    message: 'Số điện thoại không được để trống',
    trigger: ['blur', 'input'],
  },
  address: {
    required: true,
    message: 'Địa chỉ không được để trống',
    trigger: ['blur', 'input'],
  },
}

// Computed
const isFormValid = computed(() => {
  return editForm.value.shopName && editForm.value.email && editForm.value.phone && editForm.value.address
})

// Methods
const loadShopInfo = async () => {
  isLoading.value = true
  try {
    const response = await axios.get('/api/user/shop')
    if (response.data.shop) {
      shopInfo.value = response.data.shop
      canUpdate.value = response.data.canUpdate
      hoursUntilNextUpdate.value = response.data.hoursUntilNextUpdate || 0
    } else {
      message.error(response.data.message || 'Không thể tải thông tin cửa hàng')
    }
  } catch (error) {
    message.error('Lỗi khi tải thông tin cửa hàng')
    console.error('Error:', error)
  } finally {
    isLoading.value = false
  }
}

const startEditing = () => {
  if (!canUpdate.value) {
    message.warning('Bạn chưa thể cập nhật thông tin lúc này')
    return
  }

  editForm.value = {
    shopName: shopInfo.value.shopName || '',
    email: shopInfo.value.email || '',
    phone: shopInfo.value.phone || '',
    address: shopInfo.value.address || '',
    description: shopInfo.value.description || '',
  }
  isEditing.value = true
}

const cancelEditing = () => {
  isEditing.value = false
  editForm.value = {
    shopName: '',
    email: '',
    phone: '',
    address: '',
    description: '',
  }
}

const updateShopInfo = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    isUpdating.value = true

    const response = await axios.put('/api/user/shop/update', editForm.value)

    if (response.data.shop) {
      shopInfo.value = response.data.shop
      canUpdate.value = response.data.canUpdate
      hoursUntilNextUpdate.value = response.data.hoursUntilNextUpdate || 24
      message.success('Cập nhật thông tin thành công!')
      isEditing.value = false
    } else {
      message.error(response.data.message || 'Có lỗi xảy ra')
    }
  } catch (error) {
    if (error.errors) {
      message.error('Vui lòng kiểm tra lại thông tin')
    } else {
      message.error('Không thể cập nhật thông tin')
      console.error('Error:', error)
    }
  } finally {
    isUpdating.value = false
  }
}

const refreshInfo = () => {
  loadShopInfo()
  message.info('Đã làm mới thông tin')
}

// Helper functions
const formatDateTime = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatTimeRemaining = (hours) => {
  if (hours <= 0) return '0 giờ'

  const wholeHours = Math.floor(hours)
  const minutes = Math.round((hours - wholeHours) * 60)

  if (wholeHours > 0 && minutes > 0) {
    return `${wholeHours} giờ ${minutes} phút`
  } else if (wholeHours > 0) {
    return `${wholeHours} giờ`
  } else {
    return `${minutes} phút`
  }
}

// Load data on mount
onMounted(() => {
  loadShopInfo()
})
</script>
