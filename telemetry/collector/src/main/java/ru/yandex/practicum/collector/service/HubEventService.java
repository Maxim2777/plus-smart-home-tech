package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HubEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void processEvent(HubEvent event) {
        HubEventAvro avro = mapToAvro(event);
        kafkaTemplate.send("telemetry.hubs.v1", avro.getHubId(), avro);
    }

    private HubEventAvro mapToAvro(HubEvent event) {
        long timestamp = event.getTimestamp() != null ? event.getTimestamp().toEpochMilli() : Instant.now().toEpochMilli();

        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(timestamp);

        switch (event.getType()) {
            case DEVICE_ADDED -> {
                DeviceAddedEvent e = (DeviceAddedEvent) event;
                DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                        .setId(e.getId())
                        .setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()))
                        .build();
                builder.setPayload(payload);
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEvent e = (DeviceRemovedEvent) event;
                DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                        .setId(e.getId())
                        .build();
                builder.setPayload(payload);
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEvent e = (ScenarioAddedEvent) event;

                List<ScenarioConditionAvro> conditions = e.getConditions().stream()
                        .map(c -> ScenarioConditionAvro.newBuilder()
                                .setSensorId(c.getSensorId())
                                .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                                .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                                .setValue(c.getValue())
                                .build())
                        .toList();

                List<DeviceActionAvro> actions = e.getActions().stream()
                        .map(a -> DeviceActionAvro.newBuilder()
                                .setSensorId(a.getSensorId())
                                .setType(ActionTypeAvro.valueOf(a.getType().name()))
                                .setValue(a.getValue())
                                .build())
                        .toList();

                ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                        .setName(e.getName())
                        .setConditions(conditions)
                        .setActions(actions)
                        .build();

                builder.setPayload(payload);
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent e = (ScenarioRemovedEvent) event;
                ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                        .setName(e.getName())
                        .build();
                builder.setPayload(payload);
            }
            default -> throw new IllegalArgumentException("Unknown hub event type: " + event.getType());
        }

        return builder.build();
    }
}

