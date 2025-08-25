-- Kiểm tra xem bảng chat_messages đã tồn tại chưa
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='chat_messages' AND xtype='U')
BEGIN
    -- Tạo bảng chat_messages
    CREATE TABLE [chat_messages] (
        [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
        [product_id] uniqueidentifier,
        [shop_id] uniqueidentifier,
        [user_id] uniqueidentifier,
        [message_type] varchar(20),
        [content] nvarchar(max),
        [is_read] bit DEFAULT (0),
        [created_at] datetimeoffset(6),
        [updated_at] datetimeoffset(6)
    )
    
    -- Thêm foreign keys
    ALTER TABLE [chat_messages] ADD CONSTRAINT FK_chat_messages_product 
        FOREIGN KEY ([product_id]) REFERENCES [products] ([id])
    
    ALTER TABLE [chat_messages] ADD CONSTRAINT FK_chat_messages_shop 
        FOREIGN KEY ([shop_id]) REFERENCES [shops] ([id])
    
    ALTER TABLE [chat_messages] ADD CONSTRAINT FK_chat_messages_user 
        FOREIGN KEY ([user_id]) REFERENCES [accounts] ([id])
    
    PRINT 'Bảng chat_messages đã được tạo thành công!'
END
ELSE
BEGIN
    PRINT 'Bảng chat_messages đã tồn tại!'
END
