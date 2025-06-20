<script setup></script>

<template>
  <div class="bg-white w-[400px] p-8 shadow-xl rounded">
    <p class="mb-8 text-xl text-center">Đăng nhập</p>
    <form action="">
      <div class="flex flex-col mb-6">
        <label for="emailUser" class="text-sm mb-3">Tài khoản</label>
        <input
          class="border-b-2 pb-2 text-sm focus:border-b-blue-500 focus:text-black border-b-gray-300 text-gray-600"
          type="text"
          name="emailUser"
          id="emailUser"
          placeholder="Nhập tài khoản"
          value="admin"
        />
      </div>
      <div class="flex flex-col">
        <label for="password" class="text-sm mb-3">Mật khẩu</label>
        <input
          class="border-b-2 pb-2 text-sm focus:border-b-blue-500 focus:text-black border-b-gray-300 text-gray-600"
          type="password"
          name="password"
          id="password"
          placeholder="Nhập mật khẩu"
          value="123456"
        />
      </div>
      <button class="bg-red-500 text-white mt-8 w-full py-3 rounded text-[14px]">Đăng nhập</button>
    </form>
  </div>
</template>
