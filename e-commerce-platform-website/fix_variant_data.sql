-- Script sửa dữ liệu variant bị thiếu
-- Chạy script này để đảm bảo tất cả variants có đầy đủ thông tin

-- Cập nhật variantName nếu bị NULL hoặc rỗng
UPDATE product_variants 
SET variant_name = 'Loại' 
WHERE variant_name IS NULL OR LTRIM(RTRIM(variant_name)) = ''

-- Cập nhật variantValue nếu bị NULL hoặc rỗng
UPDATE product_variants 
SET variant_value = 'Mặc định' 
WHERE variant_value IS NULL OR LTRIM(RTRIM(variant_value)) = ''

-- Hiển thị kết quả sau khi sửa
SELECT 
    pv.id,
    pv.product_id,
    p.name as product_name,
    pv.variant_name,
    pv.variant_value,
    pv.price,
    pv.quantity
FROM product_variants pv
JOIN products p ON pv.product_id = p.id
ORDER BY p.name, pv.variant_name, pv.variant_value;
