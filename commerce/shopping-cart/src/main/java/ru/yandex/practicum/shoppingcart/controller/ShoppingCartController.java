package ru.yandex.practicum.shoppingcart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.shoppingcart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.shoppingcart.dto.ShoppingCartDto;
import ru.yandex.practicum.shoppingcart.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService service;

    @GetMapping
    public ShoppingCartDto getCart(@RequestParam String username) {
        return service.getCart(username);
    }

    @PutMapping
    public ShoppingCartDto addProduct(@RequestParam String username,
                                      @RequestBody Map<UUID, Long> productsToAdd) {
        return service.addProducts(username, productsToAdd);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeProducts(@RequestParam String username,
                                          @RequestBody List<UUID> productIds) {
        return service.removeProducts(username, productIds);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeQuantity(@RequestParam String username,
                                          @RequestBody ChangeProductQuantityRequest request) {
        return service.changeQuantity(username, request);
    }

    @DeleteMapping
    public void deactivateCart(@RequestParam String username) {
        service.deactivateCart(username);
    }
}

