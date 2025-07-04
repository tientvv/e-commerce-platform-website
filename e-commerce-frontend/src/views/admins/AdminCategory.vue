<template>
  <div class="min-h-screen bg-gray-50 py-6">
    <div class="max-w-7xl mx-auto sm:px-6 lg:px-8">
      <!-- Header -->
      <AdminPageHeader
        icon="fas fa-tags"
        title="Quản lý danh mục"
        button-text="Thêm danh mục"
        @action="openCreateModal"
      />

      <!-- Filters & Stats -->
      <FilterSection
        :filter-value="filterType"
        :search-value="searchQuery"
        :filter-options="filterOptions"
        :show-stats="true"
        :stats="categoryStats"
        @update:filter="filterType = $event"
        @update:search="searchQuery = $event"
      />

      <!-- Error Alert -->
      <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
        <div class="flex items-center">
          <i class="fas fa-exclamation-triangle text-red-400 mr-3"></i>
          <span class="text-red-800">{{ error }}</span>
        </div>
      </div>

      <!-- Categories Table -->
      <CategoryTable
        :table-columns="tableColumns"
        :filtered-categories="filteredCategories"
        :loading="loading"
        :search-query="searchQuery"
        :action-buttons="actionButtons"
        :format-date="formatDate"
        @action="handleTableAction"
        @create="openCreateModal"
      />
    </div>

    <!-- Modals -->
    <CategoryModals
      :show-modal="showModal"
      :category-form="categoryForm"
      :available-parents-for-modal="availableParentsForModal"
      :is-edit-mode="isEditMode"
      :loading="loading"
      :show-delete-modal="showDeleteModal"
      :category-to-delete="categoryToDelete"
      @close-modal="closeModal"
      @submit="handleSubmit"
      @update:form-data="categoryForm = $event"
      @confirm-delete="handleDelete"
      @cancel-delete="closeDeleteModal"
    />
  </div>
</template>

<script setup>
import { useAdminCategory } from '@/composables/useAdminCategory'

// Components
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import FilterSection from '@/components/admin/FilterSection.vue'
import CategoryTable from '@/components/admin/CategoryTable.vue'
import CategoryModals from '@/components/admin/CategoryModals.vue'

// Use admin category composable
const {
  // Data
  error,
  categoryForm,
  isEditMode,
  categoryStats,
  tableColumns,
  actionButtons,
  filterOptions,
  filteredCategories,
  availableParentsForModal,
  loading,

  // Local state
  showModal,
  showDeleteModal,
  categoryToDelete,
  filterType,
  searchQuery,

  // Methods
  formatDate,
  openCreateModal,
  closeModal,
  handleSubmit,
  closeDeleteModal,
  handleDelete,
  handleTableAction,
} = useAdminCategory()
</script>
