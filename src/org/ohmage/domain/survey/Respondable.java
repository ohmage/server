package org.ohmage.domain.survey;

import java.util.Map;

import name.jenkins.paul.john.concordia.schema.Schema;

import org.ohmage.domain.exception.InvalidArgumentException;

/**
 * <p>
 * Indicates whether or not a survey item may have responses.
 * </p>
 *
 * @author John Jenkins
 */
public interface Respondable {
    /**
     * Returns the schema for a response to this survey item or null if the
     * survey item does not have a response.
     *
     * @return The schema for a response to this survey item or null if the
     *         survey item does not have a response.
     *
     * @throws IllegalStateException
     *         There was a problem building the response schema.
     */
    public abstract Schema getResponseSchema() throws IllegalStateException;

    /**
     * Validates some response against this respondable object.
     *
     * @param response
     *        The response to validate.
     *
     * @param previousResponses
     *        The previous responses to use if the prompt was not displayed.
     *
     * @param media
     *        The map from unique identifiers to input streams for the media
     *        that may be uploaded, e.g. images, audio, video, etc.
     *
     * @throws InvalidArgumentException
     *         The response is of the wrong type, skipped when it wasn't
     *         allowed to be skipped, or not displayed when the condition does
     *         not call for it.
     */
    public abstract void validateResponse(
        final Object response,
        final Map<String, Object> previousResponses,
        final Map<String, Media> media)
        throws InvalidArgumentException;
}