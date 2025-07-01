<template>
  <main class="px-4 bg-white shadow">
    <div class="max-w-[1440px] mx-auto p-4 flex items-center gap-8 justify-between">
      <LogoNavbar />
      <div class="flex flex-col w-full">
        <SearchInput />
        <SuggestLink />
      </div>
      <div class="flex">
        <WishlistLink />
        <CartItemLink />
        <AccountLink />
      </div>
    </div>
  </main>
</template>

<script setup>
import AccountLink from '../links/AccountLink.vue'
import LogoNavbar from './LogoNavbar.vue'
import SearchInput from '../inputs/SearchInput.vue'
import SuggestLink from '../links/SuggestLink.vue'
import WishlistLink from '../links/WishlistLink.vue'
import CartItemLink from '../links/CartItemLink.vue'
</script>
