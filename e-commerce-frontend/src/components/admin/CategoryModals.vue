<template>
  <!-- Category Modal -->
  <CategoryModal
    :show="showModal"
    :form-data="categoryForm"
    :available-parents="availableParentsForModal"
    :is-edit="isEditMode"
    :loading="loading"
    @close="$emit('close-modal')"
    @submit="$emit('submit')"
    @update:form-data="$emit('update:form-data', $event)"
  />

  <!-- Delete Confirmation Modal -->
  <ConfirmModal
    :show="showDeleteModal"
    title="Xác nhận xóa danh mục"
    :message="`Bạn có chắc chắn muốn xóa danh mục '${categoryToDelete?.name}'?`"
    confirm-text="Xóa"
    cancel-text="Hủy"
    type="danger"
    @confirm="$emit('confirm-delete')"
    @cancel="$emit('cancel-delete')"
  />
</template>

<script setup>
import CategoryModal from '@/components/modals/CategoryModal.vue'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'

defineProps({
  showModal: Boolean,
  categoryForm: Object,
  availableParentsForModal: Array,
  isEditMode: Boolean,
  loading: Boolean,
  showDeleteModal: Boolean,
  categoryToDelete: Object,
})

defineEmits(['close-modal', 'submit', 'update:form-data', 'confirm-delete', 'cancel-delete'])
</script>
