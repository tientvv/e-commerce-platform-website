-- Sửa CHECK constraint cho bảng chat_messages
-- Xóa constraint cũ nếu tồn tại
IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK__chat_mess__messa__236943A5')
BEGIN
    ALTER TABLE chat_messages DROP CONSTRAINT CK__chat_mess__messa__236943A5
END

-- Tạo constraint mới với các giá trị enum đúng
ALTER TABLE chat_messages 
ADD CONSTRAINT CK_chat_messages_message_type 
CHECK (message_type IN ('CUSTOMER_MESSAGE', 'SHOP_REPLY', 'SYSTEM_MESSAGE'))

PRINT 'Đã sửa CHECK constraint cho bảng chat_messages'
