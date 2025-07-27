<template>
  <div>
    <form @submit.prevent="registerProduct">
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
        <label for="image" class="mb-2 block">Hình ảnh cửa hàng:</label>
        <input
          id="image"
          type="file"
          class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          ref="imageInput"
          @change="handleImageChange"
        />
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
      <div class="mt-8">
        <button
          :disabled="isLoading"
          class="w-full py-2 px-3 rounded border border-blue-600 hover:bg-blue-600 hover:text-white text-blue-600"
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

const name = ref('')
const brand = ref('')
const description = ref('')
const productImage = ref(null)
const imageInput = ref(null)
const isLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const handleImageChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    productImage.value = file
  }
}

const resetForm = () => {
  name.value = ''
  brand.value = ''
  description.value = ''
  productImage.value = null
  imageInput.value.value = ''
}

const registerProduct = async () => {
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
