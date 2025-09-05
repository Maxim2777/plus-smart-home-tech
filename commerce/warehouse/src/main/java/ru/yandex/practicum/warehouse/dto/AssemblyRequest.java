package ru.yandex.practicum.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssemblyRequest {
    private UUID orderId;
    private Map<UUID, Integer> products; // productId -> quantity
}