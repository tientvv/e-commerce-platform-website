<template>
  <div class="p-4">
    <div>Danh mục</div>
    <div class="mt-4 flex flex-col gap-2">
      <div>
        <RouterLink to="/user/shop/profile" v-slot="{ isActive }">
          <div :class="['py-1 px-2 flex items-center gap-2 rounded', isActive ? 'bg-gray-200 ' : 'hover:bg-gray-200']">
            <Info width="18" />
            Thông tin cửa hàng
          </div>
        </RouterLink>
      </div>
      <div>
        <RouterLink to="/user/shop/product/list">
          <div
            :class="[
              'py-1 px-2 flex items-center gap-2 rounded',
              route.path.startsWith('/user/shop/product') &&
              !route.path.startsWith('/user/shop/product-variants') &&
              !route.path.startsWith('/user/shop/product-images')
                ? 'bg-gray-200'
                : 'hover:bg-gray-200',
            ]"
          >
            <Package width="18" />
            Quản lý sản phẩm
          </div>
        </RouterLink>
      </div>
      <div>
        <RouterLink to="/user/shop/product-variants/index">
          <div
            :class="[
              'py-1 px-2 flex items-center gap-2 rounded',
              route.path.startsWith('/user/shop/product-variants') ? 'bg-gray-200' : 'hover:bg-gray-200',
            ]"
          >
            <Settings width="18" />
            Quản lý SP biến thể
          </div>
        </RouterLink>
      </div>
      <div>
        <RouterLink to="/user/shop/product-images/index">
          <div
            :class="[
              'py-1 px-2 flex items-center gap-2 rounded',
              route.path.startsWith('/user/shop/product-images') ? 'bg-gray-200' : 'hover:bg-gray-200',
            ]"
          >
            <Image width="18" />
            Quản lý ảnh SP
          </div>
        </RouterLink>
      </div>
      <div>
        <RouterLink to="/user/shop/statistics" v-slot="{ isActive }">
          <div :class="['py-1 px-2 flex items-center gap-2 rounded', isActive ? 'bg-gray-200 ' : 'hover:bg-gray-200']">
            <BarChart3 width="18" />
            Thống kê cửa hàng
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Info, Package, Settings, Image, BarChart3 } from 'lucide-vue-next'
import { useRoute } from 'vue-router'

const route = useRoute()
</script>
