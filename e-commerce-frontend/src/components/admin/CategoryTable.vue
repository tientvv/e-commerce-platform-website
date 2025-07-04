<template>
  <DataTable
    :columns="tableColumns"
    :data="filteredCategories"
    :loading="loading"
    loading-text="Đang tải dữ liệu..."
    empty-icon="fas fa-folder-open"
    empty-title="Không có danh mục nào"
    :empty-message="emptyMessage"
  >
    <!-- Image Column -->
    <template #cell-image="{ item }">
      <CategoryImage :src="item.categoryImage" :alt="item.name" />
    </template>

    <!-- Name Column -->
    <template #cell-name="{ item }">
      <div class="text-sm font-medium text-gray-900">{{ item.name }}</div>
    </template>

    <!-- Parent Column -->
    <template #cell-parent="{ item }">
      <CategoryBadge :is-root="!item.parentName" :parent-name="item.parentName" />
    </template>

    <!-- Date Column -->
    <template #cell-createdAt="{ item }">
      <span class="text-sm text-gray-500">{{ formatDate(item.createdAt) }}</span>
    </template>

    <!-- Actions Column -->
    <template #cell-actions="{ item }">
      <ActionButtons
        :actions="actionButtons"
        :item="item"
        @action="$emit('action', $event, item)"
      />
    </template>

    <!-- Empty Action -->
    <template #empty-action>
      <BaseButton
        v-if="!searchQuery"
        icon="fas fa-plus"
        @click="$emit('create')"
        class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors duration-200"
      >
        Tạo danh mục đầu tiên
      </BaseButton>
    </template>
  </DataTable>
</template>

<script setup>
import { computed } from 'vue'
import DataTable from '@/components/admin/DataTable.vue'
import ActionButtons from '@/components/admin/ActionButtons.vue'
import CategoryImage from '@/components/category/CategoryImage.vue'
import CategoryBadge from '@/components/category/CategoryBadge.vue'
import BaseButton from '@/components/base/BaseButton.vue'

const props = defineProps({
  tableColumns: Array,
  filteredCategories: Array,
  loading: Boolean,
  searchQuery: String,
  actionButtons: Array,
  formatDate: Function,
})

defineEmits(['action', 'create'])

const emptyMessage = computed(() =>
  props.searchQuery
    ? 'Không tìm thấy danh mục phù hợp với từ khóa tìm kiếm'
    : 'Chưa có danh mục nào được tạo',
)
</script>
