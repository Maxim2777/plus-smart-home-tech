package ru.yandex.practicum.order.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartDto {
    private UUID shoppingCartId;
    private Map<UUID, Integer> products;
}

