package ru.yandex.practicum.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.delivery.dto.ShipmentRequest;

@FeignClient(name = "warehouse")
public interface WarehouseClient {

    @PostMapping("/api/v1/warehouse/shipped")
    void markAsShipped(@RequestBody ShipmentRequest request);
}

