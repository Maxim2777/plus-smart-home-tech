package ru.yandex.practicum.collector.model.sensor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemperatureSensorEvent extends SensorEvent {

    private int temperatureC;
    private int temperatureF;

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}

