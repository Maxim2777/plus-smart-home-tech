package ru.yandex.practicum.collector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.collector.service.SensorEventService;
import ru.yandex.practicum.collector.model.SensorEvent;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/sensors")
public class SensorEventController {

    private final SensorEventService sensorEventService;

    @PostMapping
    public ResponseEntity<Void> collectSensorEvent(@RequestBody SensorEvent event) {
        sensorEventService.processEvent(event);
        return ResponseEntity.ok().build();
    }
}

