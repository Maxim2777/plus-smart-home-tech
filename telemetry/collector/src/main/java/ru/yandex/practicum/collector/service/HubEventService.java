package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {

    private final Producer<String, SpecificRecordBase> kafkaProducer;

    public void processEvent(HubEvent event) {
        HubEventAvro avro = mapToAvro(event);
        log.info("HubEvent отправляется в Kafka с payload: {}", avro.getPayload().getClass().getSimpleName());
        kafkaProducer.send(new ProducerRecord<>("telemetry.hubs.v1", avro.getHubId(), avro));
    }

    private HubEventAvro mapToAvro(HubEvent event) {
        long timestamp = event.getTimestamp() != null ? event.getTimestamp().toEpochMilli() : Instant.now().toEpochMilli();

        SpecificRecord payload = switch (event.getType()) {
            case DEVICE_ADDED -> {
                DeviceAddedEvent e = (DeviceAddedEvent) event;
                yield DeviceAddedEventAvro.newBuilder()
                        .setId(e.getId())
                        .setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()))
                        .build();
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEvent e = (DeviceRemovedEvent) event;
                yield DeviceRemovedEventAvro.newBuilder()
                        .setId(e.getId())
                        .build();
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

                yield ScenarioAddedEventAvro.newBuilder()
                        .setName(e.getName())
                        .setConditions(conditions)
                        .setActions(actions)
                        .build();
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent e = (ScenarioRemovedEvent) event;
                yield ScenarioRemovedEventAvro.newBuilder()
                        .setName(e.getName())
                        .build();
            }
        };

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}