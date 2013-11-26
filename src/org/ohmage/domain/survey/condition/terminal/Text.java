package org.ohmage.domain.survey.condition.terminal;

import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.Fragment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A {@link Terminal} that represents some quoted text.
 * </p>
 *
 * @author John Jenkins
 */
public class Text extends Terminal {
    /**
     * <p>
     * A builder for {@link Text} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder implements Terminal.Builder<Text> {
        /**
         * The quoted text value.
         */
        private final String value;

        /**
         * Creates a new builder with some quoted text value.
         *
         * @param value
         *        The quoted text value.
         */
        public Builder(final String value) {
            this.value = value;
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
        public Text build() throws InvalidArgumentException {
            return new Text(value);
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
    private final String value;

    /**
     * Creates a new text node.
     *
     * @param value
     *        The value of the text node.
     *
     * @throws InvalidArgumentException
     *         The value was null or did not begin and end with a quotation
     *         mark.
     */
    @JsonCreator
    public Text(@JsonProperty(JSON_KEY_VALUE) final String value)
        throws InvalidArgumentException {

        // Be sure it is not null.
        if(value == null) {
            throw new IllegalStateException("The text is null.");
        }

        // Be sure it begins with a quote.
        if(! value.startsWith("\"")) {
            throw
                new InvalidArgumentException(
                    "Text values must begin with a \".");
        }

        // Be sure it ends with a quote.
        if(! value.endsWith("\"")) {
            throw
                new InvalidArgumentException(
                    "Text values must end with a \".");
        }

        // Remove the quotes.
        this.value = value.substring(1, value.length() - 1);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Terminal#getValue(java.util.Map)
     */
    @Override
    public String getValue(final Map<String, Object> responses) {
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
     * @return Always returns true.
     */
    @Override
    public boolean evaluate(final Map<String, Object> responses) {
        return true;
    }

    /**
     * @return Always returns false.
     */
    @Override
    public boolean lessThanValue(
        final Map<String, Object> responses,
        final Object value) {

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

        return value.equals(value);
    }

    /**
     * @return Always returns false.
     */
    @Override
    public boolean greaterThanValue(
        final Map<String, Object> responses,
        final Object value) {

        return false;
    }
}