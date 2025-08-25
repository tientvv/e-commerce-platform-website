# Tính năng Chatbot cho E-commerce Platform

## Tổng quan
Tính năng chatbot cho phép khách hàng chat trực tiếp với shop về sản phẩm, tương tự như Shopee. Shop có thể trả lời tin nhắn từ khách hàng thông qua giao diện quản lý.

## Tính năng chính

### 1. Chat cho khách hàng
- Chat button xuất hiện ở góc phải dưới trang chi tiết sản phẩm
- Khách hàng có thể gửi tin nhắn hỏi về sản phẩm
- Hiển thị số tin nhắn chưa đọc
- Giao diện chat thân thiện với người dùng

### 2. Quản lý tin nhắn cho shop
- Giao diện quản lý tin nhắn trong shop dashboard
- Hiển thị danh sách tin nhắn từ khách hàng
- Shop có thể trả lời tin nhắn
- Đánh dấu tin nhắn đã đọc
- Hiển thị số tin nhắn chưa đọc

## Cấu trúc Database

### Bảng chat_messages
```sql
CREATE TABLE [chat_messages] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [product_id] uniqueidentifier,
  [shop_id] uniqueidentifier,
  [user_id] uniqueidentifier,
  [message_type] varchar(20),
  [content] nvarchar(max),
  [is_read] bit DEFAULT (0),
  [created_at] datetimeoffset(6),
  [updated_at] datetimeoffset(6)
)
```

### Các loại tin nhắn
- `CUSTOMER_MESSAGE`: Tin nhắn từ khách hàng
- `SHOP_REPLY`: Tin nhắn trả lời từ shop
- `SYSTEM_MESSAGE`: Tin nhắn hệ thống

## API Endpoints

### Backend APIs
- `GET /api/chat/product/{productId}` - Lấy tin nhắn theo sản phẩm
- `GET /api/chat/product/{productId}/user/{userId}` - Lấy tin nhắn theo sản phẩm và user
- `GET /api/chat/shop/{shopId}` - Lấy tin nhắn theo shop
- `GET /api/chat/user/{userId}` - Lấy tin nhắn theo user
- `POST /api/chat/send` - Gửi tin nhắn mới
- `POST /api/chat/reply/{messageId}` - Trả lời tin nhắn
- `PUT /api/chat/read/{messageId}` - Đánh dấu đã đọc
- `GET /api/chat/unread/shop/{shopId}` - Đếm tin nhắn chưa đọc cho shop
- `GET /api/chat/unread/user/{userId}` - Đếm tin nhắn chưa đọc cho user

## Cài đặt

### 1. Chạy script SQL
```sql
-- Chạy file chat_messages_table.sql để tạo bảng
```

### 2. Khởi động backend
```bash
cd back-end
mvn spring-boot:run
```

### 3. Khởi động frontend
```bash
cd front-end
npm install
npm run dev
```

## Sử dụng

### Cho khách hàng
1. Vào trang chi tiết sản phẩm
2. Click vào nút chat (góc phải dưới)
3. Nhập tin nhắn và gửi
4. Chờ shop trả lời

### Cho shop
1. Đăng nhập vào tài khoản shop
2. Vào "Quản lý tin nhắn" trong menu
3. Chọn cuộc trò chuyện cần trả lời
4. Nhập tin nhắn trả lời và gửi

## Cấu trúc Files

### Backend
- `ChatMessage.java` - Model cho tin nhắn chat
- `ChatMessageDto.java` - DTO cho tin nhắn
- `CreateChatMessageDto.java` - DTO tạo tin nhắn mới
- `ChatMessageRepository.java` - Repository
- `ChatMessageService.java` - Service logic
- `ChatMessageController.java` - Controller API

### Frontend
- `ChatBot.vue` - Component chat cho khách hàng
- `ShopChatView.vue` - Giao diện quản lý tin nhắn cho shop
- Cập nhật `ProductDetailView.vue` để thêm ChatBot component
- Cập nhật `MenuBarView.vue` để thêm menu quản lý tin nhắn
- Cập nhật `router/index.js` để thêm route cho shop chat

## Tính năng nâng cao có thể thêm

1. **Real-time chat** với WebSocket
2. **Push notification** khi có tin nhắn mới
3. **File attachment** (hình ảnh, file)
4. **Auto-reply** cho shop
5. **Chat history** và search
6. **Typing indicator**
7. **Read receipts**
8. **Chat analytics** cho shop

## Lưu ý

- Cần đăng nhập để sử dụng tính năng chat
- Shop cần có tài khoản shop để quản lý tin nhắn
- Tin nhắn được lưu trữ theo sản phẩm và user
- Có thể mở rộng để chat trực tiếp với shop (không theo sản phẩm)
