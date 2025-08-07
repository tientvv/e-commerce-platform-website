<template>
  <div class="min-h-[400px] flex items-center justify-center">
    <div class="text-center max-w-md mx-auto p-8">
      <div class="mb-6">
        <Store class="w-24 h-24 text-gray-300 mx-auto mb-4" />
        <h2 class="text-2xl font-bold text-gray-900 mb-2">Chưa có cửa hàng</h2>
        <p class="text-gray-600">Bạn cần đăng ký cửa hàng trước khi có thể truy cập vào kênh bán hàng.</p>
      </div>

      <div class="space-y-4">
        <RouterLink
          to="/register-shop"
          class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 transition-colors"
        >
          <Plus class="w-5 h-5 mr-2" />
          Đăng ký cửa hàng ngay
        </RouterLink>

        <div class="text-sm text-gray-500">
          <p>Sau khi đăng ký thành công, bạn sẽ có thể:</p>
          <ul class="mt-2 space-y-1">
            <li>• Quản lý sản phẩm và biến thể</li>
            <li>• Xem thống kê bán hàng</li>
            <li>• Cập nhật thông tin cửa hàng</li>
            <li>• Quản lý đơn hàng</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Store, Plus } from 'lucide-vue-next'
</script>
