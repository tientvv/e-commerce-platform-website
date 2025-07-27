<template>
  <div class="p-4">
    <h2 class="text-xl font-bold mb-4">🧪 Test CRUD Products</h2>

    <div class="space-y-4">
      <!-- Test Buttons -->
      <div class="flex gap-2">
        <button @click="testGetShop" class="px-4 py-2 bg-blue-500 text-white rounded">Test Get Shop</button>
        <button @click="testGetProducts" class="px-4 py-2 bg-green-500 text-white rounded">Test Get Products</button>
        <button @click="testGetCategories" class="px-4 py-2 bg-purple-500 text-white rounded">
          Test Get Categories
        </button>
      </div>

      <!-- Results Display -->
      <div class="bg-gray-100 p-4 rounded">
        <h3 class="font-bold mb-2">Test Results:</h3>
        <pre class="text-sm overflow-auto max-h-96">{{ testResults }}</pre>
      </div>

      <!-- Quick Stats -->
      <div class="grid grid-cols-3 gap-4">
        <div class="bg-blue-50 p-3 rounded">
          <div class="text-blue-800 font-bold">Shop Status</div>
          <div class="text-blue-600">{{ shopStatus }}</div>
        </div>
        <div class="bg-green-50 p-3 rounded">
          <div class="text-green-800 font-bold">Products Count</div>
          <div class="text-green-600">{{ productsCount }}</div>
        </div>
        <div class="bg-purple-50 p-3 rounded">
          <div class="text-purple-800 font-bold">Categories Count</div>
          <div class="text-purple-600">{{ categoriesCount }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const testResults = ref('Click buttons to test endpoints...')
const shopStatus = ref('Unknown')
const productsCount = ref(0)
const categoriesCount = ref(0)

const testGetShop = async () => {
  try {
    testResults.value = 'Testing /api/user/shop...\n'
    const response = await axios.get('/api/user/shop')

    testResults.value += `✅ Shop API Response:\n${JSON.stringify(response.data, null, 2)}\n`

    if (response.data.shop) {
      shopStatus.value = `✅ ${response.data.shop.shopName}`
    } else {
      shopStatus.value = '❌ No shop found'
    }
  } catch (error) {
    testResults.value += `❌ Shop API Error:\n${JSON.stringify(error.response?.data || error.message, null, 2)}\n`
    shopStatus.value = '❌ Error'
  }
}

const testGetProducts = async () => {
  try {
    testResults.value += '\nTesting /api/products/all...\n'

    // First get shop
    const shopResponse = await axios.get('/api/user/shop')
    const shopId = shopResponse.data.shop?.id

    if (!shopId) {
      testResults.value += '❌ No shop ID found\n'
      return
    }

    testResults.value += `Shop ID: ${shopId}\n`

    // Then get products
    const response = await axios.get('/api/products/all', {
      params: { shopId },
    })

    testResults.value += `✅ Products API Response:\n${JSON.stringify(response.data, null, 2)}\n`

    productsCount.value = response.data.products?.length || 0
  } catch (error) {
    testResults.value += `❌ Products API Error:\n${JSON.stringify(error.response?.data || error.message, null, 2)}\n`
    productsCount.value = 0
  }
}

const testGetCategories = async () => {
  try {
    testResults.value += '\nTesting /api/categories/all...\n'
    const response = await axios.get('/api/categories/all')

    testResults.value += `✅ Categories API Response:\n${JSON.stringify(response.data, null, 2)}\n`

    categoriesCount.value = response.data.categories?.length || 0
  } catch (error) {
    testResults.value += `❌ Categories API Error:\n${JSON.stringify(error.response?.data || error.message, null, 2)}\n`
    categoriesCount.value = 0
  }
}
</script>
