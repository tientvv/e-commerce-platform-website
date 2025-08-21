-- Script kiểm tra và xóa đơn hàng trùng lặp
-- Chạy script này để tìm và xóa các đơn hàng bị tạo trùng lặp

-- 1. Hiển thị tất cả đơn hàng để kiểm tra
SELECT 
    o.id,
    o.order_code,
    o.total_amount,
    o.order_status,
    o.order_date,
    a.username as customer_name,
    s.shop_name,
    COUNT(oi.id) as item_count
FROM orders o
LEFT JOIN accounts a ON o.account_id = a.id
LEFT JOIN shops s ON o.shop_id = s.id
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id, o.order_code, o.total_amount, o.order_status, o.order_date, a.username, s.shop_name
ORDER BY o.order_date DESC;

-- 2. Tìm các đơn hàng có cùng thông tin (có thể trùng lặp)
SELECT 
    o1.id as order1_id,
    o1.order_code as order1_code,
    o1.total_amount as order1_amount,
    o1.order_date as order1_date,
    o2.id as order2_id,
    o2.order_code as order2_code,
    o2.total_amount as order2_amount,
    o2.order_date as order2_date,
    DATEDIFF(SECOND, o1.order_date, o2.order_date) as time_diff_seconds
FROM orders o1
JOIN orders o2 ON 
    o1.account_id = o2.account_id 
    AND o1.shop_id = o2.shop_id 
    AND o1.total_amount = o2.total_amount
    AND o1.id != o2.id
    AND ABS(DATEDIFF(SECOND, o1.order_date, o2.order_date)) < 60  -- Chênh lệch thời gian < 60 giây
ORDER BY o1.order_date DESC;

-- 3. Xóa đơn hàng trùng lặp (chỉ giữ lại đơn hàng đầu tiên)
-- UNCOMMENT DÒNG DƯỚI ĐÂY NẾU MUỐN XÓA ĐƠN HÀNG TRÙNG LẶP
-- DELETE o2 FROM orders o1
-- JOIN orders o2 ON 
--     o1.account_id = o2.account_id 
--     AND o1.shop_id = o2.shop_id 
--     AND o1.total_amount = o2.total_amount
--     AND o1.id != o2.id
--     AND o1.order_date < o2.order_date
--     AND ABS(DATEDIFF(SECOND, o1.order_date, o2.order_date)) < 60;

-- 4. Xóa order_items của đơn hàng đã xóa
-- UNCOMMENT DÒNG DƯỚI ĐÂY NẾU MUỐN XÓA ORDER_ITEMS CỦA ĐƠN HÀNG TRÙNG LẶP
-- DELETE oi FROM order_items oi
-- LEFT JOIN orders o ON oi.order_id = o.id
-- WHERE o.id IS NULL;

-- 5. Xóa transactions của đơn hàng đã xóa
-- UNCOMMENT DÒNG DƯỚI ĐÂY NẾU MUỐN XÓA TRANSACTIONS CỦA ĐƠN HÀNG TRÙNG LẶP
-- DELETE t FROM transactions t
-- LEFT JOIN orders o ON t.order_id = o.id
-- WHERE o.id IS NULL;
