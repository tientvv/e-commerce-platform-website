<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

// Các imports khác giữ nguyên
import { TransitionRoot, TransitionChild, Dialog, DialogPanel, DialogTitle } from '@headlessui/vue'

const isOpen = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const categories = ref([])
const parentCategories = ref([])
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
  }
}

// Lấy danh sách danh mục cha cho select
const fetchParentCategories = async () => {
  try {
    const response = await axios.get('/api/category/select')
    parentCategories.value = response.data
  } catch (error) {
    console.error('Error fetching parent categories:', error)
  }
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

// Xử lý file upload
const handleFileUpload = (event) => {
  editingCategory.value.image = event.target.files[0]
}

// Gửi form cập nhật
const submitForm = async () => {
  try {
    let imageUrl = editingCategory.value.imageUrl // Mặc định giữ ảnh cũ
    if (editingCategory.value.image) {
      const formData = new FormData()
      formData.append('file', editingCategory.value.image)
      const res = await axios.post('/api/upload', formData)
      imageUrl = res.data.url // Nếu có ảnh mới thì lấy ảnh mới
    }
    await axios.put(`/api/category/${editingCategory.value.id}`, {
      name: editingCategory.value.name,
      parentId: editingCategory.value.parentId || null,
      imageUrl: imageUrl,
    })
    closeModal()
    fetchCategories(currentPage.value)
    alert('Cập nhật danh mục thành công!')
    window.location.reload()
  } catch (error) {
    console.error('Error updating category:', error)
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

onMounted(() => {
  fetchCategories()
  fetchParentCategories()
})
</script>

<template>
  <div>
    <table class="w-full text-left border-collapse rounded overflow-hidden">
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
          <td class="border-b border-gray-300 p-2">{{ category.id }}</td>
          <td class="border-b border-gray-300 p-2">{{ category.name }}</td>
          <td class="border-b border-gray-300 p-2">
            {{ category.parent?.name || 'Không có' }}
          </td>
          <td class="border-b border-gray-300 p-2">
            <img
              v-if="category.imageUrl"
              :src="category.imageUrl"
              class="h-10 w-10 object-cover rounded"
            />
          </td>
          <td class="border-b border-gray-300 p-2">
            <button @click="openEditModal(category)" class="text-blue-600 hover:underline">
              Chỉnh sửa
            </button>
            <button class="text-red-600 hover:underline ml-2">Xóa</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Phân trang -->
    <div class="flex justify-between items-center mt-4">
      <button
        @click="fetchCategories(currentPage - 1)"
        :disabled="currentPage === 0"
        class="px-4 py-2 bg-gray-200 rounded-md disabled:opacity-50"
      >
        Trước
      </button>
      <span>Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
      <button
        @click="fetchCategories(currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
        class="px-4 py-2 bg-gray-200 rounded-md disabled:opacity-50"
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
                        <p class="mb-2">Tên danh mục</p>
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
                          >
                            {{ category.name }}
                          </option>
                        </select>
                      </li>
                      <li class="mt-2">
                        <p class="block mb-2">Hình ảnh danh mục</p>
                        <input
                          @change="handleFileUpload"
                          type="file"
                          class="file:cursor-pointer w-full rounded-md file:border file:py-1 file:px-2 file:bg-blue-50 file:border-blue-200 file:text-blue-800 file:rounded-md"
                          accept="image/*"
                        />
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
  </div>
</template>
