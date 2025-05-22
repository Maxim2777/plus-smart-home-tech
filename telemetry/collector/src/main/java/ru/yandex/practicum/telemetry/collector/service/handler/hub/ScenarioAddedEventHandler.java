package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

@Slf4j
@Service
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {

    public ScenarioAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED_EVENT;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEventProto event) {
        ScenarioAddedEventProto proto = event.getScenarioAddedEvent();

        return ScenarioAddedEventAvro.newBuilder()
                .setScenarioId(proto.getScenarioId())
                .setHubId(event.getHubId())
                .setConditions(proto.getConditionsList().stream().map(c ->
                        ScenarioConditionAvro.newBuilder()
                                .setSensorId(c.getSensorId())
                                .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                                .setOperation(c.getOperation())
                                .setValue(c.getValue())
                                .build()
                ).toList())
                .setActions(proto.getActionsList().stream().map(a ->
                        DeviceActionAvro.newBuilder()
                                .setSensorId(a.getSensorId())
                                .setAction(a.getAction())
                                .build()
                ).toList())
                .build();
    }
}
