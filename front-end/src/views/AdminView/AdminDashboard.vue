<template>
  <n-space vertical :size="24" class="w-full">
    <!-- Page Header -->
    <div>
      <n-h1 style="font-size: 16px">Dashboard</n-h1>
      <n-text depth="3">Tổng quan hệ thống quản trị</n-text>
    </div>

    <!-- Statistics Cards -->
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

    <!-- Recent Activities -->
    <n-card title="Hoạt động gần đây">
      <template #header-extra>
        <n-button text type="primary" @click="viewAllActivities"> Xem tất cả → </n-button>
      </template>

      <div class="activities-container">
        <div v-for="activity in activities" :key="activity.id" class="activity-item">
          <div class="activity-avatar">
            <n-avatar :size="40" :style="{ backgroundColor: activity.bgColor }">
              <n-icon :size="20">
                <component :is="activity.icon" />
              </n-icon>
            </n-avatar>
          </div>

          <div class="activity-content">
            <div class="activity-title">{{ activity.title }}</div>
            <div class="activity-meta">
              <n-text depth="3" size="small">{{ activity.time }}</n-text>
              <n-tag size="small" :type="getActivityTagType(activity.type)">
                {{ getActivityTypeText(activity.type) }}
              </n-tag>
            </div>
          </div>
        </div>
      </div>

      <n-empty v-if="activities.length === 0" description="Chưa có hoạt động nào" />
    </n-card>

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
import { useRouter } from 'vue-router'
import { Package, Users, ShoppingBag, Store, UserPlus, ShoppingCart } from 'lucide-vue-next'
import { NH1, NH2, NSpace, NCard, NAvatar, NIcon, NTag, NText, NButton, NStatistic, NProgress, NEmpty } from 'naive-ui'

const router = useRouter()

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

const activities = ref([
  {
    id: 1,
    title: 'Sản phẩm mới được thêm',
    time: '5 phút trước',
    type: 'product',
    icon: ShoppingBag,
    bgColor: '#10b981',
  },
  {
    id: 2,
    title: 'Đơn hàng #1234 được tạo',
    time: '15 phút trước',
    type: 'order',
    icon: ShoppingCart,
    bgColor: '#3b82f6',
  },
  {
    id: 3,
    title: 'Người dùng mới đăng ký',
    time: '1 giờ trước',
    type: 'user',
    icon: UserPlus,
    bgColor: '#8b5cf6',
  },
  {
    id: 4,
    title: 'Cửa hàng ABC được duyệt',
    time: '2 giờ trước',
    type: 'shop',
    icon: Store,
    bgColor: '#f59e0b',
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

const viewAllActivities = () => {
  router.push('/admin/activities')
}

const getActivityTagType = (type) => {
  switch (type) {
    case 'product':
      return 'info'
    case 'order':
      return 'success'
    case 'user':
      return 'warning'
    case 'shop':
      return 'error'
    default:
      return 'info'
  }
}

const getActivityTypeText = (type) => {
  switch (type) {
    case 'product':
      return 'Sản phẩm'
    case 'order':
      return 'Đơn hàng'
    case 'user':
      return 'Người dùng'
    case 'shop':
      return 'Cửa hàng'
    default:
      return 'Hoạt động'
  }
}

// Simulate data loading
onMounted(() => {
  setTimeout(() => {
    stats.value[0].value = '24'
    stats.value[0].subtitle = '+3 tuần này'
    stats.value[0].trend = '+14%'

    stats.value[1].value = '156'
    stats.value[1].subtitle = '+12 tuần này'
    stats.value[1].trend = '+8.3%'

    stats.value[2].value = '89'
    stats.value[2].subtitle = '+7 tuần này'
    stats.value[2].trend = '+8%'

    stats.value[3].value = '23'
    stats.value[3].subtitle = '+3 tuần này'
    stats.value[3].trend = '+15%'
  }, 1000)
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
</style>
