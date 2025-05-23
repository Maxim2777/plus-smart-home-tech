package ru.yandex.practicum.telemetry.aggregation.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregation.service.AggregationService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final AggregationService aggregationService;
    private final KafkaTemplate<String, SensorsSnapshotAvro> kafkaTemplate;

    private final String SENSOR_TOPIC = "telemetry.sensors.v1";
    private final String SNAPSHOT_TOPIC = "telemetry.snapshots.v1";

    public void start() {
        log.info("🚀 Aggregator запущен");

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregator-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "ru.yandex.practicum.telemetry.aggregation.serialization.SensorEventDeserializer");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(List.of(SENSOR_TOPIC));

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();
                    Optional<SensorsSnapshotAvro> snapshotOpt = aggregationService.updateState(event);
                    snapshotOpt.ifPresent(snapshot -> {
                        kafkaTemplate.send(SNAPSHOT_TOPIC, snapshot.getHubId(), snapshot);
                        log.debug("📦 Отправлен снапшот хаба {} в Kafka", snapshot.getHubId());
                    });
                }
                consumer.commitSync();
            }

        } catch (Exception e) {
            log.error("💥 Ошибка во время обработки событий", e);
        }
    }
}