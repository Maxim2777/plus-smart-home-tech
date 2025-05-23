package ru.yandex.practicum.aggregator.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaSnapshotProducer {

    private final Producer<String, SensorsSnapshotAvro> producer;

    public void send(String topic, String key, SensorsSnapshotAvro message) {
        ProducerRecord<String, SensorsSnapshotAvro> record = new ProducerRecord<>(topic, key, message);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке снапшота в Kafka: {}", exception.getMessage(), exception);
            } else {
                log.info("Снапшот успешно отправлен в Kafka: topic={}, partition={}, offset={}, key={}",
                        metadata.topic(), metadata.partition(), metadata.offset(), key);
            }
        });
    }

    public void close() {
        producer.close();
    }

    public void flush() {
        producer.flush();
    }

}
