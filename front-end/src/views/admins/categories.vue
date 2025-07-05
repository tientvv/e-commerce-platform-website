<script setup>
import { TransitionRoot, TransitionChild, Dialog, DialogPanel, DialogTitle } from '@headlessui/vue'
import Navbar from '@/components/admins/Navbar.vue'
import AdminFooter from '@/components/admins/Footer.vue'
import { useRoute } from 'vue-router'
import { ref } from 'vue'

const isOpen = ref(false)
const isOpen1 = ref(false)
const route = useRoute()

function closeModal() {
  isOpen.value = false
}

function openModal() {
  isOpen.value = true
}

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
          </div>
        </aside>
        <aside class="w-[80%] bg-white rounded shadow-sm p-4">
          <div class="pb-4 border-b border-gray-300">
            <button @click="openModal1" class="bg-blue-600 py-1 px-2 rounded text-white">
              Thêm danh mục
            </button>
          </div>
          <div class="mt-2 mb-4 flex gap-4">
            <div class="w-1/3 bg-blue-50 p-4 rounded border border-blue-200">
              <div class="text-blue-800 font-semibold">Tổng danh mục</div>
              <div class="font-semibold">0</div>
            </div>
            <div class="w-1/3 bg-blue-50 p-4 rounded border border-blue-200">
              <div class="text-blue-800 font-semibold">Tổng danh mục cha</div>
              <div class="font-semibold">0</div>
            </div>
            <div class="w-1/3 bg-blue-50 p-4 rounded border border-blue-200">
              <div class="text-blue-800 font-semibold">Tổng danh mục con</div>
              <div class="font-semibold">0</div>
            </div>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse rounded overflow-hidden">
              <thead>
                <tr class="bg-blue-950 text-white">
                  <th class="p-2 border-b border-gray-300">ID</th>
                  <th class="p-2 border-b border-gray-300">Tên danh mục</th>
                  <th class="p-2 border-b border-gray-300">Danh mục cha</th>
                  <th class="p-2 border-b border-gray-300">Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td class="border-b border-gray-300 p-2">1</td>
                  <td class="border-b border-gray-300 p-2">Danh mục 1</td>
                  <td class="border-b border-gray-300 p-2">Không có</td>
                  <td class="border-b border-gray-300 p-2">
                    <button @click="openModal" class="text-blue-600 hover:underline">
                      Chỉnh sửa
                    </button>
                    <button class="text-red-600 hover:underline ml-2">Xóa</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </aside>
      </div>
    </div>

    <AdminFooter />

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
                <DialogTitle as="h3" class="text-xl text-gray-800">Chỉnh sửa danh mục </DialogTitle>
                <div class="mt-2">
                  <p class="text-sm text-gray-600">
                    Your payment has been successfully submitted. We’ve sent you an email with all
                    of the details of your order.
                  </p>
                </div>
                <div class="mt-4">
                  <button
                    type="button"
                    class="py-1 px-2 bg-blue-600 text-white rounded me-2"
                    @click="closeModal"
                  >
                    Lưu danh mục
                  </button>
                  <button
                    type="button"
                    class="py-1 px-2 bg-red-400/25 text-red-600 rounded"
                    @click="closeModal"
                  >
                    Huỷ
                  </button>
                </div>
              </DialogPanel>
            </TransitionChild>
          </div>
        </div>
      </Dialog>
    </TransitionRoot>

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
                class="w-full max-w-[500px] transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all"
              >
                <DialogTitle as="h3" class="text-xl text-gray-800"> Thêm danh mục </DialogTitle>
                <div class="mt-2">
                  <p class="text-sm text-gray-400">
                    Your payment has been successfully submitted. We' ve sent you an email with all
                    of the details of your order.
                  </p>
                </div>
                <div class="mt-4">
                  <button
                    type="button"
                    class="py-1 px-2 bg-blue-600 text-white rounded me-2"
                    @click="closeModal1"
                  >
                    Thêm danh mục
                  </button>
                  <button
                    type="button"
                    class="py-1 px-2 bg-red-400/25 text-red-600 rounded"
                    @click="closeModal1"
                  >
                    Huỷ
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
