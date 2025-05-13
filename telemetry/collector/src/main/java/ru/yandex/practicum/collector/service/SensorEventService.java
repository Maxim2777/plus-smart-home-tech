package ru.yandex.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.model.*;
import ru.yandex.practicum.collector.model.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;


import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void processEvent(SensorEvent event) {
        try {
            SensorEventAvro avro = mapToAvro(event);
            log.info("📤 Отправка события в Kafka: payload = {}", avro.getPayload().getClass().getName());
            kafkaTemplate.send("telemetry.sensors.v1", avro.getId(), avro);
        } catch (Exception e) {
            log.error("❌ Ошибка при обработке SensorEvent: {}", event, e);
            throw e;
        }
    }

    private SensorEventAvro mapToAvro(SensorEvent event) {
        long timestamp = event.getTimestamp() != null
                ? event.getTimestamp().toEpochMilli()
                : Instant.now().toEpochMilli();

        Object payload;

        switch (event.getType()) {
            case LIGHT_SENSOR_EVENT -> {
                LightSensorEvent light = (LightSensorEvent) event;
                payload = LightSensorAvro.newBuilder()
                        .setLinkQuality(light.getLinkQuality())
                        .setLuminosity(light.getLuminosity())
                        .build();
            }
            case MOTION_SENSOR_EVENT -> {
                MotionSensorEvent motion = (MotionSensorEvent) event;
                payload = MotionSensorAvro.newBuilder()
                        .setLinkQuality(motion.getLinkQuality())
                        .setMotion(motion.isMotion())
                        .setVoltage(motion.getVoltage())
                        .build();
            }
            case TEMPERATURE_SENSOR_EVENT -> {
                TemperatureSensorEvent temp = (TemperatureSensorEvent) event;
                payload = TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(temp.getTemperatureC())
                        .setTemperatureF(temp.getTemperatureF())
                        .build();
            }
            case CLIMATE_SENSOR_EVENT -> {
                ClimateSensorEvent climate = (ClimateSensorEvent) event;
                payload = ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climate.getTemperatureC())
                        .setHumidity(climate.getHumidity())
                        .setCo2Level(climate.getCo2Level())
                        .build();
            }
            case SWITCH_SENSOR_EVENT -> {
                SwitchSensorEvent sw = (SwitchSensorEvent) event;
                payload = SwitchSensorAvro.newBuilder()
                        .setState(sw.isState())
                        .build();
            }
            default -> throw new IllegalArgumentException("❌ Unknown sensor event type: " + event.getType());
        }

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(payload)  // важно: объект должен быть SpecificRecord!
                .build();
    }
}