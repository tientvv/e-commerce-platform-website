<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
    <div class="flex flex-col md:flex-row gap-4 items-end">
      <div class="flex-shrink-0 w-full md:w-64">
        <BaseSelect
          :model-value="filterValue"
          @update:model-value="$emit('update:filter', $event)"
          label="Lọc theo:"
          :options="filterOptions"
          class="w-full"
        />
      </div>
      <div class="flex-1">
        <BaseInput
          :model-value="searchValue"
          @update:model-value="$emit('update:search', $event)"
          placeholder="Tìm kiếm..."
          type="text"
          class="w-full"
        >
          <template #prefix>
            <i class="fas fa-search text-gray-400"></i>
          </template>
        </BaseInput>
      </div>
    </div>

    <!-- Stats -->
    <div v-if="showStats" class="mt-6">
      <StatsCards :stats="stats" />
    </div>
  </div>
</template>

<script setup>
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import StatsCards from '@/components/admin/StatsCards.vue'

defineProps({
  filterValue: [String, Number],
  searchValue: String,
  filterOptions: Array,
  showStats: { type: Boolean, default: false },
  stats: { type: Array, default: () => [] },
})

defineEmits(['update:filter', 'update:search'])
</script>
