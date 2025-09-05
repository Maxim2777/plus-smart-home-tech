package ru.yandex.practicum.payment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private UUID paymentId;
    private UUID orderId;
    private BigDecimal productCost;
    private BigDecimal deliveryCost;
    private BigDecimal totalCost;
    private String status;
}
