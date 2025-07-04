<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
    <!-- Loading State -->
    <div v-if="loading" class="p-12">
      <div class="flex flex-col items-center justify-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
        <span class="text-gray-600">{{ loadingText }}</span>
      </div>
    </div>

    <!-- Table -->
    <div v-else class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th
              v-for="column in columns"
              :key="column.key"
              :class="[
                'px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider',
                column.align === 'center' ? 'text-center' : 'text-left',
              ]"
            >
              {{ column.label }}
            </th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr
            v-for="(item, index) in data"
            :key="getItemKey(item, index)"
            class="hover:bg-gray-50 transition-colors duration-150"
          >
            <td
              v-for="column in columns"
              :key="column.key"
              :class="[
                'px-6 py-4 whitespace-nowrap',
                column.align === 'center' ? 'text-center' : '',
              ]"
            >
              <slot
                :name="`cell-${column.key}`"
                :item="item"
                :value="getColumnValue(item, column.key)"
                :index="index"
              >
                {{ getColumnValue(item, column.key) }}
              </slot>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && data.length === 0" class="text-center py-12">
      <div class="mx-auto h-24 w-24 text-gray-300 mb-4">
        <i :class="`${emptyIcon} text-6xl`"></i>
      </div>
      <h3 class="text-lg font-medium text-gray-900 mb-2">{{ emptyTitle }}</h3>
      <p class="text-gray-500 mb-6">{{ emptyMessage }}</p>
      <slot name="empty-action"></slot>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  columns: {
    type: Array,
    required: true,
    validator: (columns) => columns.every((col) => col.key && col.label),
  },
  data: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  loadingText: { type: String, default: 'Đang tải dữ liệu...' },
  emptyIcon: { type: String, default: 'fas fa-folder-open' },
  emptyTitle: { type: String, default: 'Không có dữ liệu' },
  emptyMessage: { type: String, default: 'Chưa có dữ liệu nào được tạo' },
  itemKey: { type: String, default: 'id' },
})

const getItemKey = (item, index) => {
  return item[props.itemKey] || index
}

const getColumnValue = (item, key) => {
  return key.split('.').reduce((obj, k) => obj?.[k], item)
}
</script>
