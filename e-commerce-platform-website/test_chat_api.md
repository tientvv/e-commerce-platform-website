# Test Chat API

## 1. Kiểm tra API Product Detail
```bash
curl -X GET "http://localhost:8080/api/products/{productId}"
```

Kiểm tra response có chứa:
- `shopId`
- `shopName`

## 2. Kiểm tra API Chat Send
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

## 3. Kiểm tra API Chat Messages
```bash
curl -X GET "http://localhost:8080/api/chat/product/{productId}/user/{userId}"
```

## 4. Kiểm tra Database
```sql
-- Kiểm tra bảng chat_messages
SELECT * FROM chat_messages;

-- Kiểm tra product có shopId
SELECT p.id, p.name, s.id as shopId, s.shopName 
FROM products p 
JOIN shops s ON p.shop_id = s.id 
WHERE p.id = '{productId}';
```

## 5. Debug Frontend
Mở Developer Tools (F12) và kiểm tra:
- Console logs
- Network requests
- User info có được load không
