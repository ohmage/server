package org.ohmage.domain.survey.condition.comparator;

import org.ohmage.domain.survey.condition.Fragment;
import org.ohmage.domain.survey.condition.terminal.Terminal;

/**
 * <p>
 * A {@link Fragment} that represents comparing two {@link Terminal} values.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class Comparator extends Fragment {
    /**
     * <p>
     * A marker interface for builders for {@link Comparator}s that extends the
     * {@link Fragment.Builder}.
     * </p>
     *
     * @author John Jenkins
     */
    public static interface Builder<T extends Comparator>
        extends Fragment.Builder<T> {}

    /**
     * Parses a word into its appropriate Comparator value or returns null
     * if no corresponding Comparator exists.
     *
     * @param word
     *        The word to parse into the Comparator.
     *
     * @return The Comparator that represents this word or null if no such
     *         Comparator exists.
     */
    public static Fragment.Builder<?> parseWord(final String word) {
        switch(word) {
            case Equals.VALUE:
                return new Equals.Builder();
            case NotEquals.VALUE:
                return new NotEquals.Builder();
            case LessThan.VALUE:
                return new LessThan.Builder();
            case LessThanEquals.VALUE:
                return new LessThanEquals.Builder();
            case GreaterThan.VALUE:
                return new GreaterThan.Builder();
            case GreaterThanEquals.VALUE:
                return new GreaterThanEquals.Builder();
        }

        return null;
    }
}