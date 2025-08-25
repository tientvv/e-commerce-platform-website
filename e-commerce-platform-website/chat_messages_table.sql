-- Bảng chat_messages để lưu trữ tin nhắn chat giữa khách hàng và shop
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
GO

-- Foreign keys cho bảng chat_messages
ALTER TABLE [chat_messages] ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id])
GO
ALTER TABLE [chat_messages] ADD FOREIGN KEY ([shop_id]) REFERENCES [shops] ([id])
GO
ALTER TABLE [chat_messages] ADD FOREIGN KEY ([user_id]) REFERENCES [accounts] ([id])
GO
