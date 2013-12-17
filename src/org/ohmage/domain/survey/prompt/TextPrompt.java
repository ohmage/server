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
 * A prompt for the user to enter text.
 * </p>
 *
 * @author John Jenkins
 */
public class TextPrompt extends Prompt<String> {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "text_prompt";

    /**
     * The JSON key for the minimum length.
     */
    public static final String JSON_KEY_MIN = "min";
    /**
     * The JSON key for the maximum length.
     */
    public static final String JSON_KEY_MAX = "max";

    /**
     * The minimum allowed length for a response.
     */
    @JsonProperty(JSON_KEY_MIN)
    private final Long min;
    /**
     * The maximum allowed length for a response.
     */
    @JsonProperty(JSON_KEY_MAX)
    private final Long max;

    /**
     * Creates a new text prompt.
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
    public TextPrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE) final String defaultResponse,
        @JsonProperty(JSON_KEY_MIN) final Long min,
        @JsonProperty(JSON_KEY_MAX) final Long max)
        throws InvalidArgumentException {

        super(
            surveyItemId,
            condition,
            text,
            displayLabel,
            skippable,
            defaultResponse);

        this.min = min;
        this.max = max;
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
    public String validateResponse(
        final String response,
        final Map<String, Media> media)
        throws InvalidArgumentException {

        // If a 'min' exists, check that the response conforms.
        if((min != null) && (response.length() < min)) {
            throw
                new InvalidArgumentException(
                    "The response was too short: " + getSurveyItemId());
        }

        // If a 'max' exists, check that the response conforms.
        if((max != null) && (response.length() > max)) {
            throw
                new InvalidArgumentException(
                    "The response was too long: " + getSurveyItemId());
        }

        return response;
    }
}