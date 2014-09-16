package org.ohmage.domain.survey.condition.terminal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.jackson.OhmageNumber;
import org.ohmage.domain.survey.NoResponse;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.Fragment;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A {@link Terminal} that represents some prompt ID
 * </p>
 *
 * @author John Jenkins
 */
public class PromptId extends Terminal {
    /**
     * <p>
     * A builder for {@link PromptId} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder implements Terminal.Builder<PromptId> {
        /**
         * The prompt ID.
         */
        private final String value;

        /**
         * Creates a new builder with some prompt ID.
         *
         * @param value
         *        The prompt ID.
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
         * @see org.ohmage.domain.survey.condition.Condition.Fragment.Builder#build()
         */
        @Override
        public PromptId build() throws InvalidArgumentException {
            return new PromptId(value);
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
     * Creates a new prompt ID node.
     *
     * @param value
     *        The value of the prompt ID node.
     *
     * @throws InvalidArgumentException
     *         The prompt ID was null.
     */
    @JsonCreator
    public PromptId(@JsonProperty(JSON_KEY_VALUE) final String value)
        throws InvalidArgumentException {

        // Be sure it is not null.
        if(value == null) {
            throw new IllegalStateException("The prompt ID is null.");
        }

        // Remove the quotes.
        this.value = value;
    }

    /**
     * Returns the prompt ID that this PromptId object references.
     *
     * @return The prompt ID that this PromptId object references.
     */
    public String getPromptId() {
        return value;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Terminal#getValue(java.util.Map)
     */
    @Override
    public Object getValue(final Map<String, Object> responses) {
        // Get the response value.
        Object response = responses.get(value);

        // If the response doesn't exist, then this prompt does not exist as a
        // previous prompt in this survey, which means it should never have
        // reached this point.
        if(response == null) {
            throw
                new IllegalStateException(
                    "The response does not exist in the response map: " +
                        value);
        }

        // Return the response value from the map.
        return response;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Fragment#validate(java.util.Map)
     */
    @Override
    public void validate(final Map<String, SurveyItem> surveyItems)
        throws InvalidArgumentException {

        if(! surveyItems.containsKey(value)) {
            throw
                new InvalidArgumentException(
                    "The prompt ID '" + value + "' for the condition.");
        }
    }

    /**
     * @return Only returns false if the response is a {@link NoResponse}
     *         object.
     */
    @Override
    public boolean evaluate(final Map<String, Object> responses) {
        // Get the response.
        Object response = getValue(responses);

        // Return true if this prompt had any response.
        return ! (response instanceof NoResponse);
    }

    @Override
    public String toString() {
        return value;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.terminal.Terminal#lessThanValue(java.util.Map, java.lang.Object)
     */
    @Override
    public boolean lessThanValue(
        final Map<String, Object> responses,
        final Object value) {

        OhmageNumber thisNumber;
        Object thisResponse = getValue(responses);
        if(thisResponse instanceof Number) {
            thisNumber = new OhmageNumber((Number) thisResponse);
        }
        else if(value instanceof OhmageNumber) {
            thisNumber = (OhmageNumber) value;
        }
        else {
            return false;
        }

        Number otherNumber;
        if(value instanceof Number) {
            otherNumber = (Number) value;
        }
        else if(value instanceof OhmageNumber) {
            otherNumber = ((OhmageNumber) value).getNumber();
        }
        else {
            return false;
        }

        return thisNumber.compareTo(otherNumber) < 0;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.terminal.Terminal#equalsValue(java.util.Map, java.lang.Object)
     */
    @Override
    public boolean equalsValue(
        final Map<String, Object> responses,
        final Object value) {

        Object response = getValue(responses);

        if(response instanceof Number) {
            return (new OhmageNumber((Number) response)).equals(value);
        }
        else if(response instanceof OhmageNumber) {
            return response.equals(value);
        }
        else if(response instanceof Collection) {
            return ((Collection) response).contains(value);
        }
        else {
            return response.equals(value);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.terminal.Terminal#lessThanValue(java.util.Map, java.lang.Object)
     */
    @Override
    public boolean greaterThanValue(
        final Map<String, Object> responses,
        final Object value) {

        OhmageNumber thisNumber;
        Object thisResponse = getValue(responses);
        if(thisResponse instanceof Number) {
            thisNumber = new OhmageNumber((Number) thisResponse);
        }
        else if(value instanceof OhmageNumber) {
            thisNumber = (OhmageNumber) value;
        }
        else {
            return false;
        }

        Number otherNumber;
        if(value instanceof Number) {
            otherNumber = (Number) value;
        }
        else if(value instanceof OhmageNumber) {
            otherNumber = ((OhmageNumber) value).getNumber();
        }
        else {
            return false;
        }

        return thisNumber.compareTo(otherNumber) > 0;
    }
}