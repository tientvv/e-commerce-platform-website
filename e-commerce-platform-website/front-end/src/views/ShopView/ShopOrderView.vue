<template>
  <div class="h-full flex flex-col overflow-hidden">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-4">Quản lý đơn hàng</h1>

      <!-- Statistics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
        <!-- Hàng 1: Tổng đơn hàng, Chờ xử lý, Đã thanh toán -->
        <div class="relative overflow-hidden rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 p-6 text-white shadow-lg">
          <div class="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -translate-y-16 translate-x-16"></div>
          <div class="relative flex items-center gap-4">
            <div class="p-3 rounded-lg bg-white bg-opacity-20 backdrop-blur-sm">
              <component :is="Package" class="w-6 h-6 text-blue-600" />
          </div>
          <div class="flex-1">
              <div class="text-3xl font-bold">{{ statistics.totalOrders || 0 }}</div>
              <div class="text-blue-100 font-medium">Tổng đơn hàng</div>
            </div>
          </div>
        </div>

        <div class="relative overflow-hidden rounded-xl bg-gradient-to-br from-amber-500 to-orange-500 p-6 text-white shadow-lg">
          <div class="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -translate-y-16 translate-x-16"></div>
          <div class="relative flex items-center gap-4">
            <div class="p-3 rounded-lg bg-white bg-opacity-20 backdrop-blur-sm">
              <component :is="Clock" class="w-6 h-6 text-amber-600" />
          </div>
          <div class="flex-1">
              <div class="text-3xl font-bold">{{ statistics.pendingOrders || 0 }}</div>
              <div class="text-amber-100 font-medium">Chờ xử lý</div>
            </div>
          </div>
        </div>

        <div class="relative overflow-hidden rounded-xl bg-gradient-to-br from-emerald-500 to-green-600 p-6 text-white shadow-lg">
          <div class="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -translate-y-16 translate-x-16"></div>
          <div class="relative flex items-center gap-4">
            <div class="p-3 rounded-lg bg-white bg-opacity-20 backdrop-blur-sm">
              <component :is="CheckCircle" class="w-6 h-6 text-emerald-600" />
          </div>
          <div class="flex-1">
              <div class="text-3xl font-bold">{{ statistics.paidOrders || 0 }}</div>
              <div class="text-emerald-100 font-medium">Đã thanh toán</div>
            </div>
          </div>
        </div>

        <!-- Hàng 2: Đã giao hàng, Đã hủy, Đã hoàn tiền -->
        <div class="relative overflow-hidden rounded-xl bg-gradient-to-br from-purple-500 to-indigo-600 p-6 text-white shadow-lg">
          <div class="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -translate-y-16 translate-x-16"></div>
          <div class="relative flex items-center gap-4">
            <div class="p-3 rounded-lg bg-white bg-opacity-20 backdrop-blur-sm">
              <Truck class="w-6 h-6 text-purple-600" />
          </div>
          <div class="flex-1">
              <div class="text-3xl font-bold">{{ statistics.deliveredOrders || 0 }}</div>
              <div class="text-purple-100 font-medium">Đã giao hàng</div>
            </div>
          </div>
        </div>

        <div class="relative overflow-hidden rounded-xl bg-gradient-to-br from-red-500 to-pink-600 p-6 text-white shadow-lg">
          <div class="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -translate-y-16 translate-x-16"></div>
          <div class="relative flex items-center gap-4">
            <div class="p-3 rounded-lg bg-white bg-opacity-20 backdrop-blur-sm">
              <XCircle class="w-6 h-6 text-red-600" />
          </div>
          <div class="flex-1">
              <div class="text-3xl font-bold">{{ statistics.cancelledOrders || 0 }}</div>
              <div class="text-red-100 font-medium">Đã hủy</div>
            </div>
          </div>
        </div>

        <div class="relative overflow-hidden rounded-xl bg-gradient-to-br from-cyan-500 to-blue-500 p-6 text-white shadow-lg">
          <div class="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -translate-y-16 translate-x-16"></div>
          <div class="relative flex items-center gap-4">
            <div class="p-3 rounded-lg bg-white bg-opacity-20 backdrop-blur-sm">
              <RotateCcw class="w-6 h-6 text-cyan-600" />
          </div>
          <div class="flex-1">
              <div class="text-3xl font-bold">{{ statistics.refundedOrders || 0 }}</div>
              <div class="text-cyan-100 font-medium">Đã hoàn tiền</div>
          </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="mb-6">
      <n-card title="Bộ lọc" class="mb-6">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-4">
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">Trạng thái</label>
            <n-select
              v-model:value="filters.status"
              :options="statusOptions"
              placeholder="Chọn trạng thái"
              clearable
              @update:value="loadOrders"
            />
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">Phương thức thanh toán</label>
            <n-select
              v-model:value="filters.paymentMethod"
              :options="paymentOptions"
              placeholder="Chọn phương thức"
              clearable
              @update:value="loadOrders"
            />
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">Thời gian</label>
            <n-select
              v-model:value="filters.timeFilter"
              :options="timeOptions"
              placeholder="Chọn thời gian"
              @update:value="loadOrders"
            />
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">Từ ngày</label>
            <n-date-picker
              v-model:value="filters.startDate"
              type="date"
              placeholder="Chọn ngày bắt đầu"
              clearable
              format="dd/MM/yyyy"
              value-format="yyyy-MM-dd"
              @update:value="(value) => {
                console.log('Start date changed:', value)
                filters.startDate = value
                loadOrders()
              }"
            />
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">Đến ngày</label>
            <n-date-picker
              v-model:value="filters.endDate"
              type="date"
              placeholder="Chọn ngày kết thúc"
              clearable
              format="dd/MM/yyyy"
              value-format="yyyy-MM-dd"
              @update:value="(value) => {
                console.log('End date changed:', value)
                filters.endDate = value
                loadOrders()
              }"
            />
          </div>

          <div class="flex items-end">
            <n-button @click="clearFilters" secondary class="w-full">
            <template #icon>
              <RefreshCw class="w-4 h-4" />
            </template>
            Xóa bộ lọc
          </n-button>
          </div>
        </div>
      </n-card>
    </div>

    <!-- Orders Table -->
    <div class="flex-1 overflow-hidden">
      <n-card title="Danh sách đơn hàng" class="h-full flex flex-col">
        <template #header-extra>
          <div class="flex items-center gap-2">
            <span class="text-sm text-gray-600">Tổng: {{ orders.length }} đơn hàng</span>
            <n-button @click="loadOrders" size="small">
              <template #icon>
                <RefreshCw class="w-4 h-4" />
              </template>
              Làm mới
            </n-button>
          </div>
        </template>

                <div class="flex-1">
          <div class="w-full h-full">
            <div class="w-full h-full">
              <div class="w-full">
                                <div class="swiper-table-container" ref="swiperContainer">
                  <!-- Loading State -->
                  <div v-if="loading" class="flex items-center justify-center py-8">
                    <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                    <span class="ml-2 text-gray-600">Đang tải...</span>
                  </div>

                  <!-- Swiper Table -->
                  <div v-else class="swiper swiper-table">
                    <div class="swiper-wrapper">
                      <div class="swiper-slide">
                        <div class="custom-table">
                          <!-- Table Header -->
                          <div class="table-header">
                            <div class="header-cell">Mã đơn hàng</div>
                            <div class="header-cell">Khách hàng</div>
                            <div class="header-cell">Tổng tiền</div>
                            <div class="header-cell">Trạng thái đơn hàng</div>
                            <div class="header-cell">Trạng thái thanh toán</div>
                            <div class="header-cell">Phương thức thanh toán</div>
                            <div class="header-cell">Ngày đặt</div>
                            <div class="header-cell">Hủy Đơn</div>
                            <div class="header-cell">Thao tác</div>
                          </div>

                          <!-- Table Body -->
                          <div class="table-body">
                            <div v-for="order in orders" :key="order.id" class="table-row">
                              <div class="table-cell text-center">{{ order.id.substring(0, 8).toUpperCase() }}</div>
                                                      <div class="table-cell text-center">
                          <div class="font-medium whitespace-nowrap overflow-hidden text-ellipsis">{{ order.accountName || 'N/A' }}</div>
                        </div>
                              <div class="table-cell font-semibold">{{ formatCurrency(order.totalAmount) }}</div>
                              <div class="table-cell">
                                <span :class="getStatusBadgeClass(order.orderStatus)">
                                  {{ getStatusLabel(order.orderStatus) }}
                                </span>
                              </div>
                              <div class="table-cell">
                                <span :class="getPaymentStatusBadgeClass(order.transactionStatus || 'PENDING')">
                                  {{ getPaymentStatusLabel(order.transactionStatus || 'PENDING') }}
                                </span>
                              </div>
                              <div class="table-cell whitespace-nowrap overflow-hidden text-ellipsis">{{ order.paymentName || 'N/A' }}</div>
                              <div class="table-cell">{{ formatDate(order.orderDate) }}</div>
                              <div class="table-cell">
                                <button
                                  v-if="order.orderStatus !== 'CANCELLED' && order.orderStatus !== 'DELIVERED'"
                                  @click="cancelOrder(order.id)"
                                  class="action-btn-cancel"
                                  :disabled="order.orderStatus === 'CANCELLED'"
                                >
                                  <XCircle class="w-4 h-4" />
                                  Hủy
                                </button>
                                <span v-else class="text-gray-400 text-sm">-</span>
                              </div>
                              <div class="table-cell">
                                <button @click="viewOrderDetail(order)" class="action-btn">
                                  <Eye class="w-4 h-4" />
                                  Xem
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Pagination -->
                  <div class="pagination-container">
                    <div class="pagination-info">
                      Hiển thị {{ (pagination.page - 1) * pagination.pageSize + 1 }} - {{ Math.min(pagination.page * pagination.pageSize, orders.length) }} của {{ orders.length }} đơn hàng
                    </div>
                    <div class="pagination-controls">
                      <button
                        @click="pagination.onChange(pagination.page - 1)"
                        :disabled="pagination.page <= 1"
                        class="pagination-btn"
                      >
                        Trước
                      </button>
                      <span class="pagination-page">{{ pagination.page }}</span>
                      <button
                        @click="pagination.onChange(pagination.page + 1)"
                        :disabled="pagination.page * pagination.pageSize >= orders.length"
                        class="pagination-btn"
                      >
                        Sau
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- Order Detail Modal -->
    <n-modal v-model:show="showOrderDetail" preset="card" style="width: 800px" title="Chi tiết đơn hàng">
      <div v-if="selectedOrder" class="space-y-6">
        <!-- Order Info -->
        <div class="mb-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Mã đơn hàng:</label>
              <span class="font-mono text-gray-900">{{ selectedOrder.id }}</span>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Ngày đặt:</label>
              <span class="text-gray-900">{{ formatDate(selectedOrder.orderDate) }}</span>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Trạng thái:</label>
              <n-tag :type="getStatusType(selectedOrder.orderStatus)" size="medium" round>
                <template #default>
                  <div class="flex items-center gap-2">
                    <component :is="getStatusIcon(selectedOrder.orderStatus)" class="w-4 h-4" />
                    <span class="font-medium">{{ getStatusLabel(selectedOrder.orderStatus) }}</span>
                  </div>
                </template>
              </n-tag>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Tổng tiền:</label>
              <span class="font-semibold text-lg text-gray-900">{{ formatCurrency(selectedOrder.totalAmount) }}</span>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Phí ship:</label>
              <span class="text-gray-900">{{ selectedOrder.shippingPrice ? formatCurrency(selectedOrder.shippingPrice) : 'Miễn phí' }}</span>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Phương thức thanh toán:</label>
              <span class="text-gray-900">{{ selectedOrder.paymentName || 'N/A' }}</span>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Trạng thái thanh toán:</label>
              <n-tag :type="getPaymentStatusType(selectedOrder.transactionStatus || 'PENDING')" size="medium" round>
                <template #default>
                  <div class="flex items-center gap-2">
                    <component :is="getPaymentStatusIcon(selectedOrder.transactionStatus || 'PENDING')" class="w-4 h-4" />
                    <span class="font-medium">{{ getPaymentStatusLabel(selectedOrder.transactionStatus || 'PENDING') }}</span>
                  </div>
                </template>
              </n-tag>
            </div>
          </div>
        </div>

        <!-- Customer Info -->
        <div class="mb-6">
          <h4 class="text-md font-semibold mb-3">Thông tin khách hàng</h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Tên:</label>
              <span class="text-gray-900">{{ selectedOrder.accountName || 'N/A' }}</span>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Email:</label>
              <span class="text-gray-900">{{ selectedOrder.accountEmail || 'N/A' }}</span>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Số điện thoại:</label>
              <span class="text-gray-900">{{ selectedOrder.accountPhone || 'N/A' }}</span>
            </div>
            <div class="flex flex-col space-y-1">
              <label class="text-sm font-medium text-gray-700">Địa chỉ:</label>
              <span class="text-gray-900">{{ selectedOrder.shippingAddress || 'N/A' }}</span>
            </div>
          </div>
        </div>

        <!-- Order Items -->
        <div class="mb-6">
          <h4 class="text-md font-semibold mb-3">Sản phẩm đã đặt</h4>
          <div class="space-y-3">
            <div
              v-for="item in selectedOrder.orderItems"
              :key="item.id"
              class="flex items-center gap-4 p-3 border border-gray-200 rounded-lg"
            >
              <div>
                <img
                  :src="item.productImage || '/placeholder.png'"
                  :alt="item.productName"
                  class="w-16 h-16 object-cover rounded"
                  @error="$event.target.src = '/placeholder.png'"
                />
              </div>
              <div class="flex-1">
                <div class="font-medium text-gray-900">{{ item.productName }}</div>
                <div v-if="formatVariantInfo(item.variantName, item.variantValue)" class="text-sm text-gray-600">
                  {{ formatVariantInfo(item.variantName, item.variantValue) }}
                </div>
                <div class="text-sm text-gray-500">{{ formatCurrency(item.productPrice) }} x {{ item.quantity }}</div>
              </div>
              <div class="font-semibold text-gray-900">
                {{ formatCurrency(item.productPrice * item.quantity) }}
              </div>
            </div>
          </div>
        </div>

        <!-- Status Update -->
        <div v-if="selectedOrder.orderStatus !== 'CANCELLED'" class="space-y-4">
        <div>
            <h4 class="text-md font-semibold mb-3">Cập nhật trạng thái đơn hàng</h4>
          <div class="flex gap-3 items-end">
            <n-select
              v-model:value="newStatus"
              :options="statusOptions"
              placeholder="Chọn trạng thái mới"
              class="flex-1"
            />
            <n-button
              @click="updateOrderStatus"
              :loading="updatingStatus"
                :disabled="!newStatus || newStatus === selectedOrder.orderStatus"
                type="primary"
              >
                Cập nhật
              </n-button>
            </div>
          </div>

          <div>
            <h4 class="text-md font-semibold mb-3">Cập nhật trạng thái thanh toán</h4>
            <div class="flex gap-3 items-end">
              <n-select
                v-model:value="newPaymentStatus"
                :options="paymentStatusOptions"
                placeholder="Chọn trạng thái thanh toán mới"
                class="flex-1"
                :disabled="selectedOrder.paymentName === 'Thanh toán bằng ngân hàng'"
              />
              <n-button
                @click="updatePaymentStatus"
                :loading="updatingPaymentStatus"
                :disabled="!newPaymentStatus || newPaymentStatus === selectedOrder.transactionStatus || selectedOrder.paymentName === 'Thanh toán bằng ngân hàng'"
              type="primary"
            >
              Cập nhật
            </n-button>
            </div>
          </div>
        </div>

        <!-- Thông báo khi đơn hàng đã hủy -->
        <div v-else class="bg-red-50 border border-red-200 rounded-lg p-4">
          <div class="flex items-center">
            <XCircle class="w-5 h-5 text-red-500 mr-2" />
            <span class="text-red-700 font-medium">Đơn hàng này đã bị hủy và không thể cập nhật trạng thái</span>
          </div>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useMessage, NDatePicker } from 'naive-ui'
import { Package, Clock, CheckCircle, Truck, XCircle, DollarSign, RefreshCw, Eye, RotateCcw, ArrowLeft } from 'lucide-vue-next'
import axios from '~/utils/axios'
import Swiper from 'swiper'
import 'swiper/css'

const message = useMessage()

// Reactive data
const loading = ref(false)
const orders = ref([])
const statistics = ref({})
const showOrderDetail = ref(false)
const selectedOrder = ref(null)
const newStatus = ref('')
const updatingStatus = ref(false)
const newPaymentStatus = ref('')
const updatingPaymentStatus = ref(false)

// Swiper refs
const swiperContainer = ref(null)
let swiperInstance = null



// Filters
const filters = reactive({
  status: null,
  paymentMethod: null,
  timeFilter: 'TODAY', // Mặc định là hôm nay
  startDate: null, // Không mặc định ngày
  endDate: null, // Không mặc định ngày
})

// Pagination
const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page) => {
    pagination.page = page
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
  },
})

// Options for filters
const statusOptions = [
  { label: 'Chờ xử lý', value: 'PENDING_PROCESSING' },
  { label: 'Đã xử lý', value: 'PROCESSED' },
  { label: 'Chờ lấy hàng', value: 'READY_FOR_PICKUP' },
  { label: 'Đang giao hàng', value: 'IN_TRANSIT' },
  { label: 'Đã giao hàng', value: 'DELIVERED' },
]

const paymentOptions = [
  { label: 'Thanh toán khi nhận hàng', value: 'COD' },
  { label: 'Thanh toán bằng ngân hàng', value: 'PAYOS' },
]

const timeOptions = [
  { label: 'Tất cả', value: 'ALL' },
  { label: 'Hôm nay', value: 'TODAY' },
  { label: 'Tuần này', value: 'THIS_WEEK' },
  { label: 'Tháng này', value: 'THIS_MONTH' },
  { label: 'Tùy chọn', value: 'CUSTOM' },
]

const paymentStatusOptions = [
  { label: 'Chờ thanh toán', value: 'PENDING' },
  { label: 'Thanh toán thành công', value: 'SUCCESS' },
  { label: 'Thanh toán thất bại', value: 'FAILED' },
  { label: 'Đã hủy thanh toán', value: 'CANCELLED' },
  { label: 'Đã hoàn tiền', value: 'REFUNDED' },
]



// Methods
const loadOrders = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (filters.status) params.append('status', filters.status)
    if (filters.paymentMethod) params.append('paymentMethod', filters.paymentMethod)

        // Xử lý time filter
    if (filters.timeFilter && filters.timeFilter !== 'ALL') {
      params.append('timeFilter', filters.timeFilter)
    }

    // Gửi startDate và endDate nếu có giá trị - xử lý timezone Việt Nam
    if (filters.startDate) {
      const startDate = new Date(filters.startDate)
      // Sử dụng local date thay vì UTC
      const startDateStr = startDate.getFullYear() + '-' +
        String(startDate.getMonth() + 1).padStart(2, '0') + '-' +
        String(startDate.getDate()).padStart(2, '0')
      params.append('startDate', startDateStr)
      console.log('DEBUG: Converted startDate from', filters.startDate, 'to', startDateStr, '(Local date)')
    }
    if (filters.endDate) {
      const endDate = new Date(filters.endDate)
      // Sử dụng local date thay vì UTC
      const endDateStr = endDate.getFullYear() + '-' +
        String(endDate.getMonth() + 1).padStart(2, '0') + '-' +
        String(endDate.getDate()).padStart(2, '0')
      params.append('endDate', endDateStr)
      console.log('DEBUG: Converted endDate from', filters.endDate, 'to', endDateStr, '(Local date)')
    }

    console.log('API Params:', params.toString())
    const response = await axios.get(`/api/shop/orders?${params.toString()}`)

    if (response.data.success) {
      orders.value = response.data.orders
      statistics.value = response.data.statistics
      // Reinitialize swiper after data loads
      nextTick(() => {
        initSwiper()
      })
    } else {
      message.error(response.data.message || 'Lỗi tải danh sách đơn hàng')
    }
  } catch (error) {
    console.error('Error loading orders:', error)
    console.error('Error response:', error.response?.data)
    console.error('Error status:', error.response?.status)
    message.error(`Lỗi tải danh sách đơn hàng: ${error.response?.data?.message || error.message}`)
  } finally {
    loading.value = false
  }
}

const viewOrderDetail = (order) => {
  selectedOrder.value = order
  newStatus.value = order.orderStatus
  newPaymentStatus.value = order.transactionStatus || 'PENDING'
  showOrderDetail.value = true

  // Debug: Log variant information for order items
  console.log('=== DEBUG SHOP ORDER DETAIL ===')
  if (order.orderItems) {
    console.log(`Order items count: ${order.orderItems.length}`)
    for (let i = 0; i < order.orderItems.length; i++) {
      const item = order.orderItems[i]
      console.log(`Item ${i + 1}:`)
      console.log(`  - ProductName: ${item.productName}`)
      console.log(`  - VariantName: ${item.variantName}`)
      console.log(`  - VariantValue: ${item.variantValue}`)
      console.log(`  - ProductVariantId: ${item.productVariantId}`)
      console.log(`  - Formatted variant: ${formatVariantInfo(item.variantName, item.variantValue)}`)
    }
  }
  console.log('=== END DEBUG SHOP ORDER DETAIL ===')
}

const updateOrderStatus = async () => {
  if (!selectedOrder.value || !newStatus.value) return

  updatingStatus.value = true
  try {
    const response = await axios.put(`/api/shop/orders/${selectedOrder.value.id}/status`, {
      status: newStatus.value,
    })

    if (response.data.success) {
      message.success('Cập nhật trạng thái thành công')
      selectedOrder.value = response.data.order
      loadOrders() // Reload to update statistics
    } else {
      message.error(response.data.message || 'Lỗi cập nhật trạng thái')
    }
  } catch (error) {
    console.error('Error updating order status:', error)
    message.error('Lỗi cập nhật trạng thái đơn hàng')
  } finally {
    updatingStatus.value = false
  }
}

const cancelOrder = async (orderId) => {
  if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) return

  try {
    const response = await axios.put(`/api/shop/orders/${orderId}/status`, {
      status: 'CANCELLED',
    })

    if (response.data.success) {
      message.success('Hủy đơn hàng thành công')
      loadOrders() // Reload to update statistics
    } else {
      message.error(response.data.message || 'Lỗi hủy đơn hàng')
    }
  } catch (error) {
    console.error('Error cancelling order:', error)
    message.error('Lỗi hủy đơn hàng')
  }
}

const updatePaymentStatus = async () => {
  if (!selectedOrder.value || !newPaymentStatus.value) return

  updatingPaymentStatus.value = true
  try {
    const response = await axios.put(`/api/shop/orders/${selectedOrder.value.id}/payment-status`, {
      paymentStatus: newPaymentStatus.value,
    })

    if (response.data.success) {
      message.success('Cập nhật trạng thái thanh toán thành công')

      // Cập nhật selectedOrder với dữ liệu mới
      selectedOrder.value = response.data.order

      // Cập nhật order trong danh sách orders
      const orderIndex = orders.value.findIndex(order => order.id === selectedOrder.value.id)
      if (orderIndex !== -1) {
        orders.value[orderIndex] = { ...orders.value[orderIndex], ...response.data.order }
      }

      // Reload để cập nhật statistics
      loadOrders()
    } else {
      message.error(response.data.message || 'Lỗi cập nhật trạng thái thanh toán')
    }
  } catch (error) {
    console.error('Error updating payment status:', error)
    message.error('Lỗi cập nhật trạng thái thanh toán')
  } finally {
    updatingPaymentStatus.value = false
  }
}

const clearFilters = () => {
  isClearing.value = true // Bật flag để tránh tự động điền ngày

  filters.status = null
  filters.paymentMethod = null
  filters.timeFilter = 'TODAY' // Reset về hôm nay
  filters.startDate = null // Không reset về ngày hiện tại
  filters.endDate = null // Không reset về ngày hiện tại

  // Tắt flag sau khi đã set xong
  nextTick(() => {
    isClearing.value = false
    loadOrders()
  })
}

// Swiper initialization
const initSwiper = () => {
  if (swiperInstance) {
    swiperInstance.destroy()
  }

  nextTick(() => {
    if (swiperContainer.value) {
      swiperInstance = new Swiper('.swiper-table', {
        direction: 'horizontal',
        grabCursor: true,
        scrollbar: {
          hide: true
        },
        mousewheel: false,
        keyboard: false,
        allowTouchMove: true,
        resistance: true,
        resistanceRatio: 0.85,
        slidesPerView: 'auto',
        spaceBetween: 0,
        freeMode: true,
        freeModeSticky: true,
        freeModeMomentum: true,
        freeModeMomentumRatio: 0.25,
        freeModeMomentumVelocityRatio: 0.2,
        freeModeMomentumBounce: true,
        freeModeMomentumBounceRatio: 0.25
      })
    }
  })
}

// Status badge functions
const getStatusBadgeClass = (status) => {
  const classMap = {
    PENDING_PROCESSING: 'badge-warning',
    PROCESSED: 'badge-info',
    READY_FOR_PICKUP: 'badge-info',
    IN_TRANSIT: 'badge-info',
    DELIVERED: 'badge-success',
    CANCELLED: 'badge-error',
    PENDING: 'badge-warning',
    PAID: 'badge-success',
    SHIPPING: 'badge-info',
    REFUNDED: 'badge-error',
    RETURNED: 'badge-error',
  }
  return `status-badge ${classMap[status] || 'badge-default'}`
}

const getPaymentStatusBadgeClass = (status) => {
  const classMap = {
    PENDING: 'badge-warning',
    SUCCESS: 'badge-success',
    FAILED: 'badge-error',
    CANCELLED: 'badge-error',
    REFUNDED: 'badge-error',
  }
  return `status-badge ${classMap[status] || 'badge-default'}`
}







// Utility functions
const formatCurrency = (amount) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
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

const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Date(date).toLocaleDateString('vi-VN')
}

const getStatusLabel = (status) => {
  const statusMap = {
    PENDING_PROCESSING: 'Chờ xử lý',
    PROCESSED: 'Đã xử lý',
    READY_FOR_PICKUP: 'Chờ lấy hàng',
    IN_TRANSIT: 'Đang giao hàng',
    DELIVERED: 'Đã giao hàng',
    CANCELLED: 'Đã hủy',
    PENDING: 'Chờ thanh toán',
    PAID: 'Đã thanh toán',
    SHIPPING: 'Đang giao hàng',
    REFUNDED: 'Đã hoàn tiền',
    RETURNED: 'Đã trả hàng',
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    PENDING_PROCESSING: 'warning',
    PROCESSED: 'info',
    READY_FOR_PICKUP: 'info',
    IN_TRANSIT: 'info',
    DELIVERED: 'success',
    CANCELLED: 'error',
    PENDING: 'warning',
    PAID: 'success',
    SHIPPING: 'info',
    REFUNDED: 'error',
    RETURNED: 'error',
  }
  return typeMap[status] || 'default'
}

const getStatusIcon = (status) => {
  const iconMap = {
    PENDING_PROCESSING: Clock,
    PROCESSED: CheckCircle,
    READY_FOR_PICKUP: Package,
    IN_TRANSIT: Truck,
    DELIVERED: Package,
    CANCELLED: XCircle,
    PENDING: Clock,
    PAID: DollarSign,
    SHIPPING: Truck,
    REFUNDED: RotateCcw,
    RETURNED: ArrowLeft,
  }
  return iconMap[status] || Package
}

// Payment status functions
const getPaymentStatusLabel = (status) => {
  const statusMap = {
    PENDING: 'Chờ thanh toán',
    SUCCESS: 'Thanh toán thành công',
    FAILED: 'Thanh toán thất bại',
    CANCELLED: 'Đã hủy thanh toán',
    REFUNDED: 'Đã hoàn tiền',
  }
  return statusMap[status] || status
}

const getPaymentStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    SUCCESS: 'success',
    FAILED: 'error',
    CANCELLED: 'error',
    REFUNDED: 'error',
  }
  return typeMap[status] || 'default'
}

const getPaymentStatusIcon = (status) => {
  const iconMap = {
    PENDING: Clock,
    SUCCESS: CheckCircle,
    FAILED: XCircle,
    CANCELLED: XCircle,
    REFUNDED: RotateCcw,
  }
  return iconMap[status] || Clock
}



// Flag để kiểm tra xem có đang clear filters hay không
const isClearing = ref(false)

// Watch timeFilter changes - không tự động điền ngày
watch(() => filters.timeFilter, (newValue) => {
  // Không tự động điền ngày khi đang clear filters
  if (isClearing.value) return

  // Chỉ reset về null khi chọn CUSTOM
  if (newValue === 'CUSTOM') {
    filters.startDate = null
    filters.endDate = null
  }
  // Các trường hợp khác (TODAY, THIS_WEEK, THIS_MONTH) không tự động điền ngày
})

// Load data on mount
onMounted(() => {
  loadOrders()
  initSwiper()
})
</script>

<style scoped>
/* Custom scrollbar cho Naive UI với màu xanh gradient */
:deep(.n-data-table .n-scrollbar) {
  cursor: grab !important;
  user-select: none !important;
}

:deep(.n-data-table .n-scrollbar:hover) {
  cursor: grabbing !important;
}

:deep(.n-data-table .n-scrollbar-rail) {
  cursor: grab !important;
  user-select: none !important;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%) !important;
}

:deep(.n-data-table .n-scrollbar-rail:hover) {
  cursor: grabbing !important;
}

:deep(.n-data-table .n-scrollbar-content) {
  cursor: grab !important;
  user-select: none !important;
}

:deep(.n-data-table .n-scrollbar-content:hover) {
  cursor: grabbing !important;
}

:deep(.n-data-table .n-scrollbar-thumb) {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  border-radius: 4px !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

:deep(.n-data-table .n-scrollbar-thumb:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%) !important;
}

/* Custom scrollbar cho pagination với màu xanh gradient */
:deep(.n-pagination .n-scrollbar) {
  cursor: grab !important;
  user-select: none !important;
}

:deep(.n-pagination .n-scrollbar:hover) {
  cursor: grabbing !important;
}

:deep(.n-pagination .n-scrollbar-rail) {
  cursor: grab !important;
  user-select: none !important;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%) !important;
}

:deep(.n-pagination .n-scrollbar-rail:hover) {
  cursor: grabbing !important;
}

:deep(.n-pagination .n-scrollbar-thumb) {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  border-radius: 4px !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
}

:deep(.n-pagination .n-scrollbar-thumb:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%) !important;
}

/* Swiper Table Styles */
.swiper-table-container {
  width: 100%;
  height: 100%;
}

.swiper-table {
  width: 100%;
  height: 100%;
}

.swiper-slide {
  width: auto;
  height: auto;
}

.custom-table {
  min-width: 1300px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 150px 250px 120px 180px 180px 200px 120px 100px 120px;
  background-color: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.header-cell {
  padding: 12px 16px;
  font-weight: 600;
  color: #374151;
  text-align: center;
  border-right: 1px solid #e5e7eb;
}

.header-cell:last-child {
  border-right: none;
}

.table-body {
  background-color: white;
}

.table-row {
  display: grid;
  grid-template-columns: 150px 250px 120px 180px 180px 200px 120px 100px 120px;
  border-bottom: 1px solid #f3f4f6;
  transition: background-color 0.2s;
}

.table-row:hover {
  background-color: #f9fafb;
}

.table-row:last-child {
  border-bottom: none;
}

.table-cell {
  padding: 12px 16px;
  border-right: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
}

.table-cell:last-child {
  border-right: none;
}

/* Status Badges */
.status-badge {
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
}

.badge-warning {
  background-color: #fef3c7;
  color: #92400e;
}

.badge-info {
  background-color: #dbeafe;
  color: #1e40af;
}

.badge-success {
  background-color: #d1fae5;
  color: #065f46;
}

.badge-error {
  background-color: #fee2e2;
  color: #991b1b;
}

.badge-default {
  background-color: #f3f4f6;
  color: #374151;
}

/* Action Button */
.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn:hover {
  background-color: #2563eb;
}

.action-btn-cancel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 12px;
  background-color: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
  min-width: fit-content;
}

.action-btn-cancel:hover:not(:disabled) {
  background-color: #dc2626;
}

.action-btn-cancel:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: white;
}

.pagination-info {
  color: #6b7280;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  padding: 6px 12px;
  background-color: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #f9fafb;
  border-color: #9ca3af;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-page {
  padding: 6px 12px;
  background-color: #3b82f6;
  color: white;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
}
</style>