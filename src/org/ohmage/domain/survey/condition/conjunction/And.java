package org.ohmage.domain.survey.condition.conjunction;

import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.Fragment;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A {@link Conjunction} that represents true when all of its children
 * represent true.
 * </p>
 *
 * @author John Jenkins
 */
public class And extends Conjunction {
    /**
     * <p>
     * A builder for {@link And} objects.
     * <p>
     *
     * @author John Jenkins
     */
    public static class Builder implements Conjunction.Builder<And> {
        /**
         * The builder for the left operand.
         */
        private Fragment.Builder<?> left;
        /**
         * The builder for the right operand.
         */
        private Fragment.Builder<?> right;

        /*
         * (non-Javadoc)
         * @see org.ohmage.domain.survey.condition.Condition.Fragment.Builder#merge(org.ohmage.domain.survey.condition.Condition.Fragment.Builder)
         */
        @Override
        public Fragment.Builder<?> merge(final Fragment.Builder<?> other) {
            if(left == null) {
                left = other;
            }
            else if(right == null) {
                right = other;
            }
            else {
                right = right.merge(other);
            }

            return this;
        }

        /*
         * (non-Javadoc)
         * @see org.ohmage.domain.survey.condition.Condition.Fragment.Builder#build()
         */
        @Override
        public And build() throws InvalidArgumentException {
            if(left == null) {
                throw
                    new InvalidArgumentException(
                        "The 'and' operator does not have a left operand.");
            }
            if(right == null) {
                throw
                    new InvalidArgumentException(
                        "The 'and' operator does not have a right operand.");
            }
            return new And(left.build(), right.build());
        }
    }

    /**
     * The string value of an {@link And} within a condition sentence.
     */
    public static final String VALUE = "AND";

    /**
     * The JSON key for the left operand.
     */
    public static final String JSON_KEY_LEFT_OPERAND = "left";
    /**
     * The JSON key for the right operand.
     */
    public static final String JSON_KEY_RIGHT_OPERAND = "right";

    /**
     * The left operand.
     */
    @JsonProperty(JSON_KEY_LEFT_OPERAND)
    private final Fragment left;
    /**
     * The right operand.
     */
    @JsonProperty(JSON_KEY_RIGHT_OPERAND)
    private final Fragment right;

    /**
     * Creates a new And object with left and right operands.
     *
     * @param left
     *        The left operand.
     *
     * @param right
     *        The right operand.
     *
     * @throws InvalidArgumentException
     *         One or both of the operands is null.
     */
    public And(
        final Fragment left,
        final Fragment right)
        throws InvalidArgumentException {

        if(left == null) {
            throw
                new InvalidArgumentException(
                    "The left operand is missing.");
        }
        if(right == null) {
            throw
                new InvalidArgumentException(
                    "The right operand is missing.");
        }

        this.left = left;
        this.right = right;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Fragment#validate(java.util.Map)
     */
    @Override
    public void validate(final Map<String, SurveyItem> surveyItems)
        throws InvalidArgumentException {

        left.validate(surveyItems);
        right.validate(surveyItems);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Fragment#evaluate(java.util.Map)
     */
    @Override
    public boolean evaluate(final Map<String, Object> responses) {
        return left.evaluate(responses) && right.evaluate(responses);
    }

    @Override
    public String toString() {
        return left.toString() + " AND " + right.toString();
    }
}