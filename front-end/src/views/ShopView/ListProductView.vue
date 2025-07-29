<template>
  <n-space vertical :size="16">
    <n-card title="Danh sách sản phẩm" size="small">
      <n-spin :show="loading">
        <n-data-table
          v-if="!loading && products.length > 0"
          :columns="columns"
          :data="products"
          :row-key="(row) => row.id"
          :pagination="pagination"
        />

        <!-- Empty State -->
        <n-empty v-else-if="!loading && products.length === 0" description="Chưa có sản phẩm nào">
          <template #icon>
            <n-icon size="48" color="#d1d5db">
              <Package />
            </n-icon>
          </template>
        </n-empty>
      </n-spin>
    </n-card>
  </n-space>
</template>

<script setup>
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../../utils/axios'
import { Package, Edit, Trash2 } from 'lucide-vue-next'
import { NSpace, NCard, NSpin, NDataTable, NEmpty, NIcon, NButton, NImage, useMessage, useDialog } from 'naive-ui'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const products = ref([])
const loading = ref(true)

// Table columns
const columns = [
  {
    title: 'Tên sản phẩm',
    key: 'name',
    render(row) {
      return h('div', { style: { fontWeight: '500' } }, row.name)
    },
  },
  {
    title: 'Thương hiệu',
    key: 'brand',
  },
  {
    title: 'Danh mục',
    key: 'categoryName',
  },
  {
    title: 'Mô tả',
    key: 'description',
    render(row) {
      return h(
        'div',
        {
          style: {
            maxWidth: '200px',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          },
        },
        row.description || 'Không có mô tả',
      )
    },
  },
  {
    title: 'Hình ảnh',
    key: 'productImage',
    width: 100,
    render(row) {
      return h(NImage, {
        width: 60,
        height: 60,
        src: row.productImage,
        fallbackSrc: '/default-product.png',
        objectFit: 'cover',
        style: { borderRadius: '4px' },
      })
    },
  },
  {
    title: 'Thao tác',
    key: 'actions',
    width: 150,
    render(row) {
      return h('div', { style: { display: 'flex', gap: '8px' } }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            onClick: () => editProduct(row.id),
          },
          {
            icon: () => h(NIcon, null, { default: () => h(Edit) }),
            default: () => 'Sửa',
          },
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            onClick: () => confirmDelete(row.id, row.name),
          },
          {
            icon: () => h(NIcon, null, { default: () => h(Trash2) }),
            default: () => 'Xóa',
          },
        ),
      ])
    },
  },
]

// Pagination
const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page) => {
    pagination.page = page
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
  },
})

// Methods
const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/products/user')
    products.value = response.data.products || []
  } catch (error) {
    message.error('Không thể tải danh sách sản phẩm')
    console.error('Error:', error)
  } finally {
    loading.value = false
  }
}

const editProduct = (productId) => {
  router.push(`/user/shop/product/edit/${productId}`)
}

const confirmDelete = (productId, productName) => {
  dialog.warning({
    title: 'Xác nhận xóa',
    content: `Bạn có chắc chắn muốn xóa sản phẩm "${productName}" không?`,
    positiveText: 'Xóa',
    negativeText: 'Hủy',
    onPositiveClick: () => deleteProduct(productId),
  })
}

const deleteProduct = async (productId) => {
  try {
    const response = await axios.delete(`/api/products/${productId}`)
    if (response.data.message) {
      message.success(response.data.message)
    } else {
      message.success('Xóa sản phẩm thành công')
    }
    await fetchProducts()
  } catch (error) {
    console.error('Delete error:', error)
    if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('Không thể xóa sản phẩm')
    }
  }
}

// Load data on mount
onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
table {
  border-collapse: collapse;
}

button {
  color: white;
}
</style>
