package org.ohmage.domain.survey.condition;

import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.comparator.Comparator;
import org.ohmage.domain.survey.condition.conjunction.Conjunction;
import org.ohmage.domain.survey.condition.terminal.Terminal;

/**
 * <p>
 * A generic type for all fragments. This could be anything from a prompt
 * reference to a complex 'AND' condition.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class Fragment {
    /**
     * <p>
     * The root type for all fragment builders.
     * </p>
     *
     * @author John Jenkins
     */
    public static interface Builder<T extends Fragment> {
        /**
         * Merges another Builder with this Builder.
         *
         * @param other
         *        The Builder that should be merged into this Builder.
         *
         * @return After the merge, this is the Builder that should be treated
         *         as the new root.
         *
         * @throws InvalidArgumentException
         *         The parameterized builder is not valid for this builder.
         */
        public abstract Fragment.Builder<?> merge(
            final Fragment.Builder<?> other)
            throws InvalidArgumentException;

        /**
         * Builds the {@link Fragment} to which this builder is associated.
         *
         * @return The validated {@link Fragment} tree element.
         *
         * @throws InvalidArgumentException
         *         This builder or one of its components is not in a valid
         *         state.
         */
        public abstract T build() throws InvalidArgumentException;
    }

    /**
     * Validates that the condition is valid given the previous survey items.
     *
     * @param surveyItems
     *        A map of survey item IDs to their corresponding
     *        {@link SurveyItem}.
     *
     * @throws InvalidArgumentException
     *         The condition is invalid.
     */
    public abstract void validate(final Map<String, SurveyItem> surveyItems)
        throws InvalidArgumentException;

    /**
     * Determines if this condition is true based on a set of given responses.
     *
     * @param responses
     *        The set of responses.
     *
     * @return True if the condition evaluates as such based on the responses;
     *         false, otherwise.
     */
    public abstract boolean evaluate(final Map<String, Object> responses);

    /**
     * Parses a word by determining if it is one of the static fragments,
     * e.g. {@link AND}, {@link OR}, {@link EQUALS}, etc, and, if none
     * match, a {@link TerminalBuilder} is generated and returned.
     *
     * @param word
     *        The word to parse.
     *
     * @return The builder that represents the fragment.
     */
    public static Fragment.Builder<?> parseWord(final String word) {
        // FIXME: The profiler shows that this is horribly slow. Given that
        // many of the possibilities are static values, e.g. "AND", "OR", "<=",
        // there should probably be some lookup Map that is first consulted
        // and, if that fails, the Terminal.parseWord() should then be
        // consulted.

        // Create a handler for the builder.
        Fragment.Builder<?> result = null;

        // First, try a NOT.
        if(Not.VALUE.equals(word)) {
            result = new Not.Builder();
        }

        // Then, try a conjunction.
        if(result == null) {
            result = Conjunction.parseWord(word);
        }

        // If not a conjunction, try a conditional.
        if(result == null) {
            result = Comparator.parseWord(word);
        }

        // Finally, try a terminal, which should always return something.
        if(result == null) {
            result = Terminal.parseWord(word);
        }

        return result;
    }
}