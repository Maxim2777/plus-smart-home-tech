package ru.yandex.practicum.telemetry.aggregation.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregation.configuration.KafkaConsumerConfig;
import ru.yandex.practicum.telemetry.aggregation.configuration.KafkaProducerConfig;
import ru.yandex.practicum.telemetry.aggregation.service.AggregationService;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final AggregationService aggregationService;
    private final KafkaConsumerConfig kafkaConsumerConfig;
    private final KafkaProducerConfig kafkaProducerConfig;

    public void start() {
        log.info("🚀 Aggregator стартует с конфигурациями:");
        log.info("Consumer: {}", kafkaConsumerConfig);
        log.info("Producer: {}", kafkaProducerConfig);

        Properties consumerProps = new Properties();
        consumerProps.putAll(kafkaConsumerConfig.getProperties());
        String sensorTopic = kafkaConsumerConfig.getTopics().getSensorEvents();

        Properties producerProps = new Properties();
        producerProps.putAll(kafkaProducerConfig.getProperties());
        String snapshotTopic = kafkaProducerConfig.getTopics().getSnapshotsEvents();


        try (KafkaConsumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(consumerProps);
             KafkaProducer<String, SensorsSnapshotAvro> producer = new KafkaProducer<>(producerProps)) {

            consumer.subscribe(List.of(sensorTopic));
            log.info("📡 Подписка на топик Kafka: {}", sensorTopic);

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    aggregationService.updateState(record.value()).ifPresent(snapshot -> {
                        log.info("📤 Отправка снапшота в топик {} для хаба {}", snapshotTopic, snapshot.getHubId());
                        producer.send(new ProducerRecord<>(snapshotTopic, snapshot.getHubId(), snapshot));
                    });
                }
            }

        } catch (Exception e) {
            log.error("❌ Ошибка в Aggregator при обработке событий", e);
        }
    }
}