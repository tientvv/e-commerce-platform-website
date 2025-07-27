<template>
  <div class="mt-2">
    <!-- Form tạo biến thể mới -->
    <div class="mb-6">
      <h2 class="text-lg font-semibold mb-4">Thêm Biến thể Mới</h2>
      <form @submit.prevent="createVariant">
        <div v-if="successMessage" class="mt-4 border border-green-500 text-green-500 py-3 px-4 rounded">
          {{ successMessage }}
        </div>
        <div v-if="errorMessage" class="mt-4 border border-red-500 text-red-500 py-3 px-4 rounded">
          {{ errorMessage }}
        </div>

        <div class="mt-4 flex gap-4">
          <div class="w-[50%]">
            <label class="block mb-2">Sản phẩm:</label>
            <select
              v-model="newVariant.productId"
              class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
              required
            >
              <option value="">Chọn sản phẩm</option>
              <option v-for="product in products" :key="product.id" :value="product.id">
                {{ product.name }}
              </option>
            </select>
          </div>
          <div class="w-[50%]">
            <label class="block mb-2">Tên biến thể:</label>
            <input
              v-model="newVariant.variantName"
              type="text"
              placeholder="VD: Màu sắc, Kích thước"
              class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
              required
            />
          </div>
        </div>

        <div class="mt-4 flex gap-4">
          <div class="w-[50%]">
            <label class="block mb-2">Giá trị biến thể:</label>
            <input
              v-model="newVariant.variantValue"
              type="text"
              placeholder="VD: Đỏ, XL"
              class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
              required
            />
          </div>
          <div class="w-[50%]">
            <label class="block mb-2">Số lượng:</label>
            <input
              v-model="newVariant.quantity"
              type="number"
              min="0"
              class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
              required
            />
          </div>
        </div>

        <div class="mt-4">
          <label class="block mb-2">Giá (VNĐ):</label>
          <input
            v-model="newVariant.price"
            type="number"
            min="0"
            step="1000"
            class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
            required
          />
        </div>

        <div class="mt-8">
          <button
            type="submit"
            :disabled="isLoading"
            class="w-full py-2 px-3 rounded border border-blue-600 hover:bg-blue-600 hover:text-white text-blue-600"
          >
            <span v-if="isLoading">Đang tạo...</span>
            <span v-else>Thêm Biến thể</span>
          </button>
        </div>
      </form>
    </div>

    <!-- Danh sách biến thể -->
    <div>
      <h2 class="text-lg font-semibold mb-4">Danh sách Biến thể</h2>

      <!-- Filter -->
      <div class="mb-4">
        <label class="block mb-2">Lọc theo sản phẩm:</label>
        <select
          v-model="selectedProductId"
          @change="loadVariants"
          class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        >
          <option value="">Tất cả sản phẩm</option>
          <option v-for="product in products" :key="product.id" :value="product.id">
            {{ product.name }}
          </option>
        </select>
      </div>

      <!-- Table -->
      <div class="overflow-hidden rounded border border-gray-200">
        <table class="w-full text-left">
          <thead class="bg-gray-200">
            <tr>
              <th class="p-2 border-r border-gray-200">Sản phẩm</th>
              <th class="p-2 border-r border-gray-200">Tên biến thể</th>
              <th class="p-2 border-r border-gray-200">Giá trị</th>
              <th class="p-2 border-r border-gray-200">Số lượng</th>
              <th class="p-2 border-r border-gray-200">Giá</th>
              <th class="p-2 border-r border-gray-200">Trạng thái</th>
              <th class="p-2 border-gray-200 w-32">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="variant in variants" :key="variant.id">
              <td class="p-2 border-t border-r border-gray-200">{{ getProductName(variant.productId) }}</td>
              <td class="p-2 border-t border-r border-gray-200">{{ variant.variantName }}</td>
              <td class="p-2 border-t border-r border-gray-200">{{ variant.variantValue }}</td>
              <td class="p-2 border-t border-r border-gray-200">{{ variant.quantity }}</td>
              <td class="p-2 border-t border-r border-gray-200">{{ formatPrice(variant.price) }}</td>
              <td class="p-2 border-t border-r border-gray-200">
                <span :class="variant.isActive ? 'text-green-600' : 'text-red-600'">
                  {{ variant.isActive ? 'Hoạt động' : 'Không hoạt động' }}
                </span>
              </td>
              <td class="p-2 border-t border-gray-200 flex flex-col gap-2 w-32">
                <button
                  @click="editVariant(variant)"
                  class="px-3 py-2 bg-green-600 text-white rounded hover:bg-green-700 text-center"
                >
                  Sửa
                </button>
                <button
                  @click="deleteVariant(variant.id)"
                  class="px-3 py-2 bg-red-600 text-white rounded hover:bg-red-700 text-center"
                >
                  Xóa
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty state -->
      <div v-if="variants.length === 0" class="text-center py-8">
        <p class="text-gray-500">Chưa có biến thể nào được tạo</p>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="showEditModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div class="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
        <div class="mt-3">
          <h3 class="text-lg font-medium text-gray-900 mb-4">Sửa Biến thể</h3>
          <form @submit.prevent="updateVariant">
            <div class="mb-4">
              <label class="block mb-2">Tên biến thể:</label>
              <input
                v-model="editingVariant.variantName"
                type="text"
                class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
                required
              />
            </div>
            <div class="mb-4">
              <label class="block mb-2">Giá trị biến thể:</label>
              <input
                v-model="editingVariant.variantValue"
                type="text"
                class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
                required
              />
            </div>
            <div class="mb-4">
              <label class="block mb-2">Số lượng:</label>
              <input
                v-model="editingVariant.quantity"
                type="number"
                min="0"
                class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
                required
              />
            </div>
            <div class="mb-4">
              <label class="block mb-2">Giá (VNĐ):</label>
              <input
                v-model="editingVariant.price"
                type="number"
                min="0"
                step="1000"
                class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
                required
              />
            </div>
            <div class="mb-4">
              <label class="flex items-center">
                <input
                  v-model="editingVariant.isActive"
                  type="checkbox"
                  class="rounded border-gray-300 text-blue-600 shadow-sm focus:border-blue-300 focus:ring focus:ring-blue-200 focus:ring-opacity-50"
                />
                <span class="ml-2 text-sm text-gray-700">Hoạt động</span>
              </label>
            </div>
            <div class="flex justify-end space-x-3">
              <button
                type="button"
                @click="showEditModal = false"
                class="px-4 py-2 text-gray-700 bg-gray-200 rounded hover:bg-gray-300"
              >
                Hủy
              </button>
              <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Cập nhật</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const products = ref([])
const variants = ref([])
const selectedProductId = ref('')
const showEditModal = ref(false)
const editingVariant = ref({})
const isLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const newVariant = ref({
  productId: '',
  variantName: '',
  variantValue: '',
  quantity: 0,
  price: 0,
})

onMounted(() => {
  loadProducts()
  loadVariants()
})

const loadProducts = async () => {
  try {
    const response = await axios.get('/api/products/all')
    products.value = response.data.products || []
  } catch (error) {
    console.error('Lỗi khi tải danh sách sản phẩm:', error)
  }
}

const loadVariants = async () => {
  try {
    let url = '/api/product-variants/product/all'
    if (selectedProductId.value) {
      url = `/api/product-variants/product/${selectedProductId.value}`
    }
    const response = await axios.get(url)
    variants.value = response.data.variants || []
  } catch (error) {
    console.error('Lỗi khi tải danh sách biến thể:', error)
  }
}

const createVariant = async () => {
  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    await axios.post('/api/product-variants/create', newVariant.value)
    successMessage.value = 'Tạo biến thể thành công!'
    resetForm()
    loadVariants()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Có lỗi xảy ra!'
  } finally {
    isLoading.value = false
  }
}

const editVariant = (variant) => {
  editingVariant.value = { ...variant }
  showEditModal.value = true
}

const updateVariant = async () => {
  try {
    await axios.put(`/api/product-variants/update/${editingVariant.value.id}`, editingVariant.value)
    successMessage.value = 'Cập nhật biến thể thành công!'
    showEditModal.value = false
    loadVariants()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Có lỗi xảy ra!'
  }
}

const deleteVariant = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa biến thể này?')) {
    try {
      await axios.delete(`/api/product-variants/delete/${id}`)
      successMessage.value = 'Xóa biến thể thành công!'
      loadVariants()
    } catch (error) {
      errorMessage.value = error.response?.data?.message || 'Có lỗi xảy ra!'
    }
  }
}

const resetForm = () => {
  newVariant.value = {
    productId: '',
    variantName: '',
    variantValue: '',
    quantity: 0,
    price: 0,
  }
}

const getProductName = (productId) => {
  const product = products.value.find((p) => p.id === productId)
  return product ? product.name : 'Không xác định'
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(price)
}
</script>

<style scoped>
table {
  border-collapse: collapse;
}

button {
  color: #155dfc;
}
button:hover {
  color: white;
}
</style>
