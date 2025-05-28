package ru.yandex.practicum.telemetry.analyzer.mapper;

import com.google.protobuf.Timestamp;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;

import java.time.Instant;
import java.util.List;

public class DeviceActionRequestMapper {

    public static DeviceActionRequest map(Scenario scenario, String hubId, String sensorId, Action action) {
        DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(action.getType()));

        if (action.getValue() != null) {
            actionBuilder.setValue(action.getValue());
        }

        return DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenario.getName())
                .setAction(actionBuilder.build())
                .setTimestamp(currentTimestamp())
                .build();
    }

    public static List<DeviceActionRequest> mapAll(Scenario scenario, String hubId) {
        return scenario.getActions().entrySet().stream()
                .map(entry -> map(scenario, hubId, entry.getKey(), entry.getValue()))
                .toList();
    }

    private static Timestamp currentTimestamp() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }
}