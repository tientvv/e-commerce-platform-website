<template>
  <n-space vertical :size="24" class="w-full">
        <!-- Page Header -->
    <div>
      <n-h1 style="font-size: 16px">Dashboard</n-h1>
      <n-text depth="3">Tổng quan hệ thống quản trị</n-text>
    </div>

    <!-- Statistics Cards -->
    <n-spin :show="loading">
      <div class="stats-grid">
        <n-card v-for="stat in stats" :key="stat.title" hoverable>
        <div class="stat-card-content">
          <div class="stat-header">
            <n-avatar :size="48" :style="{ backgroundColor: stat.bgColor }">
              <n-icon :size="24">
                <component :is="stat.icon" />
              </n-icon>
            </n-avatar>
            <n-tag :type="stat.trendType" size="small">
              {{ stat.trend }}
            </n-tag>
          </div>

          <div class="stat-info">
            <n-text depth="3">{{ stat.title }}</n-text>
            <n-h2 style="margin: 8px 0">{{ stat.value }}</n-h2>
            <n-text depth="3" size="small">{{ stat.subtitle }}</n-text>
          </div>
        </div>
      </n-card>
      </div>
    </n-spin>

    <!-- Charts Section -->
    <div class="charts-grid">
      <!-- Categories Chart -->
      <n-card title="Thống kê theo danh mục">
        <div class="chart-container">
          <div class="chart-item">
            <div class="chart-label">Danh mục</div>
            <div class="chart-value">{{ stats[0].value }}</div>
            <div class="chart-bar" :style="{ width: getChartPercentage(stats[0].value, 50) + '%' }"></div>
          </div>
          <div class="chart-item">
            <div class="chart-label">Sản phẩm</div>
            <div class="chart-value">{{ stats[1].value }}</div>
            <div class="chart-bar" :style="{ width: getChartPercentage(stats[1].value, 50) + '%' }"></div>
          </div>
          <div class="chart-item">
            <div class="chart-label">Người dùng</div>
            <div class="chart-value">{{ stats[2].value }}</div>
            <div class="chart-bar" :style="{ width: getChartPercentage(stats[2].value, 50) + '%' }"></div>
          </div>
          <div class="chart-item">
            <div class="chart-label">Cửa hàng</div>
            <div class="chart-value">{{ stats[3].value }}</div>
            <div class="chart-bar" :style="{ width: getChartPercentage(stats[3].value, 50) + '%' }"></div>
          </div>
        </div>
      </n-card>

      <!-- Growth Chart -->
      <n-card title="Tăng trưởng hệ thống">
        <div class="growth-chart">
          <div class="growth-item">
            <n-icon size="24" color="#3b82f6">
              <Package />
            </n-icon>
            <div class="growth-info">
              <div class="growth-label">Danh mục mới</div>
              <div class="growth-value">+{{ stats[0].trend }}</div>
            </div>
          </div>
          <div class="growth-item">
            <n-icon size="24" color="#10b981">
              <ShoppingBag />
            </n-icon>
            <div class="growth-info">
              <div class="growth-label">Sản phẩm mới</div>
              <div class="growth-value">+{{ stats[1].trend }}</div>
            </div>
          </div>
          <div class="growth-item">
            <n-icon size="24" color="#8b5cf6">
              <Users />
            </n-icon>
            <div class="growth-info">
              <div class="growth-label">Người dùng mới</div>
              <div class="growth-value">+{{ stats[2].trend }}</div>
            </div>
          </div>
          <div class="growth-item">
            <n-icon size="24" color="#f59e0b">
              <Store />
            </n-icon>
            <div class="growth-info">
              <div class="growth-label">Cửa hàng mới</div>
              <div class="growth-value">+{{ stats[3].trend }}</div>
            </div>
          </div>
        </div>
      </n-card>
    </div>

    <!-- System Status -->
    <n-card title="Trạng thái hệ thống">
      <div class="system-status-grid">
        <div v-for="status in systemStatus" :key="status.name" class="status-item">
          <n-statistic :label="status.name" :value="status.value">
            <template #suffix>{{ status.suffix }}</template>
          </n-statistic>
          <n-progress type="line" :percentage="status.value" :color="status.color" :height="8" style="margin: 12px 0" />
          <n-tag :type="status.statusType" size="small">{{ status.status }}</n-tag>
        </div>
      </div>
    </n-card>
  </n-space>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Package, Users, ShoppingBag, Store } from 'lucide-vue-next'
import { NH1, NH2, NSpace, NCard, NAvatar, NIcon, NTag, NText, NStatistic, NProgress, NSpin, useMessage } from 'naive-ui'
import axios from '../../utils/axios'

const message = useMessage()

const loading = ref(false)

const stats = ref([
  {
    title: 'Tổng danh mục',
    value: '0',
    subtitle: 'Đang tải...',
    trend: '+0%',
    icon: Package,
    bgColor: '#3b82f6',
    trendType: 'info',
  },
  {
    title: 'Tổng sản phẩm',
    value: '0',
    subtitle: 'Đang tải...',
    trend: '+0%',
    icon: ShoppingBag,
    bgColor: '#10b981',
    trendType: 'success',
  },
  {
    title: 'Tổng người dùng',
    value: '0',
    subtitle: 'Đang tải...',
    trend: '+0%',
    icon: Users,
    bgColor: '#8b5cf6',
    trendType: 'warning',
  },
  {
    title: 'Tổng cửa hàng',
    value: '0',
    subtitle: 'Đang tải...',
    trend: '+0%',
    icon: Store,
    bgColor: '#f59e0b',
    trendType: 'error',
  },
])

const systemStatus = ref([
  {
    name: 'CPU Usage',
    value: 45,
    suffix: '%',
    status: 'Bình thường',
    color: '#10b981',
    statusType: 'success',
  },
  {
    name: 'Memory',
    value: 72,
    suffix: '%',
    status: 'Cảnh báo',
    color: '#f59e0b',
    statusType: 'warning',
  },
  {
    name: 'Storage',
    value: 28,
    suffix: '%',
    status: 'Tốt',
    color: '#3b82f6',
    statusType: 'info',
  },
])



// Load real data from API
const fetchDashboardStats = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/dashboard/stats')
    const data = response.data.stats

    // Cập nhật thống kê danh mục
    stats.value[0].value = data.totalCategories?.toString() || '0'
    stats.value[0].subtitle = 'Tổng số danh mục'
    stats.value[0].trend = '+0%'

    // Cập nhật thống kê sản phẩm
    stats.value[1].value = data.totalProducts?.toString() || '0'
    stats.value[1].subtitle = 'Tổng số sản phẩm'
    stats.value[1].trend = '+0%'

    // Cập nhật thống kê người dùng
    stats.value[2].value = data.totalUsers?.toString() || '0'
    stats.value[2].subtitle = 'Tổng số người dùng'
    stats.value[2].trend = '+0%'

    // Cập nhật thống kê cửa hàng
    stats.value[3].value = data.totalShops?.toString() || '0'
    stats.value[3].subtitle = 'Tổng số cửa hàng'
    stats.value[3].trend = '+0%'
  } catch (error) {
    console.error('Error fetching dashboard stats:', error)
    message.error('Lỗi khi tải thống kê dashboard')
  } finally {
    loading.value = false
  }
}

// Chart helper method
const getChartPercentage = (value, maxValue) => {
  const numValue = parseInt(value) || 0
  return Math.min((numValue / maxValue) * 100, 100)
}

onMounted(() => {
  fetchDashboardStats()
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

@media (min-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.stat-card-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.activities-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  background-color: #f5f5f5;
}

.activity-avatar {
  flex-shrink: 0;
}

.activity-content {
  flex-grow: 1;
}

.activity-title {
  font-weight: bold;
  margin-bottom: 4px;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.system-status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 16px;
}

.chart-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.chart-label {
  flex: 1;
  font-weight: 500;
  color: #374151;
}

.chart-value {
  font-weight: bold;
  color: #1f2937;
  min-width: 40px;
  text-align: right;
}

.chart-bar {
  height: 8px;
  background: linear-gradient(90deg, #3b82f6, #1d4ed8);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.growth-chart {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.growth-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
}

.growth-info {
  flex: 1;
}

.growth-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 4px;
}

.growth-value {
  font-weight: bold;
  color: #1f2937;
  font-size: 18px;
}
</style>
