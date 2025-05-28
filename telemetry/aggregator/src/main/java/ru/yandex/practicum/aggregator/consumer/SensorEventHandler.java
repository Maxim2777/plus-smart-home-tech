package ru.yandex.practicum.aggregator.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.producer.SensorsSnapshotProducer;
import ru.yandex.practicum.aggregator.service.AggregationService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorEventHandler {

    private final AggregationService aggregationService;
    private final SensorsSnapshotProducer producer;
    private final KafkaConsumer<String, SensorEventAvro> consumer;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void handle(ConsumerRecords<String, SensorEventAvro> records) {
        log.info("🔄 Получен пакет сообщений из Kafka: {} записей", records.count());

        for (ConsumerRecord<String, SensorEventAvro> record : records) {
            SensorEventAvro event = record.value();
            String hubId = event.getHubId();
            String sensorId = event.getId();

            log.debug("⚙️ Обработка события от сенсора [{}] хаба [{}] (offset={})", sensorId, hubId, record.offset());

            aggregationService.aggregateEvent(event)
                    .ifPresentOrElse(
                            snapshot -> {
                                log.info("📤 Отправка обновлённого снапшота хаба [{}] в Kafka", snapshot.getHubId());
                                producer.send("telemetry.snapshots.v1", snapshot.getHubId(), snapshot);
                            },
                            () -> log.debug("⏭ Пропущено событие от сенсора [{}] хаба [{}] (неактуально)", sensorId, hubId)
                    );

            currentOffsets.put(
                    new TopicPartition(record.topic(), record.partition()),
                    new OffsetAndMetadata(record.offset() + 1)
            );
        }

        commitOffsets();
    }

    private void commitOffsets() {
        log.debug("✅ Коммит смещений (асинхронно): {}", currentOffsets);
        consumer.commitAsync(currentOffsets, null);
    }

    public void shutdown() {
        log.info("🛑 Завершение работы SensorEventHandler: коммит и закрытие Kafka consumer");
        consumer.commitSync(currentOffsets);
        consumer.close();
    }
}