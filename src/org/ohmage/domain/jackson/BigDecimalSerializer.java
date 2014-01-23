package org.ohmage.domain.jackson;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * <p>
 * Serializes the BigDecimal as its numeric value. JSON allows numbers to be
 * any level of precision as does BigDecimal, so the toString() functionality
 * in BigDecimal should produce valid JSON.
 * </p>
 *
 * @author John Jenkins
 */
public class BigDecimalSerializer extends JsonSerializer<BigDecimal> {
    /*
     * (non-Javadoc)
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(
        final BigDecimal value,
        final JsonGenerator generator,
        final SerializerProvider provider)
        throws IOException, JsonProcessingException {

        generator.writeRawValue(value.toString());
    }
}