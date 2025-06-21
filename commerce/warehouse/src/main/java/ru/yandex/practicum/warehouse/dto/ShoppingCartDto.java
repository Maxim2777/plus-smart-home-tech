package ru.yandex.practicum.warehouse.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartDto {
    private UUID shoppingCartId;
    private Map<UUID, Long> products;
}