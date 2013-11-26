package org.ohmage.domain.survey.prompt;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A prompt for the user to add an audio file.
 * </p>
 *
 * @author John Jenkins
 */
public class AudioPrompt extends MediaPrompt {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "audio_prompt";

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
    public AudioPrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE) final String defaultResponse)
        throws InvalidArgumentException {

        super(surveyItemId, condition, text, skippable, defaultResponse);

        if(defaultResponse != null) {
            throw
                new InvalidArgumentException(
                    "Default responses are not allowed for media prompts.");
        }
    }
}