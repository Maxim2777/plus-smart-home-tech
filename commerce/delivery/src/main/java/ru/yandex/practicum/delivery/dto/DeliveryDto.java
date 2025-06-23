package ru.yandex.practicum.delivery.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDto {
    private UUID deliveryId;
    private UUID orderId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private double weight;
    private double volume;
    private boolean fragile;
    private String deliveryStatus;
}

