package ru.yandex.practicum.shoppingstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.shoppingstore.dto.ProductDto;
import ru.yandex.practicum.shoppingstore.model.ProductCategory;
import ru.yandex.practicum.shoppingstore.model.SetProductQuantityStateRequest;

import java.util.Optional;
import java.util.UUID;

public interface ProductService {

    Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable);

    Optional<ProductDto> getProductById(UUID productId);

    ProductDto createProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    boolean removeProductFromStore(UUID productId);

    boolean updateProductQuantityState(SetProductQuantityStateRequest request);
}