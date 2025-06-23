package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.order.dto.AddressDto;
import ru.yandex.practicum.order.dto.BookedProductsDto;
import ru.yandex.practicum.order.dto.ShoppingCartDto;

import java.util.UUID;

@FeignClient(name = "warehouse")
public interface WarehouseClient {

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assembleProducts(@RequestBody AssemblyRequest request);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();
}
