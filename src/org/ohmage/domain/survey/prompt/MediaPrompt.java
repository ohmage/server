package org.ohmage.domain.survey.prompt;

import java.util.Map;

import name.jenkins.paul.john.concordia.schema.Schema;
import name.jenkins.paul.john.concordia.schema.StringSchema;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The base class for all media-related prompts.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class MediaPrompt extends Prompt<String> {
    /**
     * Creates a new media prompt.
     *
     * @param id
     *        The survey-unique identifier for this prompt.
     *
     * @param condition
     *        The condition on whether or not to show this prompt.
     *
     * @param text
     *        The text to display to the user.
     *
     * @param displayLabel
     *        The text to use as a short name in visualizations.
     *
     * @param displayLabel
     *        The text to use as a short name in visualizations.
     *
     * @param skippable
     *        Whether or not this prompt may be skipped.
     *
     * @param defaultResponse
     *        The default response for this prompt or null if a default is not
     *        allowed.
     *
     * @param min
     *        The minimum allowed length for a response or null if any minimum
     *        length is allowed.
     *
     * @param max
     *        The maximum allowed length for a response or null if any maximum
     *        length is allowed.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public MediaPrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE) final String defaultResponse)
        throws InvalidArgumentException {

        super(
            surveyItemId,
            condition,
            text,
            displayLabel,
            skippable,
            defaultResponse);

        if(defaultResponse != null) {
            throw
                new InvalidArgumentException(
                    "Default responses are not allowed for media prompts.");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.Respondable#getResponseSchema()
     */
    @Override
    public Schema getResponseSchema() {
        return
            new StringSchema(
                getText(),
                (skippable() || (getCondition() != null)),
                getSurveyItemId());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.prompt.Prompt#validateResponse(java.lang.Object, java.util.Map)
     */
    @Override
    public final String validateResponse(
        final String response,
        final Map<String, Media> media)
        throws InvalidArgumentException {

        // Retrieve the media and ensure that it exists.
        Media mediaResponse = media.get(response);
        if(mediaResponse == null) {
            throw
                new InvalidArgumentException(
                    "The media is missing: " + getSurveyItemId());
        }

        // Pass the validation onto the specific media-type validator.
        validateResponse(mediaResponse);

        // Update the media survey response.
        return mediaResponse.getId();
    }

    /**
     * Validate that the media is valid.
     *
     * @param response
     *        The media to validate.
     *
     * @throws InvalidArgumentException
     *         The media is invalid.
     */
    public abstract void validateResponse(final Media response)
        throws InvalidArgumentException;
}