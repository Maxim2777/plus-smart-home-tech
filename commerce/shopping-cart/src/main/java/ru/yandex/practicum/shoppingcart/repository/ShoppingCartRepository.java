package ru.yandex.practicum.shoppingcart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.shoppingcart.model.ShoppingCart;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, java.util.UUID> {
    Optional<ShoppingCart> findByUsername(String username);
}

