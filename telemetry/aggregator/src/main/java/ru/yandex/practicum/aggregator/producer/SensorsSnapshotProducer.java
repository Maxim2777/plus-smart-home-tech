package ru.yandex.practicum.aggregator.producer;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;


@Service
@RequiredArgsConstructor
public class SensorsSnapshotProducer {

    private final Producer<String, SensorsSnapshotAvro> producer;

    public void send(String topic, String key, SensorsSnapshotAvro message) {
        ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(topic, key, message);
        producer.send(record, callback(key));
    }

    private Callback callback(String key) {
        return (RecordMetadata metadata, Exception exception) -> {
            if (exception != null) {
                System.err.printf("❌ Kafka error sending key=%s: %s%n", key, exception.getMessage());
            }
        };
    }

    @PreDestroy
    void shutdown() {
        producer.flush();
        producer.close();
    }
}