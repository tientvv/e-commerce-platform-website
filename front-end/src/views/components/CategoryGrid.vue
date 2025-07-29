<template>
  <div class="category-section">
    <!-- Section Header -->
    <div class="section-header">
      <h2 class="section-title">Danh mục</h2>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="loading-container">
      <div class="loading-grid">
        <div v-for="i in 20" :key="i" class="loading-category-item">
          <div class="loading-icon"></div>
          <div class="loading-name"></div>
        </div>
      </div>
    </div>

    <!-- Category Grid -->
    <div v-else class="category-grid">
      <div
        v-for="category in categories"
        :key="category.id"
        class="category-item"
        @click="navigateToCategory(category.id)"
      >
        <div class="category-icon">
          <img
            v-if="category.categoryImage"
            :src="category.categoryImage"
            :alt="category.name"
            class="category-image"
          />
          <div v-else class="category-placeholder">
            <svg class="placeholder-icon" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"
              />
            </svg>
          </div>
        </div>
        <div class="category-name">{{ category.name }}</div>
      </div>
    </div>

    <!-- Navigation Arrow -->
    <div class="navigation-arrow">
      <button class="arrow-button" @click="scrollToNext">
        <svg class="arrow-icon" fill="currentColor" viewBox="0 0 20 20">
          <path
            fill-rule="evenodd"
            d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
            clip-rule="evenodd"
          />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../../utils/axios'

const router = useRouter()

// State
const isLoading = ref(true)
const categories = ref([])

// Methods
const loadCategories = async () => {
  isLoading.value = true
  try {
    const response = await axios.get('/api/categories')
    if (response.data) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('Error loading categories:', error)
    // Fallback data for demo - matching the image categories
    categories.value = [
      {
        id: 1,
        name: 'Thời Trang Nam',
        categoryImage: null,
      },
      {
        id: 2,
        name: 'Điện Thoại & Phụ Kiện',
        categoryImage: null,
      },
      {
        id: 3,
        name: 'Thiết Bị Điện Tử',
        categoryImage: null,
      },
      {
        id: 4,
        name: 'Máy Tính & Laptop',
        categoryImage: null,
      },
      {
        id: 5,
        name: 'Máy Ảnh & Máy Quay Phim',
        categoryImage: null,
      },
      {
        id: 6,
        name: 'Đồng Hồ',
        categoryImage: null,
      },
      {
        id: 7,
        name: 'Giày Dép Nam',
        categoryImage: null,
      },
      {
        id: 8,
        name: 'Thiết Bị Điện Gia Dụng',
        categoryImage: null,
      },
      {
        id: 9,
        name: 'Thể Thao & Du Lịch',
        categoryImage: null,
      },
      {
        id: 10,
        name: 'Ô Tô & Xe Máy & Xe Đạp',
        categoryImage: null,
      },
      {
        id: 11,
        name: 'Thời Trang Nữ',
        categoryImage: null,
      },
      {
        id: 12,
        name: 'Mẹ & Bé',
        categoryImage: null,
      },
      {
        id: 13,
        name: 'Nhà Cửa & Đời Sống',
        categoryImage: null,
      },
      {
        id: 14,
        name: 'Sắc Đẹp',
        categoryImage: null,
      },
      {
        id: 15,
        name: 'Sức Khỏe',
        categoryImage: null,
      },
      {
        id: 16,
        name: 'Giày Dép Nữ',
        categoryImage: null,
      },
      {
        id: 17,
        name: 'Túi Ví Nữ',
        categoryImage: null,
      },
      {
        id: 18,
        name: 'Phụ Kiện & Trang Sức Nữ',
        categoryImage: null,
      },
      {
        id: 19,
        name: 'Bách Hóa Online',
        categoryImage: null,
      },
      {
        id: 20,
        name: 'Nhà Sách Online',
        categoryImage: null,
      },
    ]
  } finally {
    isLoading.value = false
  }
}

const navigateToCategory = (categoryId) => {
  router.push(`/category/${categoryId}`)
}

const scrollToNext = () => {
  // Scroll to next set of categories
  const container = document.querySelector('.category-grid')
  if (container) {
    container.scrollBy({ left: 800, behavior: 'smooth' })
  }
}

// Load data on mount
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-section {
  width: 100%;
  position: relative;
  background: #ffffff;
  border-radius: 0.375rem;
  padding: 20px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-weight: 600;
  color: #374151;
  margin: 0;
}

/* Loading State */
.loading-container {
  width: 100%;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 16px;
  max-width: 100%;
  overflow-x: auto;
}

.loading-category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.loading-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #e5e7eb;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.loading-name {
  height: 12px;
  width: 60px;
  background: #e5e7eb;
  border-radius: 4px;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* Category Grid */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 16px;
  max-width: 100%;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.category-grid::-webkit-scrollbar {
  display: none;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 8px;
  border-radius: 8px;
  min-width: 80px;
}

.category-item:hover {
  background: #f3f4f6;
  transform: translateY(-2px);
}

.category-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.2s ease;
}

.category-item:hover .category-icon {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
}

.category-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.category-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
}

.placeholder-icon {
  width: 24px;
  height: 24px;
  color: #ffffff;
  opacity: 0.8;
}

.category-name {
  font-size: 0.75rem;
  font-weight: 500;
  color: #374151;
  text-align: center;
  line-height: 1.2;
  max-width: 80px;
  word-wrap: break-word;
}

/* Navigation Arrow */
.navigation-arrow {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
}

.arrow-button {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.arrow-button:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.arrow-icon {
  width: 16px;
  height: 16px;
  color: #6b7280;
}

/* Responsive Design */
@media (max-width: 768px) {
  .category-section {
    padding: 16px;
  }

  .section-title {
    font-size: 1.25rem;
  }

  .category-grid {
    grid-template-columns: repeat(auto-fit, minmax(70px, 1fr));
    gap: 12px;
  }

  .category-item {
    min-width: 70px;
    padding: 6px;
  }

  .category-icon {
    width: 50px;
    height: 50px;
  }

  .category-name {
    font-size: 0.7rem;
    max-width: 70px;
  }

  .navigation-arrow {
    right: 4px;
  }

  .arrow-button {
    width: 28px;
    height: 28px;
  }

  .arrow-icon {
    width: 14px;
    height: 14px;
  }
}

@media (max-width: 480px) {
  .category-grid {
    grid-template-columns: repeat(auto-fit, minmax(60px, 1fr));
    gap: 8px;
  }

  .category-item {
    min-width: 60px;
    padding: 4px;
  }

  .category-icon {
    width: 45px;
    height: 45px;
  }

  .category-name {
    font-size: 0.65rem;
    max-width: 60px;
  }

  .placeholder-icon {
    width: 20px;
    height: 20px;
  }
}

/* Tablet Layout */
@media (min-width: 769px) and (max-width: 1024px) {
  .category-grid {
    grid-template-columns: repeat(auto-fit, minmax(85px, 1fr));
    gap: 18px;
  }

  .category-item {
    min-width: 85px;
  }

  .category-icon {
    width: 65px;
    height: 65px;
  }

  .category-name {
    font-size: 0.8rem;
    max-width: 85px;
  }
}

/* Desktop Improvements */
@media (min-width: 1025px) {
  .category-section {
    padding: 24px;
  }

  .section-title {
    font-size: 16px;
  }

  .category-grid {
    grid-template-columns: repeat(auto-fit, minmax(90px, 1fr));
    gap: 20px;
  }

  .category-item {
    min-width: 90px;
    padding: 10px;
  }

  .category-icon {
    width: 70px;
    height: 70px;
  }

  .category-name {
    font-size: 0.8rem;
    max-width: 90px;
  }

  .placeholder-icon {
    width: 28px;
    height: 28px;
  }
}

/* Smooth animations */
.category-item {
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
