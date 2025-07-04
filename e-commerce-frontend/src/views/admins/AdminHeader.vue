<template>
  <main class="px-4 bg-blue-950 shadow">
    <div class="max-w-[1440px] mx-auto py-4 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <div class="text-[20px] text-white font-bold">Quản trị viên</div>
        <div class="text-white border-l pl-4 ml-4">{{ pageTitle }}</div>
      </div>
      <form>
        <button class="bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded">
          Đăng xuất
        </button>
      </form>
    </div>
  </main>
</template>

<script setup>
defineProps({
  pageTitle: {
    type: String,
    default: 'Trang chủ',
  },
})
</script>
