package org.ohmage.domain.survey.condition.conjunction;

import org.ohmage.domain.survey.condition.Fragment;

/**
 * <p>
 * A {@link Fragment} that represents combining two boolean expressions.
 * <p>
 *
 * @author John Jenkins
 */
public abstract class Conjunction extends Fragment {
    /**
     * <p>
     * A marker interface for builders for {@link Conjunction}s that extends
     * the {@link Fragment.Builder}.
     * </p>
     *
     * @author John Jenkins
     */
    public static interface Builder<T extends Conjunction>
        extends Fragment.Builder<T> {}

    /**
     * Parses a word into its appropriate Conjunction value or returns null
     * if no corresponding Conjunction exists.
     *
     * @param word
     *        The word to parse into the Conjunction.
     *
     * @return The Conjunction that represents this word or null if no such
     *         Conjunction exists.
     */
    public static Fragment.Builder<? extends Conjunction> parseWord(
        final String word) {

        switch(word) {
            case And.VALUE:
                return new And.Builder();
            case Or.VALUE:
                return new Or.Builder();
        }

        return null;
    }
}
