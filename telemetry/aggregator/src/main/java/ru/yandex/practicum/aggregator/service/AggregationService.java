package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final Map<String, SensorsSnapshotAvro> snapshotsByHubId = new HashMap<>();

    public Optional<SensorsSnapshotAvro> aggregateEvent(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorsSnapshotAvro hubSnapshot = snapshotsByHubId.computeIfAbsent(hubId, id -> {
            SensorsSnapshotAvro snapshot = new SensorsSnapshotAvro();
            snapshot.setHubId(id);
            snapshot.setTimestamp(event.getTimestamp());
            snapshot.setSensorsState(new HashMap<>());
            return snapshot;
        });

        Map<String, SensorStateAvro> sensorsState = hubSnapshot.getSensorsState();
        SensorStateAvro existingState = sensorsState.get(sensorId);

        if (!shouldUpdateState(event, existingState)) {
            return Optional.empty();
        }

        SensorStateAvro updatedSensorState = new SensorStateAvro();
        updatedSensorState.setTimestamp(event.getTimestamp());
        updatedSensorState.setData(event.getPayload());

        sensorsState.put(sensorId, updatedSensorState);
        hubSnapshot.setTimestamp(event.getTimestamp());

        return Optional.of(hubSnapshot);
    }

    private boolean shouldUpdateState(SensorEventAvro event, SensorStateAvro existingState) {
        if (existingState == null) {
            return true;
        }

        boolean isNewer = event.getTimestamp().isAfter(existingState.getTimestamp());
        boolean isChanged = !event.getPayload().equals(existingState.getData());

        return isNewer && isChanged;
    }
}