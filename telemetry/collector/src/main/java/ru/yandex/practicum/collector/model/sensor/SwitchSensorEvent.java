package ru.yandex.practicum.collector.model.sensor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwitchSensorEvent extends SensorEvent {

    private boolean state;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}
