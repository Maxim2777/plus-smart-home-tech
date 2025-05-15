package ru.yandex.practicum.collector.model.hub;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScenarioRemovedEvent extends HubEvent {
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}

