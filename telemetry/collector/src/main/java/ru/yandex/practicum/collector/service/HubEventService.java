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
        long timestamp = event.getTimestamp() != null
                ? event.getTimestamp().toEpochMilli()
                : Instant.now().toEpochMilli();

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

                log.info("Mapping SCENARIO_ADDED: name={}, conditions={}, actions={}",
                        e.getName(), e.getConditions(), e.getActions());

                List<ScenarioConditionAvro> conditions = e.getConditions().stream()
                        .map(c -> {
                            Object rawValue = c.getValue();

                            ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                                    .setSensorId(c.getSensorId())
                                    .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                                    .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()));

                            if (rawValue == null) {
                                log.info("✅ SC Condition: value=null");
                                builder.setValue(null);
                            } else if (rawValue instanceof Integer) {
                                log.info("✅ SC Condition: value=Integer({})", rawValue);
                                builder.setValue((Integer) rawValue);
                            } else if (rawValue instanceof Boolean) {
                                log.info("✅ SC Condition: value=Boolean({})", rawValue);
                                builder.setValue((Boolean) rawValue);
                            } else {
                                log.warn("⚠️ SC Condition: неподдерживаемый тип value={} (class={})", rawValue, rawValue.getClass().getName());
                                builder.setValue(null);
                            }

                            return builder.build();
                        })
                        .toList();

                List<DeviceActionAvro> actions = e.getActions().stream()
                        .map(a -> DeviceActionAvro.newBuilder()
                                .setSensorId(a.getSensorId())
                                .setType(ActionTypeAvro.valueOf(a.getType().name()))
                                .setValue(a.getValue())  // int или null
                                .build())
                        .toList();

                yield ScenarioAddedEventAvro.newBuilder()
                        .setName(e.getName() != null ? e.getName() : "")
                        .setConditions(conditions)
                        .setActions(actions)
                        .build();
            }

            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent e = (ScenarioRemovedEvent) event;
                yield ScenarioRemovedEventAvro.newBuilder()
                        .setName(e.getName() != null ? e.getName() : "")
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