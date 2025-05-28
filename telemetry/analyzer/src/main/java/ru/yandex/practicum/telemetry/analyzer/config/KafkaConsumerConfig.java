package ru.yandex.practicum.telemetry.analyzer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public KafkaConsumer<String, SensorsSnapshotAvro> snapshotKafkaConsumer(SnapshotKafkaProperties props) {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, props.getGroupId());
        kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, props.getAutoOffsetReset());
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, props.getKeyDeserializer());
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, props.getValueDeserializer());

        return new KafkaConsumer<>(kafkaProps);
    }

    @Bean
    public KafkaConsumer<String, HubEventAvro> hubEventKafkaConsumer(HubKafkaProperties props) {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, props.getGroupId());
        kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, props.getAutoOffsetReset());
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, props.getKeyDeserializer());
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, props.getValueDeserializer());

        return new KafkaConsumer<>(kafkaProps);
    }
}