<template>
  <main class="px-4 bg-white shadow">
    <div class="max-w-[1440px] mx-auto py-4 flex items-center gap-8 justify-between">
      <LogoNavbar />
      <div class="flex flex-col w-full">
        <SearchInput />
        <SuggestLink />
      </div>
      <div class="flex gap-4">
        <WishlistLink />
        <CartItemLink />
        <AccountLink v-if="!infoAccount" />
        <UserProfileLink v-if="infoAccount" />
      </div>
    </div>
  </main>
</template>

<script setup>
import AccountLink from '../links/AccountLink.vue'
import SearchInput from '../inputs/SearchInput.vue'
import SuggestLink from '../links/SuggestLink.vue'
import WishlistLink from '../links/WishlistLink.vue'
import CartItemLink from '../links/CartItemLink.vue'
import UserProfileLink from '../links/UserProfileLink.vue'
import { onMounted, ref } from 'vue'
import { getInfoAccount } from '@/api/AuthController'
import LogoNavbar from '@/components/navbars/LogoNavbar.vue'

const infoAccount = ref(null)

onMounted(async () => {
  try {
    infoAccount.value = await getInfoAccount()
  } catch (error) {
    console.error('Failed to fetch account info:', error)
  }
})
</script>
