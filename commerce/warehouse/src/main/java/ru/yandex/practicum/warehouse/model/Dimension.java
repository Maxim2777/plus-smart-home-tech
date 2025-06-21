package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dimension {
    private double width;
    private double height;
    private double depth;
}

