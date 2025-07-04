<template>
  <div
    class="w-16 h-16 rounded-lg overflow-hidden flex items-center justify-center bg-gray-100 border border-gray-200"
  >
    <img
      v-if="src && !imageError"
      :src="src"
      :alt="alt || 'Category image'"
      @error="handleImageError"
      @load="handleImageLoad"
      class="w-full h-full object-cover"
    />
    <div v-else class="text-gray-400 text-xl">
      <i class="fas fa-image"></i>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  src: String,
  alt: String,
})

const imageError = ref(false)

const handleImageError = () => {
  imageError.value = true
}

const handleImageLoad = () => {
  // Image loaded successfully
}
</script>
