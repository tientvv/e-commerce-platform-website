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
              <h3 class="text-sm font-medium text-gray-900 mb-2">
                {{ orderInfo.multipleOrders ? 'Thông tin đơn hàng' : 'Thông tin đơn hàng' }}
              </h3>
              <div class="text-sm text-gray-600 space-y-1">
                <p v-if="orderInfo.multipleOrders">
                  <strong>Tổng đơn hàng:</strong> {{ orderInfo.totalOrders }}
                </p>
                <p v-else>
                  <strong>Mã đơn hàng:</strong> {{ orderInfo.orderId }}
                </p>
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

      // Kiểm tra xem có nhiều đơn hàng không
      const pendingMultipleOrders = localStorage.getItem('pendingMultipleOrders')

      if (pendingMultipleOrders) {
        // Xử lý nhiều đơn hàng
        try {
          const multipleOrdersData = JSON.parse(pendingMultipleOrders)
          orderInfo.value.multipleOrders = true
          orderInfo.value.totalOrders = multipleOrdersData.length

          // Tạo tất cả đơn hàng
          const createdOrders = []
          for (const orderData of multipleOrdersData) {
            try {
              console.log('Creating order for shop:', orderData.shopId, 'with total:', orderData.totalAmount)
              const orderResponse = await axios.post('/api/orders', orderData)
              if (orderResponse.data) {
                createdOrders.push(orderResponse.data)
                console.log('Order created successfully:', orderResponse.data.id)

                // Cập nhật trạng thái thanh toán thành SUCCESS cho đơn hàng vừa tạo
                try {
                  await axios.put(`/api/shop/orders/${orderResponse.data.id}/payment-status`, {
                    paymentStatus: 'SUCCESS'
                  })
                  console.log('Payment status updated to SUCCESS for order:', orderResponse.data.id)
                } catch (paymentStatusError) {
                  console.error('Error updating payment status for order:', orderResponse.data.id, paymentStatusError)
                }
              }
            } catch (orderError) {
              console.error('Error creating order:', orderError)
              message.error('Lỗi tạo đơn hàng cho shop: ' + orderData.shopId)
            }
          }

          // Tính tổng tiền
          orderInfo.value.amount = createdOrders.reduce((total, order) => total + order.totalAmount, 0)
          orderInfo.value.paymentMethod = 'PayOS'

          if (createdOrders.length > 0) {
            message.success(`Thanh toán thành công! Đã tạo ${createdOrders.length} đơn hàng.`)
          } else {
            message.error('Không thể tạo đơn hàng nào. Vui lòng liên hệ hỗ trợ.')
          }

          // Clear localStorage
          localStorage.removeItem('pendingMultipleOrders')
          localStorage.removeItem('pendingOrderCode')
          localStorage.removeItem('pendingOrderAmount')

        } catch (multipleOrdersError) {
          console.error('Error processing multiple orders:', multipleOrdersError)
          message.error('Có lỗi xảy ra khi tạo đơn hàng')
        }
      } else {
        // Xử lý đơn hàng đơn lẻ - không cần tạo đơn hàng mới vì đã được tạo trong PayOS
        try {
          // Lấy thông tin đơn hàng từ localStorage hoặc tạo mới
          const pendingOrderAmount = localStorage.getItem('pendingOrderAmount')
          if (pendingOrderAmount) {
            orderInfo.value.amount = parseFloat(pendingOrderAmount)
          }

          message.success('Thanh toán thành công!')

          // Clear localStorage
          localStorage.removeItem('pendingOrderCode')
          localStorage.removeItem('pendingOrderAmount')
        } catch (error) {
          console.error('Error processing single order:', error)
          message.error('Có lỗi xảy ra khi xử lý đơn hàng')
        }
      }

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
  // Chuyển đến trang đơn hàng của user
  router.push('/user/order')
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
