package org.kiirun.beverages.domain;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(final BigDecimal value, final JsonGenerator gen, final SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeString(value.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
    }
}
