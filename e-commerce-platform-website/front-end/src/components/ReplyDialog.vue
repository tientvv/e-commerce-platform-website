<template>
  <div v-if="show" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg p-6 w-full max-w-md mx-4">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold text-gray-800">Trả lời đánh giá</h3>
        <button
          @click="$emit('update:show', false)"
          class="text-gray-400 hover:text-gray-600 transition-colors"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>

      <!-- Review Info -->
      <div class="bg-gray-50 rounded-lg p-3 mb-4">
        <div class="flex items-center space-x-2 mb-2">
          <span class="font-medium text-gray-800">{{ review?.accountName }}</span>
          <div class="flex items-center">
            <svg
              v-for="i in 5"
              :key="i"
              :class="i <= review?.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'"
              class="w-4 h-4"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
            </svg>
          </div>
        </div>
        <p class="text-gray-600 text-sm">{{ review?.comment }}</p>
      </div>

      <!-- Reply Form -->
      <form @submit.prevent="handleSubmit">
        <div class="mb-4">
          <label for="reply" class="block text-sm font-medium text-gray-700 mb-2">
            Trả lời của bạn
          </label>
          <textarea
            id="reply"
            v-model="replyText"
            rows="4"
            class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            placeholder="Nhập trả lời của bạn..."
            required
          ></textarea>
        </div>

        <div class="flex space-x-3">
          <button
            type="button"
            @click="$emit('update:show', false)"
            class="flex-1 px-4 py-2 text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 transition-colors"
          >
            Hủy
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ loading ? 'Đang gửi...' : 'Gửi trả lời' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  review: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:show', 'reply-submitted'])

const replyText = ref('')
const loading = ref(false)

const handleSubmit = async () => {
  if (!replyText.value.trim()) return

  try {
    loading.value = true
    const response = await axios.post('/api/reviews/reply', {
      reviewId: props.review.id,
      reply: replyText.value.trim()
    })

    if (response.data.success) {
      emit('reply-submitted', response.data.review)
      emit('update:show', false)
      replyText.value = ''
    }
  } catch (error) {
    console.error('Error submitting reply:', error)
    alert(error.response?.data?.message || 'Có lỗi xảy ra khi gửi trả lời')
  } finally {
    loading.value = false
  }
}
</script>
