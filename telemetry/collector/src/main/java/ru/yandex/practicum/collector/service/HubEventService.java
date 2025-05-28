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
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {

    private final Producer<String, SpecificRecordBase> kafkaProducer;

    public void handleHubEvent(HubEventProto proto) {
        String hubId = proto.getHubId();
        String payloadType = proto.getPayloadCase().name();
        log.info("📨 Получен HubEventProto: hubId={}, payloadType={}, timestamp={}", hubId, payloadType, proto.getTimestamp());

        HubEvent event;
        Instant ts = Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos());

        switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventProto d = proto.getDeviceAdded();
                log.debug("→ Маппинг DEVICE_ADDED: id={}, type={}", d.getId(), d.getType());

                DeviceAddedEvent model = new DeviceAddedEvent();
                model.setId(d.getId());
                model.setDeviceType(DeviceType.valueOf(d.getType().name()));
                event = model;
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEventProto d = proto.getDeviceRemoved();
                log.debug("→ Маппинг DEVICE_REMOVED: id={}", d.getId());

                DeviceRemovedEvent model = new DeviceRemovedEvent();
                model.setId(d.getId());
                event = model;
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEventProto s = proto.getScenarioAdded();
                log.debug("→ Маппинг SCENARIO_ADDED: name={}, conditions={}, actions={}",
                        s.getName(), s.getConditionCount(), s.getActionCount());

                ScenarioAddedEvent model = new ScenarioAddedEvent();
                model.setName(s.getName());

                List<ScenarioCondition> mappedConditions = s.getConditionList().stream()
                        .map(p -> {
                            ScenarioCondition c = new ScenarioCondition();
                            c.setSensorId(p.getSensorId());
                            c.setType(ConditionType.valueOf(p.getType().name()));
                            c.setOperation(ConditionOperation.valueOf(p.getOperation().name()));

                            if (p.getValueCase() == null || p.getValueCase() == ScenarioConditionProto.ValueCase.VALUE_NOT_SET) {
                                c.setValue(null);
                                log.debug("⚠️ Условие без значения: sensorId={}", p.getSensorId());
                            } else {
                                switch (p.getValueCase()) {
                                    case INT_VALUE -> c.setValue(p.getIntValue());
                                    case BOOL_VALUE -> c.setValue(p.getBoolValue() ? 1 : 0);
                                }
                            }

                            return c;
                        }).collect(Collectors.toList());

                List<DeviceAction> mappedActions = s.getActionList().stream()
                        .map(p -> {
                            DeviceAction a = new DeviceAction();
                            a.setSensorId(p.getSensorId());
                            a.setType(ActionType.valueOf(p.getType().name()));
                            a.setValue(p.hasValue() ? p.getValue() : null);
                            return a;
                        }).collect(Collectors.toList());

                model.setConditions(mappedConditions);
                model.setActions(mappedActions);
                event = model;
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventProto s = proto.getScenarioRemoved();
                log.debug("→ Маппинг SCENARIO_REMOVED: name={}", s.getName());

                ScenarioRemovedEvent model = new ScenarioRemovedEvent();
                model.setName(s.getName());
                event = model;
            }
            default -> {
                log.warn("❌ Неизвестный тип HubEvent: {}", proto.getPayloadCase());
                return;
            }
        }

        event.setHubId(proto.getHubId());
        event.setTimestamp(ts);

        processEvent(event);
    }

    public void processEvent(HubEvent event) {
        HubEventAvro avro = mapToAvro(event);
        log.info("📤 Отправка HubEvent в Kafka: hubId={}, type={}, payload={}",
                event.getHubId(), event.getType(), avro.getPayload().getClass().getSimpleName());

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

                List<ScenarioConditionAvro> conditions = e.getConditions() != null
                        ? e.getConditions().stream()
                        .map(c -> ScenarioConditionAvro.newBuilder()
                                .setSensorId(c.getSensorId())
                                .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                                .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                                .setValue(c.getValue())
                                .build())
                        .toList()
                        : List.of();

                List<DeviceActionAvro> actions = e.getActions() != null
                        ? e.getActions().stream()
                        .map(a -> DeviceActionAvro.newBuilder()
                                .setSensorId(a.getSensorId())
                                .setType(ActionTypeAvro.valueOf(a.getType().name()))
                                .setValue(a.getValue())
                                .build())
                        .toList()
                        : List.of();

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