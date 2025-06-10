package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationService {

    private final Map<String, SensorsSnapshotAvro> snapshotsByHubId = new HashMap<>();

    public Optional<SensorsSnapshotAvro> aggregateEvent(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        log.debug("Получено событие от сенсора [{}] хаба [{}] с timestamp {}", sensorId, hubId, event.getTimestamp());

        SensorsSnapshotAvro hubSnapshot = snapshotsByHubId.computeIfAbsent(hubId, hubIdKey -> {
            log.info("Создание нового снимка для хаба [{}]", hubIdKey);
            SensorsSnapshotAvro newSnapshot = new SensorsSnapshotAvro();
            newSnapshot.setHubId(hubIdKey);
            newSnapshot.setTimestamp(event.getTimestamp());
            newSnapshot.setSensorsState(new HashMap<>());
            return newSnapshot;
        });

        Map<String, SensorStateAvro> sensorsState = hubSnapshot.getSensorsState();
        SensorStateAvro oldState = sensorsState.get(sensorId);

        if (oldState != null) {
            boolean isOlderTimestamp = event.getTimestamp().isBefore(oldState.getTimestamp());
            boolean isSameData = event.getPayload().equals(oldState.getData());

            if (isOlderTimestamp) {
                log.debug("Пропущено событие от сенсора [{}]: более старый timestamp", sensorId);
                return Optional.empty();
            }

            if (isSameData) {
                log.debug("Пропущено событие от сенсора [{}]: данные не изменились", sensorId);
                return Optional.empty();
            }
        }

        SensorStateAvro updatedSensorState = new SensorStateAvro();
        updatedSensorState.setTimestamp(event.getTimestamp());
        updatedSensorState.setData(event.getPayload());

        sensorsState.put(sensorId, updatedSensorState);
        hubSnapshot.setTimestamp(event.getTimestamp());

        log.info("Обновлён снимок для хаба [{}], сенсор [{}], новый timestamp: {}", hubId, sensorId, event.getTimestamp());

        return Optional.of(hubSnapshot);
    }
}