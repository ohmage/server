package org.ohmage.domain.survey.condition.terminal;

import java.util.Map;

import org.ohmage.domain.survey.NoResponse;
import org.ohmage.domain.survey.condition.Fragment;

/**
 * <p>
 * The {@link Fragment} that represents a concrete object, e.g. prompt ID,
 * string, number, etc.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class Terminal extends Fragment {
    /**
     * <p>
     * A marker interface for builders for {@link Terminal}s that extends the
     * {@link Fragment.Builder}.
     * </p>
     *
     * @author John Jenkins
     */
    public static interface Builder<T extends Terminal>
        extends Fragment.Builder<T> {}

    /**
     * Parses a word into its appropriate Terminal value. If it is not one
     * of the statically named Terminals, e.g. SKIPPED or NOT_DISPLAYED, it
     * will be evaluated as either a string (surrounded in quotes) or a
     * number. If neither of those is appropriate, it will be returned as a
     * prompt ID.
     *
     * @param word
     *        The word to parse into the Terminal.
     *
     * @return The Terminal that represents this word.
     */
    public static Fragment.Builder<?> parseWord(final String token) {
        if(token.equals(NoResponse.NOT_DISPLAYED.toString())) {
            return new NotDisplayed.Builder();
        }
        if(token.equals(NoResponse.SKIPPED.toString())) {
            return new Skipped.Builder();
        }
        if(token.startsWith("\"")) {
            return new Text.Builder(token);
        }
        try {
            return new Numeric.Builder(Double.parseDouble(token));
        }
        catch(NumberFormatException e) {
            // It is not a number.
        }

        return new PromptId.Builder(token);
    }

    /**
     * Returns the value of this terminal. If it is a prompt ID, it will use
     * the parameterized responses to decode the response value. Otherwise, it
     * will simply return its value.
     *
     * @param responses
     *        The list of responses from the user.
     *
     * @return The value of this terminal.
     */
    public abstract Object getValue(final Map<String, Object> responses);

    /**
     * Compares this terminal with a value and returns true only if this
     * terminal's value can be compared to the given value and has some value
     * that is less than the given value. This does not do type coercion, so 0
     * compared with "1" or "one" or NOT_DISPLAYED or SKIPPED will all return
     * false because they are not numbers.
     *
     * @param responses
     *        The responses to use if a prompt's value is needed.
     *
     * @param value
     *        The value to compare with this terminal.
     *
     * @return True if the value of this terminal and the parameterized value
     *         are compatible and this terminal's value represents some value
     *         that is less than the parameterized value.
     */
    public abstract boolean lessThanValue(
        final Map<String, Object> responses,
        final Object value);

    /**
     * Compares this terminal with a value and returns true only if this
     * terminal's value can be compared to the given value and has some
     * representation that is equal to the given value's representation. This
     * does not do type coercion, so 1 compared with "1" or "one" or
     * NOT_DISPLAYED or SKIPPED will all return false because they are not
     * numbers.
     *
     * @param responses
     *        The responses to use if a prompt's value is needed.
     *
     * @param value
     *        The value to compare to this terminal.
     *
     * @return True if the value of this terminal and the parameterized value
     *         are compatible and this terminal's value represents some value
     *         that is equal to the parameterized value.
     */
    public abstract boolean equalsValue(
        final Map<String, Object> responses,
        final Object value);

    /**
     * Compares this terminal with a value and returns true only if this
     * terminal's value can be compared to the given value and has some value
     * that is greater than the given value. This does not do type coercion, so
     * 0 compared with "1" or "one" or NOT_DISPLAYED or SKIPPED will all return
     * false because they are not numbers.
     *
     * @param responses
     *        The responses to use if a prompt's value is needed.
     *
     * @param value
     *        The value to compare with this terminal.
     *
     * @return True if the value of this terminal and the parameterized value
     *         are compatible and this terminal's value represents some value
     *         that is greater than the parameterized value.
     */
    public abstract boolean greaterThanValue(
        final Map<String, Object> responses,
        final Object value);
}