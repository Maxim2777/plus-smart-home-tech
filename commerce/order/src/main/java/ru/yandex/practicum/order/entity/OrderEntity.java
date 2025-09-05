package ru.yandex.practicum.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue
    private UUID orderId;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Integer> products;

    private UUID shoppingCartId;
    private UUID deliveryId;
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    private OrderState state;

    private double deliveryWeight;
    private double deliveryVolume;
    private boolean fragile;

    private BigDecimal productPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal totalPrice;
}