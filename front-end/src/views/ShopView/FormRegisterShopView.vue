<template>
  <form action="">
    <div class="text-xl">Đăng ký bán hàng</div>
    <div class="mt-4">
      <label for="shopName" class="mb-2 block"> Tên cửa hàng:<span class="text-red-500">*</span> </label>
      <input
        id="shopName"
        type="text"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập tên cửa hàng"
      />
    </div>
    <div class="mt-4">
      <label for="description" class="mb-2 block"> Mô tả cửa hàng:</label>
      <textarea
        id="description"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập mô tả cửa hàng"
      ></textarea>
    </div>
    <div class="mt-4 flex gap-4">
      <div class="w-[50%]">
        <label for="email" class="mb-2 block">Email:<span class="text-red-500">*</span></label>
        <input
          id="email"
          type="text"
          class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          placeholder="Nhập email"
          autocomplete="email"
        />
      </div>
      <div class="w-[50%]">
        <label for="phone" class="mb-2 block">Số điện thoại:<span class="text-red-500">*</span></label>
        <input
          id="phone"
          type="text"
          class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
          placeholder="Nhập số điện thoại"
          autocomplete="tel"
        />
      </div>
    </div>
    <div class="mt-4">
      <label for="address" class="mb-2 block">Địa chỉ:<span class="text-red-500">*</span></label>
      <input
        id="address"
        type="text"
        class="w-full py-2 px-3 border rounded focus:outline-blue-600 border-gray-400"
        placeholder="Nhập địa chỉ cửa hàng"
      />
    </div>
    <div class="mt-8">
      <button class="w-full py-2 px-3 rounded border border-blue-600 hover:bg-blue-600 hover:text-white text-blue-600">
        Đăng ký
      </button>
    </div>
  </form>
</template>

<script setup></script>

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
