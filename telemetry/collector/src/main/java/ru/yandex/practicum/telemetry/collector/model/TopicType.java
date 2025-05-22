package ru.yandex.practicum.telemetry.collector.model;

public enum TopicType {
    SENSOR_EVENTS,
    HUBS_EVENTS;

    public static TopicType fromString(String value) {
        return switch (value.toLowerCase()) {
            case "sensor-events" -> SENSOR_EVENTS;
            case "hubs-events" -> HUBS_EVENTS;
            default -> throw new IllegalArgumentException("Unknown topic type: " + value);
        };
    }
}
