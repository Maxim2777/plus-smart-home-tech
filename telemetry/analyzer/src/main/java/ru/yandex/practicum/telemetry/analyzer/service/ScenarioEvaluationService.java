package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.service.grpc.HubRouterClient;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioEvaluationService {

    private final ScenarioRepository scenarioRepository;
    private final HubRouterClient hubRouterClient;

    public void evaluateAndExecute(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.info("Нет сценариев для хаба {}", hubId);
            return;
        }

        for (Scenario scenario : scenarios) {
            boolean matched = scenario.getConditions().stream().allMatch(conditionLink -> {
                SensorStateAvro state = states.get(conditionLink.getSensor().getId());
                if (state == null) return false;
                return evaluateCondition(conditionLink.getCondition(), state);
            });

            if (matched) {
                log.info("🎯 Сценарий '{}' активирован", scenario.getName());

                for (ScenarioAction actionLink : scenario.getActions()) {
                    DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                            .setSensorId(actionLink.getSensor().getId())
                            .setType(ActionTypeProto.valueOf(actionLink.getAction().getType()))
                            .setValue(actionLink.getAction().getValue())
                            .build();

                    DeviceActionRequest request = DeviceActionRequest.newBuilder()
                            .setHubId(hubId)
                            .setScenarioName(scenario.getName())
                            .setAction(actionProto)
                            .setTimestamp(toProtoTimestamp(snapshot.getTimestamp().toEpochMilli()))
                            .build();

                    hubRouterClient.sendAction(request);
                }
            }
        }
    }

    private boolean evaluateCondition(Condition condition, SensorStateAvro state) {
        Integer actual = extractValueFromSensor(state);
        if (actual == null) return false;

        return switch (condition.getOperation()) {
            case "EQUALS" -> actual.equals(condition.getValue());
            case "GREATER_THAN" -> actual > condition.getValue();
            case "LOWER_THAN" -> actual < condition.getValue();
            default -> false;
        };
    }

    private Integer extractValueFromSensor(SensorStateAvro state) {
        switch (state.getData().getClass().getSimpleName()) {
            case "MotionSensorAvro":
                return ((MotionSensorAvro) state.getData()).getMotion() ? 1 : 0;
            case "TemperatureSensorAvro":
                return ((TemperatureSensorAvro) state.getData()).getTemperatureC();
            case "LightSensorAvro":
                return ((LightSensorAvro) state.getData()).getLuminosity();
            case "SwitchSensorAvro":
                return ((SwitchSensorAvro) state.getData()).getState() ? 1 : 0;
            case "ClimateSensorAvro":
                return ((ClimateSensorAvro) state.getData()).getCo2Level(); // или другое значение
            default:
                log.warn("⚠️ Неизвестный тип сенсора: {}", state.getData().getClass().getSimpleName());
                return null;
        }
    }

    private Timestamp toProtoTimestamp(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}