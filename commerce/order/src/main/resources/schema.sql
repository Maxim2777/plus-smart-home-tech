DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS order_products CASCADE;

CREATE TABLE orders (
    order_id UUID PRIMARY KEY,
    shopping_cart_id UUID,
    delivery_id UUID,
    payment_id UUID,
    state VARCHAR(64),
    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile BOOLEAN,
    product_price NUMERIC,
    delivery_price NUMERIC,
    total_price NUMERIC
);

CREATE TABLE order_products (
    order_id UUID,
    product_id UUID,
    quantity BIGINT,
    PRIMARY KEY (order_id, product_id)
);
