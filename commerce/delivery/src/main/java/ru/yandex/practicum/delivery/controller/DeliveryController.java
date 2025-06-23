package ru.yandex.practicum.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.delivery.dto.DeliveryDto;
import ru.yandex.practicum.delivery.service.DeliveryService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService service;

    @PutMapping
    public DeliveryDto createDelivery(@RequestBody DeliveryDto dto) {
        return service.createDelivery(dto);
    }

    @PostMapping("/cost")
    public double calculateDeliveryCost(@RequestBody DeliveryDto dto) {
        return service.calculateCost(dto);
    }

    @PostMapping("/picked")
    public DeliveryDto markPicked(@RequestBody UUID orderId) {
        return service.markPicked(orderId);
    }

    @PostMapping("/successful")
    public DeliveryDto markDelivered(@RequestBody UUID orderId) {
        return service.markDelivered(orderId);
    }

    @PostMapping("/failed")
    public DeliveryDto markFailed(@RequestBody UUID orderId) {
        return service.markFailed(orderId);
    }
}
