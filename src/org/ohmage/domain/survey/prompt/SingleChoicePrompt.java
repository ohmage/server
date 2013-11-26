package org.ohmage.domain.survey.prompt;

import java.util.List;

import name.jenkins.paul.john.concordia.schema.Schema;
import name.jenkins.paul.john.concordia.schema.StringSchema;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A prompt for the user to make one choice among a list of choices.
 * </p>
 *
 * @author John Jenkins
 */
public class SingleChoicePrompt extends ChoicePrompt<String> {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "single_choice_prompt";

    /**
     * Creates a new single-choice prompt.
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
     * @param choices
     *        The list of choices.
     *
     * @param allowCustom
     *        Whether or not custom choices are allowed.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public SingleChoicePrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE) final String defaultResponse,
        @JsonProperty(JSON_KEY_CHOICES) final List<Choice> choices,
        @JsonProperty(JSON_KEY_ALLOW_CUSTOM) final Boolean allowCustom)
        throws InvalidArgumentException {

        super(
            surveyItemId,
            condition,
            text,
            skippable,
            defaultResponse,
            choices,
            allowCustom);

        if((defaultResponse != null) && (getChoice(defaultResponse) == null)) {
            throw
                new InvalidArgumentException(
                    "The default response '" +
                        defaultResponse +
                        "' is unknown: " +
                        getSurveyItemId());
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
     * @see org.ohmage.domain.survey.Prompt#validateResponse(java.lang.Object)
     */
    @Override
    public void validateResponse(final String response)
        throws InvalidArgumentException {

        if((! allowsCustom()) && (getChoice(response) == null)) {
            throw
                new InvalidArgumentException(
                    "The response value '" +
                        response +
                        "' is unknown: " +
                        getSurveyItemId());
        }
    }
}