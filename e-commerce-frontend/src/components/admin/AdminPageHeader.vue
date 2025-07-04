<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between">
      <div class="flex items-center space-x-3">
        <div class="p-2 bg-blue-100 rounded-lg">
          <i :class="`${icon} text-blue-600 text-xl`"></i>
        </div>
        <div>
          <h1 class="text-2xl font-bold text-gray-900">{{ title }}</h1>
          <p v-if="subtitle" class="text-sm text-gray-500 mt-1">{{ subtitle }}</p>
        </div>
      </div>
      <div class="mt-4 sm:mt-0">
        <BaseButton
          :icon="buttonIcon"
          @click="$emit('action')"
          class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors duration-200 flex items-center space-x-2"
        >
          <span>{{ buttonText }}</span>
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import BaseButton from '@/components/base/BaseButton.vue'

defineProps({
  icon: { type: String, default: 'fas fa-cog' },
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  buttonIcon: { type: String, default: 'fas fa-plus' },
  buttonText: { type: String, default: 'Thêm mới' },
})

defineEmits(['action'])
</script>
