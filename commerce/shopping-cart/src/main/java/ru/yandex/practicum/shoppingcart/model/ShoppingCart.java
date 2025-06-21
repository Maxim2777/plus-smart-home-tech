package ru.yandex.practicum.shoppingcart.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCart {

    @Id
    private UUID shoppingCartId;

    @Column(nullable = false)
    private String username;

    @ElementCollection
    @CollectionTable(name = "cart_products")
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Long> products;

    @Enumerated(EnumType.STRING)
    private ShoppingCartState state;
}

