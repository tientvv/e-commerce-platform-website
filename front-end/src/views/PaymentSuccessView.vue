<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <div class="text-center">
          <!-- Loading State -->
          <div v-if="loading" class="text-center">
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-blue-100">
              <svg class="animate-spin h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path
                  class="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                ></path>
              </svg>
            </div>
            <h2 class="mt-4 text-lg font-medium text-gray-900">Đang xác minh thanh toán...</h2>
            <p class="mt-2 text-sm text-gray-600">Vui lòng chờ trong giây lát.</p>
          </div>

          <!-- Success State -->
          <div v-else-if="paymentSuccess" class="text-center">
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100">
              <svg class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 class="mt-4 text-lg font-medium text-gray-900">Thanh toán thành công!</h2>
            <p class="mt-2 text-sm text-gray-600">
              Cảm ơn bạn đã mua sắm. Đơn hàng của bạn đã được xác nhận và sẽ được giao hàng sớm.
            </p>

            <!-- Order Info -->
            <div v-if="orderInfo" class="mt-6 bg-gray-50 rounded-lg p-4">
              <h3 class="text-sm font-medium text-gray-900 mb-2">Thông tin đơn hàng</h3>
              <div class="text-sm text-gray-600 space-y-1">
                <p><strong>Mã đơn hàng:</strong> {{ orderInfo.orderId }}</p>
                <p><strong>Tổng tiền:</strong> {{ formatPrice(orderInfo.amount) }}</p>
                <p><strong>Phương thức:</strong> {{ orderInfo.paymentMethod }}</p>
              </div>
            </div>

            <!-- Actions -->
            <div class="mt-6 space-y-3">
              <button
                @click="viewOrder"
                class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                Xem chi tiết đơn hàng
              </button>
              <button
                @click="goHome"
                class="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                Về trang chủ
              </button>
            </div>
          </div>

          <!-- Error State -->
          <div v-else class="text-center">
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
              <svg class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 class="mt-4 text-lg font-medium text-gray-900">Thanh toán thất bại</h2>
            <p class="mt-2 text-sm text-gray-600">{{ errorMessage || 'Có lỗi xảy ra trong quá trình thanh toán.' }}</p>

            <!-- Actions -->
            <div class="mt-6 space-y-3">
              <button
                @click="retryPayment"
                class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                Thử lại
              </button>
              <button
                @click="goHome"
                class="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                Về trang chủ
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { clearCart } from '../utils/cart.js'
import axios from '../utils/axios'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const loading = ref(true)
const paymentSuccess = ref(false)
const orderInfo = ref(null)
const errorMessage = ref('')

onMounted(async () => {
  try {
    // Lấy thông tin từ URL parameters
    const orderCode = route.query.orderCode
    const transactionCode = route.query.transactionCode
    const amount = route.query.amount

    if (!orderCode) {
      errorMessage.value = 'Không tìm thấy thông tin đơn hàng'
      loading.value = false
      return
    }

    // Verify payment với backend
    const response = await axios.post('/api/payos/verify-payment', {
      orderCode: orderCode,
      transactionCode: transactionCode || 'TEST_TRANSACTION',
    })

    if (response.data.success) {
      // Thanh toán thành công, tạo đơn hàng thật
      paymentSuccess.value = true
      orderInfo.value = {
        orderId: response.data.orderId,
        amount: amount ? parseFloat(amount) : 0,
        paymentMethod: 'PayOS',
      }

      // Clear pending order code và zpTransToken từ localStorage
      localStorage.removeItem('pendingOrderCode')

      // Clear cart sau khi thanh toán thành công
      clearCart()

      message.success('Thanh toán thành công!')
    } else {
      errorMessage.value = response.data.message || 'Thanh toán thất bại'
    }
  } catch (error) {
    console.error('Error verifying payment:', error)
    errorMessage.value = error.response?.data?.message || 'Lỗi xác minh thanh toán'
  } finally {
    loading.value = false
  }
})

const viewOrder = () => {
  if (orderInfo.value?.orderId) {
    router.push(`/order/${orderInfo.value.orderId}`)
  }
}

const goHome = () => {
  router.push('/')
}

const retryPayment = () => {
  // Redirect về trang giỏ hàng để thử lại
  router.push('/cart')
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}
</script>
