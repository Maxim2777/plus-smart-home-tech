package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment")
public interface PaymentClient {

    @PostMapping("/api/v1/payment/productCost")
    BigDecimal calculateProductCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/totalCost")
    BigDecimal calculateTotalCost(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment")
    OrderDto createPayment(@RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/refund")
    void markPaymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/api/v1/payment/failed")
    void markPaymentFailed(@RequestBody UUID paymentId);
}