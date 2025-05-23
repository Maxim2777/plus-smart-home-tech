package ru.yandex.practicum.telemetry.aggregation.deserializer;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Decoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;

import java.util.Map;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final Schema schema;
    private final DecoderFactory decoderFactory;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Не используется
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) return null;

        try {
            Decoder decoder = decoderFactory.binaryDecoder(data, null);
            SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при десериализации Avro-сообщения", e);
        }
    }

    @Override
    public void close() {
        // Не используется
    }
}