package org.ohmage.domain.survey.condition.terminal;

import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.NoResponse;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.Fragment;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * <p>
 * A {@link Terminal} that represents not displaying a prompt.
 * </p>
 *
 * @author John Jenkins
 */
public class NotDisplayed extends Terminal {
    /**
     * <p>
     * A builder for {@link NotDisplayed} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder implements Terminal.Builder<NotDisplayed> {
        /**
         * Creates a new builder.
         */
        public Builder() {
            // Do nothing.
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
        public NotDisplayed build() throws InvalidArgumentException {
            return new NotDisplayed();
        }
    }

    /**
     * Creates a new "not-displayed" node.
     */
    @JsonCreator
    public NotDisplayed() {
        // Do nothing.
    }

    /**
     * @return Always returns {@link NoResponse#NOT_DISPLAYED}.
     */
    @Override
    public Object getValue(final Map<String, Object> responses) {
        return NoResponse.NOT_DISPLAYED;
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
     * @return Always returns false.
     */
    @Override
    public boolean evaluate(final Map<String, Object> responses) {
        return false;
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

    /**
     * Compares this value to {@link NoResponse#NOT_DISPLAYED}.
     */
    @Override
    public boolean equalsValue(
        final Map<String, Object> responses,
        final Object value) {

        return NoResponse.NOT_DISPLAYED.equals(value);
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