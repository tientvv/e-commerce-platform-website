import { ref, computed } from 'vue'
import { categoryAPI } from '@/api/CategoryController'
import { uploadImage } from '@/stores/useUploadImage'

export function useCategories() {
  const categories = ref([])
  const rootCategories = ref([])
  const loading = ref(false)
  const error = ref(null)

  // Form data cho modal
  const categoryForm = ref({
    id: null,
    name: '',
    categoryImage: null,
    categoryImageFile: null,
    parentId: null,
  })

  const isEditMode = computed(() => !!categoryForm.value.id)

  // Reset form
  const resetForm = () => {
    categoryForm.value = {
      id: null,
      name: '',
      categoryImage: null,
      categoryImageFile: null,
      parentId: null,
    }
    error.value = null
  }

  // Lấy tất cả danh mục
  const fetchCategories = async () => {
    try {
      loading.value = true
      categories.value = await categoryAPI.getAllCategories()
    } catch (err) {
      error.value = 'Không thể tải danh sách danh mục'
      console.error(err)
    } finally {
      loading.value = false
    }
  }

  // Lấy danh mục gốc
  const fetchRootCategories = async () => {
    try {
      rootCategories.value = await categoryAPI.getRootCategories()
    } catch (err) {
      error.value = 'Không thể tải danh mục gốc'
      console.error(err)
    }
  }

  // Tạo/cập nhật danh mục
  const saveCategory = async () => {
    try {
      loading.value = true

      // Upload image nếu có file mới
      let imageUrl = categoryForm.value.categoryImage
      if (categoryForm.value.categoryImageFile) {
        console.log('Uploading image file:', categoryForm.value.categoryImageFile)
        imageUrl = await uploadImage(categoryForm.value.categoryImageFile)
        console.log('Image uploaded successfully:', imageUrl)
      }

      const categoryData = {
        name: categoryForm.value.name,
        categoryImage: imageUrl,
        parentId: categoryForm.value.parentId,
      }

      console.log('Saving category data:', categoryData)

      if (isEditMode.value) {
        await categoryAPI.updateCategory(categoryForm.value.id, categoryData)
        console.log('Category updated successfully')
      } else {
        await categoryAPI.createCategory(categoryData)
        console.log('Category created successfully')
      }

      await fetchCategories()
      await fetchRootCategories()
      resetForm()
      return true
    } catch (err) {
      error.value = isEditMode.value ? 'Không thể cập nhật danh mục' : 'Không thể tạo danh mục mới'
      console.error('Error saving category:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  // Xóa danh mục
  const deleteCategory = async (id) => {
    try {
      loading.value = true
      await categoryAPI.deleteCategory(id)
      await fetchCategories()
      await fetchRootCategories()
      return true
    } catch (err) {
      error.value = 'Không thể xóa danh mục'
      console.error(err)
      return false
    } finally {
      loading.value = false
    }
  }

  // Chuẩn bị form cho chỉnh sửa
  const editCategory = (category) => {
    categoryForm.value = {
      id: category.id,
      name: category.name,
      categoryImage: category.categoryImage,
      categoryImageFile: null,
      parentId: category.parentId,
    }
  }

  // Build tree structure
  const buildCategoryTree = (categories, parentId = null) => {
    return categories
      .filter((cat) => cat.parentId === parentId)
      .map((cat) => ({
        ...cat,
        children: buildCategoryTree(categories, cat.id),
      }))
  }

  const categoryTree = computed(() => buildCategoryTree(categories.value))

  return {
    categories,
    rootCategories,
    categoryTree,
    loading,
    error,
    categoryForm,
    isEditMode,
    resetForm,
    fetchCategories,
    fetchRootCategories,
    saveCategory,
    deleteCategory,
    editCategory,
  }
}
