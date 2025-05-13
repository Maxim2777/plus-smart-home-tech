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

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void processEvent(SensorEvent event) {
        try {
            SensorEventAvro avro = mapToAvro(event);
            kafkaTemplate.send("telemetry.sensors.v1", avro.getId(), avro);
        } catch (Exception e) {
            log.error("Ошибка при обработке события SensorEvent: {}", event, e);
            throw e;
        }
    }

    private SensorEventAvro mapToAvro(SensorEvent event) {
        long timestamp = event.getTimestamp() != null ? event.getTimestamp().toEpochMilli() : Instant.now().toEpochMilli();

        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(timestamp);

        switch (event.getType()) {
            case LIGHT_SENSOR_EVENT -> {
                LightSensorEvent light = (LightSensorEvent) event;
                LightSensorAvro payload = LightSensorAvro.newBuilder()
                        .setLinkQuality(light.getLinkQuality())
                        .setLuminosity(light.getLuminosity())
                        .build();
                builder.setPayload(payload);
            }
            case MOTION_SENSOR_EVENT -> {
                MotionSensorEvent motion = (MotionSensorEvent) event;
                MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                        .setLinkQuality(motion.getLinkQuality())
                        .setMotion(motion.isMotion())
                        .setVoltage(motion.getVoltage())
                        .build();
                builder.setPayload(payload);
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                TemperatureSensorEvent temp = (TemperatureSensorEvent) event;
                TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(temp.getTemperatureC())
                        .setTemperatureF(temp.getTemperatureF())
                        .build();
                builder.setPayload(payload);
            }
            case CLIMATE_SENSOR_EVENT -> {
                ClimateSensorEvent climate = (ClimateSensorEvent) event;
                ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climate.getTemperatureC())
                        .setHumidity(climate.getHumidity())
                        .setCo2Level(climate.getCo2Level())
                        .build();
                builder.setPayload(payload);
            }
            case SWITCH_SENSOR_EVENT -> {
                SwitchSensorEvent sw = (SwitchSensorEvent) event;
                SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                        .setState(sw.isState())
                        .build();
                builder.setPayload(payload);
            }
            default -> throw new IllegalArgumentException("Unknown sensor event type: " + event.getType());
        }

        return builder.build();
    }
}