# Debug Chat API

## 1. Kiểm tra Database
Chạy script `test_chat_data.sql` để kiểm tra:
- Có tin nhắn nào trong database không
- Tin nhắn có đúng shop_id không
- Constraint có đúng không

## 2. Test API trực tiếp

### Test API Info Account
```bash
curl -X GET "http://localhost:8080/api/info-account" \
  -H "Cookie: JSESSIONID=your_session_id"
```

### Test API Chat Shop
```bash
curl -X GET "http://localhost:8080/api/chat/shop/{shopId}" \
  -H "Cookie: JSESSIONID=your_session_id"
```

### Test API Chat Product
```bash
curl -X GET "http://localhost:8080/api/chat/product/{productId}/user/{userId}" \
  -H "Cookie: JSESSIONID=your_session_id"
```

## 3. Debug Frontend

### Mở Developer Tools (F12)
1. Vào tab Console
2. Vào `/user/shop/chat`
3. Kiểm tra các log:
   - "ShopChatView mounted"
   - "UserInfo after fetch: ..."
   - "Loading chat list..."
   - "Chat list response: ..."

### Kiểm tra Network tab
1. Xem API calls có thành công không
2. Kiểm tra response data
3. Xem có lỗi 404, 500 không

## 4. Các vấn đề có thể xảy ra

### A. userInfo không có shopId
- Kiểm tra user có shop không
- Kiểm tra API `/api/info-account` có trả về shopId

### B. Database không có tin nhắn
- Chạy script SQL để kiểm tra
- Kiểm tra constraint có đúng không

### C. API trả về lỗi
- Kiểm tra backend logs
- Kiểm tra CORS configuration

### D. Frontend không load dữ liệu
- Kiểm tra console logs
- Kiểm tra network requests
