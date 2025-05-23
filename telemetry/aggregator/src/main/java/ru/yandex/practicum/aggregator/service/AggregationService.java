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

    private final Map<String, SensorsSnapshotAvro> snapshotsByHubId = new HashMap<>(); // key = hubId

    public Optional<SensorsSnapshotAvro> aggregateEvent(SensorEventAvro event) {

        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorsSnapshotAvro hubSnapshot = snapshotsByHubId.computeIfAbsent(hubId, hubIdKey -> {
            SensorsSnapshotAvro newSnapshot = new SensorsSnapshotAvro();
            newSnapshot.setHubId(hubIdKey);
            newSnapshot.setTimestamp(event.getTimestamp());
            newSnapshot.setSensorsState(new HashMap<>());
            return newSnapshot;
        });

        Map<String, SensorStateAvro> sensorsState = hubSnapshot.getSensorsState(); // key = sensorId
        SensorStateAvro oldState = sensorsState.get(sensorId);

        if (oldState != null) {
            boolean isOlderTimestamp = event.getTimestamp().isBefore(oldState.getTimestamp());
            boolean isSameData = event.getPayload().equals(oldState.getData());

            if (isOlderTimestamp || isSameData) {
                return Optional.empty();
            }
        }

        SensorStateAvro updatedSensorState = new SensorStateAvro();
        updatedSensorState.setTimestamp(event.getTimestamp());
        updatedSensorState.setData(event.getPayload());

        sensorsState.put(sensorId, updatedSensorState);
        hubSnapshot.setTimestamp(event.getTimestamp());

        return Optional.of(hubSnapshot);
    }
}
