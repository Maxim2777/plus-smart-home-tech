package ru.yandex.practicum.telemetry.analyzer.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @EqualsAndHashCode
public class ScenarioActionId implements Serializable {
    private Long scenario;
    private String sensor;
    private Long action;
}

