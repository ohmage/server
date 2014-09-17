package org.ohmage.domain.survey.condition;

import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.comparator.Comparator;
import org.ohmage.domain.survey.condition.terminal.Terminal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A {@link Fragment} that represents an enclosed condition that should not
 * be modified.
 * </p>
 *
 * @author John Jenkins
 */
public class Parenthetical extends Fragment {
    /**
     * <p>
     * A builder for {@link Parenthetical} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder
        implements Fragment.Builder<Parenthetical> {

        /**
         * The condition that defines the contents within the parenthesis.
         */
        private final Condition condition;

        /**
         * Creates a new builder with some condition.
         *
         * @param condition
         *        The condition.
         */
        public Builder(final Condition condition) {
            this.condition = condition;
        }

        /*
         * (non-Javadoc)
         * @see org.ohmage.domain.survey.condition.Condition.Fragment.Builder#merge(org.ohmage.domain.survey.condition.Condition.Fragment.Builder)
         */
        @Override
        public Fragment.Builder<?> merge(final Fragment.Builder<?> other) {
            if(other instanceof Terminal.Builder<?>) {
                throw
                    new InvalidArgumentException(
                        "A parenthetical cannot be merged with a " +
                            "terminal.");
            }
            else if(other instanceof Comparator.Builder<?>) {
                throw
                    new InvalidArgumentException(
                        "A parenthetical cannot be merged with a " +
                            "compartor.");
            }
            else {
                return other.merge(this);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.ohmage.domain.survey.condition.Condition.Fragment.Builder#build()
         */
        @Override
        public Parenthetical build() throws InvalidArgumentException {
            return new Parenthetical(condition);
        }
    }

    /**
     * An opening parenthesis.
     */
    public static final char START = '(';
    /**
     * A closing parenthesis.
     */
    public static final char END = ')';

    /**
     * The JSON key for the condition.
     */
    public static final String JSON_KEY_CONDITION = "condition";

    /**
     * The condition that makes up this parenthetical.
     */
    @JsonProperty(JSON_KEY_CONDITION)
    private final Condition condition;

    /**
     * Creates a new parenthetical backed by some condition.
     *
     * @param condition
     *        The condition that backs this parenthetical.
     *
     * @throws InvalidArgumentException
     *         The underlying condition was null.
     */
    @JsonCreator
    public Parenthetical(
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition)
        throws InvalidArgumentException {

        if(condition == null) {
            throw
                new InvalidArgumentException(
                    "The parenthetical condition is null.");
        }

        this.condition = condition;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Fragment#validate(java.util.Map)
     */
    @Override
    public void validate(final Map<String, SurveyItem> surveyItems)
        throws InvalidArgumentException {

        condition.validate(surveyItems);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Fragment#evaluate(java.util.Map)
     */
    @Override
    public boolean evaluate(final Map<String, Object> responses) {
        return condition.evaluate(responses);
    }

    @Override
    public String toString() {
        return "(" + condition.toString() + ")";
    }
}
