package ru.yandex.practicum.shoppingcart.service;

import ru.yandex.practicum.shoppingcart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.shoppingcart.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto getCart(String username);
    ShoppingCartDto addProducts(String username, Map<UUID, Long> productsToAdd);
    ShoppingCartDto removeProducts(String username, List<UUID> productIds);
    ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request);
    void deactivateCart(String username);
}

