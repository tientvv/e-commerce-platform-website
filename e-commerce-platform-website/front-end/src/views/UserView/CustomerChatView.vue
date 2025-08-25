<template>
  <div class="max-w-6xl mx-auto px-4">


    <!-- Main Content -->
    <div class="bg-white rounded-lg shadow-lg overflow-hidden">
      <div class="flex h-[600px]">
        <!-- Chat List -->
        <div class="w-1/3 border-r border-gray-200 flex flex-col bg-gray-50">
          <!-- Header -->
          <div class="p-4 border-b border-gray-200 bg-white">
            <div class="flex items-center justify-between">
              <h3 class="font-semibold text-gray-800">Cuộc trò chuyện</h3>
              <span v-if="unreadCount > 0" class="bg-red-500 text-white text-xs rounded-full px-2 py-1">
                {{ unreadCount }}
              </span>
            </div>
          </div>

          <!-- Chat List -->
          <div class="flex-1 overflow-y-auto">
            <div v-if="loading" class="flex justify-center p-4">
              <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
            </div>

            <div v-else-if="chatList.length === 0" class="text-center p-8 text-gray-500">
              <MessageCircle class="w-12 h-12 mx-auto mb-2 text-gray-300" />
              <p class="text-sm">Chưa có cuộc trò chuyện nào</p>
              <p class="text-xs text-gray-400 mt-1">Hãy chat với shop để bắt đầu!</p>
            </div>

            <div v-else class="space-y-0">
              <div
                v-for="chat in chatList"
                :key="`${chat.productId}-${chat.shopId}`"
                @click="selectChat(chat)"
                class="p-4 hover:bg-white cursor-pointer transition-colors border-b border-gray-100"
                :class="selectedChat?.productId === chat.productId && selectedChat?.shopId === chat.shopId ? 'bg-white border-r-2 border-blue-600' : ''"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1 min-w-0">
                    <h4 class="font-medium text-gray-900 truncate text-sm">{{ chat.shopName }}</h4>
                    <p class="text-xs text-gray-500 truncate mt-1">{{ chat.productName }}</p>
                    <p class="text-xs text-gray-600 mt-2 line-clamp-2">{{ chat.lastMessage }}</p>
                  </div>
                  <div class="text-right ml-2">
                    <p class="text-xs text-gray-400">{{ formatTime(chat.lastMessageTime) }}</p>
                    <div v-if="!chat.isRead" class="w-2 h-2 bg-red-500 rounded-full mt-2 ml-auto"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Chat Messages -->
        <div class="flex-1 flex flex-col bg-white">
          <!-- Chat Header -->
          <div v-if="selectedChat" class="p-4 border-b border-gray-200 bg-gradient-to-r from-blue-600 to-purple-600">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-semibold text-white">{{ selectedChat.shopName }}</h3>
                <p class="text-sm text-blue-100">{{ selectedChat.productName }}</p>
              </div>
              <button
                @click="deleteConversation"
                class="text-white hover:text-red-200 transition-colors"
                title="Xóa cuộc trò chuyện"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Messages -->
          <div class="flex-1 overflow-y-auto p-4 space-y-3" ref="messagesContainer">
            <div v-if="messagesLoading" class="flex justify-center">
              <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
            </div>

            <div v-else-if="messages.length === 0" class="text-center text-gray-500 py-8">
              <MessageCircle class="w-12 h-12 mx-auto mb-2 text-gray-300" />
              <p class="text-sm">Chưa có tin nhắn nào</p>
              <p class="text-xs text-gray-400 mt-1">Hãy bắt đầu cuộc trò chuyện!</p>
            </div>

            <div
              v-for="message in messages"
              :key="message.id"
              class="flex"
              :class="message.messageType === 'CUSTOMER_MESSAGE' ? 'justify-end' : 'justify-start'"
            >
              <div
                class="max-w-xs px-4 py-3 rounded-lg shadow-sm"
                :class="
                  message.messageType === 'CUSTOMER_MESSAGE'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-800'
                "
              >
                <p class="text-sm leading-relaxed">{{ message.content }}</p>
                <p class="text-xs mt-2 opacity-70">
                  {{ formatTime(message.createdAt) }}
                </p>
              </div>
            </div>
          </div>

          <!-- Input -->
          <div v-if="selectedChat" class="p-4 border-t border-gray-200 bg-gray-50">
            <div class="flex space-x-3">
              <input
                v-model="newMessage"
                @keyup.enter="sendMessage"
                type="text"
                placeholder="Nhập tin nhắn..."
                class="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                :disabled="sending"
              />
              <button
                @click="sendMessage"
                :disabled="!newMessage.trim() || sending"
                class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <Send v-if="!sending" class="w-4 h-4" />
                <div v-else class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
              </button>
            </div>
          </div>

          <!-- No Chat Selected -->
          <div v-else class="flex-1 flex items-center justify-center bg-gray-50">
            <div class="text-center text-gray-500">
              <MessageCircle class="w-16 h-16 mx-auto mb-4 text-gray-300" />
              <p class="text-lg font-medium text-gray-600 mb-2">Chọn một cuộc trò chuyện</p>
              <p class="text-sm text-gray-400">Chọn cuộc trò chuyện từ danh sách bên trái để bắt đầu chat</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { MessageCircle, Send } from 'lucide-vue-next'
import axios from '~/utils/axios'
import { useUserInfo } from '~/composables/useUserInfo'
import { useDialog } from 'naive-ui'

const { userInfo, fetchUserInfo } = useUserInfo()
const dialog = useDialog()

const loading = ref(false)
const messagesLoading = ref(false)
const sending = ref(false)
const chatList = ref([])
const messages = ref([])
const selectedChat = ref(null)
const newMessage = ref('')
const unreadCount = ref(0)
const messagesContainer = ref(null)

const loadChatList = async () => {
  console.log('Loading chat list for user:', userInfo.value?.id)

  if (!userInfo.value?.id) {
    console.log('No user ID, cannot load chat list')
    return
  }

  loading.value = true
  try {
    const response = await axios.get(`/api/chat/user/${userInfo.value.id}`)
    console.log('Chat list response:', response.data)
    chatList.value = response.data
    loadUnreadCount()
  } catch (error) {
    console.error('Lỗi khi tải danh sách chat:', error)
    console.error('Error details:', error.response?.data)
  } finally {
    loading.value = false
  }
}

const loadMessages = async (productId, shopId, isRefresh = false) => {
  if (!productId || !shopId) return

  if (!isRefresh) {
    messagesLoading.value = true
  }

  try {
    const response = await axios.get(`/api/chat/product/${productId}/shop/${shopId}/user/${userInfo.value.id}`)

    if (isRefresh) {
      const newMessages = response.data.filter(newMsg =>
        !messages.value.some(existingMsg => existingMsg.id === newMsg.id)
      )
      if (newMessages.length > 0) {
        messages.value.push(...newMessages)
        await nextTick()
        scrollToBottom()
      }
    } else {
      messages.value = response.data
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('Lỗi khi tải tin nhắn:', error)
  } finally {
    if (!isRefresh) {
      messagesLoading.value = false
    }
  }
}

const selectChat = (chat) => {
  selectedChat.value = chat
  loadMessages(chat.productId, chat.shopId)
}

const sendMessage = async () => {
  console.log('Sending message:', {
    message: newMessage.value,
    selectedChat: selectedChat.value,
    userId: userInfo.value?.id
  })

  if (!newMessage.value.trim() || !selectedChat.value || sending.value) {
    console.log('Cannot send message - validation failed')
    return
  }

  sending.value = true
  try {
    const requestData = {
      productId: selectedChat.value.productId,
      shopId: selectedChat.value.shopId,
      content: newMessage.value.trim(),
      messageType: 'CUSTOMER_MESSAGE'
    }
    console.log('Sending request:', requestData)

    const response = await axios.post(`/api/chat?userId=${userInfo.value.id}`, requestData)
    console.log('Send message response:', response.data)

    messages.value.push(response.data)
    newMessage.value = ''

    // Cập nhật chat list
    const updatedChat = chatList.value.find(chat =>
      chat.productId === selectedChat.value.productId &&
      chat.shopId === selectedChat.value.shopId
    )
    if (updatedChat) {
      updatedChat.lastMessage = response.data.content
      updatedChat.lastMessageTime = response.data.createdAt
      chatList.value.sort((a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime))
    }

    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('Lỗi khi gửi tin nhắn:', error)
    console.error('Error details:', error.response?.data)
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
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadUnreadCount = async () => {
  if (!userInfo.value?.id) return

  try {
    const response = await axios.get(`/api/chat/unread/user/${userInfo.value.id}`)
    unreadCount.value = response.data
  } catch (error) {
    console.error('Lỗi khi tải số tin nhắn chưa đọc:', error)
  }
}

const deleteConversation = async () => {
  if (!selectedChat.value) return

  dialog.warning({
    title: 'Xác nhận ẩn cuộc trò chuyện',
    content: `Bạn có chắc muốn ẩn cuộc trò chuyện với ${selectedChat.value.shopName}?`,
    positiveText: 'Xác nhận',
    negativeText: 'Hủy',
    onPositiveClick: async () => {
      try {
        await axios.delete(`/api/chat/conversation/user/${selectedChat.value.productId}/${selectedChat.value.shopId}/${userInfo.value.id}`)

        // Xóa cuộc trò chuyện khỏi danh sách
        chatList.value = chatList.value.filter(chat =>
          !(chat.productId === selectedChat.value.productId && chat.shopId === selectedChat.value.shopId)
        )

        // Reset selected chat và messages
        selectedChat.value = null
        messages.value = []

        // Refresh unread count
        loadUnreadCount()
      } catch (error) {
        console.error('Lỗi khi ẩn cuộc trò chuyện:', error)
      }
    }
  })
}

onMounted(async () => {
  console.log('CustomerChatView mounted, userInfo:', userInfo.value)

  if (!userInfo.value) {
    console.log('No userInfo, fetching...')
    await fetchUserInfo()
    console.log('After fetchUserInfo, userInfo:', userInfo.value)
  }

  loadChatList()

  // Auto refresh mỗi 5 giây
  setInterval(async () => {
    if (userInfo.value?.id) {
      await loadChatList()
      if (selectedChat.value) {
        await loadMessages(selectedChat.value.productId, selectedChat.value.shopId, true)
      }
    }
  }, 5000)
})

watch(selectedChat, (newChat) => {
  if (newChat) {
    loadMessages(newChat.productId, newChat.shopId)
  }
})
</script>
