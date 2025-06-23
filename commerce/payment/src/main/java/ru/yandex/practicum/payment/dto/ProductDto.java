package ru.yandex.practicum.payment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private UUID productId;
    private String name;
    private BigDecimal price;
}