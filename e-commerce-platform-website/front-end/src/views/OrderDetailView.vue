<template>
  <div class="min-h-screen bg-gray-50">
    <NavbarView />

    <main class="container mx-auto px-4 py-8">
      <div class="max-w-4xl mx-auto">
        <!-- Breadcrumb -->
        <nav class="flex mb-8" aria-label="Breadcrumb">
          <ol class="inline-flex items-center space-x-1 md:space-x-3">
            <li class="inline-flex items-center">
              <RouterLink to="/" class="text-gray-700 hover:text-blue-600"> Trang chủ </RouterLink>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRight class="w-4 h-4 text-gray-400" />
                <span class="ml-1 text-gray-500 md:ml-2">Chi tiết đơn hàng</span>
              </div>
            </li>
          </ol>
        </nav>

        <!-- Loading -->
        <div v-if="loading" class="text-center py-8">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p class="mt-4 text-gray-600">Đang tải thông tin đơn hàng...</p>
        </div>

        <!-- Order Details -->
        <div v-else-if="order" class="bg-white rounded-lg shadow-md p-6">
          <!-- Order Header -->
          <div class="border-b border-gray-200 pb-4 mb-6">
            <div class="flex justify-between items-start">
              <div>
                <h1 class="text-2xl font-bold text-gray-900">Đơn hàng #{{ order.id }}</h1>
                <p class="text-gray-600 mt-1">Ngày đặt: {{ formatDate(order.orderDate) }}</p>
              </div>
              <div class="text-right">
                <span :class="getStatusClass(order.orderStatus)" class="px-3 py-1 rounded-full text-sm font-medium">
                  {{ getStatusText(order.orderStatus) }}
                </span>
              </div>
            </div>
          </div>

          <!-- Order Items -->
          <div class="mb-8">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">Sản phẩm đã đặt</h2>
            <div class="space-y-4">
              <div
                v-for="item in order.orderItems"
                :key="item.id"
                class="flex items-center space-x-4 p-4 border border-gray-200 rounded-lg"
              >
                <div class="flex-shrink-0">
                  <img
                    :src="item.productImage || '/placeholder-image.jpg'"
                    :alt="item.productName"
                    class="w-16 h-16 object-cover rounded"
                    @error="$event.target.src = '/placeholder-image.jpg'"
                  />
                </div>
                <div class="flex-1">
                  <h3 class="font-medium text-gray-900">{{ item.productName }}</h3>
                  <p v-if="item.variantName" class="text-sm text-gray-600">
                    {{ item.variantName }}: {{ item.variantValue }}
                  </p>
                  <p class="text-sm text-gray-600">Số lượng: {{ item.quantity }}</p>
                </div>
                <div class="text-right">
                  <p class="font-medium text-gray-900">{{ formatPrice(item.productPrice) }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Payment Information -->
          <div class="mb-8">
            <h2 class="text-lg font-semibold text-gray-900 mb-4">Thông tin thanh toán</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="bg-gray-50 p-4 rounded-lg">
                <h3 class="font-medium text-gray-900 mb-2">Phương thức thanh toán</h3>
                <p class="text-gray-600">{{ order.paymentName || 'N/A' }}</p>
              </div>
              <div class="bg-gray-50 p-4 rounded-lg">
                <h3 class="font-medium text-gray-900 mb-2">Phương thức vận chuyển</h3>
                <p class="text-gray-600">{{ order.shippingMethod || 'N/A' }}</p>
              </div>
            </div>
          </div>

          <!-- Order Summary -->
          <div class="border-t border-gray-200 pt-6">
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-gray-600">Tạm tính:</span>
                <span class="text-gray-900">{{
                  formatPrice(order.totalAmount - (order.discountAmount || 0) - (order.shippingPrice || 0))
                }}</span>
              </div>
              <div v-if="order.shippingPrice && order.shippingPrice > 0" class="flex justify-between">
                <span class="text-gray-600">Phí vận chuyển:</span>
                <span class="text-gray-900">{{ formatPrice(order.shippingPrice) }}</span>
              </div>
              <div v-if="order.discountAmount && order.discountAmount > 0" class="flex justify-between">
                <span class="text-gray-600">Giảm giá:</span>
                <span class="text-red-600">-{{ formatPrice(order.discountAmount) }}</span>
              </div>
              <div class="flex justify-between text-lg font-bold">
                <span>Tổng cộng:</span>
                <span>{{ formatPrice(order.totalAmount) }}</span>
              </div>
            </div>
          </div>

          <!-- Shipping Address -->
          <div class="mt-6 pt-6 border-t border-gray-200">
            <h3 class="font-medium text-gray-900 mb-2">Địa chỉ giao hàng</h3>
            <p class="text-gray-600">{{ order.shippingAddress }}</p>
          </div>
        </div>

        <!-- Error -->
        <div v-else class="text-center py-8">
          <p class="text-red-600">{{ error || 'Không tìm thấy đơn hàng' }}</p>
        </div>
      </div>
    </main>

    <FooterView />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { ChevronRight } from 'lucide-vue-next'
import axios from '../utils/axios'
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'

const route = useRoute()
const order = ref(null)
const loading = ref(true)
const error = ref('')

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const getStatusClass = (status) => {
  switch (status) {
    case 'PENDING_PROCESSING':
      return 'bg-yellow-100 text-yellow-800'
    case 'PROCESSED':
      return 'bg-blue-100 text-blue-800'
    case 'READY_FOR_PICKUP':
      return 'bg-purple-100 text-purple-800'
    case 'IN_TRANSIT':
      return 'bg-indigo-100 text-indigo-800'
    case 'DELIVERED':
      return 'bg-green-100 text-green-800'
    case 'CANCELLED':
      return 'bg-red-100 text-red-800'
    case 'PENDING':
      return 'bg-orange-100 text-orange-800'
    case 'PAID':
      return 'bg-green-100 text-green-800'
    case 'SHIPPED':
      return 'bg-blue-100 text-blue-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'PENDING_PROCESSING':
      return 'Chờ xử lý'
    case 'PROCESSED':
      return 'Đã xử lý'
    case 'READY_FOR_PICKUP':
      return 'Chờ lấy hàng'
    case 'IN_TRANSIT':
      return 'Đang giao hàng'
    case 'DELIVERED':
      return 'Đã giao hàng'
    case 'CANCELLED':
      return 'Đã hủy'
    case 'PENDING':
      return 'Chờ thanh toán'
    case 'PAID':
      return 'Đã thanh toán'
    case 'SHIPPED':
      return 'Đang giao hàng'
    default:
      return status
  }
}

const fetchOrder = async () => {
  try {
    loading.value = true
    const response = await axios.get(`/api/orders/${route.params.id}`)

    if (response.data.success) {
      order.value = response.data.data
      console.log('Order data:', order.value)
    } else {
      error.value = response.data.message || 'Không thể tải thông tin đơn hàng'
    }
  } catch (err) {
    console.error('Error fetching order:', err)
    error.value = 'Không thể tải thông tin đơn hàng'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrder()
})
</script>
