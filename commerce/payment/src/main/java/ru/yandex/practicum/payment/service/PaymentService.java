package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.payment.client.ShoppingStoreClient;
import ru.yandex.practicum.payment.dto.OrderDto;
import ru.yandex.practicum.payment.dto.PaymentDto;
import ru.yandex.practicum.payment.entity.PaymentEntity;
import ru.yandex.practicum.payment.entity.PaymentStatus;
import ru.yandex.practicum.payment.mapper.PaymentMapper;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final ShoppingStoreClient storeClient;

    public BigDecimal calculateProductCost(OrderDto order) {
        return order.getProducts().entrySet().stream()
                .map(entry -> {
                    UUID productId = entry.getKey();
                    int quantity = entry.getValue();
                    BigDecimal price = storeClient.getProduct(productId).getPrice();
                    return price.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalCost(OrderDto order) {
        BigDecimal productCost = calculateProductCost(order);
        BigDecimal vat = productCost.multiply(BigDecimal.valueOf(0.10));
        BigDecimal delivery = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : BigDecimal.valueOf(50);

        return productCost.add(vat).add(delivery);
    }

    public PaymentDto createPayment(OrderDto order) {
        BigDecimal productCost = calculateProductCost(order);
        BigDecimal delivery = order.getDeliveryPrice() != null ? order.getDeliveryPrice() : BigDecimal.valueOf(50);
        BigDecimal vat = productCost.multiply(BigDecimal.valueOf(0.10));
        BigDecimal total = productCost.add(vat).add(delivery);

        PaymentEntity entity = PaymentEntity.builder()
                .orderId(order.getOrderId())
                .productCost(productCost)
                .deliveryCost(delivery)
                .totalCost(total)
                .status(PaymentStatus.PENDING)
                .build();

        return mapper.toDto(repository.save(entity));
    }

    public void markSuccess(UUID paymentId) {
        PaymentEntity payment = repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        payment.setStatus(PaymentStatus.SUCCESS);
        repository.save(payment);
    }

    public void markFailed(UUID paymentId) {
        PaymentEntity payment = repository.findByPaymentId(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        payment.setStatus(PaymentStatus.FAILED);
        repository.save(payment);
    }
}

