CREATE TABLE users (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    role VARCHAR(50) DEFAULT 'USER' NOT NULL,
    is_seller BIT DEFAULT 0 NOT NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    full_name NVARCHAR(100) NULL,
    birthday DATE NULL,
    gender BIT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    phone VARCHAR(13) NULL,
    address NVARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE (email),
    UNIQUE (phone)
);

CREATE TABLE shops (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    name_shop NVARCHAR(255) NOT NULL,
    avatar_shop VARCHAR(500) NOT NULL,
    cccd VARCHAR(50) NOT NULL,
    cccd_front_url VARCHAR(500) NOT NULL,
    cccd_back_url VARCHAR(500) NOT NULL,
    status INT DEFAULT 0 NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    description NVARCHAR(MAX) NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    reason NVARCHAR(MAX) NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE (user_id),
    UNIQUE (cccd)
);

CREATE TABLE categories (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    name NVARCHAR(100) NOT NULL,
    parent_id UNIQUEIDENTIFIER NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (parent_id) REFERENCES categories (id)
);

CREATE TABLE products (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    shop_id UNIQUEIDENTIFIER NOT NULL,
    category_id UNIQUEIDENTIFIER NULL,
    name NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX) NULL,
    price DECIMAL(18, 2) NOT NULL,
    original_price DECIMAL(18, 2) NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    sold_quantity INT NOT NULL DEFAULT 0,
    is_deleted BIT DEFAULT 0 NOT NULL,
    short_description NVARCHAR(500) NULL,
    long_description NVARCHAR(MAX) NULL,
    material NVARCHAR(100) NULL,
    origin NVARCHAR(100) NULL,
    care_instructions NVARCHAR(500) NULL,
    style NVARCHAR(100) NULL,
    features NVARCHAR(MAX) NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (shop_id) REFERENCES shops (id),
    FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE product_variants (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    product_id UNIQUEIDENTIFIER NOT NULL,
    sku VARCHAR(100) NULL,
    price DECIMAL(18, 2) NULL,
    original_price DECIMAL(18, 2) NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    sold_quantity INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500) NULL,
    is_default BIT DEFAULT 0 NOT NULL,
    is_active BIT DEFAULT 1 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    UNIQUE (product_id, sku)
);

CREATE TABLE product_images (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    product_id UNIQUEIDENTIFIER NOT NULL,
    variant_id UNIQUEIDENTIFIER NULL,
    image_url VARCHAR(500) NOT NULL,
    is_primary BIT DEFAULT 0 NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (variant_id) REFERENCES product_variants (id)
);

CREATE TABLE product_attributes (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    name NVARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_deleted BIT DEFAULT 0 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE attribute_values (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    attribute_id UNIQUEIDENTIFIER NOT NULL,
    value NVARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_deleted BIT DEFAULT 0 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (attribute_id) REFERENCES product_attributes (id),
    UNIQUE (attribute_id, value)
);

CREATE TABLE variant_attribute_values (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    variant_id UNIQUEIDENTIFIER NOT NULL,
    attribute_value_id UNIQUEIDENTIFIER NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (variant_id) REFERENCES product_variants (id),
    FOREIGN KEY (attribute_value_id) REFERENCES attribute_values (id),
    UNIQUE (
        variant_id,
        attribute_value_id
    )
);

CREATE TABLE promotions (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    code VARCHAR(50) NOT NULL,
    discount_amount DECIMAL(18, 2) NULL,
    discount_percentage DECIMAL(5, 2) NULL,
    min_order_amount DECIMAL(18, 2) NULL,
    max_discount_amount DECIMAL(18, 2) NULL,
    start_date DATETIMEOFFSET(6) NOT NULL,
    end_date DATETIMEOFFSET(6) NOT NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    UNIQUE (code)
);

CREATE TABLE product_promotions (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    product_id UNIQUEIDENTIFIER NOT NULL,
    promotion_id UNIQUEIDENTIFIER NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (promotion_id) REFERENCES promotions (id),
    UNIQUE (product_id, promotion_id)
);

CREATE TABLE shipping_options (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,
    base_cost DECIMAL(18, 2) NOT NULL,
    free_shipping_threshold DECIMAL(18, 2) NULL,
    estimated_delivery_days INT NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_addresses (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    recipient_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(13) NOT NULL,
    address_line1 NVARCHAR(255) NOT NULL,
    address_line2 NVARCHAR(255) NULL,
    city NVARCHAR(100) NOT NULL,
    district NVARCHAR(100) NOT NULL,
    ward NVARCHAR(100) NOT NULL,
    is_default BIT DEFAULT 0 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE product_reviews (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    product_id UNIQUEIDENTIFIER NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    variant_id UNIQUEIDENTIFIER NULL,
    rating DECIMAL(2, 1) NOT NULL,
    comment NVARCHAR(MAX) NULL,
    is_approved BIT DEFAULT 0 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (variant_id) REFERENCES product_variants (id)
);

CREATE INDEX idx_products_shop ON products (shop_id);

CREATE INDEX idx_products_category ON products (category_id);

CREATE INDEX idx_variants_product ON product_variants (product_id);

CREATE INDEX idx_attr_values_attr ON attribute_values (attribute_id);

CREATE
INDEX idx_product_promotions ON product_promotions (product_id, promotion_id);

CREATE TABLE flash_sales (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    start_time DATETIMEOFFSET(6) NOT NULL,
    end_time DATETIMEOFFSET(6) NOT NULL,
    banner_url VARCHAR(500) NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL
);

CREATE TABLE flash_sale_products (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    flash_sale_id UNIQUEIDENTIFIER NOT NULL,
    product_id UNIQUEIDENTIFIER NOT NULL,
    variant_id UNIQUEIDENTIFIER NULL,
    discount_percent DECIMAL(5, 2) NOT NULL,
    original_price DECIMAL(18, 2) NOT NULL,
    sale_price DECIMAL(18, 2) NOT NULL,
    stock_limit INT NULL,
    sold_quantity INT DEFAULT 0 NOT NULL,
    display_order INT DEFAULT 0 NOT NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    FOREIGN KEY (flash_sale_id) REFERENCES flash_sales (id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (variant_id) REFERENCES product_variants (id)
);