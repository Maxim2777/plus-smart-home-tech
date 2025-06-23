package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.client.DeliveryClient;
import ru.yandex.practicum.order.client.PaymentClient;
import ru.yandex.practicum.order.client.WarehouseClient;
import ru.yandex.practicum.order.dto.BookedProductsDto;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.entity.OrderEntity;
import ru.yandex.practicum.order.entity.OrderState;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;
import ru.yandex.practicum.order.client.AssemblyRequest;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;
    private final WarehouseClient warehouseClient;

    public OrderDto createOrder(OrderDto dto) {
        OrderEntity entity = mapper.fromDto(dto);
        entity.setState(OrderState.NEW); // <-- установить начальное состояние
        OrderEntity saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    public List<OrderDto> getOrdersByCartId(UUID cartId) {
        return repository.findAllByShoppingCartId(cartId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public OrderDto getById(UUID id) {
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    public OrderDto markAsAssembled(UUID orderId) {
        OrderEntity order = getOrder(orderId);

        BookedProductsDto booked = warehouseClient.assembleProducts(
                new AssemblyRequest(orderId, order.getProducts())
        );

        order.setState(OrderState.ASSEMBLED);
        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.isFragile());

        return mapper.toDto(repository.save(order));
    }

    public OrderDto markAssemblyFailed(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        return mapper.toDto(repository.save(order));
    }

    public OrderDto markAsPaid(UUID orderId) {
        OrderEntity order = getOrder(orderId);

        OrderDto result = paymentClient.createPayment(mapper.toDto(order));
        order.setPaymentId(result.getPaymentId());
        order.setState(OrderState.PAID);

        return mapper.toDto(repository.save(order));
    }

    public OrderDto markPaymentFailed(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        paymentClient.markPaymentFailed(order.getPaymentId());
        order.setState(OrderState.PAYMENT_FAILED);
        return mapper.toDto(repository.save(order));
    }

    public OrderDto markAsDelivered(UUID orderId) {
        OrderEntity order = getOrder(orderId);

        OrderDto deliveryResult = deliveryClient.planDelivery(mapper.toDto(order));
        order.setDeliveryId(deliveryResult.getDeliveryId());
        order.setState(OrderState.DELIVERED);

        return mapper.toDto(repository.save(order));
    }

    public OrderDto markDeliveryFailed(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        deliveryClient.markDeliveryFailed(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        return mapper.toDto(repository.save(order));
    }

    public OrderDto markAsCompleted(UUID orderId) {
        return mapper.toDto(changeStatus(orderId, OrderState.COMPLETED));
    }

    public OrderDto markAsReturned(UUID orderId) {
        return mapper.toDto(changeStatus(orderId, OrderState.PRODUCT_RETURNED));
    }

    public OrderDto calculateDelivery(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        BigDecimal cost = deliveryClient.calculateDeliveryCost(mapper.toDto(order));
        order.setDeliveryPrice(cost);
        return mapper.toDto(repository.save(order));
    }

    public OrderDto calculateTotal(UUID orderId) {
        OrderEntity order = getOrder(orderId);

        BigDecimal productPrice = paymentClient.calculateProductCost(mapper.toDto(order));
        BigDecimal total = paymentClient.calculateTotalCost(mapper.toDto(order));

        order.setProductPrice(productPrice);
        order.setTotalPrice(total);

        return mapper.toDto(repository.save(order));
    }

    private OrderEntity changeStatus(UUID orderId, OrderState state) {
        OrderEntity order = getOrder(orderId);
        order.setState(state);
        return repository.save(order);
    }

    private OrderEntity getOrder(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }
}