package org.ohmage.domain.jackson;

import java.io.IOException;
import java.math.BigDecimal;

import org.ohmage.domain.survey.condition.terminal.Numeric;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * <p>
 * A generic representation of a number in an attempt to bring order to the
 * chaos.
 * </p>
 *
 * @author John Jenkins
 */
public class OhmageNumber {
    /**
     * <p>
     * Deserializes anything that can be translated into a {@link Number}
     * into an {@link OhmageNumber} object.
     * </p>
     *
     * @author John Jenkins
     */
    public static class OhmageNumberDeserializer
        extends JsonDeserializer<OhmageNumber> {

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
         */
        @Override
        public OhmageNumber deserialize(
            final JsonParser parser,
            final DeserializationContext context)
            throws IOException, JsonProcessingException {

            return new OhmageNumber(parser.getNumberValue());
        }
    }

    /**
     * <p>
     * Serializes an {@link OhmageNumber} as a number.
     * </p>
     *
     * @author John Jenkins
     */
    public static class OhmageNumberSerializer
        extends JsonSerializer<OhmageNumber> {

        /*
         * (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(
            final OhmageNumber value,
            final JsonGenerator generator,
            final SerializerProvider provider)
            throws IOException, JsonProcessingException {

            generator.writeObject(value.getNumber());
        }
    }

    /**
     * The JSON key for the value before its scaling has been applied.
     */
    public static final String JSON_KEY_UNSCALED = "unscaled";
    /**
     * The JSON key for the amount to scale this number.
     */
    public static final String JSON_KEY_SCALE = "scale";

    /**
     * The internal representation of the number that backs this object.
     */
    private final BigDecimal internal;

    /**
     * Builds a OhmageNumber from a {@link Numeric} object.
     *
     * @param value
     *        The {@link Numeric} that backs this object.
     */
    public OhmageNumber(final Number value) {
        internal = new BigDecimal(value.toString());
    }

    /**
     * Returns the internal representation of this number.
     *
     * @return The internal representation of this number.
     */
    @JsonIgnore
    public BigDecimal getNumber() {
        return internal;
    }

    /**
     * Returns the value of this number before scaling has been applied.
     *
     * @return The value of this number before scaline has been applied.
     */
    @JsonProperty(JSON_KEY_UNSCALED)
    public long getUnscaled() {
        return internal.unscaledValue().longValue();
    }

    /**
     * Returns the amount to scale this value.
     *
     * @return The amount to scale this value.
     */
    @JsonProperty(JSON_KEY_SCALE)
    public int getScale() {
        return internal.scale();
    }

    /*
     * (non-Javadoc)
     * @see java.math.BigDecimal#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object other) {
        if(other instanceof BigDecimal) {
            return internal.compareTo((BigDecimal) other) == 0;
        }
        if(other instanceof OhmageNumber) {
            return internal.compareTo(((OhmageNumber) other).internal) == 0;
        }
        if(other instanceof Number) {
            return compareTo((Number) other) == 0;
        }
        return false;
    }

    /**
     * Compares this OhmageNumber to another Number.
     *
     * @param number
     *        The number to compare to this OhamgeNumber.
     *
     * @return Less than -1 if this OhmageNumber is less than the number, 0 if
     *         this OhmageNumber is equal to the number, or 1 if this Ohmage
     *         number is greater than the number.
     */
    public int compareTo(final Number number) {
        return internal.compareTo((new OhmageNumber(number)).internal);
    }

    /**
     * Compares two {@link Numeric} objects by using their double values.
     *
     * @param first
     *        The first number to compare.
     *
     * @param second
     *        The second number to compare.
     *
     * @return Less than 0 if the first number is less than the second,
     *         greater than 0 if the first number is greater than the
     *         second, or 0 if they represent the same value.
     */
    public static int compareNumbers(
        final Number first,
        final Number second) {

        OhmageNumber firstNumber = new OhmageNumber(first);
        OhmageNumber secondNumber = new OhmageNumber(second);

        return firstNumber.internal.compareTo(secondNumber.internal);
    }
}