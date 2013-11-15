package org.ohmage.domain.survey.response;

import java.util.Map;

import name.jenkins.paul.john.concordia.schema.Schema;

import org.ohmage.domain.exception.InvalidArgumentException;

public interface Respondable {
    /**
     * Returns the schema for a response to this survey item or null if the
     * survey item does not have a response.
     *
     * @return The schema for a response to this survey item or null if the
     *         survey item does not have a response.
     */
    public abstract Schema getResponseSchema();

    /**
     * Validates some response against this respondable object.
     *
     * @param response
     *        The response to validate.
     *
     * @param previousResponses
     *        The previous responses to use if the prompt was not displayed.
     *
     * @throws InvalidArgumentException
     *         The response is of the wrong type, skipped when it wasn't
     *         allowed to be skipped, or not displayed when the condition does
     *         not call for it.
     */
    public abstract void validateResponse(
        final Object response,
        final Map<String, Object> previousResponses)
        throws InvalidArgumentException;
}
