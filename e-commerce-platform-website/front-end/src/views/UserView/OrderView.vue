<template>
  <div class="max-w-6xl mx-auto px-4">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-2">Đơn hàng của bạn</h1>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center items-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
      <div class="text-red-600 mb-2">
        <AlertCircle class="w-8 h-8 mx-auto mb-2" />
      </div>
      <h3 class="text-lg font-medium text-red-800 mb-2">Không thể tải đơn hàng</h3>
      <p class="text-red-600 mb-4">{{ errorMessage }}</p>
      <button
        @click="fetchOrders"
        class="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors"
      >
        Thử lại
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="orders.length === 0" class="text-center py-12">
      <div class="text-gray-400 mb-4">
        <Package class="w-16 h-16 mx-auto" />
      </div>
      <h3 class="text-lg font-medium text-gray-600 mb-2">Chưa có đơn hàng nào</h3>
      <p class="text-gray-500 mb-6">Bạn chưa có đơn hàng nào. Hãy mua sắm để tạo đơn hàng đầu tiên!</p>
      <div class="flex justify-center">
        <RouterLink
          to="/"
          class="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <ShoppingCart class="w-4 h-4 mr-2" />
          Mua sắm ngay
        </RouterLink>
      </div>
    </div>

    <!-- Orders List -->
    <div v-else class="space-y-6">
      <div
        v-for="order in orders"
        :key="order.id"
        class="bg-white border border-gray-200 rounded-lg"
      >
        <!-- Order Header -->
        <div class="p-6 border-b border-gray-100">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="text-lg font-semibold text-gray-800">Đơn hàng #{{ order.orderCode }}</h3>
              <p class="text-sm text-gray-500 mt-1">
                Đặt hàng lúc {{ formatDate(order.orderDate) }}
              </p>
            </div>
            <div class="text-right">
              <div class="text-lg font-bold text-blue-600">
                {{ formatPrice(order.totalAmount) }}
              </div>
              <div class="text-sm text-gray-500">
                {{ order.orderItems.length }} sản phẩm
              </div>
            </div>
          </div>

          <!-- Order Status -->
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-2">
              <div :class="getStatusColor(order.orderStatus)" class="px-3 py-1 rounded-full text-sm font-medium">
                {{ getStatusText(order.orderStatus) }}
              </div>
            </div>
            <div class="text-sm text-gray-500">
              {{ order.shopName }}
            </div>
          </div>
        </div>

        <!-- Order Items -->
        <div class="p-6">
          <div class="space-y-4">
            <div
              v-for="item in order.orderItems"
              :key="item.id"
              class="flex items-center space-x-4"
            >
              <div class="w-16 h-16 flex-shrink-0">
                <img
                  :src="item.productImage"
                  :alt="item.productName"
                  class="w-full h-full object-cover rounded-lg"
                  @error="handleImageError"
                />
              </div>
              <div class="flex-1 min-w-0">
                <h4 class="font-medium text-gray-800 truncate">{{ item.productName }}</h4>
                <p v-if="item.variantName" class="text-sm text-gray-500">
                  {{ item.variantName }}: {{ item.variantValue }}
                </p>
                <div class="flex items-center justify-between mt-2">
                  <div class="text-sm text-gray-500">
                    Số lượng: {{ item.quantity }}
                  </div>
                  <div class="text-sm font-medium text-gray-800">
                    {{ formatPrice(item.productPrice) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Order Details -->
        <div class="px-6 py-4 bg-gray-50 border-t border-gray-100">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div>
              <p class="text-gray-500">Phương thức thanh toán:</p>
              <p class="font-medium text-gray-800">{{ order.paymentName }}</p>
            </div>
            <div>
              <p class="text-gray-500">Phương thức giao hàng:</p>
              <p class="font-medium text-gray-800">{{ order.shippingMethod }}</p>
            </div>
            <div>
              <p class="text-gray-500">Địa chỉ giao hàng:</p>
              <p class="font-medium text-gray-800">{{ order.shippingAddress }}</p>
            </div>
            <div>
              <p class="text-gray-500">Trạng thái thanh toán:</p>
              <p class="font-medium text-gray-800">{{ getTransactionStatus(order.transactions) }}</p>
            </div>
          </div>
        </div>

        <!-- Order Timeline -->
        <div class="px-6 py-4 border-t border-gray-100">
          <h4 class="font-medium text-gray-800 mb-6">Trạng thái đơn hàng</h4>
          <div class="relative">
            <!-- Timeline -->
            <div class="grid grid-cols-5 gap-8">
              <div
                v-for="(step, index) in getOrderTimeline(order.orderStatus)"
                :key="index"
                class="flex flex-col items-center relative"
              >
                <!-- Step Icon -->
                <div
                  :class="[
                    'w-12 h-12 rounded-full flex items-center justify-center mb-3 relative z-10',
                    step.completed
                      ? 'bg-green-500 text-white shadow-lg'
                      : step.current
                        ? 'bg-blue-500 text-white shadow-lg'
                        : 'bg-gray-200 text-gray-500'
                  ]"
                >
                  <Check v-if="step.completed" class="w-5 h-5" />
                  <Clock v-else-if="step.current" class="w-5 h-5" />
                  <Package v-else-if="step.label === 'Sẵn sàng giao hàng' && !step.completed" class="w-5 h-5" />
                  <Truck v-else-if="step.label === 'Đang giao hàng' && !step.completed" class="w-5 h-5" />
                  <AlertCircle v-else class="w-5 h-5" />
                </div>

                <!-- Step Label -->
                <div class="text-center">
                  <p
                    :class="[
                      'text-sm font-medium leading-tight',
                      step.completed
                        ? 'text-green-600'
                        : step.current
                          ? 'text-blue-600'
                          : 'text-gray-500'
                    ]"
                  >
                    {{ step.label }}
                  </p>
                  <p v-if="step.date" class="text-xs text-gray-400 mt-1">
                    {{ formatDate(step.date) }}
                  </p>
                </div>

                <!-- Connector Line -->
                <div
                  v-if="index < getOrderTimeline(order.orderStatus).length - 1"
                  :class="[
                    'absolute top-6 left-full w-8 h-0.5',
                    step.completed ? 'bg-green-500' : 'bg-gray-200'
                  ]"
                  style="transform: translateX(0);"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Order Actions -->
        <div class="px-6 py-4 border-t border-gray-100">
          <div class="flex justify-between items-center">
            <div class="text-sm text-gray-500">
              Mã đơn hàng: <span class="font-medium">{{ order.orderCode }}</span>
            </div>
            <div class="flex space-x-2">
              <button
                v-if="order.orderStatus === 'DELIVERED'"
                class="px-4 py-2 text-sm border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Đánh giá
              </button>
              <button
                v-if="canCancelOrder(order.orderStatus)"
                @click="cancelOrder(order.id)"
                class="px-4 py-2 text-sm border border-red-300 text-red-600 rounded-lg hover:bg-red-50 transition-colors"
              >
                Hủy đơn hàng
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
import { RouterLink } from 'vue-router'
import axios from '../../utils/axios'
import { Package, ShoppingCart, AlertCircle, Check, Clock, Truck } from 'lucide-vue-next'

const orders = ref([])
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')

const fetchOrders = async () => {
  try {
    loading.value = true
    error.value = false

    const response = await axios.get('/api/orders/my-orders')

    if (response.data.success) {
      // Sắp xếp đơn hàng theo thời gian đặt hàng mới nhất
      orders.value = response.data.orders.sort((a, b) => {
        const dateA = new Date(a.orderDate)
        const dateB = new Date(b.orderDate)
        return dateB - dateA // Sắp xếp giảm dần (mới nhất trước)
      })
    } else {
      error.value = true
      errorMessage.value = response.data.message || 'Không thể tải đơn hàng'
    }
  } catch (err) {
    error.value = true
    errorMessage.value = 'Có lỗi xảy ra khi tải đơn hàng'
    console.error('Error fetching orders:', err)
  } finally {
    loading.value = false
  }
}

const formatPrice = (price) => {
  if (!price || price === 0) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleImageError = (event) => {
  event.target.src = '/placeholder.png'
}

const getStatusColor = (status) => {
  const statusColors = {
    'PENDING_PROCESSING': 'bg-yellow-100 text-yellow-800',
    'PROCESSED': 'bg-blue-100 text-blue-800',
    'READY_FOR_PICKUP': 'bg-purple-100 text-purple-800',
    'IN_TRANSIT': 'bg-orange-100 text-orange-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  }
  return statusColors[status] || 'bg-gray-100 text-gray-800'
}

const getStatusText = (status) => {
  const statusTexts = {
    'PENDING_PROCESSING': 'Chờ xử lý',
    'PROCESSED': 'Đã xử lý',
    'READY_FOR_PICKUP': 'Sẵn sàng giao hàng',
    'IN_TRANSIT': 'Đang giao hàng',
    'DELIVERED': 'Đã giao hàng',
    'CANCELLED': 'Đã hủy'
  }
  return statusTexts[status] || status
}

const getTransactionStatus = (transactions) => {
  if (!transactions || transactions.length === 0) return 'Chưa thanh toán'

  const latestTransaction = transactions[transactions.length - 1]
  const statusTexts = {
    'PENDING': 'Chờ thanh toán',
    'SUCCESS': 'Đã thanh toán',
    'FAILED': 'Thanh toán thất bại',
    'CANCELLED': 'Đã hủy'
  }
  return statusTexts[latestTransaction.transactionStatus] || latestTransaction.transactionStatus
}

const getOrderTimeline = (status) => {
  const timeline = [
    { label: 'Chờ xử lý', completed: false, current: false, date: null },
    { label: 'Đã xử lý', completed: false, current: false, date: null },
    { label: 'Sẵn sàng giao hàng', completed: false, current: false, date: null },
    { label: 'Đang giao hàng', completed: false, current: false, date: null },
    { label: 'Đã giao hàng', completed: false, current: false, date: null }
  ]

  const statusOrder = ['PENDING_PROCESSING', 'PROCESSED', 'READY_FOR_PICKUP', 'IN_TRANSIT', 'DELIVERED']
  const currentIndex = statusOrder.indexOf(status)

  if (status === 'CANCELLED') {
    return [{ label: 'Đã hủy', completed: true, current: false, date: null }]
  }

  // Các bước đã đi qua sẽ hiển thị màu xanh với dấu tích
  timeline.forEach((step, index) => {
    if (index < currentIndex) {
      step.completed = true  // Các bước đã hoàn thành
    } else if (index === currentIndex) {
      if (status === 'DELIVERED') {
        step.completed = true  // Đã giao hàng = hoàn thành
      } else {
        step.current = true    // Bước hiện tại
      }
    }
    // Các bước chưa đến sẽ giữ màu xám
  })

  return timeline
}

const canCancelOrder = (status) => {
  return ['PENDING_PROCESSING', 'PROCESSED'].includes(status)
}

const cancelOrder = async (orderId) => {
  if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) return

  try {
    await axios.put(`/api/orders/${orderId}/status`, { status: 'CANCELLED' })
    await fetchOrders() // Refresh orders
  } catch (err) {
    console.error('Error cancelling order:', err)
    alert('Không thể hủy đơn hàng. Vui lòng thử lại.')
  }
}



onMounted(() => {
  fetchOrders()
})
</script>
