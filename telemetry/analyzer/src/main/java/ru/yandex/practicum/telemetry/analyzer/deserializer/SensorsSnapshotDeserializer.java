package ru.yandex.practicum.telemetry.analyzer.deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.io.IOException;

@Slf4j
public class SensorsSnapshotDeserializer implements Deserializer<SensorsSnapshotAvro> {

    @Override
    public SensorsSnapshotAvro deserialize(String topic, byte[] data) {
        if (data == null) {
            log.warn("Попытка десериализовать null-данные для топика [{}]", topic);
            return null;
        }

        try {
            log.debug("Начало десериализации SensorsSnapshotAvro из топика [{}], размер: {} байт", topic, data.length);

            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            SpecificDatumReader<SensorsSnapshotAvro> reader = new SpecificDatumReader<>(SensorsSnapshotAvro.getClassSchema());
            SensorsSnapshotAvro result = reader.read(null, decoder);

            log.debug("Успешная десериализация SensorsSnapshotAvro: hubId={}, timestamp={}",
                    result.getHubId(), result.getTimestamp());

            return result;
        } catch (IOException e) {
            log.error("Ошибка при десериализации SensorsSnapshotAvro из топика [{}]: {}", topic, e.getMessage(), e);
            throw new SerializationException("Ошибка десериализации SensorsSnapshotAvro", e);
        }
    }
}