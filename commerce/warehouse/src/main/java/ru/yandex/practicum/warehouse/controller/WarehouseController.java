package ru.yandex.practicum.warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.warehouse.dto.*;
import ru.yandex.practicum.warehouse.service.WarehouseService;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    // PUT /api/v1/warehouse
    @PutMapping
    public void registerNewProduct(@RequestBody NewProductInWarehouseRequest request) {
        service.registerNewProduct(request);
    }

    // POST /api/v1/warehouse/add
    @PostMapping("/add")
    public void addProductQuantity(@RequestBody AddProductToWarehouseRequest request) {
        service.addProductQuantity(request);
    }

    // POST /api/v1/warehouse/check
    @PostMapping("/check")
    public BookedProductsDto checkAvailability(@RequestBody ShoppingCartDto cart) {
        return service.checkAvailabilityAndBook(cart);
    }

    // GET /api/v1/warehouse/address
    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        return service.getWarehouseAddress();
    }

    @PostMapping("/assembly")
    public BookedProductsDto assemble(@RequestBody AssemblyRequest request) {
        return service.assembleProducts(request);
    }

    @PostMapping("/shipped")
    public void markAsShipped(@RequestBody ShipmentRequest request) {
        service.markAsShipped(request);
    }

    @PostMapping("/return")
    public void returnProducts(@RequestBody ReturnRequest request) {
        service.returnProducts(request);
    }
}