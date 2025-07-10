CREATE TABLE [categories] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [name] nvarchar(255) UNIQUE,
    [parent_id] uniqueidentifier,
    [image_url] varchar(max),
    [created_at] datetimeoffset(6),
    [updated_at] datetimeoffset(6)
);

CREATE TABLE [products] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [brand_id] uniqueidentifier,
    [category_id] uniqueidentifier,
    [slug] nvarchar(255) UNIQUE,
    [short_description] nvarchar(max),
    [search_keywords] nvarchar(500),
    [view_count] int DEFAULT (0),
    [shop_id] uniqueidentifier,
    [is_featured] bit DEFAULT (0)
);

CREATE TABLE [shops] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [user_id] uniqueidentifier,
    [name] nvarchar(255),
    [description] nvarchar(max),
    [created_at] datetimeoffset(6),
    [updated_at] datetimeoffset(6)
);

CREATE TABLE [shop_stats] (
    [shop_id] uniqueidentifier PRIMARY KEY,
    [total_sales] decimal(18, 2),
    [total_orders] int,
    [last_updated] datetimeoffset(6)
);

CREATE TABLE [product_variants] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [product_id] uniqueidentifier,
    [name] nvarchar(500),
    [price] decimal(18, 2),
    [original_price] decimal(18, 2),
    [size] nvarchar(50),
    [color] nvarchar(50),
    [quantity] int,
    [image_url] varchar(max),
    [created_at] datetimeoffset(6),
    [updated_at] datetimeoffset(6)
);

CREATE TABLE [product_images] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [product_variant_id] uniqueidentifier,
    [image_url] varchar(max),
    [sort_order] int
);

CREATE TABLE [brands] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [name] nvarchar(255),
    [logo_url] varchar(max),
    [is_popular] bit DEFAULT (0)
);

CREATE TABLE [promotion_products] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [promotion_id] uniqueidentifier,
    [product_id] uniqueidentifier,
    [product_variant_id] uniqueidentifier
);

CREATE TABLE [promotions] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [name] nvarchar(255),
    [discount_type] nvarchar(10),
    [discount_value] decimal(18, 2),
    [start_date] datetimeoffset(6),
    [end_date] datetimeoffset(6),
    [is_active] bit DEFAULT (1),
    [created_at] datetimeoffset(6),
    [updated_at] datetimeoffset(6)
);

CREATE TABLE [coupons] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [code] nvarchar(50) UNIQUE,
    [description] nvarchar(255),
    [discount_type] nvarchar(10),
    [discount_value] decimal(18, 2),
    [min_order_value] decimal(18, 2),
    [start_date] datetimeoffset(6),
    [end_date] datetimeoffset(6),
    [max_uses] int,
    [uses_count] int DEFAULT (0),
    [is_active] bit DEFAULT (1),
    [applies_to_all] bit DEFAULT (1),
    [created_at] datetimeoffset(6),
    [updated_at] datetimeoffset(6)
);

CREATE TABLE [coupon_products] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [coupon_id] uniqueidentifier,
    [product_id] uniqueidentifier,
    [product_variant_id] uniqueidentifier
);

CREATE TABLE [users] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [email] nvarchar(255) UNIQUE,
    [password_hash] nvarchar(255),
    [full_name] nvarchar(255),
    [phone] nvarchar(20) UNIQUE,
    [address] nvarchar(max),
    [avatar_url] nvarchar(max),
    [is_buyer] bit DEFAULT (1),
    [is_seller] bit DEFAULT (0),
    [is_admin] bit DEFAULT (0),
    [is_active] bit DEFAULT (1),
    [email_verified] bit DEFAULT (0),
    [phone_verified] bit DEFAULT (0),
    [verification_token] nvarchar(100),
    [created_at] datetimeoffset(6),
    [updated_at] datetimeoffset(6)
);

CREATE TABLE [user_logins] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [user_id] uniqueidentifier,
    [ip_address] nvarchar(50),
    [device_info] nvarchar(255),
    [created_at] datetimeoffset(6)
);

CREATE TABLE [orders] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [user_id] uniqueidentifier,
    [order_code] nvarchar(50) UNIQUE,
    [status] nvarchar(50),
    [subtotal] decimal(18, 2),
    [shipping_fee] decimal(18, 2),
    [discount_amount] decimal(18, 2),
    [total] decimal(18, 2),
    [payment_method] nvarchar(50),
    [shipping_address] nvarchar(max),
    [customer_note] nvarchar(max),
    [cancellation_reason] nvarchar(max),
    [created_at] datetimeoffset(6),
    [updated_at] datetimeoffset(6)
);

CREATE TABLE [inventory] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [product_variant_id] uniqueidentifier,
    [in_stock] int DEFAULT (0),
    [reserved] int DEFAULT (0),
    [updated_at] datetimeoffset(6)
);

CREATE TABLE [order_status_history] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [order_id] uniqueidentifier,
    [status] nvarchar(50),
    [note] nvarchar(max),
    [created_at] datetimeoffset(6)
);

CREATE TABLE [order_items] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [order_id] uniqueidentifier,
    [product_variant_id] uniqueidentifier,
    [quantity] int,
    [price] decimal(18, 2),
    [discount_price] decimal(18, 2)
);

CREATE TABLE [payments] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [order_id] uniqueidentifier,
    [amount] decimal(18, 2),
    [payment_method] nvarchar(50),
    [status] nvarchar(50),
    [transaction_code] nvarchar(255),
    [payment_date] datetimeoffset(6),
    [payment_gateway] nvarchar(50),
    [payment_details] nvarchar(max)
);

CREATE TABLE [wishlists] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [user_id] uniqueidentifier,
    [product_id] uniqueidentifier,
    [created_at] datetimeoffset(6)
);

CREATE TABLE [shipping] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [order_id] uniqueidentifier,
    [shipping_method] nvarchar(100),
    [tracking_number] nvarchar(100),
    [status] nvarchar(50),
    [estimated_delivery] datetimeoffset(6),
    [actual_delivery] datetimeoffset(6),
    [shipping_address] nvarchar(max)
);

CREATE TABLE [reviews] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [product_id] uniqueidentifier,
    [user_id] uniqueidentifier,
    [order_item_id] uniqueidentifier,
    [rating] int,
    [title] nvarchar(255),
    [comment] nvarchar(max),
    [images] text,
    [created_at] datetimeoffset(6)
);

CREATE TABLE [inventory_logs] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [product_variant_id] uniqueidentifier,
    [quantity_change] int,
    [reference_type] nvarchar(50),
    [reference_id] uniqueidentifier,
    [note] nvarchar(255),
    [created_at] datetimeoffset(6)
);

CREATE TABLE [flash_sales] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [name] nvarchar(255),
    [start_time] datetimeoffset(6),
    [end_time] datetimeoffset(6),
    [is_active] bit DEFAULT (1)
);

CREATE TABLE [flash_sale_items] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [flash_sale_id] uniqueidentifier,
    [product_variant_id] uniqueidentifier,
    [discount_percent] decimal(5, 2),
    [quantity_limit] int,
    [stock] int
);

CREATE TABLE [notifications] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [user_id] uniqueidentifier,
    [title] nvarchar(255),
    [message] nvarchar(max),
    [is_read] bit DEFAULT (0),
    [type] nvarchar(50),
    [related_id] uniqueidentifier,
    [created_at] datetimeoffset(6)
);

CREATE TABLE [chats] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [sender_id] uniqueidentifier,
    [receiver_id] uniqueidentifier,
    [product_id] uniqueidentifier,
    [last_message] nvarchar(max),
    [last_message_at] datetimeoffset(6),
    [created_at] datetimeoffset(6)
);

CREATE TABLE [messages] (
    [id] uniqueidentifier PRIMARY KEY DEFAULT NEWID(),
    [chat_id] uniqueidentifier,
    [sender_id] uniqueidentifier,
    [content] nvarchar(max),
    [is_read] bit DEFAULT (0),
    [created_at] datetimeoffset(6)
);

-- FOREIGN KEYS
ALTER TABLE [shop_stats]
ADD FOREIGN KEY ([shop_id]) REFERENCES [shops] ([id]);

ALTER TABLE [products]
ADD FOREIGN KEY ([shop_id]) REFERENCES [shops] ([id]);

ALTER TABLE [shops]
ADD FOREIGN KEY ([user_id]) REFERENCES [users] ([id]);

ALTER TABLE [wishlists]
ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id]);

ALTER TABLE [wishlists]
ADD FOREIGN KEY ([user_id]) REFERENCES [users] ([id]);

ALTER TABLE [order_status_history]
ADD FOREIGN KEY ([order_id]) REFERENCES [orders] ([id]);

ALTER TABLE [inventory]
ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id]);

ALTER TABLE [user_logins]
ADD FOREIGN KEY ([user_id]) REFERENCES [users] ([id]);

ALTER TABLE [orders]
ADD FOREIGN KEY ([user_id]) REFERENCES [users] ([id]);

ALTER TABLE [order_items]
ADD FOREIGN KEY ([order_id]) REFERENCES [orders] ([id]);

ALTER TABLE [order_items]
ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id]);

ALTER TABLE [payments]
ADD FOREIGN KEY ([order_id]) REFERENCES [orders] ([id]);

ALTER TABLE [shipping]
ADD FOREIGN KEY ([order_id]) REFERENCES [orders] ([id]);

ALTER TABLE [reviews]
ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id]);

ALTER TABLE [reviews]
ADD FOREIGN KEY ([user_id]) REFERENCES [users] ([id]);

ALTER TABLE [reviews]
ADD FOREIGN KEY ([order_item_id]) REFERENCES [order_items] ([id]);

ALTER TABLE [inventory_logs]
ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id]);

ALTER TABLE [flash_sale_items]
ADD FOREIGN KEY ([flash_sale_id]) REFERENCES [flash_sales] ([id]);

ALTER TABLE [flash_sale_items]
ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id]);

ALTER TABLE [notifications]
ADD FOREIGN KEY ([user_id]) REFERENCES [users] ([id]);

ALTER TABLE [chats]
ADD FOREIGN KEY ([sender_id]) REFERENCES [users] ([id]);

ALTER TABLE [chats]
ADD FOREIGN KEY ([receiver_id]) REFERENCES [users] ([id]);

ALTER TABLE [chats]
ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id]);

ALTER TABLE [messages]
ADD FOREIGN KEY ([chat_id]) REFERENCES [chats] ([id]);

ALTER TABLE [messages]
ADD FOREIGN KEY ([sender_id]) REFERENCES [users] ([id]);

ALTER TABLE [categories]
ADD FOREIGN KEY ([parent_id]) REFERENCES [categories] ([id]);

ALTER TABLE [products]
ADD FOREIGN KEY ([category_id]) REFERENCES [categories] ([id]);

ALTER TABLE [product_variants]
ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id]);

ALTER TABLE [products]
ADD FOREIGN KEY ([brand_id]) REFERENCES [brands] ([id]);

ALTER TABLE [promotion_products]
ADD FOREIGN KEY ([promotion_id]) REFERENCES [promotions] ([id]);

ALTER TABLE [promotion_products]
ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id]);

ALTER TABLE [promotion_products]
ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id]);

ALTER TABLE [coupon_products]
ADD FOREIGN KEY ([coupon_id]) REFERENCES [coupons] ([id]);

ALTER TABLE [coupon_products]
ADD FOREIGN KEY ([product_id]) REFERENCES [products] ([id]);

ALTER TABLE [coupon_products]
ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id]);

ALTER TABLE [product_images]
ADD FOREIGN KEY ([product_variant_id]) REFERENCES [product_variants] ([id]);