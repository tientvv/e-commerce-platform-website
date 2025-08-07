<template>
  <div>
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-4">Quản lý đơn hàng</h1>

      <!-- Statistics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4 mb-6">
        <div class="p-4 rounded-lg flex items-center gap-3 bg-blue-50 border border-blue-200">
          <div class="p-2 rounded-lg bg-blue-500">
            <Package class="w-5 h-5 text-white" />
          </div>
          <div class="flex-1">
            <div class="text-xl font-bold text-gray-900">{{ statistics.totalOrders || 0 }}</div>
            <div class="text-sm text-gray-600">Tổng đơn hàng</div>
          </div>
        </div>

        <div class="p-4 rounded-lg flex items-center gap-3 bg-yellow-50 border border-yellow-200">
          <div class="p-2 rounded-lg bg-yellow-500">
            <Clock class="w-5 h-5 text-white" />
          </div>
          <div class="flex-1">
            <div class="text-xl font-bold text-gray-900">{{ statistics.pendingOrders || 0 }}</div>
            <div class="text-sm text-gray-600">Chờ xử lý</div>
          </div>
        </div>

        <div class="p-4 rounded-lg flex items-center gap-3 bg-green-50 border border-green-200">
          <div class="p-2 rounded-lg bg-green-500">
            <CheckCircle class="w-5 h-5 text-white" />
          </div>
          <div class="flex-1">
            <div class="text-xl font-bold text-gray-900">{{ statistics.paidOrders || 0 }}</div>
            <div class="text-sm text-gray-600">Đã thanh toán</div>
          </div>
        </div>

        <div class="p-4 rounded-lg flex items-center gap-3 bg-purple-50 border border-purple-200">
          <div class="p-2 rounded-lg bg-purple-500">
            <Truck class="w-5 h-5 text-white" />
          </div>
          <div class="flex-1">
            <div class="text-xl font-bold text-gray-900">{{ statistics.deliveredOrders || 0 }}</div>
            <div class="text-sm text-gray-600">Đã giao hàng</div>
          </div>
        </div>

        <div class="p-4 rounded-lg flex items-center gap-3 bg-red-50 border border-red-200">
          <div class="p-2 rounded-lg bg-red-500">
            <XCircle class="w-5 h-5 text-white" />
          </div>
          <div class="flex-1">
            <div class="text-xl font-bold text-gray-900">{{ statistics.cancelledOrders || 0 }}</div>
            <div class="text-sm text-gray-600">Đã hủy</div>
          </div>
        </div>

        <div class="p-4 rounded-lg flex items-center gap-3 bg-orange-50 border border-orange-200">
          <div class="p-2 rounded-lg bg-orange-500">
            <RotateCcw class="w-5 h-5 text-white" />
          </div>
          <div class="flex-1">
            <div class="text-xl font-bold text-gray-900">{{ statistics.refundedOrders || 0 }}</div>
            <div class="text-sm text-gray-600">Đã hoàn tiền</div>
          </div>
        </div>

        <div class="p-4 rounded-lg flex items-center gap-3 bg-emerald-50 border border-emerald-200">
          <div class="p-2 rounded-lg bg-emerald-500">
            <DollarSign class="w-5 h-5 text-white" />
          </div>
          <div class="flex-1">
            <div class="text-xl font-bold text-gray-900">{{ formatCurrency(statistics.totalRevenue) }}</div>
            <div class="text-sm text-gray-600">Tổng doanh thu</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="mb-6">
      <n-card title="Bộ lọc" class="mb-6">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
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
        </div>

        <div class="flex justify-end">
          <n-button @click="clearFilters" secondary>
            <template #icon>
              <RefreshCw class="w-4 h-4" />
            </template>
            Xóa bộ lọc
          </n-button>
        </div>
      </n-card>
    </div>

    <!-- Orders Table -->
    <div class="mb-6">
      <n-card title="Danh sách đơn hàng" class="mb-6">
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

        <n-data-table
          :columns="columns"
          :data="orders"
          :pagination="pagination"
          :loading="loading"
          :row-key="(row) => row.id"
          class="min-h-[400px]"
        />
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
              <label class="text-sm font-medium text-gray-700">Phương thức thanh toán:</label>
              <span class="text-gray-900">{{ selectedOrder.paymentName || 'N/A' }}</span>
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
                <div class="text-sm text-gray-600">{{ item.variantName }}</div>
                <div class="text-sm text-gray-500">{{ formatCurrency(item.productPrice) }} x {{ item.quantity }}</div>
              </div>
              <div class="font-semibold text-gray-900">
                {{ formatCurrency(item.productPrice * item.quantity) }}
              </div>
            </div>
          </div>
        </div>

        <!-- Status Update -->
        <div>
          <h4 class="text-md font-semibold mb-3">Cập nhật trạng thái</h4>
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
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useMessage, NTag, NButton } from 'naive-ui'
import { Package, Clock, CheckCircle, Truck, XCircle, DollarSign, RefreshCw, Eye, RotateCcw, ArrowLeft } from 'lucide-vue-next'
import axios from '~/utils/axios'

const message = useMessage()

// Reactive data
const loading = ref(false)
const orders = ref([])
const statistics = ref({})
const showOrderDetail = ref(false)
const selectedOrder = ref(null)
const newStatus = ref('')
const updatingStatus = ref(false)

// Filters
const filters = reactive({
  status: null,
  paymentMethod: null,
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
  { label: 'Chờ xử lý', value: 'PENDING' },
  { label: 'Đã xử lý', value: 'PROCESSED' },
  { label: 'Đã thanh toán', value: 'PAID' },
  { label: 'Đang giao hàng', value: 'SHIPPING' },
  { label: 'Đã giao hàng', value: 'DELIVERED' },
  { label: 'Đã hủy', value: 'CANCELLED' },
  { label: 'Đã hoàn tiền', value: 'REFUNDED' },
  { label: 'Đã trả hàng', value: 'RETURNED' },
]

const paymentOptions = [
  { label: 'Thanh toán khi nhận hàng', value: 'COD' },
  { label: 'Thanh toán bằng ngân hàng', value: 'PAYOS' },
]

// Table columns
const columns = [
  {
    title: 'Mã đơn hàng',
    key: 'id',
    width: 200,
    render: (row) => {
      return h('span', { class: 'font-mono text-sm' }, row.id.substring(0, 8).toUpperCase())
    },
  },
  {
    title: 'Khách hàng',
    key: 'customer',
    render: (row) => {
      return h('div', [
        h('div', { class: 'font-medium' }, row.accountName || 'N/A'),
        h('div', { class: 'text-sm text-gray-500' }, row.accountEmail || 'N/A'),
      ])
    },
  },
  {
    title: 'Tổng tiền',
    key: 'totalAmount',
    render: (row) => {
      return h('span', { class: 'font-semibold' }, formatCurrency(row.totalAmount))
    },
  },
  {
    title: 'Trạng thái',
    key: 'orderStatus',
    render: (row) => {
      const StatusIcon = getStatusIcon(row.orderStatus)
      return h(
        NTag,
        {
          type: getStatusType(row.orderStatus),
          size: 'medium',
          round: true,
        },
        {
          default: () => h('div', { class: 'flex items-center gap-2' }, [
            h(StatusIcon, { class: 'w-4 h-4' }),
            h('span', { class: 'font-medium' }, getStatusLabel(row.orderStatus)),
          ]),
        },
      )
    },
  },

  {
    title: 'Phương thức thanh toán',
    key: 'payment',
    render: (row) => {
      return h('span', row.paymentName || 'N/A')
    },
  },
  {
    title: 'Ngày đặt',
    key: 'orderDate',
    render: (row) => {
      return h('span', formatDate(row.orderDate))
    },
  },
  {
    title: 'Thao tác',
    key: 'actions',
    width: 120,
    render: (row) => {
      return h('div', { class: 'flex gap-2' }, [
        h(
          NButton,
          {
            size: 'small',
            onClick: () => viewOrderDetail(row),
          },
          {
            icon: () => h(Eye, { class: 'w-4 h-4' }),
            default: () => 'Xem',
          },
        ),
      ])
    },
  },
]

// Methods
const loadOrders = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (filters.status) params.append('status', filters.status)
    if (filters.paymentMethod) params.append('paymentMethod', filters.paymentMethod)

    const response = await axios.get(`/api/shop/orders?${params.toString()}`)

    if (response.data.success) {
      orders.value = response.data.orders
      statistics.value = response.data.statistics
    } else {
      message.error(response.data.message || 'Lỗi tải danh sách đơn hàng')
    }
  } catch (error) {
    console.error('Error loading orders:', error)
    message.error('Lỗi tải danh sách đơn hàng')
  } finally {
    loading.value = false
  }
}

const viewOrderDetail = (order) => {
  selectedOrder.value = order
  newStatus.value = order.orderStatus
  showOrderDetail.value = true
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

const clearFilters = () => {
  filters.status = null
  filters.paymentMethod = null
  loadOrders()
}

// Utility functions
const formatCurrency = (amount) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount)
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

// Load data on mount
onMounted(() => {
  loadOrders()
})
</script>
