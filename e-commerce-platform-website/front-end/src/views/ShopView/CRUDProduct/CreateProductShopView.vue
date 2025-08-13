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

    <form @submit.prevent="createProduct">
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
          <span v-if="isLoading">Đang tạo...</span>
          <span v-else>Thêm sản phẩm</span>
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const name = ref('')
const brand = ref('')
const description = ref('')
const productImage = ref(null)
const imageInput = ref(null)
const isLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const previewImage = ref(null)

const router = useRouter()

const handleImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    productImage.value = file
    previewImage.value = URL.createObjectURL(file)
  }
}

const resetForm = () => {
  name.value = ''
  brand.value = ''
  description.value = ''
  productImage.value = null
  imageInput.value.value = ''
  previewImage.value = null
}

const createProduct = async () => {
  console.log('🚀 Starting product registration...')
  console.log('📝 Form data:', {
    name: name.value,
    brand: brand.value,
    description: description.value,
    categoryId: selectedCategory.value,
    hasImage: !!productImage.value,
  })

  isLoading.value = true
  const formData = new FormData()
  formData.append('name', name.value)
  formData.append('brand', brand.value)
  formData.append('description', description.value)
  formData.append('productImage', productImage.value)
  formData.append('categoryId', selectedCategory.value)
  // shopId sẽ được backend tự động lấy từ session
  try {
    console.log('📤 Sending request to /api/products/add')
    const res = await axios.post('/api/products/add', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    console.log('📥 Response:', res.data)

    if (res.data.message === 'Sản phẩm đã được tạo thành công!') {
      console.log('✅ Product created successfully!')
      successMessage.value = res.data.message
      resetForm()
      errorMessage.value = ''
      return
    } else {
      console.log('⚠️ Product creation failed:', res.data)
      errorMessage.value =
        res.data.errorMessage ||
        res.data.message ||
        'Đăng ký sản phẩm thất bại! Vui lòng điền đầy đủ thông tin sản phẩm!'
      successMessage.value = ''
    }
  } catch (error) {
    console.error('❌ Product creation error:', error.response?.data || error.message)
    errorMessage.value =
      error.response?.data?.message || 'Đăng ký sản phẩm thất bại! Vui lòng điền đầy đủ thông tin sản phẩm!'
    resetForm()
  } finally {
    isLoading.value = false
  }
}

const categories = ref([])
const selectedCategory = ref(null)
const selectedCategoryName = ref('')
const showDropdown = ref(false)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const selectCategory = (category) => {
  selectedCategory.value = category.id
  selectedCategoryName.value = category.name
  showDropdown.value = false
}

const fetchCategories = async () => {
  try {
    const res = await axios.get('/api/categories')
    categories.value = res.data || []
  } catch (err) {
    console.error('Lỗi khi tải danh mục:', err)
  }
}

// Không cần fetch shop nữa vì backend tự động lấy từ session

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
/* Custom scrollbar cho tất cả elements */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
}

::-webkit-scrollbar-corner {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

* {
  scrollbar-width: thin;
  scrollbar-color: #3b82f6 #f0f9ff;
}

/* Custom scrollbar cho parent elements */
:deep(::-webkit-scrollbar) {
  width: 8px;
  height: 8px;
}

:deep(::-webkit-scrollbar-track) {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 4px;
}

:deep(::-webkit-scrollbar-thumb) {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(::-webkit-scrollbar-thumb:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
}

:deep(::-webkit-scrollbar-corner) {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

:deep(*) {
  scrollbar-width: thin;
  scrollbar-color: #3b82f6 #f0f9ff;
}

/* Custom scrollbar cho Naive UI layout containers */
:deep(.n-layout-scroll-container) {
  scrollbar-width: thin;
  scrollbar-color: #3b82f6 #f0f9ff;
}

:deep(.n-layout-scroll-container::-webkit-scrollbar) {
  width: 8px;
  height: 8px;
}

:deep(.n-layout-scroll-container::-webkit-scrollbar-track) {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 4px;
}

:deep(.n-layout-scroll-container::-webkit-scrollbar-thumb) {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.n-layout-scroll-container::-webkit-scrollbar-thumb:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
}

:deep(.n-layout-scroll-container::-webkit-scrollbar-corner) {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

/* Custom scrollbar cho tất cả Naive UI components */
:deep(.n-scrollbar) {
  scrollbar-width: thin;
  scrollbar-color: #3b82f6 #f0f9ff;
}

:deep(.n-scrollbar::-webkit-scrollbar) {
  width: 8px;
  height: 8px;
}

:deep(.n-scrollbar::-webkit-scrollbar-track) {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 4px;
}

:deep(.n-scrollbar::-webkit-scrollbar-thumb) {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.n-scrollbar::-webkit-scrollbar-thumb:hover) {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
}

:deep(.n-scrollbar::-webkit-scrollbar-corner) {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}
</style>
