package ru.yandex.practicum.payment.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.payment.dto.PaymentDto;
import ru.yandex.practicum.payment.entity.PaymentEntity;
import ru.yandex.practicum.payment.entity.PaymentStatus;

@Component
public class PaymentMapper {

    public PaymentDto toDto(PaymentEntity entity) {
        return PaymentDto.builder()
                .paymentId(entity.getPaymentId())
                .orderId(entity.getOrderId())
                .productCost(entity.getProductCost())
                .deliveryCost(entity.getDeliveryCost())
                .totalCost(entity.getTotalCost())
                .status(entity.getStatus().name())
                .build();
    }

    public PaymentEntity fromDto(PaymentDto dto) {
        return PaymentEntity.builder()
                .paymentId(dto.getPaymentId())
                .orderId(dto.getOrderId())
                .productCost(dto.getProductCost())
                .deliveryCost(dto.getDeliveryCost())
                .totalCost(dto.getTotalCost())
                .status(PaymentStatus.valueOf(dto.getStatus()))
                .build();
    }
}

