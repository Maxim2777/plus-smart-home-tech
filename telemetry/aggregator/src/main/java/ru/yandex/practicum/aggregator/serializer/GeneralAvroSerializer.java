package ru.yandex.practicum.aggregator.serializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            log.warn("Попытка сериализовать null-объект для топика [{}]", topic);
            return null;
        }

        log.debug("Начало сериализации объекта класса [{}] для топика [{}]", data.getClass().getSimpleName(), topic);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(outputStream, null);
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());

            writer.write(data, encoder);
            encoder.flush();

            byte[] result = outputStream.toByteArray();
            log.debug("Успешная сериализация объекта [{}] ({} байт) для топика [{}]", data.getClass().getSimpleName(), result.length, topic);
            return result;

        } catch (IOException e) {
            log.error("Ошибка сериализации Avro-сообщения класса [{}] для топика [{}]: {}", data.getClass().getSimpleName(), topic, e.getMessage(), e);
            throw new SerializationException("Ошибка сериализации Avro-сообщения для топика [" + topic + "]", e);
        }
    }
}