DROP TABLE IF EXISTS payments CASCADE;

CREATE TABLE payments (
    payment_id UUID PRIMARY KEY,
    order_id UUID,
    product_cost NUMERIC,
    delivery_cost NUMERIC,
    total_cost NUMERIC,
    status VARCHAR(64)
);