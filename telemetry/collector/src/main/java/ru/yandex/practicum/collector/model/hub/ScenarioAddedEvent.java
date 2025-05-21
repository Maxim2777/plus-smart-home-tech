package ru.yandex.practicum.collector.model.hub;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ScenarioAddedEvent extends HubEvent {
    private String name = "";  // ← важно инициализировать
    private List<ScenarioCondition> conditions = new ArrayList<>();
    private List<DeviceAction> actions = new ArrayList<>();

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}

