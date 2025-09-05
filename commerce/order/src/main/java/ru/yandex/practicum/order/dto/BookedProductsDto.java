package ru.yandex.practicum.order.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedProductsDto {
    private double deliveryWeight;
    private double deliveryVolume;
    private boolean fragile;
}

