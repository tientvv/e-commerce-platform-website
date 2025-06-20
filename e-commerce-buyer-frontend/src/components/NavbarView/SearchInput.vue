<script setup>
import { Search } from 'lucide-vue-next'
</script>

<template>
  <div class="w-[60%]">
    <form
      action=""
      class="flex items-center justify-center gap-3 border border-gray-500 rounded ps-3 py-1"
    >
      <input
        id="search"
        class="flex-1 text-gray-500"
        type="text"
        placeholder="Tìm kiếm sản phẩm..."
        autocomplete="off"
      />
      <button class="bg-blue-400 py-1 px-4 me-1 rounded">
        <Search class="text-white" width="24" height="24" />
      </button>
    </form>
  </div>
</template>
