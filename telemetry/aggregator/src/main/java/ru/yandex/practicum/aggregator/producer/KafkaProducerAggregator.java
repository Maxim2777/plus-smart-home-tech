package ru.yandex.practicum.aggregator.producer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Service
@RequiredArgsConstructor
public class KafkaProducerAggregator {

    private final Producer<String, SensorsSnapshotAvro> producer;

    public void send(String topic, String key, SensorsSnapshotAvro message) {
        ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(topic, key, message);
        producer.send(record);
    }

    public void close() {
        producer.close();
    }

    public void flush() {
        producer.flush();
    }
}
