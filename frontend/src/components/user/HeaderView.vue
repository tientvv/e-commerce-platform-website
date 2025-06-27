<script setup>
import { House } from 'lucide-vue-next'
import { CircleUserRound } from 'lucide-vue-next'
</script>
<template>
  <main>
    <div class="header">
      <nav class="header_nav">
        <router-link to="/" class="header_logo">
          <img src="/images/logo.svg" alt="Logo" width="50px" height="50px" />
          <p>E-Commerce</p>
        </router-link>
        <div>
          <ul class="header_menu">
            <li>
              <router-link to="/">
                <House width="24px" height="24px" />
                <span>Trang chủ</span>
              </router-link>
            </li>
            <li>
              <router-link to="/login">
                <CircleUserRound width="24px" height="24px" />
                <span>Đăng nhập</span>
              </router-link>
            </li>
          </ul>
        </div>
      </nav>
    </div>
  </main>
</template>
