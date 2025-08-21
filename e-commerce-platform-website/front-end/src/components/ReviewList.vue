<template>
  <div class="space-y-4">
    <!-- Rating Summary -->
    <div class="bg-gray-50 rounded-lg p-4 mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-4">
          <div class="flex items-center">
            <div class="flex items-center mr-3">
              <Star
                v-for="i in 5"
                :key="i"
                :class="i <= (averageRating || 0) ? 'text-yellow-400 fill-current' : 'text-gray-300'"
                class="w-6 h-6"
              />
            </div>
            <div>
              <div class="text-2xl font-bold text-gray-800">
                {{ averageRating?.toFixed(1) || '0.0' }}
              </div>
              <div class="text-sm text-gray-500">trên 5 sao</div>
            </div>
          </div>
          <div class="text-sm text-gray-600">
            <div class="font-medium">{{ reviews.length }} đánh giá</div>
            <div class="text-gray-500">Dựa trên đánh giá thực tế</div>
          </div>
        </div>

                <!-- Rating Distribution -->
        <div v-if="reviews.length > 0" class="hidden md:block">
          <div class="text-sm text-gray-600 mb-2">Phân bố đánh giá:</div>
          <div class="space-y-1">
            <div v-for="rating in 5" :key="rating" class="flex items-center space-x-2">
              <span class="text-xs w-4">{{ 6 - rating }}★</span>
              <div class="w-20 bg-gray-200 rounded-full h-2">
                <div
                  class="bg-yellow-400 h-2 rounded-full transition-all duration-300"
                  :style="{ width: getRatingPercentage(6 - rating) + '%' }"
                ></div>
              </div>
              <span class="text-xs text-gray-500 w-8">{{ getRatingCount(6 - rating) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>



    <!-- Reviews List -->
    <div v-if="reviews.length > 0" class="space-y-4">
      <div v-for="review in reviews" :key="review.id" class="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-sm transition-shadow">
        <div class="flex items-start space-x-3">
          <!-- User Avatar -->
          <div class="flex-shrink-0">
            <img
              :src="review.accountImage || '/placeholder.png'"
              :alt="review.accountName"
              class="w-12 h-12 rounded-full object-cover border-2 border-gray-100"
              @error="$event.target.src = '/placeholder.png'"
            />
          </div>

          <!-- Review Content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center space-x-2">
                <span class="font-semibold text-gray-900">{{ review.accountName }}</span>
                <div class="flex items-center bg-yellow-50 px-2 py-1 rounded-full">
                  <Star
                    v-for="i in 5"
                    :key="i"
                    :class="i <= review.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'"
                    class="w-3 h-3"
                  />
                  <span class="ml-1 text-xs font-medium text-gray-700">{{ review.rating }}/5</span>
                </div>
              </div>
              <div class="text-xs text-gray-500">
                {{ formatDate(review.reviewDate) }}
              </div>
            </div>

            <!-- Variant Info -->
            <div v-if="review.productVariantName" class="text-sm text-blue-600 bg-blue-50 px-2 py-1 rounded-md inline-block mb-2">
              <span class="font-medium">{{ review.productVariantName }}:</span> {{ review.productVariantValue }}
            </div>

                        <!-- Comment -->
            <p class="text-gray-700 leading-relaxed">{{ review.comment }}</p>

            <!-- Reply Section -->
            <div v-if="review.reply" class="mt-3 pl-4 border-l-2 border-blue-200 bg-blue-50 rounded-r-lg p-3">
              <div class="flex items-start space-x-2">
                <div class="flex-shrink-0">
                  <div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                    <span class="text-blue-600 text-sm font-semibold">S</span>
                  </div>
                </div>
                <div class="flex-1">
                  <div class="flex items-center space-x-2 mb-1">
                    <span class="font-semibold text-blue-800 text-sm">Shop</span>
                    <span class="text-xs text-blue-600 bg-blue-100 px-2 py-1 rounded-full">Chủ shop</span>
                  </div>
                  <p class="text-blue-700 text-sm leading-relaxed">Trả lời: {{ review.reply }}</p>
                </div>
              </div>
            </div>

            <!-- Reply Button (for shop owners) -->
            <div v-if="!review.reply && isShopOwner" class="mt-3">
              <button
                @click="openReplyDialog(review)"
                class="text-sm text-blue-600 hover:text-blue-800 font-medium flex items-center space-x-1"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6"></path>
                </svg>
                <span>Trả lời</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="text-center py-12">
      <div class="text-gray-300 mb-4">
        <Star class="w-16 h-16 mx-auto" />
      </div>
      <h3 class="text-lg font-medium text-gray-700 mb-2">Chưa có đánh giá nào</h3>
      <p class="text-gray-500">Hãy là người đầu tiên đánh giá sản phẩm này!</p>
    </div>
  </div>

  <!-- Reply Dialog -->
  <ReplyDialog
    v-model:show="showReplyDialog"
    :review="selectedReview"
    @reply-submitted="onReplySubmitted"
  />
</template>

<script setup>
import { computed, ref } from 'vue'
import { Star } from 'lucide-vue-next'
import ReplyDialog from './ReplyDialog.vue'

const props = defineProps({
  reviews: {
    type: Array,
    default: () => []
  },
  isShopOwner: {
    type: Boolean,
    default: false
  }
})

const averageRating = computed(() => {
  if (props.reviews.length === 0) return 0

  const totalRating = props.reviews.reduce((sum, review) => sum + review.rating, 0)
  return totalRating / props.reviews.length
})

const getRatingCount = (rating) => {
  return props.reviews.filter(review => review.rating === rating).length
}

const getRatingPercentage = (rating) => {
  if (props.reviews.length === 0) return 0
  const count = getRatingCount(rating)
  return Math.round((count / props.reviews.length) * 100)
}

// Reply dialog state
const showReplyDialog = ref(false)
const selectedReview = ref(null)

const openReplyDialog = (review) => {
  selectedReview.value = review
  showReplyDialog.value = true
}

const emit = defineEmits(['review-updated'])

const onReplySubmitted = (updatedReview) => {
  // Emit event để parent component cập nhật
  emit('review-updated', updatedReview)
}



const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>
