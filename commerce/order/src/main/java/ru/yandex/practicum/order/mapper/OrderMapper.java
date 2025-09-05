package ru.yandex.practicum.order.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.entity.OrderEntity;
import ru.yandex.practicum.order.entity.OrderState;

@Component
public class OrderMapper {

    public OrderDto toDto(OrderEntity entity) {
        return OrderDto.builder()
                .orderId(entity.getOrderId())
                .shoppingCartId(entity.getShoppingCartId())
                .products(entity.getProducts())
                .deliveryId(entity.getDeliveryId())
                .paymentId(entity.getPaymentId())
                .state(entity.getState().name())
                .deliveryWeight(entity.getDeliveryWeight())
                .deliveryVolume(entity.getDeliveryVolume())
                .fragile(entity.isFragile())
                .productPrice(entity.getProductPrice())
                .deliveryPrice(entity.getDeliveryPrice())
                .totalPrice(entity.getTotalPrice())
                .build();
    }

    public OrderEntity fromDto(OrderDto dto) {
        return OrderEntity.builder()
                .orderId(dto.getOrderId())
                .shoppingCartId(dto.getShoppingCartId())
                .products(dto.getProducts())
                .deliveryId(dto.getDeliveryId())
                .paymentId(dto.getPaymentId())
                .state(dto.getState() != null ? OrderState.valueOf(dto.getState()) : null)
                .deliveryWeight(dto.getDeliveryWeight())
                .deliveryVolume(dto.getDeliveryVolume())
                .fragile(dto.isFragile())
                .productPrice(dto.getProductPrice())
                .deliveryPrice(dto.getDeliveryPrice())
                .totalPrice(dto.getTotalPrice())
                .build();
    }
}
