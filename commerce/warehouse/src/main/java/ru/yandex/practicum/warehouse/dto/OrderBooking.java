package ru.yandex.practicum.warehouse.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBooking {
    private UUID orderId;
    private UUID deliveryId;
    private Map<UUID, Long> products;
}
