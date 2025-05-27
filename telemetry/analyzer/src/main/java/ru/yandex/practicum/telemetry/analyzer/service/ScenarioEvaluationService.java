package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.analyzer.mapper.DeviceActionRequestMapper;
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

    @Transactional
    public void evaluateAndExecute(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.info("Нет сценариев для хаба {}", hubId);
            return;
        }

        log.info("🔍 Получено {} сценариев для хаба {}", scenarios.size(), hubId);
        for (Scenario scenario : scenarios) {
            log.info("🧪 Проверяю сценарий '{}'", scenario.getName());
            scenario.getConditions().forEach((sensorId, condition) -> {
                SensorStateAvro state = states.get(sensorId);
                if (state == null) {
                    log.warn("❌ Нет состояния сенсора {} в снапшоте", sensorId);
                } else {
                    log.info("✅ Состояние сенсора {}: тип = {}", sensorId, state.getData().getClass().getSimpleName());
                }
            });
        }

        for (Scenario scenario : scenarios) {
            boolean matched = scenario.getConditions().entrySet().stream().allMatch(entry -> {
                String sensorId = entry.getKey();
                Condition condition = entry.getValue();
                SensorStateAvro state = states.get(sensorId);
                if (state == null) return false;
                return evaluateCondition(condition, state);
            });

            if (matched) {
                log.info("🎯 Сценарий '{}' активирован", scenario.getName());
                scenario.getActions().forEach((sensorId, action) -> {
                    DeviceActionRequest request = DeviceActionRequestMapper.map(scenario, hubId, sensorId, action);
                    hubRouterClient.sendAction(request);
                });
            }
        }
    }

    private boolean evaluateCondition(Condition condition, SensorStateAvro state) {
        Integer actual = extractValueFromSensor(state);
        Integer expected = condition.getValueInt();

        log.info("🔍 Проверка условия: actual = {}, expected = {}, operation = {}", actual, expected, condition.getOperation());

        if (actual == null || expected == null) {
            log.warn("⚠️ Не удалось выполнить сравнение: actual или expected = null");
            return false;
        }

        return switch (condition.getOperation()) {
            case "EQUALS" -> actual.equals(expected);
            case "GREATER_THAN" -> actual > expected;
            case "LOWER_THAN" -> actual < expected;
            default -> {
                log.warn("⚠️ Неизвестная операция сравнения: {}", condition.getOperation());
                yield false;
            }
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