<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <div class="text-center">
          <!-- Loading State -->
          <div v-if="loading" class="text-center">
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-blue-100">
              <Loader2 class="animate-spin h-6 w-6 text-blue-600" />
            </div>
            <h2 class="mt-4 text-lg font-medium text-gray-900">Đang xác minh thanh toán...</h2>
            <p class="mt-2 text-sm text-gray-600">Vui lòng chờ trong giây lát.</p>
          </div>

          <!-- Success State -->
          <div v-else-if="paymentSuccess" class="text-center">
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100">
              <Check class="h-6 w-6 text-green-600" />
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
              <X class="h-6 w-6 text-red-600" />
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
import { useCart } from '../composables/useCart.js'
import axios from '../utils/axios'
import { Loader2, Check, X } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { clear } = useCart()

const loading = ref(true)
const paymentSuccess = ref(false)
const orderInfo = ref(null)
const errorMessage = ref('')

onMounted(async () => {
  try {
    // Lấy thông tin từ URL parameters
    const code = route.query.code
    const status = route.query.status
    const orderCode = route.query.orderCode
    const cancel = route.query.cancel

    console.log('Payment Success URL params:', route.query)

    // Kiểm tra nếu user hủy thanh toán
    if (cancel === 'true') {
      errorMessage.value = 'Bạn đã hủy thanh toán'
      loading.value = false
      return
    }

    // Kiểm tra status từ PayOS
    if (status === 'PAID' && code === '00') {
      // Thanh toán thành công
      paymentSuccess.value = true
      orderInfo.value = {
        orderId: orderCode,
        amount: 0, // Sẽ lấy từ order thật
        paymentMethod: 'PayOS',
      }

      // Verify payment với backend để cập nhật trạng thái
      try {
        const response = await axios.post('/api/payos/verify-payment', {
          orderCode: orderCode,
          transactionCode: 'PAYOS_SUCCESS',
        })

        if (response.data.success) {
          orderInfo.value.orderId = response.data.orderId

          // Lấy thông tin đơn hàng thật từ backend
          try {
            const orderResponse = await axios.get(`/api/orders/${response.data.orderId}`)
            if (orderResponse.data.success) {
              const order = orderResponse.data.data
              orderInfo.value.amount = order.totalAmount
              orderInfo.value.paymentMethod = order.paymentName || 'PayOS'
            }
          } catch (orderError) {
            console.error('Error fetching order details:', orderError)
            // Vẫn hiển thị thông tin cơ bản
          }

          message.success('Thanh toán thành công!')
        }
      } catch (verifyError) {
        console.error('Verify payment error:', verifyError)
        // Vẫn hiển thị thành công vì PayOS đã confirm
      }

      // Clear pending order code từ localStorage
      localStorage.removeItem('pendingOrderCode')

      // Clear cart sau khi thanh toán thành công
      clear()

    } else {
      // Thanh toán thất bại
      errorMessage.value = 'Thanh toán thất bại hoặc chưa hoàn tất'
    }
  } catch (error) {
    console.error('Error processing payment result:', error)
    errorMessage.value = 'Lỗi xử lý kết quả thanh toán'
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
