package org.ohmage.domain.survey.condition.terminal;

import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.NoResponse;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.Fragment;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * <p>
 * A {@link Terminal} that represents skipping a prompt.
 * </p>
 *
 * @author John Jenkins
 */
public class Skipped extends Terminal {
    /**
     * <p>
     * A builder for {@link Skipped} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder implements Terminal.Builder<Skipped> {
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
        public Skipped build() throws InvalidArgumentException {
            return new Skipped();
        }
    }

    /**
     * Creates a new "skipped" node.
     */
    @JsonCreator
    public Skipped() {
        // Do nothing.
    }

    /**
     * @return Always returns {@link NoResponse#SKIPPED}.
     */
    @Override
    public Object getValue(final Map<String, Object> responses) {
        return NoResponse.SKIPPED;
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

    @Override
    public String toString() {
        return "<SKIPPED>";
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
     * Compares the 'value' to {@link NoResponse#SKIPPED}.
     */
    @Override
    public boolean equalsValue(
        final Map<String, Object> responses,
        final Object value) {

        return NoResponse.SKIPPED.equals(value);
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