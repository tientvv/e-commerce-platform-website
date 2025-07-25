<template>
  <div class="mt-2">
    <div class="overflow-hidden rounded border border-gray-200">
      <table class="w-full text-left">
        <thead class="bg-gray-200">
          <tr>
            <th class="p-2 border-b border-r border-gray-200">Tên</th>
            <th class="p-2 border-b border-r border-gray-200">Thương hiệu</th>
            <th class="p-2 border-b border-r border-gray-200">Danh mục</th>
            <th class="p-2 border-b border-r border-gray-200">Mô tả</th>
            <th class="p-2 border-b border-gray-200">Ảnh</th>
            <th class="p-2 border-b border-gray-200">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td class="p-2 border-r border-gray-200">{{ product.name }}</td>
            <td class="p-2 border-r border-gray-200">{{ product.brand }}</td>
            <td class="p-2 border-r border-gray-200">{{ product.categoryName }}</td>
            <td class="p-2 border-r border-gray-200">{{ product.description }}</td>
            <td class="p-2 border-r border-gray-200">
              <img :src="product.productImage" alt="Ảnh" class="w-16 h-16 object-cover" />
            </td>
            <td class="p-2 border-gray-200 flex flex-col gap-2">
              <RouterLink
                :to="`/user/shop/product/edit/${product.id}`"
                class="px-3 py-2 bg-green-600 text-white rounded hover:bg-green-700 text-center"
              >
                Sửa
              </RouterLink>
              <button
                @click="deleteProduct(product.id)"
                class="px-3 py-2 bg-red-600 text-white rounded hover:bg-red-700 text-center"
              >
                Xóa
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { onMounted, ref } from 'vue'

const products = ref([])

const loadProducts = async () => {
  try {
    const response = await axios.get('/api/user/shop')
    const shop = response.data.shop
    if (!shop || !shop.id) {
      console.warn('Không tìm thấy shop')
      return
    }
    const productRes = await axios.get('/api/products/index', {
      params: {
        shopId: shop.id,
        isActive: true,
      },
    })

    products.value = productRes.data.products || []
  } catch (err) {
    console.error('Lỗi khi tải sản phẩm:', err)
  }
}

const deleteProduct = async (productId) => {
  try {
    await axios.put(`/api/products/delete/${productId}`)
    window.location.reload()
  } catch (err) {
    console.error('Lỗi khi xóa sản phẩm:', err)
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
table {
  border-collapse: collapse;
}

button {
  color: white;
}
</style>
