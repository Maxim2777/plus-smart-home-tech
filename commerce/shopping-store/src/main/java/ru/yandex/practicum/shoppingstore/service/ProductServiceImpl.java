package ru.yandex.practicum.shoppingstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.shoppingstore.dto.ProductDto;
import ru.yandex.practicum.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.shoppingstore.model.Product;
import ru.yandex.practicum.shoppingstore.model.ProductCategory;
import ru.yandex.practicum.shoppingstore.model.ProductState;
import ru.yandex.practicum.shoppingstore.model.SetProductQuantityStateRequest;
import ru.yandex.practicum.shoppingstore.repository.ProductRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    public Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable) {
        return repository.findAllByProductCategory(category, pageable)
                .map(mapper::toDto);
    }

    @Override
    public Optional<ProductDto> getProductById(UUID productId) {
        return repository.findById(productId).map(mapper::toDto);
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        Product product = mapper.toEntity(productDto);

        // Если productState явно не задан — ставим ACTIVE
        if (product.getProductState() == null) {
            product.setProductState(ProductState.ACTIVE);
        }

        return mapper.toDto(repository.save(product));
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        if (productDto.getProductId() == null || !repository.existsById(productDto.getProductId())) {
            throw new IllegalArgumentException("Product not found");
        }
        Product updated = repository.save(mapper.toEntity(productDto));
        return mapper.toDto(updated);
    }

    @Override
    public boolean removeProductFromStore(UUID productId) {
        return repository.findById(productId).map(product -> {
            product.setProductState(ProductState.DEACTIVATE);
            repository.save(product);
            return true;
        }).orElse(false);
    }

    @Override
    public boolean updateProductQuantityState(SetProductQuantityStateRequest request) {
        return repository.findById(request.getProductId()).map(product -> {
            product.setQuantityState(request.getQuantityState());
            repository.save(product);
            return true;
        }).orElse(false);
    }
}

