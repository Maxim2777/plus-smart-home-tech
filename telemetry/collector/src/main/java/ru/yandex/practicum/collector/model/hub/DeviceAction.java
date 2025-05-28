package ru.yandex.practicum.collector.model.hub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceAction {
    private String sensorId;
    private ActionType type;
    private Integer value; // Optional
}