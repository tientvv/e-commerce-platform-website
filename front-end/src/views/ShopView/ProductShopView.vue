<template>
  <div class="flex gap-2">
    <RouterLink to="/user/shop/product/list" class="py-2 px-3 bg-gray-200 block rounded"> Danh sách </RouterLink>
    <RouterLink to="/user/shop/product/add" class="py-2 px-3 bg-gray-200 block rounded"> Thêm</RouterLink>
  </div>
  <div><RouterView /></div>
</template>
