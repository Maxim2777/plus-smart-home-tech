package ru.yandex.practicum.shoppingcart.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeProductQuantityRequest {
    private UUID productId;
    private long newQuantity;
}

