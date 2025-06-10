package ru.yandex.practicum.telemetry.analyzer.mapper;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;

import java.time.Instant;

@Slf4j
public class DeviceActionRequestMapper {

    public static DeviceActionRequest map(Scenario scenario, String hubId, String sensorId, Action action) {
        log.debug("🛠️ Маппинг DeviceActionRequest: hubId={}, sensorId={}, scenario={}, actionType={}, value={}",
                hubId, sensorId, scenario.getName(), action.getType(), action.getValue());

        DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(action.getType()));

        if (action.getValue() != null) {
            actionBuilder.setValue(action.getValue());
        }

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenario.getName())
                .setAction(actionBuilder.build())
                .setTimestamp(currentTimestamp())
                .build();

        log.trace("📦 DeviceActionRequest создан: {}", request);

        return request;
    }

    private static Timestamp currentTimestamp() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }
}