package ru.yandex.practicum.aggregator.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.producer.KafkaSnapshotProducer;
import ru.yandex.practicum.aggregator.service.AggregationService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final AggregationService aggregationService;
    private final KafkaSnapshotProducer producer;

    private static final String TOPIC = "telemetry.sensors.v1";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(1000);

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();


    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(TOPIC));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(POLL_TIMEOUT);

                int count = 0;
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    aggregationService.updateState(record.value())
                            .ifPresent(snapshot ->
                                    producer.send("telemetry.snapshots.v1", snapshot.getHubId(), snapshot)
                            );

                    currentOffsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)
                    );

                    if (++count % 10 == 0) {
                        consumer.commitAsync(currentOffsets, (offsets, ex) -> {
                            if (ex != null) {
                                log.warn("Ошибка при коммите оффсетов: {}", offsets, ex);
                            }
                        });
                    }
                }

                consumer.commitAsync(currentOffsets, (offsets, ex) -> {
                    if (ex != null) {
                        log.warn("Ошибка при финальном коммите: {}", offsets, ex);
                    }
                });
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал на завершение работы.");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
                producer.flush();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}