package org.ohmage.domain.survey.prompt;

import name.jenkins.paul.john.concordia.schema.Schema;
import name.jenkins.paul.john.concordia.schema.StringSchema;

import org.joda.time.DateTime;
import org.ohmage.domain.ISOW3CDateTimeFormat;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A prompt for the user to enter a date and, optionally, time.
 * </p>
 *
 * @see ISOW3CDateTimeFormat
 *
 * @author John Jenkins
 */
public class TimestampPrompt extends Prompt<DateTime> {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "timestamp_prompt";

    /**
     * Creates a new timestamp prompt.
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
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public TimestampPrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE)
            final DateTime defaultResponse)
        throws InvalidArgumentException {

        super(surveyItemId, condition, text, skippable, defaultResponse);
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
     * @see org.ohmage.domain.survey.Prompt#validateResponse(java.lang.Object)
     */
    @Override
    public void validateResponse(final DateTime response)
        throws InvalidArgumentException {

        // If it was successfully deserialized into a DateTime object, it is
        // fine.
    }
}