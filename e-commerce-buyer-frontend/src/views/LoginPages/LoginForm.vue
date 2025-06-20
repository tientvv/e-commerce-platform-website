<script setup></script>

<template>
  <form action="">
    <div class="flex flex-col mb-6">
      <label for="emailUser" class="text-sm mb-3">Email</label>
      <input
        class="border-b-2 pb-2 text-sm focus:border-b-blue-500 focus:text-black border-b-gray-300 text-gray-400"
        type="text"
        name="emailUser"
        id="emailUser"
        placeholder="Nhập email"
      />
    </div>
    <div class="flex flex-col">
      <label for="password" class="text-sm mb-3">Mật khẩu</label>
      <input
        class="border-b-2 pb-2 text-sm focus:border-b-blue-500 focus:text-black border-b-gray-300 text-gray-400"
        type="password"
        name="password"
        id="password"
        placeholder="Nhập mật khẩu"
      />
    </div>
    <button class="bg-red-500 text-white mt-8 w-full py-3 rounded">Đăng nhập</button>
  </form>
</template>
