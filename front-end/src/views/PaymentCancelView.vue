<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <div class="text-center">
          <!-- Cancel Icon -->
          <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
            <svg class="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>

          <h2 class="mt-4 text-lg font-medium text-gray-900">Thanh toán bị hủy</h2>
          <p class="mt-2 text-sm text-gray-600">
            Bạn đã hủy quá trình thanh toán. Đơn hàng vẫn được lưu trong hệ thống.
          </p>

          <!-- Order Info -->
          <div v-if="orderInfo" class="mt-6 bg-gray-50 rounded-lg p-4">
            <h3 class="text-sm font-medium text-gray-900 mb-2">Thông tin đơn hàng tạm thời</h3>
            <div class="text-sm text-gray-600 space-y-1">
              <p><strong>Mã đơn hàng:</strong> {{ orderInfo.orderCode }}</p>
              <p><strong>Tổng tiền:</strong> {{ formatPrice(orderInfo.amount) }}</p>
                            <p class="text-yellow-600"><em>Đơn hàng chưa được tạo vì thanh toán chưa hoàn tất</em></p>
            </div>
          </div>

          <!-- Actions -->
          <div class="mt-6 space-y-3">
            <button
              @click="retryPayment"
              class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Thử thanh toán lại
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import axios from '../utils/axios'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const orderInfo = ref(null)

onMounted(async () => {
  // Lấy thông tin từ URL parameters
  const orderCode = route.query.orderCode
  const amount = route.query.amount
  const status = route.query.status
  const cancel = route.query.cancel

  console.log('Payment cancel page - URL params:', {
    orderCode,
    amount,
    status,
    cancel
  })

  if (orderCode) {
    // Tạm thời sử dụng amount từ URL hoặc localStorage
    let orderAmount = 0

    // Thử lấy từ URL parameter
    if (amount) {
      orderAmount = parseFloat(amount)
    } else {
      // Thử lấy từ localStorage (nếu đã lưu khi tạo payment)
      const savedAmount = localStorage.getItem('pendingOrderAmount')
      if (savedAmount) {
        orderAmount = parseFloat(savedAmount)
      }
    }

    orderInfo.value = {
      orderCode: orderCode,
      amount: orderAmount,
    }

    // Chỉ gọi forceCancelOrder một lần duy nhất
    console.log('Cancelling pending orders...')
    await forceCancelOrder()
  }

    // Clear pending order code từ localStorage
  localStorage.removeItem('pendingOrderCode')
})



const forceCancelOrder = async () => {
  try {
    const orderCode = route.query.orderCode
    console.log('🔍 Attempting to cancel order with orderCode:', orderCode)

    if (!orderCode) {
      console.error('❌ No orderCode found in URL parameters')
      message.error('Không tìm thấy mã đơn hàng')
      return
    }

    console.log('📞 Calling cancel API with orderCode:', orderCode)

    // Cancel đơn hàng hiện tại
    const response = await axios.post('/api/payos/cancel-current-order', null, {
      params: { orderCode: orderCode }
    })

    console.log('📥 API Response:', response.data)

    if (response.data.success) {
      console.log(`✅ Đã hủy đơn hàng: ${orderCode}`)
      message.success('Đã hủy đơn hàng thành công')
    } else {
      console.error('❌ API Error:', response.data.message)
      message.error(response.data.message || 'Lỗi khi hủy đơn hàng')
    }
  } catch (error) {
    console.error('💥 Error cancelling order:', error)
    console.error('💥 Error response:', error.response?.data)
    message.error('Lỗi khi hủy đơn hàng: ' + (error.response?.data?.message || error.message))
  }
}

const retryPayment = () => {
  // Redirect back to cart to retry payment
  router.push('/cart')
}



const goHome = () => {
  router.push('/')
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}


</script>
