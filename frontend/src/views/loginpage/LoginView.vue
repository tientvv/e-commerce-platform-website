<script setup>
// Import HeaderView and FooterView components
import FooterView from '@/components/user/FooterView.vue'
</script>

<template>
  <main class="login_page">
    <div class="login_header">
      <div class="login_navbar">
        <div class="login_title">
          <img class="login_logo" src="" alt="logo" />
          <span>Đăng nhập</span>
        </div>
      </div>
    </div>
    <!-- Login View -->
    <div class="login_main">
      <div class="login_bg"></div>
      <div class="login_overlay">
        <div class="w-1/2 flex justify-center">
          <!-- Login Form -->
          <div class="login_form--login">
            <div class="login_form--title">
              <h2>Đăng nhập</h2>
            </div>
            <form>Form Đăng nhập</form>
          </div>
        </div>
        <div class="w-1/2 flex justify-center">
          <div class="login_from--register">
            <div class="login_form--title">
              <h2>Đăng ký</h2>
            </div>
            <form>Form Đăng ký</form>
          </div>
        </div>
      </div>
    </div>
    <!-- Footer View -->
    <FooterView />
  </main>
</template>
