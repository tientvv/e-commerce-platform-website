-- Kiểm tra vấn đề duplicate chat sau khi shop trả lời

-- 1. Xem tất cả tin nhắn theo thứ tự thời gian
SELECT 
    cm.id,
    cm.content,
    cm.message_type,
    cm.is_read,
    cm.created_at,
    p.name as product_name,
    s.shopName as shop_name,
    a.name as user_name,
    a.id as user_id
FROM chat_messages cm
JOIN products p ON cm.product_id = p.id
JOIN shops s ON cm.shop_id = s.id
JOIN accounts a ON cm.user_id = a.id
ORDER BY cm.created_at DESC;

-- 2. Kiểm tra tin nhắn theo product và user
SELECT 
    p.name as product_name,
    a.name as customer_name,
    a.id as customer_id,
    COUNT(cm.id) as total_messages,
    SUM(CASE WHEN cm.message_type = 'CUSTOMER_MESSAGE' THEN 1 ELSE 0 END) as customer_messages,
    SUM(CASE WHEN cm.message_type = 'SHOP_REPLY' THEN 1 ELSE 0 END) as shop_replies,
    MAX(cm.created_at) as last_message_time
FROM chat_messages cm
JOIN products p ON cm.product_id = p.id
JOIN accounts a ON cm.user_id = a.id
WHERE cm.message_type = 'CUSTOMER_MESSAGE'
GROUP BY p.id, p.name, a.id, a.name
ORDER BY last_message_time DESC;

-- 3. Kiểm tra tin nhắn shop reply
SELECT 
    cm.id,
    cm.content,
    cm.created_at,
    p.name as product_name,
    s.shopName as shop_name,
    a.name as shop_user_name
FROM chat_messages cm
JOIN products p ON cm.product_id = p.id
JOIN shops s ON cm.shop_id = s.id
JOIN accounts a ON cm.user_id = a.id
WHERE cm.message_type = 'SHOP_REPLY'
ORDER BY cm.created_at DESC;
