package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.payment.dto.OrderDto;
import ru.yandex.practicum.payment.dto.PaymentDto;
import ru.yandex.practicum.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/productCost")
    public BigDecimal getProductCost(@RequestBody OrderDto order) {
        return service.calculateProductCost(order);
    }

    @PostMapping("/totalCost")
    public BigDecimal getTotalCost(@RequestBody OrderDto order) {
        return service.calculateTotalCost(order);
    }

    @PostMapping
    public PaymentDto createPayment(@RequestBody OrderDto order) {
        return service.createPayment(order);
    }

    @PostMapping("/refund")
    public void paymentSuccess(@RequestBody UUID paymentId) {
        service.markSuccess(paymentId);
    }

    @PostMapping("/failed")
    public void paymentFailed(@RequestBody UUID paymentId) {
        service.markFailed(paymentId);
    }
}

