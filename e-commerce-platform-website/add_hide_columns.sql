-- Thêm cột để ẩn cuộc trò chuyện thay vì xóa
-- Thêm cột is_deleted_by_user
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'chat_messages' AND COLUMN_NAME = 'is_deleted_by_user')
BEGIN
    ALTER TABLE chat_messages ADD is_deleted_by_user BIT DEFAULT 0;
    PRINT 'Đã thêm cột is_deleted_by_user';
END
ELSE
BEGIN
    PRINT 'Cột is_deleted_by_user đã tồn tại';
END

-- Thêm cột is_deleted_by_shop
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'chat_messages' AND COLUMN_NAME = 'is_deleted_by_shop')
BEGIN
    ALTER TABLE chat_messages ADD is_deleted_by_shop BIT DEFAULT 0;
    PRINT 'Đã thêm cột is_deleted_by_shop';
END
ELSE
BEGIN
    PRINT 'Cột is_deleted_by_shop đã tồn tại';
END

-- Cập nhật dữ liệu cũ (nếu có)
UPDATE chat_messages SET is_deleted_by_user = 0 WHERE is_deleted_by_user IS NULL;
UPDATE chat_messages SET is_deleted_by_shop = 0 WHERE is_deleted_by_shop IS NULL;

PRINT 'Hoàn thành thêm cột ẩn cuộc trò chuyện';
