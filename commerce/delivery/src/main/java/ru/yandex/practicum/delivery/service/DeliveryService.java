package ru.yandex.practicum.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.delivery.client.OrderClient;
import ru.yandex.practicum.delivery.client.WarehouseClient;
import ru.yandex.practicum.delivery.dto.DeliveryDto;
import ru.yandex.practicum.delivery.dto.ShipmentRequest;
import ru.yandex.practicum.delivery.entity.DeliveryEntity;
import ru.yandex.practicum.delivery.entity.DeliveryStatus;
import ru.yandex.practicum.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository repository;
    private final DeliveryMapper mapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    public DeliveryDto createDelivery(DeliveryDto dto) {
        DeliveryEntity entity = mapper.fromDto(dto);
        entity.setDeliveryStatus(DeliveryStatus.CREATED);
        return mapper.toDto(repository.save(entity));
    }

    public double calculateCost(DeliveryDto dto) {
        double cost = 5.0;

        if (dto.getFromAddress().getStreet().contains("ADDRESS_2")) {
            cost = cost * 2 + 5;
        } else if (dto.getFromAddress().getStreet().contains("ADDRESS_1")) {
            cost = cost * 1 + 5;
        }

        if (dto.isFragile()) {
            cost += cost * 0.2;
        }

        cost += dto.getWeight() * 0.3;
        cost += dto.getVolume() * 0.2;

        if (!dto.getFromAddress().getStreet().equalsIgnoreCase(dto.getToAddress().getStreet())) {
            cost += cost * 0.2;
        }

        return Math.round(cost * 100.0) / 100.0;
    }

    public DeliveryDto markPicked(UUID orderId) {
        DeliveryEntity delivery = getByOrder(orderId);
        delivery.setDeliveryStatus(DeliveryStatus.IN_PROGRESS);

        // Уведомляем заказ и склад
        orderClient.markDelivered(orderId);
        warehouseClient.markAsShipped(new ShipmentRequest(orderId, delivery.getDeliveryId()));

        return mapper.toDto(repository.save(delivery));
    }

    public DeliveryDto markDelivered(UUID orderId) {
        DeliveryEntity delivery = getByOrder(orderId);
        delivery.setDeliveryStatus(DeliveryStatus.DELIVERED);

        // Уведомляем заказ
        orderClient.markDelivered(orderId);

        return mapper.toDto(repository.save(delivery));
    }

    public DeliveryDto markFailed(UUID orderId) {
        DeliveryEntity delivery = getByOrder(orderId);
        delivery.setDeliveryStatus(DeliveryStatus.FAILED);

        // Уведомляем заказ
        orderClient.markDeliveryFailed(orderId);

        return mapper.toDto(repository.save(delivery));
    }

    private DeliveryEntity getByOrder(UUID orderId) {
        return repository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found for order: " + orderId));
    }
}

