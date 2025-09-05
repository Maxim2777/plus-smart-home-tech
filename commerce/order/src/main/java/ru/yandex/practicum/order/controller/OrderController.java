package ru.yandex.practicum.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PutMapping
    public OrderDto create(@RequestBody OrderDto dto) {
        return service.createOrder(dto);
    }

    @GetMapping
    public List<OrderDto> byCart(@RequestParam UUID cartId) {
        return service.getOrdersByCartId(cartId);
    }

    @GetMapping("/{id}")
    public OrderDto byId(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping("/assembly")
    public OrderDto assembly(@RequestBody UUID orderId) {
        return service.markAsAssembled(orderId);
    }

    @PostMapping("/assembly/failed")
    public OrderDto assemblyFailed(@RequestBody UUID orderId) {
        return service.markAssemblyFailed(orderId);
    }

    @PostMapping("/payment")
    public OrderDto payment(@RequestBody UUID orderId) {
        return service.markAsPaid(orderId);
    }

    @PostMapping("/payment/failed")
    public OrderDto paymentFailed(@RequestBody UUID orderId) {
        return service.markPaymentFailed(orderId);
    }

    @PostMapping("/delivery")
    public OrderDto delivery(@RequestBody UUID orderId) {
        return service.markAsDelivered(orderId);
    }

    @PostMapping("/delivery/failed")
    public OrderDto deliveryFailed(@RequestBody UUID orderId) {
        return service.markDeliveryFailed(orderId);
    }

    @PostMapping("/completed")
    public OrderDto complete(@RequestBody UUID orderId) {
        return service.markAsCompleted(orderId);
    }

    @PostMapping("/return")
    public OrderDto productReturn(@RequestBody UUID orderId) {
        return service.markAsReturned(orderId);
    }

    @PostMapping("/calculate/delivery")
    public OrderDto calculateDelivery(@RequestBody UUID orderId) {
        return service.calculateDelivery(orderId);
    }

    @PostMapping("/calculate/total")
    public OrderDto calculateTotal(@RequestBody UUID orderId) {
        return service.calculateTotal(orderId);
    }
}