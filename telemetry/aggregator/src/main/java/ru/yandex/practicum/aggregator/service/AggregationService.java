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

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>(); // key = hubId

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {

        String hubId = event.getHubId();
        String sensorId = event.getId();

        log.info("Получено событие от датчика: hubId={}, sensorId={}, timestamp={}, payload={}",
                hubId, sensorId, event.getTimestamp(), event.getPayload());

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, hubIdKey -> {
            log.info("Создаю новый снапшот для hubId={}", hubIdKey);
            SensorsSnapshotAvro newSnapshot = new SensorsSnapshotAvro();
            newSnapshot.setHubId(hubIdKey);
            newSnapshot.setTimestamp(event.getTimestamp());
            newSnapshot.setSensorsState(new HashMap<>());
            return newSnapshot;
        });

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState(); // key = sensorId
        SensorStateAvro oldState = sensorsState.get(sensorId);

        if (oldState != null) {
            boolean isOlderTimestamp = event.getTimestamp().isBefore(oldState.getTimestamp());
            boolean isSameData = event.getPayload().equals(oldState.getData());

            if (isOlderTimestamp || isSameData) {
                log.info("Игнорируем событие: старое или дублирующее. hubId={}, sensorId={}, eventTimestamp={}",
                        hubId, sensorId, event.getTimestamp());
                return Optional.empty();
            }
        }

        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(event.getTimestamp());
        newState.setData(event.getPayload());

        sensorsState.put(sensorId, newState);
        snapshot.setTimestamp(event.getTimestamp());

        log.info("Обновлён снапшот hubId={}, sensorId={}, newTimestamp={}, newData={}",
                hubId, sensorId, newState.getTimestamp(), newState.getData());

        return Optional.of(snapshot);
    }
}
