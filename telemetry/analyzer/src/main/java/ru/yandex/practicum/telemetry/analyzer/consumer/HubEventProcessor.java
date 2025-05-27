package ru.yandex.practicum.telemetry.analyzer.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    private final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    @Override
    public void run() {
        consumer.subscribe(Collections.singletonList("telemetry.hubs.v1"));
        log.info("🟡 HubEventProcessor запущен, слушает telemetry.hubs.v1");

        while (true) {
            ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, HubEventAvro> record : records) {
                HubEventAvro event = record.value();
                process(event);

                TopicPartition partition = new TopicPartition(record.topic(), record.partition());
                offsets.put(partition, new OffsetAndMetadata(record.offset() + 1));
            }
            consumer.commitSync(offsets);
        }
    }

    private void process(HubEventAvro event) {
        String hubId = event.getHubId();
        Object payload = event.getPayload();

        if (payload instanceof DeviceAddedEventAvro deviceAdded) {
            Sensor sensor = new Sensor();
            sensor.setId(deviceAdded.getId());
            sensor.setHubId(hubId);
            sensorRepository.save(sensor);
            log.info("➕ Добавлен сенсор {} в хаб {}", deviceAdded.getId(), hubId);

        } else if (payload instanceof DeviceRemovedEventAvro deviceRemoved) {
            sensorRepository.deleteById(deviceRemoved.getId());
            log.info("❌ Удалён сенсор {}", deviceRemoved.getId());

        } else if (payload instanceof ScenarioAddedEventAvro scenarioAdded) {
            Scenario scenario = new Scenario();
            scenario.setHubId(hubId);
            scenario.setName(scenarioAdded.getName());

            scenarioAdded.getConditions().forEach(conditionAvro -> {
                String sensorId = conditionAvro.getSensorId();

                sensorRepository.findById(sensorId).orElseGet(() -> {
                    Sensor s = new Sensor();
                    s.setId(sensorId);
                    s.setHubId(hubId);
                    return sensorRepository.save(s);
                });

                Object rawValue = conditionAvro.getValue();

                Condition condition = new Condition();
                condition.setType(ConditionType.valueOf(conditionAvro.getType().name()));
                condition.setOperation(conditionAvro.getOperation().name());

                if (rawValue instanceof Integer i) {
                    condition.setValueInt(i);
                } else if (rawValue instanceof Boolean b) {
                    condition.setValueBool(b);
                } else {
                    log.warn("⚠️ Неизвестный тип value у condition: {}", rawValue != null ? rawValue.getClass().getSimpleName() : "null");
                }

                scenario.getConditions().put(sensorId, condition);
            });

            scenarioAdded.getActions().forEach(actionAvro -> {
                String sensorId = actionAvro.getSensorId();

                sensorRepository.findById(sensorId).orElseGet(() -> {
                    Sensor s = new Sensor();
                    s.setId(sensorId);
                    s.setHubId(hubId);
                    return sensorRepository.save(s);
                });

                Action action = new Action();
                action.setType(actionAvro.getType().name());

                if (actionAvro.getValue() instanceof Integer i) {
                    action.setValue(i);
                }

                scenario.getActions().put(sensorId, action);
            });

            scenarioRepository.save(scenario);

            log.info("✅ Добавлен сценарий '{}' с {} условиями и {} действиями",
                    scenarioAdded.getName(), scenarioAdded.getConditions().size(), scenarioAdded.getActions().size());
        }
        else if (payload instanceof ScenarioRemovedEventAvro scenarioRemoved) {
            scenarioRepository.findByHubIdAndName(hubId, scenarioRemoved.getName())
                    .ifPresentOrElse(
                            scenarioRepository::delete,
                            () -> log.warn("⚠️ Сценарий '{}' не найден для удаления", scenarioRemoved.getName())
                    );

        } else {
            log.warn("⚠️ Неизвестный тип события: {}", payload.getClass().getSimpleName());
        }
    }
}
