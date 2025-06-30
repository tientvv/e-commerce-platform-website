<script setup>
import HeaderView from '@/components/user/HeaderView.vue'
</script>

<template>
  <main>
    <HeaderView />
    <div class="">
      <div class="max-w-[1440px] mx-auto flex gap-4 mt-4">
        <div class="w-[20%] bg-white ms-[21px] py-4 px-6 rounded shadow">
          <h2 class="text-[20px] text-center mb-6">Cài đặt</h2>
          <ul class="flex flex-col">
            <li class="flex">
              <div
                class="hover:bg-emerald-700 hover:text-white rounded px-3 py-2 w-full cursor-pointer"
              >
                <span class="">Tài khoản</span>
              </div>
            </li>
            <li class="flex">
              <router-link
                to="/seller"
                class="hover:bg-emerald-700 hover:text-white rounded px-3 py-2 w-full cursor-pointer"
                >Kênh bán hàng
              </router-link>
            </li>
            <li class="flex">
              <div
                class="hover:bg-red-400 hover:text-white rounded px-3 py-2 w-full bg-red-100 text-red-600 text-center cursor-pointer"
              >
                <span class="">Đăng xuất</span>
              </div>
            </li>
          </ul>
        </div>
        <div class="w-[80%] bg-white me-[21px] p-4 rounded shadow">
          <div>Cập nhật tài khoản</div>
        </div>
      </div>
    </div>
  </main>
</template>
