package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseProduct {

    @Id
    private UUID productId;

    @Embedded
    private Dimension dimension;

    private double weight;

    private boolean fragile;

    private long quantity;
}
