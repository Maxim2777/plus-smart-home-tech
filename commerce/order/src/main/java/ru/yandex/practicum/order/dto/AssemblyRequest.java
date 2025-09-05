package ru.yandex.practicum.order.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssemblyRequest {
    private UUID orderId;
    private Map<UUID, Integer> products;
}

