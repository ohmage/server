package org.ohmage.domain.survey.condition.comparator;

import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.Fragment;
import org.ohmage.domain.survey.condition.terminal.Numeric;
import org.ohmage.domain.survey.condition.terminal.PromptId;
import org.ohmage.domain.survey.condition.terminal.Terminal;
import org.ohmage.domain.survey.prompt.NumberPrompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A {@link Comparator} that returns true when a left {@link Terminal} is less
 * than or equal to a right {@link Terminal}. Both {@link Terminal}s must be
 * either be a {@link PromptId} that references a number prompt or
 * {@link Numeric}.
 * </p>
 *
 * @author John Jenkins
 */
public class LessThanEquals extends Comparator {
    /**
     * <p>
     * A builder for {@link LessThanEquals} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder implements Comparator.Builder<LessThanEquals> {
        /**
         * The builder for the left operand.
         */
        private Terminal.Builder<?> left;
        /**
         * The builder for the right operand.
         */
        private Terminal.Builder<?> right;

        /*
         * (non-Javadoc)
         * @see org.ohmage.domain.survey.condition.Condition.Fragment.Builder#merge(org.ohmage.domain.survey.condition.Condition.Fragment.Builder)
         */
        @Override
        public Fragment.Builder<?> merge(final Fragment.Builder<?> other) {
            if(other instanceof Terminal.Builder<?>) {
                if(left == null) {
                    left = (Terminal.Builder<?>) other;
                }
                else if(right == null) {
                    right = (Terminal.Builder<?>) other;
                }
                else {
                    throw
                        new InvalidArgumentException(
                            "Multiple terminals are in sequence.");
                }

                return this;
            }
            else if(other instanceof Comparator.Builder<?>) {
                throw
                    new InvalidArgumentException(
                        "A comparator cannot be compared to another " +
                            "comparator.");
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
        public LessThanEquals build() throws InvalidArgumentException {
            if(left == null) {
                throw
                    new InvalidArgumentException(
                        "The 'less than or equals' does not have a left " +
                            "operand.");
            }
            if(right == null) {
                throw
                    new InvalidArgumentException(
                        "The 'less than or equals' does not have a right " +
                            "operand.");
            }

            return new LessThanEquals(left.build(), right.build());
        }
    }

    /**
     * The string value of an {@link LessThanEquals} within a condition
     * sentence.
     */
    public static final String VALUE = "<=";

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
    private final Terminal left;
    /**
     * The right operand.
     */
    @JsonProperty(JSON_KEY_RIGHT_OPERAND)
    private final Terminal right;

    /**
     * Creates a new LessThanEquals object with left and right operands.
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
    @JsonCreator
    public LessThanEquals(
        @JsonProperty(JSON_KEY_LEFT_OPERAND) final Fragment left,
        @JsonProperty(JSON_KEY_RIGHT_OPERAND)final Fragment right)
        throws InvalidArgumentException {

        if(left == null) {
            throw
                new InvalidArgumentException(
                    "The left operand is missing.");
        }
        if(! (left instanceof Terminal)) {
            throw
            new InvalidArgumentException(
                "The left operand is not a terminal value.");
        }
        if(right == null) {
            throw
                new InvalidArgumentException(
                    "The right operand is missing.");
        }
        if(! (right instanceof Terminal)) {
            throw
            new InvalidArgumentException(
                "The right operand is not a terminal value.");
        }

        this.left = (Terminal) left;
        this.right = (Terminal) right;
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

        boolean fail = false;
        if(! (left instanceof Numeric)) {
            if(left instanceof PromptId) {
                SurveyItem surveyItem =
                    surveyItems.get(((PromptId) left).getPromptId());

                if(! (surveyItem instanceof NumberPrompt)) {
                    fail = true;
                }
            }
            else {
                fail = true;
            }
        }
        if(! (right instanceof Numeric)) {
            if(right instanceof PromptId) {
                SurveyItem surveyItem =
                    surveyItems.get(((PromptId) right).getPromptId());

                if(! (surveyItem instanceof NumberPrompt)) {
                    fail = true;
                }
            }
            else {
                fail = true;
            }
        }

        if(fail) {
            throw
                new InvalidArgumentException(
                    "The 'less than or equals' operator may only be used " +
                        "with number prompts and numbers.");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.condition.Fragment#evaluate(java.util.Map)
     */
    @Override
    public boolean evaluate(final Map<String, Object> responses) {
        Object rightValue = right.getValue(responses);

        return
            left.lessThanValue(responses, rightValue) ||
            left.equalsValue(responses, rightValue);
    }

    @Override
    public String toString() {
        return left.toString() + " <= " + right.toString();
    }
}