package org.ohmage.domain.survey.response;

/**
 * <p>
 * Prompt responses for the case where an actual response is not given, e.g.
 * the prompt was skipped.
 * </p>
 *
 * @author John Jenkins
 */
public enum NoResponse {
    /**
     * The prompt was not displayed probably due to a conditional.
     */
    NOT_DISPLAYED,
    /**
     * The user explicitly skipped the prompt.
     */
    SKIPPED;

    /**
     * Returns the enum as a human-readable value.
     */
    @Override
    public String toString() {
    	return name().toLowerCase();
    }
}