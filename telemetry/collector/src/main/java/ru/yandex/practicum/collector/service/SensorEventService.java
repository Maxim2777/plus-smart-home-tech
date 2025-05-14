package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.model.*;
import ru.yandex.practicum.collector.model.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorEventService {

    private final KafkaTemplate<String, SensorEventAvro> kafkaTemplate;

    public void processEvent(SensorEvent event) {
        SensorEventAvro avro = mapToAvro(event);
        log.info("📤 Отправка SensorEvent в Kafka. Payload: {}", avro.getPayload().getClass().getSimpleName());
        kafkaTemplate.send("telemetry.sensors.v1", avro.getId(), avro);
    }

    private SensorEventAvro mapToAvro(SensorEvent event) {
        long timestamp = event.getTimestamp() != null ? event.getTimestamp().toEpochMilli() : Instant.now().toEpochMilli();

        Object payload = switch (event.getType()) {
            case LIGHT_SENSOR_EVENT -> LightSensorAvro.newBuilder()
                    .setLinkQuality(((LightSensorEvent) event).getLinkQuality())
                    .setLuminosity(((LightSensorEvent) event).getLuminosity())
                    .build();
            case MOTION_SENSOR_EVENT -> MotionSensorAvro.newBuilder()
                    .setLinkQuality(((MotionSensorEvent) event).getLinkQuality())
                    .setMotion(((MotionSensorEvent) event).isMotion())
                    .setVoltage(((MotionSensorEvent) event).getVoltage())
                    .build();
            case TEMPERATURE_SENSOR_EVENT -> TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(((TemperatureSensorEvent) event).getTemperatureC())
                    .setTemperatureF(((TemperatureSensorEvent) event).getTemperatureF())
                    .build();
            case CLIMATE_SENSOR_EVENT -> ClimateSensorAvro.newBuilder()
                    .setTemperatureC(((ClimateSensorEvent) event).getTemperatureC())
                    .setHumidity(((ClimateSensorEvent) event).getHumidity())
                    .setCo2Level(((ClimateSensorEvent) event).getCo2Level())
                    .build();
            case SWITCH_SENSOR_EVENT -> SwitchSensorAvro.newBuilder()
                    .setState(((SwitchSensorEvent) event).isState())
                    .build();
        };

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}