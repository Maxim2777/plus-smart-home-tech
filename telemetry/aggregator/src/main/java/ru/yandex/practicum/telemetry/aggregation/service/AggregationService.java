package ru.yandex.practicum.telemetry.aggregation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import java.time.Instant;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AggregationService {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    private static final String SNAPSHOT_TOPIC = "telemetry.snapshots.v1";
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    @KafkaListener(topics = "telemetry.sensors.v1", groupId = "aggregator-group")
    public void handleSensorEvent(SensorEventAvro event) {
        updateState(event).ifPresent(snapshot -> {
            log.info("📤 Отправка снапшота для хаба {}", snapshot.getHubId());
            kafkaTemplate.send(SNAPSHOT_TOPIC, snapshot.getHubId(), snapshot);
        });
    }


    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        Instant eventTime = Instant.ofEpochMilli(event.getTimestamp());

        SensorsSnapshotAvro snapshot = snapshots.getOrDefault(hubId,
                SensorsSnapshotAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(eventTime)
                        .setSensorsState(new HashMap<>())
                        .build()
        );

        SensorStateAvro oldState = snapshot.getSensorsState().get(sensorId);

        if (oldState != null) {
            if (!eventTime.isAfter(oldState.getTimestamp())) return Optional.empty();
            if (oldState.getData().equals(event.getPayload())) return Optional.empty();
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventTime)
                .setData(event.getPayload())
                .build();

        snapshot.getSensorsState().put(sensorId, newState);
        snapshot.setTimestamp(eventTime);
        snapshots.put(hubId, snapshot);

        return Optional.of(snapshot);
    }
}