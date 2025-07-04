<template>
  <span v-if="isRoot" class="badge root-badge"> Danh mục gốc </span>
  <span v-else class="badge parent-badge">
    {{ parentName }}
  </span>
</template>

<script setup>
defineProps({
  isRoot: Boolean,
  parentName: String,
})
</script>

<style scoped>
.badge {
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

.root-badge {
  background-color: #f0f9ff;
  color: #0891b2;
}

.parent-badge {
  background-color: #e7f3ff;
  color: #0066cc;
}
</style>
