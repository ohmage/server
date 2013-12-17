package org.ohmage.domain.survey.prompt;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A prompt for the user to submit a video file.
 * </p>
 *
 * @author John Jenkins
 */
public class VideoPrompt extends MediaPrompt {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "video_prompt";

    /**
     * The JSON key for the maximum duration.
     */
    public static final String JSON_KEY_MAX_DURATION = "max_duration";

    /**
     * The maximum allowed duration of an audio file in seconds.
     */
    @JsonProperty(JSON_KEY_MAX_DURATION)
    private final Long maxDuration;

    /**
     * Creates a new video prompt.
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
     * @param skippable
     *        Whether or not this prompt may be skipped.
     *
     * @param defaultResponse
     *        The default response for this prompt or null if a default is not
     *        allowed.
     *
     * @param maxDuration
     *        The maximum allowed duration in seconds for a video file.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public VideoPrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE) final String defaultResponse,
        @JsonProperty(JSON_KEY_MAX_DURATION) final Long maxDuration)
        throws InvalidArgumentException {

        super(
            surveyItemId,
            condition,
            text,
            displayLabel,
            skippable,
            defaultResponse);

        this.maxDuration = maxDuration;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.prompt.MediaPrompt#validateResponse(org.ohmage.domain.survey.Media)
     */
    @Override
    public void validateResponse(final Media response)
        throws InvalidArgumentException {

        // For now, we are not placing any video-specific limitations on their
        // responses.
    }
}