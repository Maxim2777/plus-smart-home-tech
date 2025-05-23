package ru.yandex.practicum.telemetry.aggregation.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("aggregator.kafka.producer")
@Getter
@Setter
@ToString
public class KafkaProducerConfig {
    private Map<String, String> properties;
    private AggregatorProducerConfig.Topics topics;
}


