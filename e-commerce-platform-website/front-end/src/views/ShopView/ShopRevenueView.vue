<template>
  <div class="h-full flex flex-col overflow-hidden">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800 mb-4">Thống kê doanh thu</h1>
      <p class="text-gray-600">Theo dõi doanh thu và hiệu suất bán hàng</p>
    </div>

    <!-- Filters -->
    <div class="mb-6">
      <n-card title="Bộ lọc thời gian" class="mb-4">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Thời gian</label>
            <n-select
              v-model:value="filters.period"
              :options="periodOptions"
              placeholder="Chọn thời gian"
              @update:value="loadRevenueStatistics"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Từ ngày</label>
            <n-date-picker
              v-model:value="filters.startDate"
              type="date"
              placeholder="Chọn ngày bắt đầu"
              clearable
              format="dd/MM/yyyy"
              value-format="yyyy-MM-dd"
              @update:value="onDateChange"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Đến ngày</label>
            <n-date-picker
              v-model:value="filters.endDate"
              type="date"
              placeholder="Chọn ngày kết thúc"
              clearable
              format="dd/MM/yyyy"
              value-format="yyyy-MM-dd"
              @update:value="onDateChange"
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

    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
      <div class="relative overflow-hidden rounded-xl bg-gradient-to-br from-green-500 to-emerald-600 p-6 text-white shadow-lg">
        <div class="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -translate-y-16 translate-x-16"></div>
        <div class="relative flex items-center gap-4">
          <div class="p-3 rounded-lg bg-white bg-opacity-20 backdrop-blur-sm">
            <DollarSign class="w-6 h-6 text-green-600" />
          </div>
          <div class="flex-1">
            <div class="text-3xl font-bold">{{ formatCurrency(revenueStats.totalRevenue || 0) }}</div>
            <div class="text-green-100 font-medium">Tổng doanh thu</div>
          </div>
        </div>
      </div>

      <div class="relative overflow-hidden rounded-xl bg-gradient-to-br from-blue-500 to-indigo-600 p-6 text-white shadow-lg">
        <div class="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -translate-y-16 translate-x-16"></div>
        <div class="relative flex items-center gap-4">
          <div class="p-3 rounded-lg bg-white bg-opacity-20 backdrop-blur-sm">
            <ShoppingCart class="w-6 h-6 text-blue-600" />
          </div>
          <div class="flex-1">
            <div class="text-3xl font-bold">{{ revenueStats.totalOrders || 0 }}</div>
            <div class="text-blue-100 font-medium">Đơn hàng thành công</div>
          </div>
        </div>
      </div>

      <div class="relative overflow-hidden rounded-xl bg-gradient-to-br from-purple-500 to-violet-600 p-6 text-white shadow-lg">
        <div class="absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full -translate-y-16 translate-x-16"></div>
        <div class="relative flex items-center gap-4">
          <div class="p-3 rounded-lg bg-white bg-opacity-20 backdrop-blur-sm">
            <TrendingUp class="w-6 h-6 text-purple-600" />
          </div>
          <div class="flex-1">
            <div class="text-3xl font-bold">{{ formatCurrency(revenueStats.averageOrderValue || 0) }}</div>
            <div class="text-purple-100 font-medium">Giá trị đơn hàng TB</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts and Tables -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Revenue Chart -->
      <n-card title="Biểu đồ doanh thu" class="h-96">
        <div v-if="loading" class="flex items-center justify-center h-64">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span class="ml-2 text-gray-600">Đang tải...</span>
        </div>
        <div v-else-if="revenueStats.revenueByPeriod && revenueStats.revenueByPeriod.length > 0" class="h-64">
          <!-- Chart sẽ được thêm sau -->
          <div class="text-center text-gray-500 py-8">
            Biểu đồ doanh thu theo thời gian
          </div>
        </div>
        <div v-else class="flex items-center justify-center h-64">
          <n-empty description="Không có dữ liệu doanh thu" />
        </div>
      </n-card>

      <!-- Top Products -->
      <n-card title="Top sản phẩm bán chạy" class="h-96">
        <div v-if="loading" class="flex items-center justify-center h-64">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span class="ml-2 text-gray-600">Đang tải...</span>
        </div>
        <div v-else-if="revenueStats.topProducts && revenueStats.topProducts.length > 0" class="overflow-y-auto h-64">
          <div class="space-y-3">
            <div v-for="(product, index) in revenueStats.topProducts" :key="index"
                 class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div class="flex-1">
                <div class="font-medium text-gray-900">{{ product.productName }}</div>
                <div class="text-sm text-gray-500">
                  {{ product.variantName }}: {{ product.variantValue }}
                </div>
                <div class="text-xs text-gray-400">
                  {{ product.orderCount }} đơn hàng
                </div>
              </div>
              <div class="text-right">
                <div class="font-semibold text-green-600">
                  {{ formatCurrency(product.totalRevenue) }}
                </div>
                <div class="text-sm text-gray-500">
                  {{ product.totalQuantity }} sản phẩm
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="flex items-center justify-center h-64">
          <n-empty description="Không có dữ liệu sản phẩm" />
        </div>
      </n-card>
    </div>

    <!-- Revenue Details Table -->
    <n-card title="Chi tiết doanh thu" class="mt-6">
      <div v-if="loading" class="flex items-center justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <span class="ml-2 text-gray-600">Đang tải...</span>
      </div>
      <div v-else-if="revenueStats.revenueByPeriod && revenueStats.revenueByPeriod.length > 0">
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Ngày
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Doanh thu
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Số đơn hàng
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Giá trị TB
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(period, index) in revenueStats.revenueByPeriod" :key="index">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {{ formatDate(period.date) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-semibold">
                  {{ formatCurrency(period.revenue) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ period.orderCount }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ formatCurrency(period.revenue / period.orderCount) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-else class="flex items-center justify-center py-8">
        <n-empty description="Không có dữ liệu doanh thu" />
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import {
  DollarSign,
  ShoppingCart,
  TrendingUp,
  RefreshCw
} from 'lucide-vue-next'

const message = useMessage()

// Reactive data
const loading = ref(false)
const revenueStats = ref({
  totalRevenue: 0,
  totalOrders: 0,
  averageOrderValue: 0,
  revenueByPeriod: [],
  topProducts: []
})

// Filters
const filters = reactive({
  period: 'THIS_MONTH',
  startDate: null,
  endDate: null
})

// Options
const periodOptions = [
  { label: 'Hôm nay', value: 'TODAY' },
  { label: 'Tuần này', value: 'THIS_WEEK' },
  { label: 'Tháng này', value: 'THIS_MONTH' },
  { label: 'Năm nay', value: 'THIS_YEAR' },
  { label: 'Tùy chọn', value: 'CUSTOM' }
]

// Methods
const loadRevenueStatistics = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (filters.period) params.append('period', filters.period)

    // Convert timestamp to yyyy-MM-dd format using local date
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

    console.log('DEBUG: Sending params - period:', filters.period, 'startDate:', filters.startDate, 'endDate:', filters.endDate)
    console.log('DEBUG: URL params:', params.toString())

    const response = await axios.get(`/api/shop/orders/revenue-statistics?${params.toString()}`)

    if (response.data.success) {
      revenueStats.value = response.data.revenueStatistics
    } else {
      message.error(response.data.message || 'Lỗi tải thống kê doanh thu')
    }
  } catch (error) {
    console.error('Error loading revenue statistics:', error)
    message.error('Lỗi tải thống kê doanh thu')
  } finally {
    loading.value = false
  }
}

const onDateChange = () => {
  console.log('DEBUG: Date changed - startDate:', filters.startDate, 'endDate:', filters.endDate)
  // Khi người dùng chọn date, set period thành CUSTOM
  if (filters.startDate || filters.endDate) {
    filters.period = 'CUSTOM'
  }
  loadRevenueStatistics()
}

const clearFilters = () => {
  filters.period = 'THIS_MONTH'
  filters.startDate = null
  filters.endDate = null
  loadRevenueStatistics()
}

const formatCurrency = (amount) => {
  if (!amount) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

// Load data on mount
onMounted(() => {
  loadRevenueStatistics()
})
</script>

<style scoped>
/* Custom styles if needed */
</style>
