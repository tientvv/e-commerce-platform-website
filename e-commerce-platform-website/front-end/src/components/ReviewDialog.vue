<template>
  <n-modal v-model:show="showDialog" preset="card" style="width: 500px" title="Đánh giá sản phẩm">
    <div class="space-y-4">
      <!-- Product Info -->
      <div class="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
        <img
          :src="productImage || '/placeholder.png'"
          :alt="productName"
          class="w-16 h-16 object-cover rounded"
          @error="$event.target.src = '/placeholder.png'"
        />
        <div>
          <h3 class="font-medium text-gray-900">{{ productName }}</h3>
          <p v-if="variantName" class="text-sm text-gray-600">{{ variantName }}: {{ variantValue }}</p>
        </div>
      </div>

      <!-- Rating Stars -->
      <div class="space-y-2">
        <label class="block text-sm font-medium text-gray-700">Đánh giá của bạn</label>
        <div class="flex items-center space-x-1">
          <button
            v-for="star in 5"
            :key="star"
            @click="rating = star"
            @mouseenter="hoverRating = star"
            @mouseleave="hoverRating = 0"
            class="focus:outline-none"
          >
            <Star
              :class="[
                'w-8 h-8 transition-colors',
                (hoverRating >= star || rating >= star)
                  ? 'text-yellow-400 fill-current'
                  : 'text-gray-300'
              ]"
            />
          </button>
        </div>
        <p class="text-sm text-gray-500">
          {{ getRatingText(rating) }}
        </p>
      </div>

      <!-- Comment -->
      <div class="space-y-2">
        <label class="block text-sm font-medium text-gray-700">Nhận xét</label>
        <n-input
          v-model:value="comment"
          type="textarea"
          placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm này..."
          :rows="4"
          maxlength="500"
          show-count
        />
      </div>

      <!-- Error Message -->
      <div v-if="errorMessage" class="text-red-600 text-sm">
        {{ errorMessage }}
      </div>

      <!-- Actions -->
      <div class="flex justify-end space-x-3 pt-4">
        <n-button @click="closeDialog" :disabled="loading">
          Hủy
        </n-button>
        <n-button
          @click="submitReview"
          type="primary"
          :loading="loading"
          :disabled="!rating || !comment.trim()"
        >
          Gửi đánh giá
        </n-button>
      </div>
    </div>
  </n-modal>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Star } from 'lucide-vue-next'
import { useMessage } from 'naive-ui'
import axios from '../utils/axios'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  productId: {
    type: String,
    required: false,
    default: ''
  },
  productName: {
    type: String,
    required: false,
    default: ''
  },
  productImage: {
    type: String,
    default: ''
  },
  variantName: {
    type: String,
    default: ''
  },
  variantValue: {
    type: String,
    default: ''
  },
  productVariantId: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['update:show', 'review-submitted'])

const message = useMessage()

const showDialog = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
})

const rating = ref(0)
const hoverRating = ref(0)
const comment = ref('')
const loading = ref(false)
const errorMessage = ref('')

const getRatingText = (rating) => {
  const texts = {
    0: 'Chọn đánh giá',
    1: 'Rất không hài lòng',
    2: 'Không hài lòng',
    3: 'Bình thường',
    4: 'Hài lòng',
    5: 'Rất hài lòng'
  }
  return texts[rating] || 'Chọn đánh giá'
}

const closeDialog = () => {
  emit('update:show', false)
  resetForm()
}

const resetForm = () => {
  rating.value = 0
  hoverRating.value = 0
  comment.value = ''
  errorMessage.value = ''
}

const submitReview = async () => {
  if (!props.productId) {
    errorMessage.value = 'Không tìm thấy thông tin sản phẩm'
    return
  }

  if (!rating.value || !comment.value.trim()) {
    errorMessage.value = 'Vui lòng chọn đánh giá và nhập nhận xét'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const reviewData = {
      productId: props.productId,
      productVariantId: props.productVariantId || null,
      rating: parseFloat(rating.value),
      comment: comment.value.trim()
    }

    const response = await axios.post('/api/reviews', reviewData)

    if (response.data.success) {
      message.success('Đánh giá thành công!')
      emit('review-submitted', response.data.review)
      closeDialog()
    } else {
      errorMessage.value = response.data.message || 'Có lỗi xảy ra'
    }
  } catch (error) {
    console.error('Error submitting review:', error)
    errorMessage.value = error.response?.data?.message || 'Có lỗi xảy ra khi gửi đánh giá'
  } finally {
    loading.value = false
  }
}

watch(() => props.show, (newVal) => {
  if (!newVal) {
    resetForm()
  }
})
</script>
