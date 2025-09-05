package ru.yandex.practicum.delivery.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequest {
    private UUID orderId;
    private UUID deliveryId;
}

