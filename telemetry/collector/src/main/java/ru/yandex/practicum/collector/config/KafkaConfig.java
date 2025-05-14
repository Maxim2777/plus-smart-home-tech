package ru.yandex.practicum.collector.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private Map<String, Object> baseConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        return props;
    }

    @Bean
    public KafkaTemplate<String, SensorEventAvro> sensorEventKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(baseConfigs()));
    }

    @Bean
    public KafkaTemplate<String, HubEventAvro> hubEventKafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(baseConfigs()));
    }
}