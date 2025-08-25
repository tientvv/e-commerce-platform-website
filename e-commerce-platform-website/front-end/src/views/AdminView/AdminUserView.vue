<template>
  <n-space vertical :size="24" class="w-full">
    <!-- Page Header -->
    <div class="flex items-center justify-between">
      <div>
        <n-h1>Quản lý người dùng</n-h1>
        <n-text depth="3">Quản lý tài khoản người dùng trên hệ thống</n-text>
      </div>
    </div>



    <!-- Users Table -->
    <n-card>
      <template #header>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <n-text depth="3">{{ users.length }} người dùng</n-text>
          </div>
          <div class="flex items-center gap-2">
            <n-input
              v-model:value="searchTerm"
              placeholder="Tìm kiếm username hoặc email..."
              style="width: 300px"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <n-icon>
                  <Search />
                </n-icon>
              </template>
            </n-input>
            <n-button @click="handleSearch" type="primary">
              Tìm kiếm
            </n-button>
            <n-button @click="clearSearch" v-if="searchTerm">
              Xóa tìm kiếm
            </n-button>
            <n-button @click="refreshData" type="info">
              <template #icon>
                <n-icon>
                  <RefreshCw />
                </n-icon>
              </template>
              Làm mới
            </n-button>
          </div>
        </div>
      </template>

      <n-spin :show="loading">
        <!-- Search Results Info -->
        <div v-if="!loading && searchTerm && users.length > 0" class="mb-4">
          <n-alert type="info" show-icon>
            Tìm thấy {{ users.length }} người dùng với từ khóa "{{ searchTerm }}" (tìm theo username hoặc email, không bao gồm tài khoản ADMIN)
          </n-alert>
        </div>

        <n-data-table
          v-if="!loading && users.length > 0"
          :columns="columns"
          :data="users"
          :row-key="(row) => row.id"
          :pagination="pagination"
        />

                <!-- Empty State -->
        <n-empty
          v-else-if="!loading && users.length === 0"
          :description="searchTerm ? `Không tìm thấy người dùng nào với từ khóa '${searchTerm}' (tìm theo username hoặc email, không bao gồm tài khoản ADMIN)` : 'Chưa có người dùng nào (không bao gồm tài khoản ADMIN)'"
        >
          <template #icon>
            <n-icon size="48" color="#d1d5db">
              <Users />
            </n-icon>
          </template>
        </n-empty>
      </n-spin>
    </n-card>

    <!-- User Details Modal -->
    <n-modal
      v-model:show="detailModalVisible"
      :title="'Chi tiết người dùng: ' + selectedUser?.username"
      preset="card"
      :style="{ width: '600px' }"
    >
      <div v-if="selectedUser">
        <n-descriptions :column="2" bordered>
          <n-descriptions-item label="Username">
            {{ selectedUser.username }}
          </n-descriptions-item>
          <n-descriptions-item label="Họ tên">
            {{ selectedUser.name || 'Chưa cập nhật' }}
          </n-descriptions-item>
          <n-descriptions-item label="Email">
            {{ selectedUser.email }}
          </n-descriptions-item>
          <n-descriptions-item label="Số điện thoại">
            {{ selectedUser.phone || 'Chưa cập nhật' }}
          </n-descriptions-item>
          <n-descriptions-item label="Trạng thái">
            <n-tag :type="selectedUser.isActive ? 'success' : 'error'">
              {{ selectedUser.isActive ? 'Hoạt động' : 'Bị khóa' }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="Ngày tạo">
            {{ formatDate(selectedUser.createdAt) }}
          </n-descriptions-item>
          <n-descriptions-item label="Đăng nhập cuối">
            {{ formatDate(selectedUser.lastLogin) }}
          </n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>

    <!-- Status Toggle Modal -->
    <n-modal
      v-model:show="statusModalVisible"
      preset="dialog"
      :title="selectedUser?.isActive ? 'Khóa tài khoản' : 'Mở khóa tài khoản'"
      :positive-text="selectedUser?.isActive ? 'Khóa' : 'Mở khóa'"
      negative-text="Hủy"
      @positive-click="handleStatusToggle"
    >
      <n-text>
        Bạn có chắc chắn muốn {{ selectedUser?.isActive ? 'khóa' : 'mở khóa' }} tài khoản
        <strong>{{ selectedUser?.username }}</strong> không?
      </n-text>
    </n-modal>
  </n-space>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import axios from '../../utils/axios'
import { Users, Eye, Lock, Unlock, Search, RefreshCw } from 'lucide-vue-next'
import {
  NH1,
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
  NInput,
  NAlert,
  useMessage,
} from 'naive-ui'

const message = useMessage()

// State
const users = ref([])
const loading = ref(false)
const detailModalVisible = ref(false)
const statusModalVisible = ref(false)
const selectedUser = ref(null)
const searchTerm = ref('')

// Table columns
const columns = [
  {
    title: 'Username',
    key: 'username',
    render(row) {
      return h('div', { style: { fontWeight: '500' } }, row.username)
    },
  },
  {
    title: 'Họ tên',
    key: 'name',
    render(row) {
      return row.name || 'Chưa cập nhật'
    },
  },
  {
    title: 'Email',
    key: 'email',
  },
  {
    title: 'Trạng thái',
    key: 'isActive',
    width: 100,
    render(row) {
      return h(
        NTag,
        {
          type: row.isActive ? 'success' : 'error',
          size: 'small',
        },
        {
          default: () => (row.isActive ? 'Hoạt động' : 'Bị khóa'),
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
      return h('div', {
        style: {
          display: 'flex',
          justifyContent: 'flex-start',
          gap: '4px 8px'
        }
      }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'info',
            onClick: () => showUserDetails(row),
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
            type: row.isActive ? 'error' : 'success',
            onClick: () => showStatusModal(row),
          },
          {
            icon: () => h(NIcon, null, { default: () => (row.isActive ? h(Lock) : h(Unlock)) }),
            default: () => (row.isActive ? 'Khóa' : 'Mở khóa'),
          },
        ),
      ])
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
  showQuickJumper: true,
})

// Methods
const fetchUsers = async (search = '') => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (search && search.trim()) {
      params.append('search', search.trim())
    }

    const url = `/api/admin/users${params.toString() ? '?' + params.toString() : ''}`
    const response = await axios.get(url)
    users.value = response.data.users || []
  } catch {
    message.error('Không thể tải danh sách người dùng')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchUsers(searchTerm.value)
}

const clearSearch = () => {
  searchTerm.value = ''
  fetchUsers()
}

const refreshData = () => {
  searchTerm.value = ''
  fetchUsers()
  message.success('Đã làm mới dữ liệu')
}

const showUserDetails = (user) => {
  selectedUser.value = user
  detailModalVisible.value = true
}

const showStatusModal = (user) => {
  selectedUser.value = user
  statusModalVisible.value = true
}

const handleStatusToggle = async () => {
  try {
    await axios.put(`/api/admin/users/${selectedUser.value.id}/status`, {
      isActive: !selectedUser.value.isActive,
    })
    message.success(`${selectedUser.value.isActive ? 'Khóa' : 'Mở khóa'} tài khoản thành công`)
    await fetchUsers()
    statusModalVisible.value = false
  } catch {
    message.error('Không thể cập nhật trạng thái')
  }
}

// Helper functions
const formatDate = (dateString) => {
  if (!dateString) return 'Chưa có'
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
  fetchUsers()
})
</script>
