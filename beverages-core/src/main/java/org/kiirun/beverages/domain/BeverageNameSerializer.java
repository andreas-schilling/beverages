package org.kiirun.beverages.domain;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class BeverageNameSerializer extends StdSerializer<Beverage> {
    private static final long serialVersionUID = 1L;

    public BeverageNameSerializer() {
        super(Beverage.class);
    }

    @Override
    public void serialize(final Beverage value, final JsonGenerator jgen, final SerializerProvider provider)
            throws JsonGenerationException, IOException {
        jgen.writeFieldName(value.getName());
    }
}
