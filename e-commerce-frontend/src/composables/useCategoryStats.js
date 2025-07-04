import { computed } from 'vue'

export function useCategoryStats(categories) {
  const categoryStats = computed(() => [
    {
      label: 'Tổng danh mục',
      value: categories.value.length,
      icon: 'fas fa-list',
      gradient: 'from-blue-50 to-blue-100',
      iconBg: 'bg-blue-500',
      labelColor: 'text-blue-600',
      valueColor: 'text-blue-900',
    },
    {
      label: 'Danh mục gốc',
      value: categories.value.filter((c) => !c.parentId).length,
      icon: 'fas fa-sitemap',
      gradient: 'from-green-50 to-green-100',
      iconBg: 'bg-green-500',
      labelColor: 'text-green-600',
      valueColor: 'text-green-900',
    },
    {
      label: 'Danh mục con',
      value: categories.value.filter((c) => c.parentId).length,
      icon: 'fas fa-layer-group',
      gradient: 'from-purple-50 to-purple-100',
      iconBg: 'bg-purple-500',
      labelColor: 'text-purple-600',
      valueColor: 'text-purple-900',
    },
  ])

  return {
    categoryStats,
  }
}
