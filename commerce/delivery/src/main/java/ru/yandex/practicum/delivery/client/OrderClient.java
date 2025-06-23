package ru.yandex.practicum.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "order")
public interface OrderClient {

    @PostMapping("/api/v1/order/delivery")
    void markDelivered(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    void markDeliveryFailed(@RequestBody UUID orderId);
}

