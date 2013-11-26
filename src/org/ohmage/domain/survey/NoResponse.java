package org.ohmage.domain.survey;

/**
 * <p>
 * The possible values when a prompt does not have a response.
 * </p>
 *
 * @author John Jenkins
 */
public enum NoResponse {
    /**
     * Based on the condition associated with the prompt and the previous
     * prompt responses, this prompt should not have been displayed.
     */
    NOT_DISPLAYED,
    /**
     * The prompt was skipped by the user.
     */
    SKIPPED;
}
