<template>
  <div class="admin-category">
    <!-- Header -->
    <div class="page-header">
      <h1 class="page-title">
        <i class="fas fa-tags"></i>
        Quản lý danh mục
      </h1>
      <BaseButton icon="fas fa-plus" @click="openCreateModal"> Thêm danh mục </BaseButton>
    </div>

    <!-- Filters -->
    <div class="filters-section">
      <BaseSelect
        v-model="filterType"
        label="Lọc theo:"
        :options="filterOptions"
        class="filter-select"
      />
      <BaseInput
        v-model="searchQuery"
        placeholder="Tìm kiếm danh mục..."
        type="text"
        class="search-input"
      >
        <template #suffix>
          <i class="fas fa-search"></i>
        </template>
      </BaseInput>
    </div>

    <!-- Error Alert -->
    <div v-if="error" class="alert alert-danger">
      <i class="fas fa-exclamation-triangle"></i>
      {{ error }}
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <i class="fas fa-spinner fa-spin"></i>
      <span>Đang tải dữ liệu...</span>
    </div>

    <!-- Categories Table -->
    <div v-else class="table-container">
      <table class="table">
        <thead>
          <tr>
            <th>Hình ảnh</th>
            <th>Tên danh mục</th>
            <th>Danh mục cha</th>
            <th>Ngày tạo</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="category in filteredCategories" :key="category.id">
            <td class="image-cell">
              <CategoryImage :src="category.categoryImage" :alt="category.name" />
            </td>
            <td>
              <strong>{{ category.name }}</strong>
            </td>
            <td>
              <CategoryBadge :is-root="!category.parentName" :parent-name="category.parentName" />
            </td>
            <td>{{ formatDate(category.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <BaseButton
                  size="small"
                  variant="success"
                  icon="fas fa-edit"
                  @click="openEditModal(category)"
                />
                <BaseButton
                  size="small"
                  variant="danger"
                  icon="fas fa-trash"
                  @click="confirmDelete(category)"
                />
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Empty State -->
      <div v-if="filteredCategories.length === 0" class="empty-state">
        <i class="fas fa-folder-open"></i>
        <h3>Không có danh mục nào</h3>
        <p>
          {{ searchQuery ? 'Không tìm thấy danh mục phù hợp' : 'Chưa có danh mục nào được tạo' }}
        </p>
      </div>
    </div>

    <!-- Modals -->
    <CategoryModal
      :show="showModal"
      :form-data="categoryForm"
      :available-parents="availableParentsForModal"
      :is-edit="isEditMode"
      :loading="loading"
      @close="closeModal"
      @submit="handleSubmit"
      @update:form-data="categoryForm = $event"
    />

    <ConfirmModal
      :show="showDeleteModal"
      title="Xác nhận xóa danh mục"
      :message="`Bạn có chắc chắn muốn xóa danh mục '${categoryToDelete?.name}'?`"
      confirm-text="Xóa"
      cancel-text="Hủy"
      type="danger"
      @confirm="handleDelete"
      @cancel="closeDeleteModal"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCategories } from '@/composables/useCategories'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import CategoryModal from '@/components/modals/CategoryModal.vue'
import ConfirmModal from '@/components/modals/ConfirmModal.vue'
import CategoryImage from '@/components/category/CategoryImage.vue'
import CategoryBadge from '@/components/category/CategoryBadge.vue'

// Composable
const {
  categories,
  loading,
  error,
  categoryForm,
  isEditMode,
  resetForm,
  fetchCategories,
  saveCategory,
  deleteCategory,
  editCategory,
} = useCategories()

// Local state
const showModal = ref(false)
const showDeleteModal = ref(false)
const categoryToDelete = ref(null)
const filterType = ref('all')
const searchQuery = ref('')

// Static data
const filterOptions = [
  { value: 'all', label: 'Tất cả danh mục' },
  { value: 'root', label: 'Chỉ danh mục gốc' },
  { value: 'children', label: 'Chỉ danh mục con' },
]

// Computed
const filteredCategories = computed(() => {
  let filtered = categories.value

  if (filterType.value === 'root') {
    filtered = filtered.filter((cat) => !cat.parentId)
  } else if (filterType.value === 'children') {
    filtered = filtered.filter((cat) => cat.parentId)
  }

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(
      (cat) =>
        cat.name.toLowerCase().includes(query) ||
        (cat.parentName && cat.parentName.toLowerCase().includes(query)),
    )
  }

  return filtered
})

const availableParentsForModal = computed(() => {
  if (isEditMode.value) {
    return categories.value.filter((cat) => cat.id !== categoryForm.value.id)
  }
  return categories.value
})

// Methods
const openCreateModal = () => {
  resetForm()
  showModal.value = true
}

const openEditModal = (category) => {
  editCategory(category)
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const handleSubmit = async () => {
  const success = await saveCategory()
  if (success) {
    closeModal()
  }
}

const confirmDelete = (category) => {
  categoryToDelete.value = category
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  categoryToDelete.value = null
}

const handleDelete = async () => {
  if (categoryToDelete.value) {
    const success = await deleteCategory(categoryToDelete.value.id)
    if (success) {
      closeDeleteModal()
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

// Lifecycle
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.admin-category {
  padding: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
}

.filters-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  align-items: end;
}

.filter-select {
  max-width: 200px;
}

.search-input {
  flex: 1;
  max-width: 400px;
}

.image-cell {
  width: 80px;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

@media (max-width: 768px) {
  .admin-category {
    padding: 1rem;
  }

  .filters-section {
    flex-direction: column;
  }

  .filter-select,
  .search-input {
    max-width: none;
  }
}
</style>
