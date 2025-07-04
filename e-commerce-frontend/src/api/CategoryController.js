import axios from 'axios'

const API_BASE_URL = '/api/categories'

export const categoryAPI = {
  // Lấy tất cả danh mục
  getAllCategories: async () => {
    const response = await axios.get(API_BASE_URL)
    return response.data
  },

  // Lấy danh mục gốc
  getRootCategories: async () => {
    const response = await axios.get(`${API_BASE_URL}/root`)
    return response.data
  },

  // Lấy danh mục theo ID
  getCategoryById: async (id) => {
    const response = await axios.get(`${API_BASE_URL}/${id}`)
    return response.data
  },

  // Tạo danh mục mới
  createCategory: async (categoryData) => {
    const response = await axios.post(API_BASE_URL, categoryData)
    return response.data
  },

  // Cập nhật danh mục
  updateCategory: async (id, categoryData) => {
    const response = await axios.put(`${API_BASE_URL}/${id}`, categoryData)
    return response.data
  },

  // Xóa danh mục
  deleteCategory: async (id) => {
    await axios.delete(`${API_BASE_URL}/${id}`)
  },

  // Lấy danh mục con theo parent ID
  getCategoriesByParent: async (parentId) => {
    const response = await axios.get(`${API_BASE_URL}/parent/${parentId}`)
    return response.data
  },
}
