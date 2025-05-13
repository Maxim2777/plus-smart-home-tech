package ru.yandex.practicum.collector.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.service.HubEventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/hubs")
public class HubEventController {

    private final HubEventService hubEventService;

    @PostMapping
    public ResponseEntity<Void> collectHubEvent(@RequestBody HubEvent event) {
        hubEventService.processEvent(event);
        return ResponseEntity.ok().build();
    }
}

