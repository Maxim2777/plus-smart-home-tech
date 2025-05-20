package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.collector.model.sensor.*;
import ru.yandex.practicum.grpc.telemetry.event.SensorEvent.SensorEventProto;

import java.time.Instant;

@Component
public class SensorEventMapper {

    public SensorEvent fromProto(SensorEventProto proto) {
        return switch (proto.getPayloadCase()) {
            case MOTION_SENSOR -> {
                MotionSensorEvent e = new MotionSensorEvent();
                var p = proto.getMotionSensor();
                e.setMotion(p.getMotion());
                e.setVoltage(p.getVoltage());
                e.setLinkQuality(p.getLinkQuality());
                fillCommon(e, proto);
                yield e;
            }
            case TEMPERATURE_SENSOR -> {
                TemperatureSensorEvent e = new TemperatureSensorEvent();
                var p = proto.getTemperatureSensor();
                e.setTemperatureC(p.getTemperatureC());
                e.setTemperatureF(p.getTemperatureF());
                fillCommon(e, proto);
                yield e;
            }
            case LIGHT_SENSOR -> {
                LightSensorEvent e = new LightSensorEvent();
                var p = proto.getLightSensor();
                e.setLuminosity(p.getLuminosity());
                e.setLinkQuality(p.getLinkQuality());
                fillCommon(e, proto);
                yield e;
            }
            case CLIMATE_SENSOR -> {
                ClimateSensorEvent e = new ClimateSensorEvent();
                var p = proto.getClimateSensor();
                e.setTemperatureC(p.getTemperatureC());
                e.setHumidity(p.getHumidity());
                e.setCo2Level(p.getCo2Level());
                fillCommon(e, proto);
                yield e;
            }
            case SWITCH_SENSOR -> {
                SwitchSensorEvent e = new SwitchSensorEvent();
                var p = proto.getSwitchSensor();
                e.setState(p.getState());
                fillCommon(e, proto);
                yield e;
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Payload отсутствует");
        };
    }

    private void fillCommon(SensorEvent target, SensorEventProto proto) {
        target.setId(proto.getId());
        target.setHubId(proto.getHubId());
        target.setTimestamp(Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        ));
    }
}

