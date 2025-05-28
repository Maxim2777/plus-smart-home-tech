package ru.yandex.practicum.aggregator.deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.IOException;

@Slf4j
public class SensorEventDeserializer implements Deserializer<SensorEventAvro> {

    @Override
    public SensorEventAvro deserialize(String topic, byte[] data) {
        if (data == null) {
            log.warn("Попытка десериализовать null-данные для топика [{}]", topic);
            return null;
        }

        try {
            log.debug("Начало десериализации события SensorEventAvro из топика [{}], размер данных: {} байт", topic, data.length);

            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            SpecificDatumReader<SensorEventAvro> reader = new SpecificDatumReader<>(SensorEventAvro.getClassSchema());
            SensorEventAvro result = reader.read(null, decoder);

            log.debug("Успешная десериализация события SensorEventAvro для топика [{}]: hubId={}, sensorId={}",
                    topic, result.getHubId(), result.getId());

            return result;
        } catch (IOException e) {
            log.error("Ошибка при десериализации SensorEventAvro из топика [{}]: {}", topic, e.getMessage(), e);
            throw new SerializationException("Ошибка десериализации SensorEventAvro", e);
        }
    }
}