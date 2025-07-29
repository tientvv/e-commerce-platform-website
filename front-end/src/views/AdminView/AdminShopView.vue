<template>
  <n-space vertical :size="24" class="w-full">
    <!-- Page Header -->
    <div class="flex items-center justify-between">
      <div>
        <n-h1>Quản lý cửa hàng</n-h1>
        <n-text depth="3">Duyệt và quản lý các cửa hàng trên hệ thống</n-text>
      </div>
    </div>

    <!-- Shops Table -->
    <n-card>
      <template #header>
        <div class="flex items-center justify-between">
          <n-h3>Danh sách cửa hàng</n-h3>
          <n-text depth="3">{{ shops.length }} cửa hàng</n-text>
        </div>
      </template>

      <n-spin :show="loading">
        <n-data-table
          v-if="!loading && shops.length > 0"
          :columns="columns"
          :data="shops"
          :row-key="(row) => row.id"
          :pagination="pagination"
        />

        <!-- Empty State -->
        <n-empty v-else-if="!loading && shops.length === 0" description="Chưa có cửa hàng nào">
          <template #icon>
            <n-icon size="48" color="#d1d5db">
              <Store />
            </n-icon>
          </template>
        </n-empty>
      </n-spin>
    </n-card>

    <!-- Shop Details Modal -->
    <n-modal
      v-model:show="detailModalVisible"
      :title="'Chi tiết cửa hàng: ' + selectedShop?.name"
      preset="card"
      :style="{ width: '600px' }"
    >
      <div v-if="selectedShop">
        <n-descriptions :column="2" bordered>
          <n-descriptions-item label="Tên cửa hàng">
            {{ selectedShop.name }}
          </n-descriptions-item>
          <n-descriptions-item label="Mô tả">
            {{ selectedShop.description || 'Không có mô tả' }}
          </n-descriptions-item>
          <n-descriptions-item label="Chủ cửa hàng">
            {{ selectedShop.ownerName || 'N/A' }}
          </n-descriptions-item>
          <n-descriptions-item label="Email">
            {{ selectedShop.ownerEmail || 'N/A' }}
          </n-descriptions-item>
          <n-descriptions-item label="Trạng thái">
            <n-tag :type="getShopStatusType(selectedShop.status)">
              {{ getShopStatusText(selectedShop.status) }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="Ngày tạo">
            {{ formatDate(selectedShop.createdAt) }}
          </n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>

    <!-- Status Update Modal -->
    <n-modal
      v-model:show="statusModalVisible"
      preset="dialog"
      title="Cập nhật trạng thái cửa hàng"
      positive-text="Cập nhật"
      negative-text="Hủy"
      @positive-click="handleStatusUpdate"
    >
      <n-space vertical>
        <n-text
          >Cập nhật trạng thái cho cửa hàng: <strong>{{ selectedShop?.name }}</strong></n-text
        >
        <n-select v-model:value="newStatus" :options="statusOptions" placeholder="Chọn trạng thái mới" />
      </n-space>
    </n-modal>
  </n-space>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import axios from '../../utils/axios'
import { Store, Eye, Edit } from 'lucide-vue-next'
import {
  NH1,
  NH3,
  NSpace,
  NCard,
  NText,
  NSpin,
  NDataTable,
  NEmpty,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NTag,
  NIcon,
  NButton,
  NSelect,
  useMessage,
} from 'naive-ui'

const message = useMessage()

// State
const shops = ref([])
const loading = ref(false)
const detailModalVisible = ref(false)
const statusModalVisible = ref(false)
const selectedShop = ref(null)
const newStatus = ref('')

// Status options
const statusOptions = [
  { label: 'Hoạt động', value: 'ACTIVE' },
  { label: 'Khóa', value: 'BLOCKED' },
]

// Table columns
const columns = [
  {
    title: 'Tên cửa hàng',
    key: 'name',
    render(row) {
      return h('div', { style: { fontWeight: '500' } }, row.name)
    },
  },
  {
    title: 'Chủ cửa hàng',
    key: 'ownerName',
    render(row) {
      return row.ownerName || 'N/A'
    },
  },
  {
    title: 'Trạng thái',
    key: 'status',
    width: 120,
    render(row) {
      return h(
        NTag,
        {
          type: getShopStatusType(row.status),
          size: 'small',
        },
        {
          default: () => getShopStatusText(row.status),
        },
      )
    },
  },
  {
    title: 'Ngày tạo',
    key: 'createdAt',
    render(row) {
      return formatDate(row.createdAt)
    },
  },
  {
    title: 'Thao tác',
    key: 'actions',
    width: 200,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: 'info',
              onClick: () => showShopDetails(row),
            },
            {
              icon: () => h(NIcon, null, { default: () => h(Eye) }),
              default: () => 'Chi tiết',
            },
          ),
          h(
            NButton,
            {
              size: 'small',
              type: 'warning',
              onClick: () => showStatusModal(row),
            },
            {
              icon: () => h(NIcon, null, { default: () => h(Edit) }),
              default: () => 'Trạng thái',
            },
          ),
        ],
      })
    },
  },
]

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

// Methods
const fetchShops = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/admin/shops')
    shops.value = response.data.shops || []
  } catch {
    message.error('Không thể tải danh sách cửa hàng')
  } finally {
    loading.value = false
  }
}

const showShopDetails = (shop) => {
  selectedShop.value = shop
  detailModalVisible.value = true
}

const showStatusModal = (shop) => {
  selectedShop.value = shop
  newStatus.value = shop.status
  statusModalVisible.value = true
}

const handleStatusUpdate = async () => {
  try {
    await axios.put(`/api/admin/shops/${selectedShop.value.id}/status`, {
      status: newStatus.value,
    })
    message.success('Cập nhật trạng thái thành công')
    await fetchShops()
    statusModalVisible.value = false
  } catch {
    message.error('Không thể cập nhật trạng thái')
  }
}

// Helper functions
const getShopStatusType = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'BLOCKED':
      return 'error'
    default:
      return 'info'
  }
}

const getShopStatusText = (status) => {
  switch (status) {
    case 'ACTIVE':
      return 'Hoạt động'
    case 'BLOCKED':
      return 'Khóa'
    default:
      return 'Không xác định'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// Load data on mount
onMounted(() => {
  fetchShops()
})
</script>
