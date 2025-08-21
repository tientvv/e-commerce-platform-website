<template>
  <div class="flex flex-col h-screen">
    <NavbarView />
    <main class="flex-1 max-w-[1440px] w-full mx-auto px-4 py-6">
      <!-- Category Section -->
      <section class="mb-8">
        <CategoryGrid />
      </section>

      <!-- Discounted Products Section -->
      <section class="mb-8">
        <ProductSlider />
      </section>

      <!-- Today Suggestions -->
      <TodaySuggestions />

      <!-- Other sections can be added here -->
      <!-- <section class="mb-8">
        <h2 class="text-2xl font-bold mb-4">Sản phẩm nổi bật</h2>
        Add featured products component here
      </section> -->
    </main>
    <FooterView />
  </div>
</template>

<script setup>
import NavbarView from './components/NavbarView.vue'
import FooterView from './components/FooterView.vue'
import CategoryGrid from './components/CategoryGrid.vue'
import ProductSlider from './components/ProductSlider.vue'
import TodaySuggestions from './components/TodaySuggestions.vue'
</script>
