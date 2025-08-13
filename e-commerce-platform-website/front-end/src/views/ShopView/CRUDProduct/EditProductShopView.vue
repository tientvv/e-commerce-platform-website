<template>
  <div>
    <div class="mb-4">
      <button
        @click="router.push('/user/shop/product/list')"
        class="flex items-center gap-2 text-blue-600 hover:text-blue-800"
      >
        ← Quay lại danh sách sản phẩm
      </button>
    </div>

    <form @submit.prevent="updateProduct">
      <div v-if="successMessage" class="mt-4 border border-green-500 text-green-500 py-3 px-4 rounded">
        {{ successMessage }}
      </div>
      <div v-if="errorMessage" class="mt-4 border border-red-500 text-red-500 py-3 px-4 rounded">
        {{ errorMessage }}
      </div>
      <div class="mt-4 flex gap-4">
        <div class="w-[50%]">
          <label for="productName" class="block mb-2">Tên sản phẩm:</label>
          <input
            type="text"
            v-model="name"
            id="productName"
            class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          />
        </div>
        <div class="w-[50%]">
          <label for="productBrand" class="block mb-2">Thương hiệu:</label>
          <input
            type="text"
            v-model="brand"
            id="productBrand"
            class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          />
        </div>
      </div>
      <div class="mt-4">
        <label for="productDescription" class="block mb-2">Mô tả sản phẩm:</label>
        <textarea
          id="productDescription"
          v-model="description"
          class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400 h-32"
        ></textarea>
      </div>
      <div class="mt-4">
        <label for="image" class="mb-2 block">Hình ảnh sản phẩm:</label>
        <input
          id="image"
          type="file"
          class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          ref="imageInput"
          @change="handleImageChange"
        />
        <div v-if="previewImage" class="mt-2">
          <img :src="previewImage" alt="Ảnh sản phẩm" class="w-24 h-24 object-cover rounded" />
        </div>
      </div>
      <div class="mt-4 relative">
        <label for="category" class="block mb-2">Danh mục:</label>
        <div @click="toggleDropdown" class="border border-gray-400 rounded px-3 py-2 cursor-pointer">
          {{ selectedCategoryName || 'Chọn danh mục' }}
        </div>
        <ul
          v-show="showDropdown"
          class="absolute z-10 bg-white border border-gray-300 rounded mt-1 w-full max-h-40 overflow-y-auto shadow-md"
        >
          <li>
            <div class="px-3 py-2 text-gray-500">Chọn danh mục</div>
          </li>
          <li
            v-for="category in categories"
            :key="category.id"
            @click="selectCategory(category)"
            class="px-3 py-2 hover:bg-gray-100 cursor-pointer"
          >
            {{ category.name }}
          </li>
        </ul>
      </div>
      <div class="mt-8 flex gap-4">
        <button
          type="button"
          @click="router.push('/user/shop/product/list')"
          class="flex-1 py-2 px-3 rounded border border-gray-400 hover:bg-gray-100 text-gray-700"
        >
          Hủy
        </button>
        <button
          type="submit"
          :disabled="isLoading"
          class="flex-1 py-2 px-3 rounded border border-blue-600 hover:bg-blue-600 hover:text-white text-blue-600"
        >
          <span v-if="isLoading">Đang cập nhật...</span>
          <span v-else>Cập nhật sản phẩm</span>
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import axios from '../../../utils/axios'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const productId = route.params.id

const name = ref('')
const brand = ref('')
const description = ref('')
const productImage = ref(null)
const previewImage = ref('')
const imageInput = ref(null)
const isLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const categories = ref([])
const selectedCategoryId = ref(null)
const selectedCategoryName = ref('')
const showDropdown = ref(false)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const selectCategory = (category) => {
  selectedCategoryId.value = category.id
  selectedCategoryName.value = category.name
  showDropdown.value = false
}

const handleImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    productImage.value = file
    previewImage.value = URL.createObjectURL(file)
  }
}

const fetchCategories = async () => {
  try {
    const res = await axios.get('/api/categories')
    categories.value = res.data || []
  } catch (err) {
    console.error('Lỗi khi tải danh mục:', err)
  }
}
const fetchProduct = async () => {
  try {
    const res = await axios.get(`/api/products/edit/${productId}`)
    if (res.data) {
      const product = res.data
      name.value = product.name || ''
      brand.value = product.brand || ''
      description.value = product.description || ''
      selectedCategoryId.value = product.categoryId
      selectedCategoryName.value = product.categoryName || ''
      previewImage.value = product.productImage || ''
      productImage.value = null
    } else {
      errorMessage.value = 'Không tìm thấy sản phẩm!'
    }
  } catch (error) {
    console.error('Error fetching product:', error)
    if (error.response?.status === 404) {
      errorMessage.value = 'Không tìm thấy sản phẩm!'
    } else if (error.response?.status === 401) {
      errorMessage.value = 'Bạn chưa đăng nhập!'
    } else {
      errorMessage.value = 'Không thể tải thông tin sản phẩm!'
    }
  }
}

const updateProduct = async () => {
  if (!name.value.trim()) {
    errorMessage.value = 'Tên sản phẩm không được để trống!'
    return
  }

  if (!selectedCategoryId.value) {
    errorMessage.value = 'Vui lòng chọn danh mục!'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  const formData = new FormData()
  formData.append('name', name.value.trim())
  formData.append('brand', brand.value.trim())
  formData.append('description', description.value.trim())
  formData.append('categoryId', selectedCategoryId.value)

  // Chỉ append ảnh nếu có chọn mới
  if (productImage.value) {
    formData.append('productImage', productImage.value)
  }

  try {
    const res = await axios.put(`/api/products/update/${productId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })

    if (res.data.message) {
      successMessage.value = res.data.message
      errorMessage.value = ''
      // Redirect back to list after 2 seconds
      setTimeout(() => {
        router.push('/user/shop/product/list')
      }, 2000)
    } else {
      errorMessage.value = 'Cập nhật sản phẩm thất bại!'
      successMessage.value = ''
    }
  } catch (error) {
    console.error('Error updating product:', error)
    errorMessage.value = error.response?.data?.message || 'Cập nhật sản phẩm thất bại!'
    successMessage.value = ''
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchCategories()
  fetchProduct()
})
</script>

<style scoped>
/* Ẩn thanh scroll cho tất cả elements */
::-webkit-scrollbar {
  display: none !important;
}

::-webkit-scrollbar-track {
  display: none !important;
}

::-webkit-scrollbar-thumb {
  display: none !important;
}

::-webkit-scrollbar-corner {
  display: none !important;
}

* {
  -ms-overflow-style: none !important;
  scrollbar-width: none !important;
}

/* Ẩn thanh scroll cho parent elements */
:deep(::-webkit-scrollbar) {
  display: none !important;
}

:deep(::-webkit-scrollbar-track) {
  display: none !important;
}

:deep(::-webkit-scrollbar-thumb) {
  display: none !important;
}

:deep(::-webkit-scrollbar-corner) {
  display: none !important;
}

:deep(*) {
  -ms-overflow-style: none !important;
  scrollbar-width: none !important;
}

button {
  color: #155dfc;
}
button {
  transition: all 0.3s ease;
}
button:hover {
  color: white;
}
</style>
