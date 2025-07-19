CREATE TABLE [accounts] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [google_id] varchar(255),
  [username] varchar(255) UNIQUE,
  [password] varchar(255),
  [email] varchar(255) UNIQUE,
  [phone] varchar(13) UNIQUE,
  [address] nvarchar(max),
  [accounts_image] varchar(max),
  [created_at] datetimeoffset(6),
  [updated_at] datetimeoffset(6),
  [last_login] datetimeoffset(6),
  [role] varchar(50),
  [is_active] bit DEFAULT (1)
)
GO

CREATE TABLE [categories] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [name] nvarchar(255) UNIQUE,
  [category_image] varchar(max)
)
GO

CREATE TABLE [products] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [category_id] uniqueidentifier,
  [brand] nvarchar(50),
  [name] nvarchar(255),
  [product_image] varchar(max),
  [description] nvarchar(max),
  [view_count] int,
  [sold_count] int,
  [is_active] bit DEFAULT (1),
  [shop_id] uniqueidentifier
)
GO

CREATE TABLE [product_variants] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [product_id] uniqueidentifier,
  [variant_name] nvarchar(100),
  [variant_value] nvarchar(100),
  [quantity] int,
  [price] decimal(18,2),
  [is_active] bit DEFAULT (1)
)
GO

CREATE TABLE [shops] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [shop_image] varchar(max),
  [shop_name] nvarchar(max),
  [description] nvarchar(max),
  [phone] nvarchar(255),
  [email] nvarchar(255),
  [address] nvarchar(max),
  [rating] decimal(18,2),
  [created_at] datetimeoffset(6),
  [is_active] bit DEFAULT (1),
  [user_id] uniqueidentifier UNIQUE
)
GO

CREATE TABLE [product_images] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [image_url] varchar(max),
  [product_id] uniqueidentifier,
  [product_variant_id] uniqueidentifier
)
GO

CREATE TABLE [payments] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [payment_code] varchar(50) UNIQUE,
  [payment_type] varchar(20),
  [payment_name] nvarchar(100),
  [icon] varchar(max),
  [description] nvarchar(max),
  [is_active] bit DEFAULT (1)
)
GO

CREATE TABLE [shippings] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [shipping_method] nvarchar(100),
  [description] nvarchar(max),
  [price] decimal(18,2),
  [estimated_delivery] nvarchar(100),
  [is_active] bit DEFAULT (1)
)
GO

CREATE TABLE [reviews] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [product_id] uniqueidentifier,
  [product_variant_id] uniqueidentifier,
  [account_id] uniqueidentifier,
  [rating] decimal(18,2),
  [comment] nvarchar(max),
  [review_date] datetimeoffset(6)
)
GO

CREATE TABLE [orders] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [account_id] uniqueidentifier,
  [shop_id] uniqueidentifier,
  [shipping_id] uniqueidentifier,
  [payment_id] uniqueidentifier,
  [total_amount] decimal(18,2),
  [delivered_date] datetimeoffset(6),
  [discount_amount] decimal(18,2) DEFAULT (0),
  [order_status] varchar(50),
  [order_date] datetimeoffset(6),
  [cancelled_date] datetimeoffset(6),
  [shipping_address] nvarchar(max)
)
GO

CREATE TABLE [order_items] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [order_id] uniqueidentifier,
  [product_variant_id] uniqueidentifier,
  [quantity] int,
  [product_price] decimal(18,2),
  [discount_applied] decimal(18,2) DEFAULT (0)
)
GO

CREATE TABLE [discounts] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [name] nvarchar(255),
  [description] nvarchar(max),
  [discount_type] varchar(50),
  [discount_value] decimal(18,2),
  [start_date] datetimeoffset(6),
  [end_date] datetimeoffset(6),
  [min_order_value] decimal(18,2),
  [product_id] uniqueidentifier,
  [category_id] uniqueidentifier,
  [product_variant_id] uniqueidentifier,
  [is_active] bit DEFAULT (1)
)
GO

CREATE TABLE [transactions] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [order_id] uniqueidentifier,
  [payment_id] uniqueidentifier,
  [transaction_code] varchar(255),
  [transaction_amount] decimal(18,2),
  [transaction_status] varchar(50),
  [transaction_date] datetimeoffset(6),
  [vnpay_response_code] varchar(50)
)
GO

CREATE TABLE [wishlists] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [account_id] uniqueidentifier,
  [product_id] uniqueidentifier,
  [created_at] datetimeoffset(6)
)
GO

CREATE TABLE [return_refunds] (
  [id] uniqueidentifier PRIMARY KEY DEFAULT (NEWID()),
  [order_id] uniqueidentifier,
  [account_id] uniqueidentifier,
  [order_item_id] uniqueidentifier,
  [transaction_id] uniqueidentifier,
  [return_reason] nvarchar(max),
  [refund_amount] decimal(18,2),
  [return_status] varchar(50),
  [request_date] datetimeoffset(6),
  [approved_date] datetimeoffset(6),
  [completed_date] datetimeoffset(6),
  [rejected_date] datetimeoffset(6),
  [is_active] bit DEFAULT (1)
)
GO

ALTER TABLE [products] ADD FOREIGN KEY ([category_id]) REFERENCES [categories] ([id])
GO

ALTER TABLE [product_images] ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id])
GO

ALTER TABLE [products] ADD FOREIGN KEY ([shop_id]) REFERENCES [shops] ([id])
GO

ALTER TABLE [orders] ADD FOREIGN KEY ([account_id]) REFERENCES [accounts] ([id])
GO

ALTER TABLE [orders] ADD FOREIGN KEY ([shop_id]) REFERENCES [shops] ([id])
GO

ALTER TABLE [orders] ADD FOREIGN KEY ([shipping_id]) REFERENCES [shippings] ([id])
GO

ALTER TABLE [orders] ADD FOREIGN KEY ([payment_id]) REFERENCES [payments] ([id])
GO

ALTER TABLE [order_items] ADD FOREIGN KEY ([order_id]) REFERENCES [orders] ([id])
GO

ALTER TABLE [order_items] ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id])
GO

ALTER TABLE [reviews] ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id])
GO

ALTER TABLE [reviews] ADD FOREIGN KEY ([account_id]) REFERENCES [accounts] ([id])
GO

ALTER TABLE [transactions] ADD FOREIGN KEY ([order_id]) REFERENCES [orders] ([id])
GO

ALTER TABLE [discounts] ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id])
GO

ALTER TABLE [discounts] ADD FOREIGN KEY ([category_id]) REFERENCES [categories] ([id])
GO

ALTER TABLE [wishlists] ADD FOREIGN KEY ([account_id]) REFERENCES [accounts] ([id])
GO

ALTER TABLE [wishlists] ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id])
GO

ALTER TABLE [product_variants] ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id])
GO

ALTER TABLE [discounts] ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id])
GO

ALTER TABLE [reviews] ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id])
GO

ALTER TABLE [product_images] ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id])
GO

ALTER TABLE [shops] ADD FOREIGN KEY ([user_id]) REFERENCES [accounts] ([id])
GO

ALTER TABLE [transactions] ADD FOREIGN KEY ([payment_id]) REFERENCES [payments] ([id])
GO

ALTER TABLE [return_refunds] ADD FOREIGN KEY ([account_id]) REFERENCES [accounts] ([id])
GO

ALTER TABLE [return_refunds] ADD FOREIGN KEY ([order_item_id]) REFERENCES [order_items] ([id])
GO

ALTER TABLE [return_refunds] ADD FOREIGN KEY ([transaction_id]) REFERENCES [transactions] ([id])
GO
