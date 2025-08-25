# Debug Chat Issues

## Vấn đề hiện tại
Từ ảnh, giao diện chat đã hiển thị nhưng không gửi được tin nhắn.

## Các bước debug

### 1. Kiểm tra Database
```sql
-- Chạy script để tạo bảng chat_messages
-- File: check_and_create_chat_table.sql

-- Kiểm tra bảng đã được tạo
SELECT * FROM chat_messages;

-- Kiểm tra product có shopId
SELECT p.id, p.name, s.id as shopId, s.shopName 
FROM products p 
JOIN shops s ON p.shop_id = s.id 
LIMIT 5;
```

### 2. Kiểm tra Backend
```bash
# Khởi động backend
cd back-end
mvn spring-boot:run

# Test API product detail
curl -X GET "http://localhost:8080/api/products/{productId}"

# Test API chat send
curl -X POST "http://localhost:8080/api/chat/send?userId={userId}" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "{productId}",
    "shopId": "{shopId}",
    "messageType": "CUSTOMER_MESSAGE",
    "content": "Test message"
  }'
```

### 3. Kiểm tra Frontend
1. Mở Developer Tools (F12)
2. Vào tab Console
3. Vào trang chi tiết sản phẩm
4. Kiểm tra các log messages:
   - "ChatBot mounted, userInfo: ..."
   - "User info changed: ..."
   - "Product loaded: ..."
   - "Product shop info: ..."

### 4. Sử dụng ChatTest Component
Component ChatTest đã được thêm vào ProductDetailView để debug:
- Hiển thị thông tin user, product, shop
- Test gửi tin nhắn
- Test load tin nhắn

### 5. Các vấn đề có thể xảy ra

#### A. UserInfo không được load
```javascript
// Kiểm tra trong console
console.log('UserInfo:', userInfo.value)
```

**Fix:** Đảm bảo user đã đăng nhập và session còn hiệu lực.

#### B. Product không có shopId
```javascript
// Kiểm tra trong console
console.log('Product shop info:', {
  shopId: product.value?.shopId,
  shopName: product.value?.shopName
})
```

**Fix:** Kiểm tra database xem product có shop_id không.

#### C. API Chat không hoạt động
```javascript
// Kiểm tra network tab trong dev tools
// Xem có lỗi 404, 500 không
```

**Fix:** 
- Kiểm tra backend đã start chưa
- Kiểm tra bảng chat_messages đã tạo chưa
- Kiểm tra CORS configuration

#### D. Database connection issues
```sql
-- Kiểm tra kết nối database
SELECT 1;
```

**Fix:** Kiểm tra application.properties và database connection.

### 6. Logs cần kiểm tra

#### Backend logs:
```
Received chat message request: ...
User ID: ...
Created message: ...
Error creating chat message: ...
```

#### Frontend logs:
```
ChatBot mounted, userInfo: ...
User info changed: ...
Loading messages...
Sending message...
Lỗi khi gửi tin nhắn: ...
```

### 7. Quick Fixes

#### Nếu userInfo không load:
```javascript
// Trong ChatBot.vue, thêm
onMounted(async () => {
  if (!userInfo.value) {
    await fetchUserInfo()
  }
})
```

#### Nếu product không có shopId:
```javascript
// Trong ProductDetailView.vue, thêm logging
console.log('Product full data:', product.value)
```

#### Nếu API không hoạt động:
```bash
# Kiểm tra backend logs
tail -f back-end/logs/application.log
```

### 8. Test Cases

1. **Test với user đã đăng nhập**
2. **Test với product có shopId**
3. **Test gửi tin nhắn đơn giản**
4. **Test load tin nhắn**
5. **Test shop reply**

### 9. Rollback nếu cần

Nếu có vấn đề nghiêm trọng:
```sql
-- Xóa bảng chat_messages
DROP TABLE IF EXISTS chat_messages;
```

Và comment out ChatBot component trong ProductDetailView.vue.
