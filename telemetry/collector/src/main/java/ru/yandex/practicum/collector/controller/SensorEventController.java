package ru.yandex.practicum.collector.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.collector.service.SensorEventService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/sensors")
public class SensorEventController {

    private final SensorEventService sensorEventService;

    @PostMapping
    public ResponseEntity<String> collectSensorEvent(@RequestBody SensorEvent event) {
        try {
            sensorEventService.processEvent(event);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка в /events/sensors при обработке события: {}", event, e);
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}