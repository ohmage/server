package org.ohmage.domain.survey.condition.terminal;

import java.math.BigDecimal;
import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.Fragment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A {@link Terminal} that represents some number.
 * </p>
 *
 * @author John
 */
public class Numeric extends Terminal {
    /**
     * <p>
     * A custom version of the {@link BigDecimal} class that has a much looser
     * comparison semantics. Basically, all values are converted into their
     * 'double' value before comparison.
     * </p>
     *
     * @author John Jenkins
     */
    public static class CustomBigDecimal extends BigDecimal {
        /**
         * A version-specific ID for serialization purposes.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Builds a CustomBigDecimal from a {@link Numeric} object.
         *
         * @param value
         *        The {@link Numeric} that backs this object.
         */
        public CustomBigDecimal(final Number value) {
            super(value.toString());
        }

        /*
         * (non-Javadoc)
         * @see java.math.BigDecimal#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object other) {
            if(other instanceof BigDecimal) {
                return compareTo((BigDecimal) other) == 0;
            }
            if(other instanceof Number) {
                return
                    CustomBigDecimal
                        .compareNumbers(this, ((Number) other)) == 0;
            }
            return false;
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

            return Double.compare(first.doubleValue(), second.doubleValue());
        }
    }

    /**
     * <p>
     * A builder for {@link Numeric} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder implements Terminal.Builder<Numeric> {
        /**
         * The number value.
         */
        private final CustomBigDecimal value;

        /**
         * Creates a new builder with some number.
         *
         * @param value
         *        The number value.
         */
        public Builder(final BigDecimal value) {
            this.value = new CustomBigDecimal(value);
        }

        /*
         * (non-Javadoc)
         * @see org.ohmage.domain.survey.condition.Fragment.Builder#merge(org.ohmage.domain.survey.condition.Fragment.Builder)
         */
        @Override
        public Fragment.Builder<?> merge(final Fragment.Builder<?> other) {
            if(other instanceof Terminal.Builder<?>) {
                throw
                    new InvalidArgumentException(
                        "More than one terminals in a row are not " +
                            "allowed.");
            }

            return other.merge(this);
        }

        /*
         * (non-Javadoc)
         * @see org.ohmage.domain.survey.condition.Fragment.Builder#build()
         */
        @Override
        public Numeric build() throws InvalidArgumentException {
            return new Numeric(value);
        }
    }

    /**
     * The JSON key for the value.
     */
    public static final String JSON_KEY_VALUE = "value";

    /**
     * The text value.
     */
    @JsonProperty(JSON_KEY_VALUE)
    private final CustomBigDecimal value;

    /**
     * Creates a new number node.
     *
     * @param value
     *        The value of the number node.
     *
     * @throws InvalidArgumentException
     *         The value was null or did not begin and end with a quotation
     *         mark.
     */
    @JsonCreator
    public Numeric(@JsonProperty(JSON_KEY_VALUE) final CustomBigDecimal value)
        throws InvalidArgumentException {

        // Be sure it is not null.
        if(value == null) {
            throw new IllegalStateException("The text is null.");
        }

        // Store the value.
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Terminal#getValue(java.util.Map)
     */
    @Override
    public BigDecimal getValue(final Map<String, Object> responses) {
        return value;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Fragment#validate(java.util.Map)
     */
    @Override
    public void validate(final Map<String, SurveyItem> surveyItems)
        throws InvalidArgumentException {

        // Do nothing.
    }

    /**
     * @return Returns false if the value of this Numeric represents zero.
     *         Otherwise, true is returned.
     */
    @Override
    public boolean evaluate(final Map<String, Object> responses) {
        return CustomBigDecimal.compareNumbers(getValue(responses), 0) == 0;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.terminal.Terminal#lessThanValue(java.util.Map, java.lang.Object)
     */
    @Override
    public boolean lessThanValue(
        final Map<String, Object> responses,
        final Object value) {

        if(value instanceof Number) {
            return
                this.value.compareTo(new CustomBigDecimal((Number) value)) < 0;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.terminal.Terminal#equalsValue(java.util.Map, java.lang.Object)
     */
    @Override
    public boolean equalsValue(
        final Map<String, Object> responses,
        final Object value) {

        if(value instanceof Number) {
            return
                this.value.compareTo(new CustomBigDecimal((Number) value)) ==
                    0;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.terminal.Terminal#greaterThanValue(java.util.Map, java.lang.Object)
     */
    @Override
    public boolean greaterThanValue(
        final Map<String, Object> responses,
        final Object value) {

        if(value instanceof Number) {
            return
                this.value.compareTo(new CustomBigDecimal((Number) value)) > 0;
        }

        return false;
    }
}