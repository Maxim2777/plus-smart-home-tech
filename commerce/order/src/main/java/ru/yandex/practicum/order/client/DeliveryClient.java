package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "delivery")
public interface DeliveryClient {

    @PostMapping("/api/v1/delivery/cost")
    BigDecimal calculateDeliveryCost(@RequestBody OrderDto order);

    @PutMapping("/api/v1/delivery")
    OrderDto planDelivery(@RequestBody OrderDto order);

    @PostMapping("/api/v1/delivery/successful")
    void markDelivered(@RequestBody UUID orderId);

    @PostMapping("/api/v1/delivery/failed")
    void markDeliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/delivery/picked")
    void markPicked(@RequestBody UUID orderId);
}

