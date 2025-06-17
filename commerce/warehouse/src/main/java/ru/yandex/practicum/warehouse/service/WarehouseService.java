package ru.yandex.practicum.warehouse.service;

import ru.yandex.practicum.warehouse.dto.*;

public interface WarehouseService {

    void registerNewProduct(NewProductInWarehouseRequest request);

    void addProductQuantity(AddProductToWarehouseRequest request);

    BookedProductsDto checkAvailabilityAndBook(ShoppingCartDto cart);

    AddressDto getWarehouseAddress();
}