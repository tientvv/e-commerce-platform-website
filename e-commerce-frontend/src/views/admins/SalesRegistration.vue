<template>
  <main class="w-full">
    <div v-if="isLoading" class="text-center py-10">
      <p class="text-gray-500">Đang tải danh sách đăng ký...</p>
    </div>

    <div v-else class="bg-white shadow rounded-lg overflow-hidden">
      <table class="min-w-full">
        <thead class="bg-gray-100 border-b border-gray-200">
          <tr>
            <th
              scope="col"
              class="px-6 py-3 text-left text-xs font-bold text-gray-600 uppercase tracking-wider"
            >
              Tên cửa hàng
            </th>
            <th
              scope="col"
              class="px-6 py-3 text-left text-xs font-bold text-gray-600 uppercase tracking-wider"
            >
              CCCD
            </th>
            <th
              scope="col"
              class="px-6 py-3 text-left text-xs font-bold text-gray-600 uppercase tracking-wider"
            >
              Trạng thái
            </th>
            <th scope="col" class="relative px-6 py-3">
              <span class="sr-only">Hành động</span>
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-if="registrations.length === 0">
            <td colspan="4" class="px-6 py-4 text-center text-gray-500">
              Không có đơn đăng ký nào.
            </td>
          </tr>
          <tr v-for="reg in registrations" :key="reg.userId">
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <div class="flex-shrink-0 h-10 w-10">
                  <img
                    class="h-10 w-10 rounded-full object-cover"
                    :src="reg.avatarShop"
                    alt="Shop Avatar"
                  />
                </div>
                <div class="ml-4">
                  <div class="text-sm font-medium text-gray-900">{{ reg.nameShop }}</div>
                </div>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
              {{ reg.cccd }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap">
              <span
                class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                :class="getStatusClass(reg.status)"
              >
                {{ getStatusText(reg.status) }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <button @click="openModal(reg)" class="text-blue-600 hover:text-blue-900 mr-5">
                Xem chi tiết
              </button>
              <template v-if="reg.status === 1">
                <button
                  @click="triggerApprove(reg.userId)"
                  class="text-green-600 hover:text-green-900 mr-5"
                >
                  Duyệt
                </button>
                <button @click="triggerReject(reg.userId)" class="text-red-600 hover:text-red-900">
                  Từ chối
                </button>
              </template>
              <span v-else-if="reg.status === 3" class="text-red-600 text-sm font-medium">
                Đã từ chối
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
      v-if="isConfirmModalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-[#00000085]"
      @click.self="closeConfirmModal"
    >
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md">
        <div class="p-6">
          <h3 class="text-lg font-semibold text-gray-900">Xác nhận hành động</h3>
          <p class="mt-2 text-sm text-gray-600">{{ confirmMessage }}</p>
          <div class="mt-6 flex justify-end space-x-4">
            <button
              @click="closeConfirmModal"
              class="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300"
            >
              Hủy
            </button>
            <button
              @click="executeConfirmAction"
              class="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
            >
              Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="isModalOpen && selectedRegistration"
      class="fixed inset-0 z-50 flex items-center justify-center bg-[#00000085]"
      @click.self="closeModal"
    >
      <div class="bg-white rounded-lg shadow-xl w-full max-w-3xl max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <div class="flex justify-between items-start">
            <h3 class="text-xl font-semibold text-gray-900">
              Chi tiết đăng ký: {{ selectedRegistration.nameShop }}
            </h3>
            <button
              @click="closeModal"
              class="text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm p-1.5 ml-auto inline-flex items-center"
            >
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fill-rule="evenodd"
                  d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                  clip-rule="evenodd"
                ></path>
              </svg>
            </button>
          </div>

          <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h4 class="font-semibold text-gray-700">Thông tin chung</h4>
              <dl class="mt-2">
                <div class="py-2 sm:grid sm:grid-cols-3 sm:gap-4">
                  <dt class="text-sm font-medium text-gray-500">Tên cửa hàng</dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                    {{ selectedRegistration.nameShop }}
                  </dd>
                </div>
                <div class="py-2 sm:grid sm:grid-cols-3 sm:gap-4">
                  <dt class="text-sm font-medium text-gray-500">Số CCCD</dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                    {{ selectedRegistration.cccd }}
                  </dd>
                </div>
                <div class="py-2 sm:grid sm:grid-cols-3 sm:gap-4">
                  <dt class="text-sm font-medium text-gray-500">Trạng thái</dt>
                  <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                    <span
                      class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                      :class="getStatusClass(selectedRegistration.status)"
                    >
                      {{ getStatusText(selectedRegistration.status) }}
                    </span>
                  </dd>
                </div>
                <div
                  v-if="selectedRegistration.status === 3"
                  class="py-2 sm:grid sm:grid-cols-3 sm:gap-4"
                >
                  <dt class="text-sm font-medium text-gray-500">Lý do từ chối</dt>
                  <dd class="mt-1 text-sm text-red-600 sm:mt-0 sm:col-span-2">
                    {{ selectedRegistration.reason || 'Không có' }}
                  </dd>
                </div>
              </dl>
            </div>
            <div>
              <h4 class="font-semibold text-gray-700">Ảnh đại diện</h4>
              <img
                :src="selectedRegistration.avatarShop"
                alt="Avatar"
                class="mt-2 rounded-lg w-32 h-32 object-cover"
              />
            </div>
          </div>

          <div class="mt-6">
            <h4 class="font-semibold text-gray-700">Hình ảnh CCCD</h4>
            <div class="mt-2 grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p class="text-sm text-gray-600 mb-2">Mặt trước</p>
                <img
                  :src="selectedRegistration.cccdFrontUrl"
                  alt="CCCD Front"
                  class="rounded-lg w-full"
                />
              </div>
              <div>
                <p class="text-sm text-gray-600 mb-2">Mặt sau</p>
                <img
                  :src="selectedRegistration.cccdBackUrl"
                  alt="CCCD Back"
                  class="rounded-lg w-full"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllRegistrations, processRegistration, deleteRegistration } from '@/api/ShopController'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const registrations = ref([])
const isLoading = ref(true)
const isModalOpen = ref(false)
const selectedRegistration = ref(null)

const isConfirmModalOpen = ref(false)
const confirmMessage = ref('')
const confirmAction = ref(null)

const fetchRegistrations = async () => {
  isLoading.value = true
  try {
    registrations.value = await getAllRegistrations()
  } catch {
    toast.error('Không thể tải danh sách đăng ký.')
  } finally {
    isLoading.value = false
  }
}

const handleApprove = async (userId) => {
  console.log('Duyệt userId:', userId)
  try {
    const res = await processRegistration(userId, 2)
    console.log('Kết quả duyệt:', res)
    toast.success('Duyệt đơn đăng ký thành công!')
    await fetchRegistrations()
  } catch (err) {
    console.error('Lỗi duyệt:', err)
    toast.error('Duyệt đơn đăng ký thất bại.')
  }
}

const handleReject = async (userId) => {
  console.log('Từ chối userId:', userId)
  try {
    const res = await deleteRegistration(userId)
    console.log('Kết quả từ chối:', res)
    toast.success('Đã xóa đơn đăng ký.')
    await fetchRegistrations()
  } catch (err) {
    console.error('Lỗi từ chối:', err)
    toast.error('Xóa đơn đăng ký thất bại.')
  }
}

const openConfirmModal = (message, action) => {
  confirmMessage.value = message
  confirmAction.value = action
  isConfirmModalOpen.value = true
}

const closeConfirmModal = () => {
  isConfirmModalOpen.value = false
  confirmMessage.value = ''
  confirmAction.value = null
}

const executeConfirmAction = () => {
  if (confirmAction.value) {
    confirmAction.value()
  }
  closeConfirmModal()
}

const triggerApprove = (userId) => {
  openConfirmModal('Bạn có chắc chắn muốn duyệt đơn đăng ký này?', () => handleApprove(userId))
}

const triggerReject = (userId) => {
  openConfirmModal('Bạn có chắc chắn muốn xóa đơn đăng ký này?', () => handleReject(userId))
}

const openModal = (registration) => {
  selectedRegistration.value = registration
  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
  selectedRegistration.value = null
}

const getStatusText = (status) => {
  switch (status) {
    case 0:
      return 'Chưa đăng ký'
    case 1:
      return 'Đang chờ duyệt'
    case 2:
      return 'Đã được duyệt'
    case 3:
      return 'Đã từ chối'
    default:
      return 'Không xác định'
  }
}

const getStatusClass = (status) => {
  switch (status) {
    case 1:
      return 'bg-yellow-100 text-yellow-800'
    case 2:
      return 'bg-green-100 text-green-800'
    case 3:
      return 'bg-red-100 text-red-800'
    case 0:
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

onMounted(() => {
  fetchRegistrations()
})
</script>
