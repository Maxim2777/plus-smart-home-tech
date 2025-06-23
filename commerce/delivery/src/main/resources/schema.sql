DROP TABLE IF EXISTS deliveries CASCADE;

CREATE TABLE deliveries (
    delivery_id UUID PRIMARY KEY,
    order_id UUID,

    -- fromAddress
    country VARCHAR(128),
    city VARCHAR(128),
    street VARCHAR(128),
    house VARCHAR(64),
    flat VARCHAR(64),

    -- toAddress
    to_country VARCHAR(128),
    to_city VARCHAR(128),
    to_street VARCHAR(128),
    to_house VARCHAR(64),
    to_flat VARCHAR(64),

    weight DOUBLE PRECISION,
    volume DOUBLE PRECISION,
    fragile BOOLEAN,
    delivery_status VARCHAR(64)
);
