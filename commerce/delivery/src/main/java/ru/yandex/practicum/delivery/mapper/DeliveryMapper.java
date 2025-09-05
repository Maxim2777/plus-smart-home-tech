package ru.yandex.practicum.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.delivery.dto.*;
import ru.yandex.practicum.delivery.entity.*;

@Component
public class DeliveryMapper {

    public DeliveryDto toDto(DeliveryEntity entity) {
        return DeliveryDto.builder()
                .deliveryId(entity.getDeliveryId())
                .orderId(entity.getOrderId())
                .fromAddress(toDto(entity.getFromAddress()))
                .toAddress(toDto(entity.getToAddress()))
                .weight(entity.getWeight())
                .volume(entity.getVolume())
                .fragile(entity.isFragile())
                .deliveryStatus(entity.getDeliveryStatus().name())
                .build();
    }

    public DeliveryEntity fromDto(DeliveryDto dto) {
        return DeliveryEntity.builder()
                .deliveryId(dto.getDeliveryId())
                .orderId(dto.getOrderId())
                .fromAddress(toEntity(dto.getFromAddress()))
                .toAddress(toEntity(dto.getToAddress()))
                .weight(dto.getWeight())
                .volume(dto.getVolume())
                .fragile(dto.isFragile())
                .deliveryStatus(DeliveryStatus.valueOf(dto.getDeliveryStatus()))
                .build();
    }

    private AddressDto toDto(Address address) {
        return new AddressDto(
                address.getCountry(),
                address.getCity(),
                address.getStreet(),
                address.getHouse(),
                address.getFlat()
        );
    }

    private Address toEntity(AddressDto dto) {
        return new Address(
                dto.getCountry(),
                dto.getCity(),
                dto.getStreet(),
                dto.getHouse(),
                dto.getFlat()
        );
    }
}
