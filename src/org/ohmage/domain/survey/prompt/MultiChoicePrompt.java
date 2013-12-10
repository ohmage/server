package org.ohmage.domain.survey.prompt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import name.jenkins.paul.john.concordia.exception.ConcordiaException;
import name.jenkins.paul.john.concordia.schema.ArraySchema;
import name.jenkins.paul.john.concordia.schema.Schema;
import name.jenkins.paul.john.concordia.schema.StringSchema;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.condition.Condition;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A prompt for the user to make any number of choices among a list of choices.
 * </p>
 *
 * @author John Jenkins
 */
public class MultiChoicePrompt extends ChoicePrompt<Collection<String>> {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "multi_choice_prompt";

    /**
     * The JSON key for the minimum number of choices.
     */
    public static final String JSON_KEY_MIN_CHOICES = "min_choices";
    /**
     * The JSON key for the maximum number of choices.
     */
    public static final String JSON_KEY_MAX_CHOICES = "max_choices";

    /**
     * The minimum number of choices the user must select.
     */
    @JsonProperty(JSON_KEY_MIN_CHOICES)
    private final Integer minChoices;
    /**
     * The maximum number of choices the user may select.
     */
    @JsonProperty(JSON_KEY_MAX_CHOICES)
    private final Integer maxChoices;

    /**
     * Creates a new multi-choice prompt.
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
     * @param choices
     *        The list of choices.
     *
     * @param allowCustom
     *        Whether or not custom choices are allowed.
     *
     * @param minChoices
     *        The minimum number of choices the user must select.
     *
     * @param maxChoices
     *        The maximum number of choices the user may select.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public MultiChoicePrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE)
            final Set<String> defaultResponse,
        @JsonProperty(JSON_KEY_CHOICES) final List<Choice> choices,
        @JsonProperty(JSON_KEY_ALLOW_CUSTOM) final Boolean allowCustom,
        @JsonProperty(JSON_KEY_MIN_CHOICES) final Integer minChoices,
        @JsonProperty(JSON_KEY_MAX_CHOICES) final Integer maxChoices)
        throws InvalidArgumentException {

        super(
            surveyItemId,
            condition,
            text,
            displayLabel,
            skippable,
            defaultResponse,
            choices,
            allowCustom);

        for(String defaultResponseLabel : defaultResponse) {
            if(
                (defaultResponse != null) &&
                (getChoice(defaultResponseLabel) == null)) {

                throw
                    new InvalidArgumentException(
                        "The default response '" +
                            defaultResponseLabel +
                            "' is unknown: " +
                            getSurveyItemId());
            }
        }

        this.minChoices = minChoices;
        this.maxChoices = maxChoices;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.Respondable#getResponseSchema()
     */
    @Override
    public Schema getResponseSchema() throws IllegalStateException {
        try {
            return
                new ArraySchema(
                    getText(),
                    (skippable() || (getCondition() != null)),
                    getSurveyItemId(),
                    new StringSchema(null, false, null));
        }
        catch(ConcordiaException e) {
            throw
                new IllegalStateException(
                    "There was a problem building the multi-choice prompt's " +
                        "schema.",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.prompt.Prompt#validateResponse(java.lang.Object, java.util.Map)
     */
    @Override
    public void validateResponse(
        final Collection<String> response,
        final Map<String, MultipartFile> media)
        throws InvalidArgumentException {

        if(! allowsCustom()) {
            for(String responseLabel : response) {
                if(getChoice(responseLabel) == null) {
                    throw
                        new InvalidArgumentException(
                            "The response value '" +
                                response +
                                "' is unknown: " +
                                getSurveyItemId());
                }
            }
        }

        if((minChoices != null) && (response.size() < minChoices)) {
            throw
                new InvalidArgumentException(
                    "The user must select at least " +
                        minChoices +
                        " choices: " +
                        getSurveyItemId());
        }

        if((maxChoices != null) && (response.size() > maxChoices)) {
            throw
                new InvalidArgumentException(
                    "The user may select at most " +
                        maxChoices +
                        " choices: " +
                        getSurveyItemId());
        }
    }
}