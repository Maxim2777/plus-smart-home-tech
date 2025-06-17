DROP TABLE IF EXISTS product CASCADE;

CREATE TABLE product (
    product_id UUID PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_src VARCHAR(255),
    quantity_state VARCHAR(32) NOT NULL,
    product_state VARCHAR(32) NOT NULL,
    product_category VARCHAR(32) NOT NULL,
    price NUMERIC(19, 2) NOT NULL
);