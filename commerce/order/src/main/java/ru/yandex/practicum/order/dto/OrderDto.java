package ru.yandex.practicum.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private UUID orderId;
    private UUID shoppingCartId;
    private Map<UUID, Integer> products;
    private UUID deliveryId;
    private UUID paymentId;
    private String state;
    private double deliveryWeight;
    private double deliveryVolume;
    private boolean fragile;
    private BigDecimal productPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal totalPrice;
}