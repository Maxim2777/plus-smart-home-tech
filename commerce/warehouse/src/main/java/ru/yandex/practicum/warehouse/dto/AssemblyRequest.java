package ru.yandex.practicum.warehouse.dto;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class AssemblyRequest {
    private Map<UUID, Integer> products; // productId -> quantity
}