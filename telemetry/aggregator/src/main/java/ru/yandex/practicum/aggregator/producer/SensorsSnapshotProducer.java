package ru.yandex.practicum.aggregator.producer;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorsSnapshotProducer {

    private final Producer<String, SensorsSnapshotAvro> producer;

    public void send(String topic, String key, SensorsSnapshotAvro message) {
        log.debug("Отправка сообщения в Kafka: topic='{}', key='{}'", topic, key);
        ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(topic, key, message);
        producer.send(record, callback(topic, key));
    }

    private Callback callback(String topic, String key) {
        return (RecordMetadata metadata, Exception exception) -> {
            if (exception != null) {
                log.error("❌ Ошибка при отправке сообщения в Kafka: topic='{}', key='{}', ошибка: {}", topic, key, exception.getMessage(), exception);
            } else {
                log.info("✅ Сообщение успешно отправлено в Kafka: topic='{}', key='{}', partition={}, offset={}",
                        topic, key, metadata.partition(), metadata.offset());
            }
        };
    }

    @PreDestroy
    void shutdown() {
        log.info("⏹ Завершение работы Kafka-продюсера: флаш и закрытие");
        producer.flush();
        producer.close();
    }
}