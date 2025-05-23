package ru.yandex.practicum.telemetry.aggregation.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class AggregatorProducerConfig {
    private Map<String, String> properties;
    private Topics topics;

    @Getter
    @Setter
    @ToString
    public static class Topics {
        private String snapshotsEvents;
    }
}

