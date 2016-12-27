package org.kiirun.beverages.domain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(final LocalDateTime value, final JsonGenerator gen, final SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeNumber(value.toInstant(ZoneOffset.UTC).toEpochMilli());
    }
}
