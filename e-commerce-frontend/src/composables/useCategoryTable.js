export function useCategoryTable() {
  const tableColumns = [
    { key: 'image', label: 'Hình ảnh', align: 'left' },
    { key: 'name', label: 'Tên danh mục', align: 'left' },
    { key: 'parent', label: 'Danh mục cha', align: 'left' },
    { key: 'createdAt', label: 'Ngày tạo', align: 'left' },
    { key: 'actions', label: 'Thao tác', align: 'center' },
  ]

  const actionButtons = [
    {
      key: 'edit',
      label: 'Sửa',
      icon: 'fas fa-edit',
      variant: 'success',
    },
    {
      key: 'delete',
      label: 'Xóa',
      icon: 'fas fa-trash',
      variant: 'danger',
    },
  ]

  const filterOptions = [
    { value: 'all', label: 'Tất cả danh mục' },
    { value: 'root', label: 'Chỉ danh mục gốc' },
    { value: 'children', label: 'Chỉ danh mục con' },
  ]

  const formatDate = (dateString) => {
    if (!dateString) return ''
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    })
  }

  return {
    tableColumns,
    actionButtons,
    filterOptions,
    formatDate,
  }
}
