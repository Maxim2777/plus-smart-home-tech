package ru.yandex.practicum.collector.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.collector.model.hub.ScenarioCondition;

import java.util.List;

@Component
public class ConditionsMapper {

    public static ScenarioConditionAvro sceCondToAvro(ScenarioCondition scenario) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(scenario.getSensorId())
                .setType(ConditionTypeAvro.valueOf(scenario.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(scenario.getOperation().name()));

        Object value = scenario.getValue();
        switch (value) {
            case Integer i -> builder.setValue(i);
            case Boolean b -> builder.setValue(b);
            default -> builder.setValue(null);
        }

        return builder.build();
    }

    public static List<ScenarioConditionAvro> condListToAvro(List<ScenarioCondition> conditions) {
        return conditions.stream()
                .map(ConditionsMapper::sceCondToAvro)
                .toList();
    }
}

