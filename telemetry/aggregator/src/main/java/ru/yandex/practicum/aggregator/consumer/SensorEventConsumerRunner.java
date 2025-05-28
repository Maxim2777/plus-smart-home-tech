package ru.yandex.practicum.aggregator.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorEventConsumerRunner {

    private final SensorEventHandler eventHandler;
    private final KafkaConsumer<String, SensorEventAvro> consumer;

    private static final String TOPIC = "telemetry.sensors.v1";
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(1000);

    public void start() {
        log.info("🚀 Запуск SensorEventConsumerRunner, подписка на топик [{}]", TOPIC);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("📴 Получен сигнал завершения. Остановка потребителя Kafka.");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(List.of(TOPIC));
            log.info("📡 Подписка на топик Kafka [{}] успешно выполнена", TOPIC);

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(POLL_TIMEOUT);
                if (!records.isEmpty()) {
                    log.debug("📥 Получено {} записей из топика [{}]", records.count(), TOPIC);
                }
                eventHandler.handle(records);
            }

        } catch (WakeupException ignored) {
            log.info("⛔ Получен WakeupException — корректное завершение цикла чтения.");
        } catch (Exception e) {
            log.error("🔥 Ошибка во время обработки Kafka-сообщений: {}", e.getMessage(), e);
        } finally {
            log.info("🧹 Завершение работы SensorEventConsumerRunner, вызов shutdown()");
            eventHandler.shutdown();
        }
    }
}