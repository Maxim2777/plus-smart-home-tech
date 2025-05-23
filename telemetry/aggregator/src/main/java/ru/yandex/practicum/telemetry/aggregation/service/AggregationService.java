package ru.yandex.practicum.telemetry.aggregation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AggregationService {

    // Храним снапшоты по hubId
    private final Map<String, SensorsSnapshotAvro> snapshotStorage = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        SensorsSnapshotAvro snapshot = snapshotStorage.getOrDefault(
                hubId,
                SensorsSnapshotAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(event.getTimestamp())
                        .setSensorsState(new HashMap<>())
                        .build()
        );

        SensorStateAvro oldState = snapshot.getSensorsState() != null
                ? snapshot.getSensorsState().get(sensorId)
                : null;

// если событие старое или данные не изменились — ничего не делаем
        if (oldState != null) {
            boolean isOld = oldState.getTimestamp() >= event.getTimestamp();
            boolean isSame = oldState.getData().equals(event.getPayload());
            if (isOld || isSame) {
                return Optional.empty();
            }
        }

// Инициализируем карту, если она пуста
        if (snapshot.getSensorsState() == null) {
            snapshot.setSensorsState(new HashMap<>());
        }

// обновляем снапшот
        snapshot.getSensorsState().put(sensorId, newState);
        snapshot.setTimestamp(event.getTimestamp());
        snapshotStorage.put(hubId, snapshot);

        log.debug("Обновлён снапшот хаба {}: сенсор {} → {}", hubId, sensorId, newState.getData().getClass().getSimpleName());
        return Optional.of(snapshot);
    }
}