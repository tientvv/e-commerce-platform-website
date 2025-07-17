<script setup>
import { TransitionRoot, TransitionChild, Dialog, DialogPanel, DialogTitle } from '@headlessui/vue'
import { useRoute } from 'vue-router'
import { onMounted, onUnmounted, ref } from 'vue'
import { useToast } from 'vue-toastification'

import Navbar from '@/components/admins/Navbar.vue'
import AdminFooter from '@/components/admins/Footer.vue'
import axios from 'axios'
import TableCategory from './TableCategory.vue'

const toast = useToast()
const isOpen1 = ref(false)
const route = useRoute()
const stats = ref({
  total: 0,
  parent: 0,
  child: 0,
})

function closeModal1() {
  isOpen1.value = false
}

function openModal1() {
  isOpen1.value = true
}

const isActive = (path) => {
  if (route.path === path) {
    return 'py-2 px-3 rounded bg-blue-950 text-white'
  } else {
    return 'py-2 px-3 rounded hover:bg-gray-200 transition-colors duration-200'
  }
}

const resetForm = () => {
  category.value = {
    name: '',
    parentId: null,
    imageUrl: '',
  }
  file.value = null
}

// Lấy thống kê
const fetchStats = async () => {
  try {
    const response = await axios.get('/api/category/stats')
    stats.value = response.data
  } catch (error) {
    console.error('Error fetching stats:', error)
  }
}

const category = ref({
  name: '',
  parentId: null,
  imageUrl: '',
})

const file = ref(null)

const handleFile = (e) => {
  file.value = e.target.files[0]
}

const createCategory = async () => {
  try {
    // Kiểm tra tên category không được để trống
    if (!category.value.name.trim()) {
      toast.error('Tên danh mục không được để trống!')
      return
    }

    if (file.value) {
      const formData = new FormData()
      formData.append('file', file.value)
      const res = await axios.post('/api/upload', formData)
      category.value.imageUrl = res.data.url
    }

    await axios.post('/api/category/add', {
      name: category.value.name,
      parentId: category.value.parentId || null,
      imageUrl: category.value.imageUrl || null,
    })

    toast.success('Tạo danh mục thành công!')
    closeModal1()
    resetForm()

    // Cập nhật dữ liệu mà không reload page
    await fetchStats()

    // Reload danh sách parent categories cho select
    const res = await axios.get('/api/category/select')
    parentCategories.value = res.data

    // Emit event để TableCategory reload
    window.dispatchEvent(new CustomEvent('categoryCreated'))
  } catch (error) {
    console.error('Error creating category:', error)
    const errorMessage = error.response?.data?.message || 'Lỗi khi thêm danh mục!'
    toast.error(errorMessage)
    resetForm()
  }
}

const parentCategories = ref({})

onMounted(async () => {
  try {
    const res = await axios.get('/api/category/select')
    parentCategories.value = res.data
    await fetchStats()

    // Listen for category updates từ TableCategory
    window.addEventListener('categoryUpdated', fetchStats)
  } catch (error) {
    console.error('Error loading data:', error)
  }
})

// Cleanup event listener
onUnmounted(() => {
  window.removeEventListener('categoryUpdated', fetchStats)
})
</script>

<template>
  <div class="h-screen flex flex-col">
    <Navbar />
    <div class="flex-1 p-4">
      <div class="max-w-[1200px] mx-auto h-full flex flex-row gap-4">
        <aside class="w-[20%] bg-white rounded shadow-sm p-4">
          <div class="flex flex-col gap-2">
            <RouterLink to="/admins/dashboard" class="" :class="isActive('/admins/dashboard')">
              Bảng điều khiển
            </RouterLink>

            <RouterLink to="/admins/categories" :class="isActive('/admins/categories')">
              Quản lý danh mục
            </RouterLink>

            <RouterLink to="/admins/sellers" :class="isActive('/admins/sellers')">
              Quản lý người bán
            </RouterLink>

            <RouterLink to="/admins/coupon" :class="isActive('/admins/coupon')">
              Quản lý Mã giảm giá
            </RouterLink>
          </div>
        </aside>
        <aside class="w-[80%] bg-white rounded shadow-sm p-4">
          <div class="pb-4 border-b border-gray-300">
            <button
              @click="openModal1"
              class="bg-blue-600 py-1 px-2 rounded text-white hover:bg-blue-700 transition-colors"
            >
              Thêm danh mục
            </button>
          </div>
          <div class="mt-2 mb-4 flex gap-4">
            <div class="w-1/3 bg-blue-50 p-4 rounded border border-blue-200">
              <div class="text-blue-800 font-semibold">Tổng danh mục</div>
              <div class="font-semibold text-2xl">{{ stats.total }}</div>
            </div>
            <div class="w-1/3 bg-green-50 p-4 rounded border border-green-200">
              <div class="text-green-800 font-semibold">Tổng danh mục cha</div>
              <div class="font-semibold text-2xl">{{ stats.parent }}</div>
            </div>
            <div class="w-1/3 bg-purple-50 p-4 rounded border border-purple-200">
              <div class="text-purple-800 font-semibold">Tổng danh mục con</div>
              <div class="font-semibold text-2xl">{{ stats.child }}</div>
            </div>
          </div>
          <div class="overflow-x-auto">
            <TableCategory @categoryUpdated="fetchStats" />
          </div>
        </aside>
      </div>
    </div>

    <AdminFooter />

    <TransitionRoot appear :show="isOpen1" as="template">
      <Dialog as="div" @close="closeModal1" class="relative z-10">
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
                <form @submit.prevent="createCategory">
                  <DialogTitle as="h3" class="text-xl text-gray-800 text-center">
                    Thêm danh mục
                  </DialogTitle>
                  <div class="mt-4">
                    <ul>
                      <li>
                        <p class="mb-2">Tên danh mục <span class="text-red-500">*</span></p>
                        <input
                          v-model="category.name"
                          class="py-1 px-2 border rounded-md w-full border-gray-300 text-gray-800 focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                          type="text"
                          placeholder="Nhập tên danh mục"
                        />
                      </li>
                      <li class="mt-2">
                        <p class="block mb-2">Danh mục cha</p>
                        <select
                          v-model="category.parentId"
                          class="w-full border px-2 py-1 rounded-md border-gray-300 text-gray-800 focus:border-blue-500 focus:ring-1 focus:ring-blue-500"
                        >
                          <option :value="null">Chọn danh mục cha (tùy chọn)</option>
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
                          @change="handleFile"
                          type="file"
                          accept="image/*"
                          class="file:cursor-pointer w-full rounded-md file:border file:py-1 file:px-2 file:bg-blue-50 file:border-blue-200 file:text-blue-800 file:rounded-md file:hover:file:bg-blue-100"
                        />
                      </li>
                    </ul>
                  </div>
                  <div class="mt-6 flex gap-2">
                    <button
                      type="submit"
                      class="flex-1 py-2 px-4 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                    >
                      Thêm danh mục
                    </button>
                    <button
                      type="button"
                      class="flex-1 py-2 px-4 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors"
                      @click="closeModal1"
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
