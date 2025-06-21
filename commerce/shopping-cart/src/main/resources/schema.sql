DROP TABLE IF EXISTS cart_products CASCADE;
DROP TABLE IF EXISTS shopping_cart CASCADE;

CREATE TABLE shopping_cart (
    shopping_cart_id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    state VARCHAR(255) CHECK (state IN ('ACTIVE', 'DEACTIVATED'))
);

CREATE TABLE cart_products (
    shopping_cart_shopping_cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT,
    PRIMARY KEY (shopping_cart_shopping_cart_id, product_id),
    CONSTRAINT fk_cart_products_cart FOREIGN KEY (shopping_cart_shopping_cart_id)
        REFERENCES shopping_cart (shopping_cart_id)
);