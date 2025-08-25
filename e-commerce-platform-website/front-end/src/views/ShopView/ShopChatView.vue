<template>
  <div class="flex flex-col h-full overflow-hidden">
    <!-- Header -->
    <div class="border-b border-gray-200 p-4">
      <h1 class="text-xl font-bold text-gray-800">Quản lý tin nhắn</h1>
      <p class="text-gray-600 mt-1">Trả lời tin nhắn từ khách hàng</p>
    </div>

    <!-- Content -->
    <div class="flex flex-1 min-h-0">
      <!-- Left Sidebar - Chat List -->
      <div class="w-1/3 border-r border-gray-200 flex flex-col">
        <div class="p-3 border-b border-gray-200">
          <div class="flex items-center justify-between">
            <h3 class="font-semibold text-gray-800">Tin nhắn</h3>
            <div class="flex items-center space-x-2">
              <span v-if="unreadCount > 0" class="bg-red-500 text-white text-xs rounded-full px-2 py-1">
                {{ unreadCount }}
              </span>
            </div>
          </div>
        </div>

        <div class="flex-1 overflow-y-auto">
          <div v-if="loading" class="flex justify-center p-4">
            <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          </div>

                        <div v-else-if="chatList.length === 0" class="text-center p-8 text-gray-500">
                <MessageCircle class="w-12 h-12 mx-auto mb-2 text-gray-300" />
                <p>Chưa có tin nhắn nào</p>
              </div>

          <div v-else class="space-y-1">
            <div
              v-for="chat in chatList"
              :key="`${chat.productId}-${chat.userId}`"
              @click="selectChat(chat)"
              class="p-4 hover:bg-gray-50 cursor-pointer border-b border-gray-100"
              :class="{ 'bg-blue-50 border-blue-200': selectedChat?.productId === chat.productId && selectedChat?.userId === chat.userId }"
            >
              <div class="flex items-center justify-between">
                <div class="flex-1 min-w-0">
                  <div class="flex items-center space-x-2">
                    <span class="font-medium text-gray-800 truncate">
                      {{ chat.userName }}
                    </span>
                    <span v-if="!chat.isRead" class="w-2 h-2 bg-red-500 rounded-full"></span>
                  </div>
                  <p class="text-sm text-gray-600 truncate mt-1">
                    {{ chat.lastMessage }}
                  </p>
                  <p class="text-xs text-gray-400 mt-1">
                    {{ formatTime(chat.lastMessageTime) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Side - Chat Messages -->
      <div class="flex-1 flex flex-col">
        <div v-if="!selectedChat" class="flex-1 flex items-center justify-center text-gray-500">
          <div class="text-center">
            <MessageCircle class="w-16 h-16 mx-auto mb-4 text-gray-300" />
            <p>Chọn một cuộc trò chuyện để bắt đầu</p>
          </div>
        </div>

        <div v-else class="flex-1 flex flex-col min-h-0">
          <!-- Chat Header -->
          <div class="p-3 border-b border-gray-200 bg-gray-50">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="font-semibold text-gray-800">{{ selectedChat.userName }}</h3>
                <p class="text-sm text-gray-600">Sản phẩm: {{ selectedChat.productName }}</p>
              </div>
              <div class="flex items-center space-x-2">
                <div v-if="hasNewMessages" class="flex items-center text-xs text-green-600">
                  <div class="w-2 h-2 bg-green-500 rounded-full mr-1 animate-pulse"></div>
                  Tin nhắn mới
                </div>
                <button
                  @click="markAsRead"
                  class="text-sm text-blue-600 hover:text-blue-700"
                >
                  Đánh dấu đã đọc
                </button>
                <button
                  @click="deleteConversation"
                  class="text-sm text-red-600 hover:text-red-700"
                  title="Xóa cuộc trò chuyện"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- Messages -->
          <div class="flex-1 overflow-y-auto p-3 space-y-2 min-h-0 max-h-96" ref="messagesContainer">
            <div v-if="messagesLoading" class="flex justify-center">
              <div class="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
            </div>

            <div
              v-for="message in messages"
              :key="message.id"
              class="flex"
              :class="message.messageType === 'SHOP_REPLY' ? 'justify-end' : 'justify-start'"
            >
              <div
                class="max-w-xs px-3 py-2 rounded-lg"
                :class="
                  message.messageType === 'SHOP_REPLY'
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

          <!-- Reply Input -->
          <div class="p-3 border-t border-gray-200">
            <div class="flex space-x-2">
              <input
                v-model="replyMessage"
                @keyup.enter="sendReply"
                type="text"
                placeholder="Nhập tin nhắn trả lời..."
                class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                :disabled="sending"
              />
              <button
                @click="sendReply"
                :disabled="!replyMessage.trim() || sending"
                class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Send v-if="!sending" class="w-4 h-4" />
                <div v-else class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
              </button>
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
const replyMessage = ref('')
const unreadCount = ref(0)
const messagesContainer = ref(null)
const hasNewMessages = ref(false)

const loadChatList = async () => {
  console.log('Loading chat list...', {
    userInfo: userInfo.value,
    shopId: userInfo.value?.shopId
  })

  if (!userInfo.value?.shopId) {
    console.log('No shopId, cannot load chat list')
    return
  }

  loading.value = true
  try {
    const response = await axios.get(`/api/chat/shop/${userInfo.value.shopId}`)
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

const loadMessages = async (productId, userId, isRefresh = false) => {
  if (!productId || !userId) return

  if (!isRefresh) {
    messagesLoading.value = true
  }

  try {
    const response = await axios.get(`/api/chat/product/${productId}/user/${userId}`)
    console.log('Loaded messages:', response.data)

    if (isRefresh) {
      // Nếu là refresh, chỉ thêm tin nhắn mới
      const newMessages = response.data.filter(newMsg =>
        !messages.value.some(existingMsg => existingMsg.id === newMsg.id)
      )
      if (newMessages.length > 0) {
        messages.value.push(...newMessages)
        hasNewMessages.value = true
        await nextTick()
        scrollToBottom()
        // Reset indicator sau 3 giây
        setTimeout(() => {
          hasNewMessages.value = false
        }, 3000)
      }
    } else {
      // Nếu là load lần đầu, thay thế toàn bộ
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
  console.log('Selecting chat:', chat)
  selectedChat.value = chat
  loadMessages(chat.productId, chat.userId)
}

const sendReply = async () => {
  if (!replyMessage.value.trim() || !selectedChat.value || sending.value) return

  sending.value = true
  try {
    // Tìm tin nhắn cuối cùng của customer để reply
    const lastCustomerMessage = messages.value
      .filter(msg => msg.messageType === 'CUSTOMER_MESSAGE')
      .pop()

    if (!lastCustomerMessage) {
      console.error('Không tìm thấy tin nhắn để trả lời')
      return
    }

    const response = await axios.post(`/api/chat/reply/${lastCustomerMessage.id}`, null, {
      params: {
        content: replyMessage.value.trim(),
        shopUserId: userInfo.value.id
      }
    })

    // Thêm tin nhắn mới vào danh sách ngay lập tức
    messages.value.push(response.data)
    replyMessage.value = ''

    // Cập nhật chat list ngay lập tức
    const updatedChat = chatList.value.find(chat =>
      chat.productId === selectedChat.value.productId &&
      chat.userId === selectedChat.value.userId
    )
    if (updatedChat) {
      updatedChat.lastMessage = response.data.content
      updatedChat.lastMessageTime = response.data.createdAt
      // Sắp xếp lại để tin nhắn mới nhất lên đầu
      chatList.value.sort((a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime))
    }

    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('Lỗi khi gửi tin nhắn:', error)
  } finally {
    sending.value = false
  }
}

const markAsRead = async () => {
  if (!selectedChat.value) return

  try {
    // Đánh dấu tất cả tin nhắn trong cuộc trò chuyện này là đã đọc
    const customerMessages = messages.value.filter(msg => msg.messageType === 'CUSTOMER_MESSAGE')
    for (const message of customerMessages) {
      await axios.put(`/api/chat/read/${message.id}`)
    }
    loadChatList()
  } catch (error) {
    console.error('Lỗi khi đánh dấu đã đọc:', error)
  }
}

const deleteConversation = async () => {
  if (!selectedChat.value) return

  dialog.warning({
    title: 'Xác nhận ẩn cuộc trò chuyện',
    content: `Bạn có chắc muốn ẩn cuộc trò chuyện với ${selectedChat.value.userName}?`,
    positiveText: 'Xác nhận',
    negativeText: 'Hủy',
    onPositiveClick: async () => {
      try {
        await axios.delete(`/api/chat/conversation/shop/${selectedChat.value.productId}/${userInfo.value.shopId}/${selectedChat.value.userId}`)

        // Xóa cuộc trò chuyện khỏi danh sách
        chatList.value = chatList.value.filter(chat =>
          !(chat.productId === selectedChat.value.productId && chat.userId === selectedChat.value.userId)
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

const scrollToBottom = () => {
  if (messagesContainer.value) {
    nextTick(() => {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    })
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
  if (!userInfo.value?.shopId) return

  try {
    const response = await axios.get(`/api/chat/unread/shop/${userInfo.value.shopId}`)
    unreadCount.value = response.data
  } catch (error) {
    console.error('Lỗi khi tải số tin nhắn chưa đọc:', error)
  }
}

watch(selectedChat, (newChat) => {
  if (newChat) {
    loadMessages(newChat.productId, newChat.userId)
  }
})

onMounted(async () => {
  console.log('ShopChatView mounted')
  if (!userInfo.value) {
    console.log('No userInfo, fetching...')
    await fetchUserInfo()
  }
  console.log('UserInfo after fetch:', userInfo.value)
  loadChatList()

  // Auto refresh chat list mỗi 5 giây để cập nhật tin nhắn mới
  setInterval(async () => {
    if (userInfo.value?.shopId) {
      await loadChatList()
      // Nếu đang xem một cuộc trò chuyện, cập nhật tin nhắn mới
      if (selectedChat.value) {
        await loadMessages(selectedChat.value.productId, selectedChat.value.userId, true)
      }
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
