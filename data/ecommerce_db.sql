-- Before creating tables, remember to create the database first
-- Example: CREATE DATABASE ecommerce_db;
-- USE ecommerce_db;
CREATE TABLE users (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    full_name NVARCHAR(100) NULL,
    birthday DATE NULL,
    gender BIT NULL,
    avatar_url VARCHAR(500) NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    role VARCHAR(50) DEFAULT 'USER' NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(13) NULL,
    address NVARCHAR(500) NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (email),
    UNIQUE (phone)
);

CREATE TABLE categories (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    description NVARCHAR(500) NULL,
    name_ NVARCHAR(100) NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE(),
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE(),
    image_url VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE (name_)
);

CREATE TABLE shops (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    name_shop NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX) NULL,
    avatar_url VARCHAR(500) NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    cccd VARCHAR(50) NOT NULL,
    cccd_front_url VARCHAR(500) NOT NULL,
    cccd_back_url VARCHAR(500) NOT NULL,
    status BIT DEFAULT 0 NOT NULL,
    reason NVARCHAR(MAX) NULL,
    is_seller BIT DEFAULT 0 NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE (user_id),
    UNIQUE (cccd)
);

CREATE TABLE products (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    name_product NVARCHAR(500) NOT NULL,
    description NVARCHAR(MAX) NULL,
    price DECIMAL(18, 2) NOT NULL,
    quantity INT DEFAULT 0 NOT NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    category_id UNIQUEIDENTIFIER NOT NULL,
    shop_id UNIQUEIDENTIFIER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES categories (id),
    FOREIGN KEY (shop_id) REFERENCES shops (id)
);

CREATE TABLE products_images (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    image_url VARCHAR(500) NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updated_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    product_id UNIQUEIDENTIFIER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE product_attributes (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    attribute_name NVARCHAR(100) NOT NULL,
    attribute_value NVARCHAR(255) NOT NULL,
    product_id UNIQUEIDENTIFIER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE product_variants (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    price_ DECIMAL(18, 2) NOT NULL,
    quantity_ INT DEFAULT 0 NOT NULL,
    image_url VARCHAR(500) NULL,
    created_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    updatet_at DATETIMEOFFSET(6) DEFAULT GETDATE() NULL,
    is_deleted BIT DEFAULT 0 NOT NULL,
    product_id UNIQUEIDENTIFIER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE variant_attributes (
    id UNIQUEIDENTIFIER DEFAULT NEWID() NOT NULL,
    attribute_name NVARCHAR(100) NOT NULL,
    attribute_value NVARCHAR(255) NOT NULL,
    variant_id UNIQUEIDENTIFIER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (variant_id) REFERENCES product_variants (id)
);