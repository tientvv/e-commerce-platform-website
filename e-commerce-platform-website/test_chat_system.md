# Test Hệ Thống Chat

## 1. Test Customer Chat (Frontend)

### Bước 1: Đăng nhập customer
- Vào trang login
- Đăng nhập với tài khoản customer
- Vào trang chi tiết sản phẩm

### Bước 2: Test gửi tin nhắn
- Click vào chat button (góc phải dưới)
- Nhập tin nhắn và gửi
- Kiểm tra tin nhắn có hiển thị không

## 2. Test Shop Chat Management (Frontend)

### Bước 1: Đăng nhập shop owner
- Đăng nhập với tài khoản có shop
- Vào `/user/shop/chat`

### Bước 2: Test quản lý tin nhắn
- Kiểm tra danh sách tin nhắn hiển thị
- Click vào một cuộc trò chuyện
- Gửi tin nhắn trả lời
- Đánh dấu đã đọc

## 3. Test Backend APIs

### Test API Product Detail
```bash
curl -X GET "http://localhost:8080/api/products/{productId}"
```

### Test API Chat Send
```bash
curl -X POST "http://localhost:8080/api/chat/send?userId={userId}" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "{productId}",
    "shopId": "{shopId}",
    "messageType": "CUSTOMER_MESSAGE",
    "content": "Test message"
  }'
```

### Test API Chat Messages
```bash
curl -X GET "http://localhost:8080/api/chat/product/{productId}/user/{userId}"
```

### Test API Shop Chat List
```bash
curl -X GET "http://localhost:8080/api/chat/shop/{shopId}"
```

## 4. Kiểm tra Database

### Kiểm tra bảng chat_messages
```sql
SELECT * FROM chat_messages ORDER BY created_at DESC;
```

### Kiểm tra constraint
```sql
SELECT * FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = 'dbo' 
AND CONSTRAINT_NAME LIKE '%message_type%';
```

## 5. Debug Issues

### Nếu userInfo không có shopId
- Kiểm tra user có shop không
- Kiểm tra API `/api/info-account` có trả về shopId

### Nếu không gửi được tin nhắn
- Kiểm tra database constraint
- Kiểm tra console logs
- Kiểm tra network requests

### Nếu shop chat không hiển thị
- Kiểm tra shopId có đúng không
- Kiểm tra API `/api/chat/shop/{shopId}`
- Kiểm tra dữ liệu trả về
