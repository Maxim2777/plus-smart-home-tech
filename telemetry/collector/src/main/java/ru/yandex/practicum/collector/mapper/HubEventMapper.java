package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.hub.*;
import ru.yandex.practicum.grpc.telemetry.event.HubEvent.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEvent.DeviceAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEvent.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HubEventMapper {

    public HubEvent fromProto(HubEventProto proto) {
        HubEvent event;

        switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEventProto p = proto.getDeviceAdded();
                DeviceAddedEvent e = new DeviceAddedEvent();
                e.setId(p.getId()); // ✅ добавляем id
                e.setDeviceType(DeviceType.valueOf(p.getType().name()));
                event = e;
            }

            case DEVICE_REMOVED -> {
                DeviceRemovedEventProto p = proto.getDeviceRemoved();
                DeviceRemovedEvent e = new DeviceRemovedEvent();
                e.setId(p.getId()); // ✅
                event = e;
            }

            case SCENARIO_ADDED -> {
                ScenarioAddedEventProto p = proto.getScenarioAdded();
                ScenarioAddedEvent e = new ScenarioAddedEvent();

                e.setName(p.getName()); // ← используем правильное поле name

                List<ScenarioCondition> conditions = p.getConditionsList().stream()
                        .map(this::mapCondition)
                        .collect(Collectors.toList());
                e.setConditions(conditions);

                List<DeviceAction> actions = p.getActionsList().stream()
                        .map(this::mapAction)
                        .collect(Collectors.toList());
                e.setActions(actions);

                event = e;
            }

            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent e = new ScenarioRemovedEvent();
                event = e;
            }

            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Payload отсутствует");

            default -> throw new IllegalArgumentException("Неизвестный тип события: " + proto.getPayloadCase());
        }

        event.setHubId(proto.getHubId());
        event.setTimestamp(Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        ));

        return event;
    }

    private ScenarioCondition mapCondition(ScenarioConditionProto proto) {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId(
                proto.getSensorId() == null || proto.getSensorId().isBlank()
                        ? "unknown"
                        : proto.getSensorId()
        );
        condition.setType(ConditionType.valueOf(proto.getType().name()));
        condition.setOperation(ConditionOperation.valueOf(proto.getOperation().name()));
        condition.setValue(proto.getValue());
        return condition;
    }

    private DeviceAction mapAction(DeviceActionProto proto) {
        DeviceAction action = new DeviceAction();
        action.setSensorId(proto.getDeviceId());
        action.setType(ActionType.valueOf(proto.getAction().name()));
        action.setValue(proto.getValue());
        return action;
    }

}