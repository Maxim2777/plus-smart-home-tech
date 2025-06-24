package ru.yandex.practicum.warehouse.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRequest {
    private UUID orderId;
    private UUID deliveryId;
}