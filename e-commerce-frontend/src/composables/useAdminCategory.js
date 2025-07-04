import { ref, computed, onMounted } from 'vue'
import { useCategories } from '@/composables/useCategories'
import { useCategoryStats } from '@/composables/useCategoryStats'
import { useCategoryTable } from '@/composables/useCategoryTable'

export function useAdminCategory() {
  // Composables
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

  const { categoryStats } = useCategoryStats(categories)
  const { tableColumns, actionButtons, filterOptions, formatDate } = useCategoryTable()

  // Local state
  const showModal = ref(false)
  const showDeleteModal = ref(false)
  const categoryToDelete = ref(null)
  const filterType = ref('all')
  const searchQuery = ref('')

  // Computed
  const filteredCategories = computed(() => {
    let filtered = categories.value

    // Filter by type
    if (filterType.value === 'root') {
      filtered = filtered.filter((cat) => !cat.parentId)
    } else if (filterType.value === 'children') {
      filtered = filtered.filter((cat) => cat.parentId)
    }

    // Search filter
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

  // Modal actions
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

  // Delete actions
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

  // Table actions
  const handleTableAction = (action, category) => {
    if (action === 'edit') {
      openEditModal(category)
    } else if (action === 'delete') {
      confirmDelete(category)
    }
  }

  // Lifecycle
  onMounted(() => {
    fetchCategories()
  })

  return {
    // Data
    categories,
    loading,
    error,
    categoryForm,
    isEditMode,
    categoryStats,
    tableColumns,
    actionButtons,
    filterOptions,
    filteredCategories,
    availableParentsForModal,

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
  }
}
