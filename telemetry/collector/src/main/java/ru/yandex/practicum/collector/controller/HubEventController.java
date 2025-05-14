package ru.yandex.practicum.collector.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.service.HubEventService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/hubs")
public class HubEventController {

    private final HubEventService hubEventService;

    @PostMapping
    public ResponseEntity<String> collectHubEvent(@RequestBody HubEvent event) {
        try {
            hubEventService.processEvent(event);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка в /events/hubs при обработке события: {}", event, e);
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}