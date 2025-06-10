package ru.yandex.practicum.telemetry.analyzer.deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.io.IOException;

@Slf4j
public class HubEventDeserializer implements Deserializer<HubEventAvro> {

    @Override
    public HubEventAvro deserialize(String topic, byte[] data) {
        if (data == null) {
            log.warn("Попытка десериализовать null-данные для топика [{}]", topic);
            return null;
        }

        try {
            log.debug("Начало десериализации HubEventAvro из топика [{}], размер: {} байт", topic, data.length);

            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            SpecificDatumReader<HubEventAvro> reader = new SpecificDatumReader<>(HubEventAvro.getClassSchema());
            HubEventAvro result = reader.read(null, decoder);

            log.debug("Успешная десериализация HubEventAvro: hubId={}, timestamp={}",
                    result.getHubId(), result.getTimestamp());

            return result;
        } catch (IOException e) {
            log.error("Ошибка при десериализации HubEventAvro из топика [{}]: {}", topic, e.getMessage(), e);
            throw new SerializationException("Ошибка десериализации HubEventAvro", e);
        }
    }
}