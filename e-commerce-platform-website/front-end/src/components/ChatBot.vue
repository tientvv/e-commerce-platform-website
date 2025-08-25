<template>
  <div class="fixed bottom-4 right-4 z-50">
    <!-- Chat Button -->
    <button
      v-if="!isOpen"
      @click="openChat"
      class="bg-blue-600 text-white rounded-full p-4 shadow-lg hover:bg-blue-700 transition-colors relative"
    >
      <MessageCircle class="w-6 h-6" />
      <span
        v-if="userInfo?.id && unreadCount > 0"
        class="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-6 w-6 flex items-center justify-center"
      >
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </button>

        <!-- Chat Window -->
    <div
      v-if="isOpen"
      class="bg-white rounded-lg shadow-xl border border-gray-200 w-80 h-96 flex flex-col"
    >
      <!-- Header -->
      <div class="bg-blue-600 text-white p-4 rounded-t-lg flex items-center justify-between">
        <div class="flex items-center space-x-2">
          <MessageCircle class="w-5 h-5" />
          <span class="font-semibold">Chat với {{ shopName }}</span>
        </div>
        <button @click="closeChat" class="text-white hover:text-gray-200">
          <X class="w-5 h-5" />
        </button>
      </div>

      <!-- Login Required Message -->
      <div v-if="!userInfo?.id" class="flex-1 flex items-center justify-center p-4">
        <div class="text-center text-gray-500">
          <MessageCircle class="w-12 h-12 mx-auto mb-2 text-gray-300" />
          <p class="font-medium mb-2">Cần đăng nhập để chat</p>
          <p class="text-sm mb-4">Vui lòng đăng nhập để có thể gửi tin nhắn</p>
          <RouterLink
            to="/login"
            class="inline-block px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            Đăng nhập
          </RouterLink>
        </div>
      </div>

      <!-- Messages (only show if logged in) -->
      <div v-else class="flex-1 overflow-y-auto p-4 space-y-3" ref="messagesContainer">
        <div v-if="loading" class="flex justify-center">
          <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
        </div>

        <div v-else-if="messages.length === 0" class="text-center text-gray-500 py-8">
          <MessageCircle class="w-12 h-12 mx-auto mb-2 text-gray-300" />
          <p>Chưa có tin nhắn nào</p>
          <p class="text-sm">Hãy bắt đầu cuộc trò chuyện!</p>
        </div>

        <div
          v-for="message in messages"
          :key="message.id"
          class="flex"
          :class="message.messageType === 'CUSTOMER_MESSAGE' ? 'justify-end' : 'justify-start'"
        >
          <div
            class="max-w-xs px-3 py-2 rounded-lg"
            :class="
              message.messageType === 'CUSTOMER_MESSAGE'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-800'
            "
          >
            <p class="text-sm">{{ message.content }}</p>
            <p class="text-xs mt-1 opacity-70">
              {{ formatTime(message.createdAt) }}
            </p>
          </div>
        </div>
      </div>

      <!-- Input (only show if logged in) -->
      <div v-if="userInfo?.id" class="p-4 border-t border-gray-200">
        <div class="flex space-x-2">
          <input
            v-model="newMessage"
            @keyup.enter="sendMessage"
            type="text"
            placeholder="Nhập tin nhắn..."
            class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            :disabled="sending"
          />
          <button
            @click="sendMessage"
            :disabled="!newMessage.trim() || sending"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Send v-if="!sending" class="w-4 h-4" />
            <div v-else class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { MessageCircle, X, Send } from 'lucide-vue-next'
import axios from '~/utils/axios'
import { useUserInfo } from '~/composables/useUserInfo'

const props = defineProps({
  productId: {
    type: String,
    required: true
  },
  shopId: {
    type: String,
    required: true
  },
  shopName: {
    type: String,
    required: true
  }
})

const { userInfo, fetchUserInfo } = useUserInfo()

const isOpen = ref(false)
const messages = ref([])
const newMessage = ref('')
const loading = ref(false)
const sending = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref(null)

const openChat = () => {
  isOpen.value = true
  loadMessages()
}

const closeChat = () => {
  isOpen.value = false
}

const loadMessages = async (isRefresh = false) => {
  if (!userInfo.value?.id) {
    messages.value = []
    loading.value = false
    return
  }

  if (!isRefresh) {
    loading.value = true
  }

  try {
    const response = await axios.get(`/api/chat/product/${props.productId}/shop/${props.shopId}/user/${userInfo.value.id}`)

    if (isRefresh) {
      // Chỉ thêm tin nhắn mới
      const newMessages = response.data.filter(newMsg =>
        !messages.value.some(existingMsg => existingMsg.id === newMsg.id)
      )
      if (newMessages.length > 0) {
        messages.value.push(...newMessages)
        await nextTick()
        scrollToBottom()
      }
    } else {
      // Thay thế toàn bộ tin nhắn
      messages.value = response.data
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('Lỗi khi tải tin nhắn:', error)
    if (!isRefresh) {
      messages.value = []
    }
  } finally {
    if (!isRefresh) {
      loading.value = false
    }
  }
}

const sendMessage = async () => {
  console.log('Send message called:', {
    message: newMessage.value,
    userInfo: userInfo.value,
    sending: sending.value
  })

  if (!newMessage.value.trim() || !userInfo.value?.id || sending.value) {
    console.log('Cannot send message - validation failed')
    return
  }

  sending.value = true
  try {
    const messageData = {
      productId: props.productId,
      shopId: props.shopId,
      messageType: 'CUSTOMER_MESSAGE',
      content: newMessage.value.trim()
    }

    console.log('Sending message data:', messageData)
    const response = await axios.post(`/api/chat?userId=${userInfo.value.id}`, messageData)
    console.log('Message sent successfully:', response.data)

    messages.value.push(response.data)
    newMessage.value = ''
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('Lỗi khi gửi tin nhắn:', error)
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadUnreadCount = async () => {
  if (!userInfo.value?.id) {
    unreadCount.value = 0
    return
  }

  try {
    const response = await axios.get(`/api/chat/unread/user/${userInfo.value.id}`)
    unreadCount.value = response.data
  } catch (error) {
    console.error('Lỗi khi tải số tin nhắn chưa đọc:', error)
    unreadCount.value = 0
  }
}

watch(isOpen, (newValue) => {
  if (newValue) {
    loadUnreadCount()
  }
})

watch(userInfo, (newValue) => {
  if (newValue?.id && isOpen.value) {
    loadMessages()
  }
  loadUnreadCount()
}, { immediate: true })

onMounted(async () => {
  if (!userInfo.value) {
    await fetchUserInfo()
  }
  loadUnreadCount()

  // Auto refresh unread count
  setInterval(loadUnreadCount, 30000)

  // Auto refresh messages when chat is open
  setInterval(() => {
    if (isOpen.value && userInfo.value?.id) {
      loadMessages(true)
    }
  }, 5000)
})
</script>

<style scoped>
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
