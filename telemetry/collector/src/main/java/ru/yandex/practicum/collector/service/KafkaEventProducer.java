package ru.yandex.practicum.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.collector.model.TopicType;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    public void send(SpecificRecordBase event, TopicType topic) {
        log.info("📤 Sending event [{}] to topic [{}]", event.getClass().getSimpleName(), topic.name());
        kafkaTemplate.send(topic.name().toLowerCase().replace("_", "-"), event);
    }
}
