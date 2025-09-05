package ru.yandex.practicum.payment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue
    private UUID paymentId;

    private UUID orderId;

    private BigDecimal productCost;
    private BigDecimal deliveryCost;
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}

