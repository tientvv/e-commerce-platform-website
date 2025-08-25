-- Kiểm tra cuộc trò chuyện cụ thể

-- 1. Tìm customer "Tien Tran Van"
SELECT 
    a.id as customer_id,
    a.name as customer_name,
    a.email as customer_email
FROM accounts a
WHERE a.name LIKE '%Tien%' OR a.name LIKE '%Tran%' OR a.name LIKE '%Van%';

-- 2. Tìm product "CHUỘT CHƠI GAME PRO X SUPERLIGHT 2"
SELECT 
    p.id as product_id,
    p.name as product_name,
    s.shopName as shop_name
FROM products p
JOIN shops s ON p.shop_id = s.id
WHERE p.name LIKE '%CHUỘT%' OR p.name LIKE '%GAME%';

-- 3. Xem tất cả tin nhắn trong cuộc trò chuyện cụ thể
-- Thay thế {product_id} và {customer_id} bằng ID thực tế từ query trên
SELECT 
    cm.id,
    cm.content,
    cm.message_type,
    cm.is_read,
    cm.created_at,
    p.name as product_name,
    s.shopName as shop_name,
    a.name as user_name,
    a.id as user_id,
    CASE 
        WHEN cm.message_type = 'CUSTOMER_MESSAGE' THEN 'Customer'
        WHEN cm.message_type = 'SHOP_REPLY' THEN 'Shop'
        ELSE 'Other'
    END as sender_type
FROM chat_messages cm
JOIN products p ON cm.product_id = p.id
JOIN shops s ON cm.shop_id = s.id
JOIN accounts a ON cm.user_id = a.id
WHERE p.name LIKE '%CHUỘT%' OR p.name LIKE '%GAME%'
ORDER BY cm.created_at ASC;

-- 4. Kiểm tra tin nhắn theo thời gian gần đây
SELECT 
    cm.id,
    cm.content,
    cm.message_type,
    cm.created_at,
    p.name as product_name,
    a.name as user_name
FROM chat_messages cm
JOIN products p ON cm.product_id = p.id
JOIN accounts a ON cm.user_id = a.id
WHERE cm.created_at >= DATEADD(hour, -1, GETDATE())
ORDER BY cm.created_at DESC;
