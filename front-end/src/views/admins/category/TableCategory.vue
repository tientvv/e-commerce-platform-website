<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { useToast } from 'vue-toastification'

// Các imports khác giữ nguyên
import { TransitionRoot, TransitionChild, Dialog, DialogPanel, DialogTitle } from '@headlessui/vue'

const toast = useToast()
const isOpen = ref(false)
const isDeleteModalOpen = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const categories = ref([])
const parentCategories = ref([])
const categoryToDelete = ref(null)
const editingCategory = ref({
  id: null,
  name: '',
  parentId: null,
  image: null,
})

// Lấy danh sách danh mục
const fetchCategories = async (page = 0) => {
  try {
    const response = await axios.get(`/api/category/list?page=${page}&size=10`)
    categories.value = response.data.content
    totalPages.value = response.data.totalPages
    currentPage.value = page
  } catch (error) {
    console.error('Error fetching categories:', error)
    toast.error('Lỗi khi tải danh sách danh mục!')
  }
}

// Lấy danh sách danh mục cha cho select
const fetchParentCategories = async () => {
  try {
    const response = await axios.get('/api/category/select')
    parentCategories.value = response.data
  } catch (error) {
    console.error('Error fetching parent categories:', error)
    toast.error('Lỗi khi tải danh mục cha!')
  }
}

// Function để reload tất cả dữ liệu và emit event
const refreshData = async () => {
  await fetchCategories(currentPage.value)
  await fetchParentCategories()
  // Emit event để parent component cập nhật thống kê
  window.dispatchEvent(new CustomEvent('categoryUpdated'))
}

// Mở modal chỉnh sửa
const openEditModal = (category) => {
  editingCategory.value = {
    id: category.id,
    name: category.name,
    parentId: category.parent?.id || null,
    image: null,
    imageUrl: category.imageUrl || null,
  }
  isOpen.value = true
}

// Mở modal xác nhận xóa
const openDeleteModal = (category) => {
  categoryToDelete.value = category
  isDeleteModalOpen.value = true
}

// Xử lý file upload
const handleFileUpload = (event) => {
  editingCategory.value.image = event.target.files[0]
}

// Gửi form cập nhật
const submitForm = async () => {
  try {
    // Kiểm tra tên category không được để trống
    if (!editingCategory.value.name.trim()) {
      toast.error('Tên danh mục không được để trống!')
      return
    }

    let imageUrl = editingCategory.value.imageUrl
    if (editingCategory.value.image) {
      const formData = new FormData()
      formData.append('file', editingCategory.value.image)
      const res = await axios.post('/api/upload', formData)
      imageUrl = res.data.url
    }

    await axios.put(`/api/category/${editingCategory.value.id}`, {
      name: editingCategory.value.name,
      parentId: editingCategory.value.parentId || null,
      imageUrl: imageUrl,
    })

    toast.success('Cập nhật danh mục thành công!')
    closeModal()
    await refreshData() // Sử dụng refreshData thay vì gọi riêng lẻ
  } catch (error) {
    console.error('Error updating category:', error)
    const errorMessage = error.response?.data?.message || 'Lỗi khi cập nhật danh mục!'
    toast.error(errorMessage)
  }
}

// Xóa danh mục
const deleteCategory = async () => {
  try {
    await axios.delete(`/api/category/${categoryToDelete.value.id}`)
    toast.success('Xóa danh mục thành công!')
    closeDeleteModal()
    await refreshData() // Sử dụng refreshData thay vì gọi riêng lẻ
  } catch (error) {
    console.error('Error deleting category:', error)
    const errorMessage = error.response?.data?.message || 'Lỗi khi xóa danh mục!'
    toast.error(errorMessage)
  }
}

const closeModal = () => {
  isOpen.value = false
  editingCategory.value = {
    id: null,
    name: '',
    parentId: null,
    image: null,
    imageUrl: null,
  }
}

const closeDeleteModal = () => {
  isDeleteModalOpen.value = false
  categoryToDelete.value = null
}

onMounted(() => {
  fetchCategories()
  fetchParentCategories()

  // Listen for category updates từ parent component
  window.addEventListener('categoryUpdated', refreshData)
})

// Cleanup event listener
onUnmounted(() => {
  window.removeEventListener('categoryUpdated', refreshData)
})
</script>

<template>
  <div>
    <div class="overflow-x-auto">
      <table class="w-full text-left border-collapse rounded overflow-hidden min-w-[600px]">
        <thead>
          <tr class="bg-blue-950 text-white">
            <th class="p-2 border-b border-gray-300">ID</th>
            <th class="p-2 border-b border-gray-300">Tên danh mục</th>
            <th class="p-2 border-b border-gray-300">Danh mục cha</th>
            <th class="p-2 border-b border-gray-300">Hình ảnh</th>
            <th class="p-2 border-b border-gray-300">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="category in categories" :key="category.id">
            <td class="border-b border-gray-300 p-2">
              <span class="inline-block max-w-[120px] truncate align-middle" title="category.id">{{
                category.id
              }}</span>
            </td>
            <td class="border-b border-gray-300 p-2 whitespace-nowrap">
              {{ category.name }}
            </td>
            <td class="border-b border-gray-300 p-2 whitespace-nowrap">
              {{ category.parent?.name || 'Không có' }}
            </td>
            <td class="border-b border-gray-300 p-2">
              <img
                v-if="category.imageUrl"
                :src="category.imageUrl"
                class="h-10 w-10 object-cover rounded"
                :alt="category.name"
              />
              <span v-else class="text-gray-400">Không có ảnh</span>
            </td>
            <td class="border-b border-gray-300 p-2 whitespace-nowrap">
              <button @click="openEditModal(category)" class="text-blue-600 hover:underline mr-2">
                Chi tiết
              </button>
              <button @click="openDeleteModal(category)" class="text-red-600 hover:underline">
                Xóa
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Phân trang -->
    <div class="flex justify-between items-center mt-4">
      <button
        @click="fetchCategories(currentPage - 1)"
        :disabled="currentPage === 0"
        class="px-4 py-2 bg-gray-200 rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Trước
      </button>
      <span>Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
      <button
        @click="fetchCategories(currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
        class="px-4 py-2 bg-gray-200 rounded-md disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Tiếp
      </button>
    </div>

    <!-- Modal chỉnh sửa -->
    <TransitionRoot appear :show="isOpen" as="template">
      <Dialog as="div" @close="closeModal" class="relative z-10">
        <TransitionChild
          as="template"
          enter="duration-300 ease-out"
          enter-from="opacity-0"
          enter-to="opacity-100"
          leave="duration-200 ease-in"
          leave-from="opacity-100"
          leave-to="opacity-0"
        >
          <div class="fixed inset-0 bg-black/25"></div>
        </TransitionChild>
        <div class="fixed inset-0 overflow-y-auto">
          <div class="flex min-h-full items-center justify-center p-4 text-center">
            <TransitionChild
              as="template"
              enter="duration-300 ease-out"
              enter-from="opacity-0 scale-95"
              enter-to="opacity-100 scale-100"
              leave="duration-200 ease-in"
              leave-from="opacity-100 scale-100"
              leave-to="opacity-0 scale-95"
            >
              <DialogPanel
                class="w-full max-w-[400px] transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all"
              >
                <form @submit.prevent="submitForm">
                  <DialogTitle as="h3" class="text-xl text-gray-800 text-center">
                    Sửa danh mục
                  </DialogTitle>
                  <div class="mt-4">
                    <ul>
                      <li>
                        <p class="mb-2">Tên danh mục <span class="text-red-500">*</span></p>
                        <input
                          v-model="editingCategory.name"
                          class="py-1 px-2 border rounded-md w-full border-gray-300 text-gray-800"
                          type="text"
                          placeholder="Nhập tên danh mục"
                          required
                        />
                      </li>
                      <li class="mt-2">
                        <p class="block mb-2">Danh mục cha</p>
                        <select
                          v-model="editingCategory.parentId"
                          class="w-full border px-2 py-1 rounded-md border-gray-300 text-gray-800 bg-white"
                        >
                          <option :value="null">Không có danh mục cha</option>
                          <option
                            v-for="category in parentCategories"
                            :key="category.id"
                            :value="category.id"
                            :disabled="category.id === editingCategory.id"
                          >
                            {{ category.name }}
                          </option>
                        </select>
                      </li>
                      <li class="mt-2">
                        <p class="block mb-2">Hình ảnh danh mục</p>
                        <div v-if="editingCategory.imageUrl" class="mb-2">
                          <img
                            :src="editingCategory.imageUrl"
                            class="h-20 w-20 object-cover rounded border"
                            alt="Current image"
                          />
                          <p class="text-sm text-gray-500 mt-1">Ảnh hiện tại</p>
                        </div>
                        <input
                          @change="handleFileUpload"
                          type="file"
                          class="file:cursor-pointer w-full rounded-md file:border file:py-1 file:px-2 file:bg-blue-50 file:border-blue-200 file:text-blue-800 file:rounded-md"
                          accept="image/*"
                        />
                        <p class="text-sm text-gray-500 mt-1">
                          Chọn ảnh mới để thay đổi (tùy chọn)
                        </p>
                      </li>
                    </ul>
                  </div>
                  <div class="mt-4">
                    <button type="submit" class="py-1 px-2 bg-blue-600 text-white rounded-md me-2">
                      Lưu thay đổi
                    </button>
                    <button
                      type="button"
                      class="py-1 px-2 bg-red-400/25 text-red-600 rounded-md"
                      @click="closeModal"
                    >
                      Huỷ
                    </button>
                  </div>
                </form>
              </DialogPanel>
            </TransitionChild>
          </div>
        </div>
      </Dialog>
    </TransitionRoot>

    <!-- Modal xác nhận xóa -->
    <TransitionRoot appear :show="isDeleteModalOpen" as="template">
      <Dialog as="div" @close="closeDeleteModal" class="relative z-10">
        <TransitionChild
          as="template"
          enter="duration-300 ease-out"
          enter-from="opacity-0"
          enter-to="opacity-100"
          leave="duration-200 ease-in"
          leave-from="opacity-100"
          leave-to="opacity-0"
        >
          <div class="fixed inset-0 bg-black/25"></div>
        </TransitionChild>
        <div class="fixed inset-0 overflow-y-auto">
          <div class="flex min-h-full items-center justify-center p-4 text-center">
            <TransitionChild
              as="template"
              enter="duration-300 ease-out"
              enter-from="opacity-0 scale-95"
              enter-to="opacity-100 scale-100"
              leave="duration-200 ease-in"
              leave-from="opacity-100 scale-100"
              leave-to="opacity-0 scale-95"
            >
              <DialogPanel
                class="w-full max-w-md transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all"
              >
                <DialogTitle as="h3" class="text-lg font-medium leading-6 text-gray-900">
                  Xác nhận xóa danh mục
                </DialogTitle>
                <div class="mt-2">
                  <p class="text-sm text-gray-500">
                    Bạn có chắc chắn muốn xóa danh mục
                    <span class="font-semibold">{{ categoryToDelete?.name }}</span
                    >?
                  </p>
                  <p class="text-sm text-red-600 mt-2">
                    ⚠️ Lưu ý: Nếu danh mục này có danh mục con, các danh mục con sẽ trở thành danh
                    mục gốc.
                  </p>
                </div>

                <div class="mt-4 flex gap-2">
                  <button
                    type="button"
                    class="inline-flex justify-center rounded-md border border-transparent bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 focus:outline-none"
                    @click="deleteCategory"
                  >
                    Xóa
                  </button>
                  <button
                    type="button"
                    class="inline-flex justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none"
                    @click="closeDeleteModal"
                  >
                    Hủy
                  </button>
                </div>
              </DialogPanel>
            </TransitionChild>
          </div>
        </div>
      </Dialog>
    </TransitionRoot>
  </div>
</template>
