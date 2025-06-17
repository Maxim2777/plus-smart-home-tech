package ru.yandex.practicum.warehouse.dto;

import lombok.*;
import ru.yandex.practicum.warehouse.model.Dimension;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewProductInWarehouseRequest {
    private UUID productId;
    private boolean fragile;
    private Dimension dimension;
    private double weight;
}
