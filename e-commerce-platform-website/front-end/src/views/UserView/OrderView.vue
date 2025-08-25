<!-- eslint-disable no-unused-vars -->
<template>
  <div class="max-w-6xl mx-auto px-4">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-2">Đơn hàng của bạn</h1>

      <!-- Filter Buttons -->
      <div class="flex flex-wrap gap-2 mt-4">
        <button
          @click="setFilter('all')"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
            currentFilter === 'all'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
        >
          Tất cả đơn hàng
        </button>
        <button
          @click="setFilter('new')"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
            currentFilter === 'new'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
        >
          Đơn hàng mới đặt
        </button>
        <button
          @click="setFilter('delivered')"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
            currentFilter === 'delivered'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
        >
          Đơn hàng đã giao
        </button>
        <button
          @click="setFilter('cancelled')"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
            currentFilter === 'cancelled'
              ? 'bg-blue-600 text-white'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
        >
          Đơn hàng đã hủy
        </button>
      </div>
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
            <div class="text-right">
              <div class="text-sm font-medium text-blue-600">
                {{ order.shopName }}
              </div>

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
                <p v-if="formatVariantInfo(item.variantName, item.variantValue)" class="text-sm text-gray-500">
                  {{ formatVariantInfo(item.variantName, item.variantValue) }}
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
              <span :class="getPaymentMethodColor(order.paymentName)" class="px-2 py-1 rounded-full text-sm font-medium">
                {{ order.paymentName }}
              </span>
            </div>
            <div>
              <p class="text-gray-500">Phương thức giao hàng:</p>
              <span :class="getShippingMethodColor(order.shippingMethod)" class="px-2 py-1 rounded-full text-sm font-medium">
                {{ order.shippingMethod }}
              </span>
            </div>
            <div>
              <p class="text-gray-500">Địa chỉ giao hàng:</p>
              <p class="font-medium text-gray-800">{{ order.shippingAddress }}</p>
            </div>
            <div>
              <p class="text-gray-500">Trạng thái thanh toán:</p>
              <span :class="getTransactionStatusColor(order.transactions)" class="px-2 py-1 rounded-full text-sm font-medium">
                {{ getTransactionStatus(order.transactions) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Order Timeline -->
        <div class="px-6 py-4 border-t border-gray-100">
          <div class="flex items-center justify-between mb-6">
            <h4 class="font-medium text-gray-800">Trạng thái đơn hàng</h4>

          </div>
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
                @click="exportInvoice(order.id)"
                class="px-4 py-2 text-sm border border-blue-300 text-blue-600 rounded-lg hover:bg-blue-50 transition-colors"
              >
                Xuất hóa đơn
              </button>
              <button
                @click="downloadInvoice(order.id)"
                class="px-4 py-2 text-sm border border-purple-300 text-purple-600 rounded-lg hover:bg-purple-50 transition-colors"
              >
                Tải hóa đơn
              </button>
              <button
                v-if="order.orderStatus === 'DELIVERED'"
                @click="openReviewDialog(order)"
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
    <!-- Review Dialog -->
    <ReviewDialog
      v-model:show="showReviewDialog"
      :product-id="selectedProductForReview?.id"
      :product-name="selectedProductForReview?.name"
      :product-image="selectedProductForReview?.image"
      :variant-name="selectedProductForReview?.variantName"
      :variant-value="selectedProductForReview?.variantValue"
      :product-variant-id="selectedProductForReview?.variantId"
      @review-submitted="onReviewSubmitted"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import axios from '../../utils/axios'
import { Package, ShoppingCart, AlertCircle, Check, Clock, Truck } from 'lucide-vue-next'
import ReviewDialog from '../../components/ReviewDialog.vue'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'

const orders = ref([])
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')
const currentFilter = ref('all') // Thêm state cho filter

// Review dialog state
const showReviewDialog = ref(false)
const selectedProductForReview = ref(null)

const setFilter = async (filter) => {
  currentFilter.value = filter
  await fetchOrders()
}

const fetchOrders = async () => {
  try {
    loading.value = true
    error.value = false

    // Sử dụng endpoint filter nếu không phải "all"
    const url = currentFilter.value === 'all'
      ? '/api/orders/my-orders'
      : `/api/orders/my-orders/filter?filter=${currentFilter.value}`

    const response = await axios.get(url)

    if (response.data.success) {
      // Sắp xếp đơn hàng theo thời gian đặt hàng mới nhất
      orders.value = response.data.orders.sort((a, b) => {
        const dateA = new Date(a.orderDate)
        const dateB = new Date(b.orderDate)
        return dateB - dateA // Sắp xếp giảm dần (mới nhất trước)
      })

      // Debug: Log variant information
      console.log('=== FRONTEND DEBUG ORDERS ===')
      console.log('Current filter:', currentFilter.value)
      for (let i = 0; i < orders.value.length; i++) {
        const order = orders.value[i]
        console.log(`Order ${i + 1} (ID: ${order.id}, Status: ${order.orderStatus}):`)
        if (order.orderItems) {
          console.log(`  Order items count: ${order.orderItems.length}`)
          for (let j = 0; j < order.orderItems.length; j++) {
            const item = order.orderItems[j]
            console.log(`    Item ${j + 1}:`)
            console.log(`      - ProductName: ${item.productName}`)
            console.log(`      - VariantName: ${item.variantName}`)
            console.log(`      - VariantValue: ${item.variantValue}`)
            console.log(`      - ProductVariantId: ${item.productVariantId}`)
          }
        }
      }
      console.log('=== END FRONTEND DEBUG ===')
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
    'READY_FOR_PICKUP': 'Chờ lấy hàng',
    'IN_TRANSIT': 'Đang giao hàng',
    'DELIVERED': 'Đã giao hàng',
    'CANCELLED': 'Đã hủy'
  }
  return statusTexts[status] || status
}

const getTransactionStatus = (transactions) => {
  if (!transactions || transactions.length === 0) return 'Chưa thanh toán'

  const latestTransaction = transactions[transactions.length - 1]
  console.log('=== DEBUG TRANSACTION STATUS ===')
  console.log('Transactions:', transactions)
  console.log('Latest transaction:', latestTransaction)
  console.log('Transaction status:', latestTransaction.transactionStatus)

  const statusTexts = {
    'PENDING': 'Chờ thanh toán',
    'SUCCESS': 'Đã thanh toán',
    'FAILED': 'Thanh toán thất bại',
    'CANCELLED': 'Đã hủy'
  }
  const result = statusTexts[latestTransaction.transactionStatus] || latestTransaction.transactionStatus
  console.log('Final status text:', result)
  console.log('=== END DEBUG ===')
  return result
}

const getTransactionStatusColor = (transactions) => {
  if (!transactions || transactions.length === 0) return 'bg-gray-100 text-gray-800'

  const latestTransaction = transactions[transactions.length - 1]
  const statusColors = {
    'PENDING': 'bg-yellow-100 text-yellow-800',
    'SUCCESS': 'bg-green-100 text-green-800',
    'FAILED': 'bg-red-100 text-red-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  }
  return statusColors[latestTransaction.transactionStatus] || 'bg-gray-100 text-gray-800'
}

const getPaymentMethodColor = (paymentName) => {
  const paymentColors = {
    'Thanh toán khi nhận hàng': 'bg-green-100 text-green-800',
    'Thanh toán bằng ngân hàng': 'bg-blue-100 text-blue-800',
    'PayOS': 'bg-purple-100 text-purple-800'
  }
  return paymentColors[paymentName] || 'bg-gray-100 text-gray-800'
}

const getShippingMethodColor = (shippingMethod) => {
  const shippingColors = {
    'Giao hàng nhanh': 'bg-green-100 text-green-800',
    'Giao hàng tiêu chuẩn': 'bg-blue-100 text-blue-800',
    'Giao hàng tiết kiệm': 'bg-orange-100 text-orange-800'
  }
  return shippingColors[shippingMethod] || 'bg-gray-100 text-gray-800'
}

const getOrderTimeline = (status) => {
  const timeline = [
    { label: 'Chờ xử lý', completed: false, current: false, date: null },
    { label: 'Đã xử lý', completed: false, current: false, date: null },
    { label: 'Chờ lấy hàng', completed: false, current: false, date: null },
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

// eslint-disable-next-line no-unused-vars
const getUniqueShopsCount = () => {
  const uniqueShops = new Set(orders.value.map(order => order.shopName))
  return uniqueShops.size
}

const formatVariantInfo = (variantName, variantValue) => {
  // Kiểm tra và làm sạch dữ liệu
  const cleanVariantName = variantName ? variantName.trim() : ''
  const cleanVariantValue = variantValue ? variantValue.trim() : ''

  if (!cleanVariantName) {
    return ''
  }

  // Nếu variantName là "Màu sắc" hoặc "Color", hiển thị rõ ràng hơn
  if (cleanVariantName.toLowerCase().includes('màu') || cleanVariantName.toLowerCase().includes('color')) {
    return `Màu sắc: ${cleanVariantValue || 'Không xác định'}`
  }

  // Nếu có cả variantName và variantValue
  if (cleanVariantValue) {
    return `${cleanVariantName}: ${cleanVariantValue}`
  }

  // Chỉ có variantName
  return cleanVariantName
}

const openReviewDialog = (order) => {
  // Lấy sản phẩm đầu tiên từ đơn hàng để đánh giá
  if (order.orderItems && order.orderItems.length > 0) {
    const firstItem = order.orderItems[0]
    console.log('Order item for review:', firstItem) // Debug log
    console.log('Order item keys:', Object.keys(firstItem)) // Debug log

    // Kiểm tra các trường có thể có product ID
    const productId = firstItem.productId ||
                     firstItem.product?.id ||
                     firstItem.productVariant?.product?.id ||
                     firstItem.productVariantId

    if (!productId) {
      console.error('No product ID found in order item')
      console.error('Available fields:', Object.keys(firstItem))
      return
    }

    selectedProductForReview.value = {
      id: productId,
      name: firstItem.productName ||
            firstItem.product?.name ||
            firstItem.productVariant?.product?.name ||
            'Sản phẩm',
      image: firstItem.productImage ||
             firstItem.product?.productImage ||
             firstItem.productVariant?.product?.productImage ||
             '',
      variantName: firstItem.variantName ||
                  firstItem.productVariant?.variantName ||
                  '',
      variantValue: firstItem.variantValue ||
                   firstItem.productVariant?.variantValue ||
                   '',
      variantId: firstItem.productVariantId ||
                firstItem.productVariant?.id ||
                null
    }
    showReviewDialog.value = true
  }
}

const onReviewSubmitted = (review) => {
  // Có thể thêm logic cập nhật UI sau khi đánh giá thành công
  console.log('Review submitted:', review)
}

const exportInvoice = async (orderId) => {
  try {
    const response = await axios.get(`/api/orders/${orderId}/invoice`)

    // Tạo một div tạm thời để render HTML
    const tempDiv = document.createElement('div')
    tempDiv.innerHTML = response.data
    tempDiv.style.position = 'absolute'
    tempDiv.style.left = '-9999px'
    tempDiv.style.top = '-9999px'
    document.body.appendChild(tempDiv)

    // Chuyển HTML thành canvas
    const canvas = await html2canvas(tempDiv, {
      scale: 2,
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff'
    })

    // Tạo PDF
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 210
    const pageHeight = 295
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight

    let position = 0

    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    // Tải PDF
    pdf.save(`hoa-don-${orderId}.pdf`)

    // Cleanup
    document.body.removeChild(tempDiv)
  } catch (err) {
    console.error('Error exporting invoice:', err)
    alert('Không thể xuất hóa đơn. Vui lòng thử lại.')
  }
}

const downloadInvoice = async (orderId) => {
  try {
    const response = await axios.get(`/api/orders/${orderId}/invoice`)

    // Tạo một div tạm thời để render HTML
    const tempDiv = document.createElement('div')
    tempDiv.innerHTML = response.data
    tempDiv.style.position = 'absolute'
    tempDiv.style.left = '-9999px'
    tempDiv.style.top = '-9999px'
    document.body.appendChild(tempDiv)

    // Chuyển HTML thành canvas
    const canvas = await html2canvas(tempDiv, {
      scale: 2,
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff'
    })

    // Tạo PDF
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 210
    const pageHeight = 295
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight

    let position = 0

    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    // Tải PDF
    pdf.save(`hoa-don-${orderId}.pdf`)

    // Cleanup
    document.body.removeChild(tempDiv)
  } catch (err) {
    console.error('Error downloading invoice:', err)
    alert('Không thể tải hóa đơn. Vui lòng thử lại.')
  }
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
