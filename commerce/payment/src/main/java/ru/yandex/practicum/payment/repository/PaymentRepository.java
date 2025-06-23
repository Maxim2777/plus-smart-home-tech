package ru.yandex.practicum.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.payment.entity.PaymentEntity;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByPaymentId(UUID paymentId);
}

