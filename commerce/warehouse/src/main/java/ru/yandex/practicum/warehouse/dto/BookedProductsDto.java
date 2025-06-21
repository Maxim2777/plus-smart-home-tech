package ru.yandex.practicum.warehouse.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookedProductsDto {
    private double deliveryWeight;
    private double deliveryVolume;
    private boolean fragile;
}