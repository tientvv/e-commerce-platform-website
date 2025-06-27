<script setup>
// Import HeaderView and FooterView components
import FooterView from '@/components/user/FooterView.vue'
// Import LoginForm component
import LoginForm from './LoginForm.vue'
</script>

<template>
  <main class="login_page">
    <div class="bg-white">
      <div class="login_header">
        <div class="login_navbar">
          <div class="login_title">
            <router-link to="/" class="login_logo">
              <img src="/images/logo.svg" alt="Logo" width="50px" height="50px" />
              <p>E-Commerce</p>
            </router-link>
            <span class="ms-4">Đăng nhập</span>
          </div>
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
            <LoginForm />
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
