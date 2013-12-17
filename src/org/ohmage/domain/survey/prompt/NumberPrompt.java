package org.ohmage.domain.survey.prompt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import name.jenkins.paul.john.concordia.schema.NumberSchema;
import name.jenkins.paul.john.concordia.schema.Schema;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A prompt for the user to enter a numeric value.
 * </p>
 *
 * @author John Jenkins
 */
public class NumberPrompt extends Prompt<BigDecimal> {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "number_prompt";

    /**
     * The JSON key for the minimum value.
     */
    public static final String JSON_KEY_MIN = "min";
    /**
     * The JSON key for the maximum value.
     */
    public static final String JSON_KEY_MAX = "max";
    /**
     * The JSON key for whether or not the response must be a whole number.
     */
    public static final String JSON_KEY_WHOLE_NUMBERS_ONLY =
        "whole_numbers_only";

    /**
     * The minimum allowed value for a response.
     */
    @JsonProperty(JSON_KEY_MIN)
    private final BigDecimal min;
    /**
     * The maximum allowed value for a response.
     */
    @JsonProperty(JSON_KEY_MAX)
    private final BigDecimal max;
    /**
     * Whether or not only whole numbers are allowed.
     */
    @JsonProperty(JSON_KEY_WHOLE_NUMBERS_ONLY)
    private final boolean wholeNumbersOnly;

    /**
     * Creates a new number prompt.
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
     *        The minimum allowed value for a response or null if there is no
     *        minimum value.
     *
     * @param max
     *        The maximum allowed value for a response or null if there is no
     *        maximum value.
     *
     * @param wholeNumbersOnly
     *        Whether or not the response must be whole number as opposed to a
     *        decimal. If null, the default is false.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public NumberPrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE)
            final BigDecimal defaultResponse,
        @JsonProperty(JSON_KEY_MIN) final BigDecimal min,
        @JsonProperty(JSON_KEY_MAX) final BigDecimal max,
        @JsonProperty(JSON_KEY_WHOLE_NUMBERS_ONLY)
            final boolean wholeNumbersOnly)
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
        this.wholeNumbersOnly = wholeNumbersOnly;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.Respondable#getResponseSchema()
     */
    @Override
    public Schema getResponseSchema() {
        return
            new NumberSchema(
                getText(),
                (skippable() || (getCondition() != null)),
                getSurveyItemId());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.prompt.Prompt#validateResponse(java.lang.Object, java.util.Map)
     */
    @Override
    public BigDecimal validateResponse(
        final BigDecimal response,
        final Map<String, Media> media)
        throws InvalidArgumentException {

        // If a 'min' exists, check that the response conforms.
        if((min != null) && (response.compareTo(min) < 0)) {
            throw
                new InvalidArgumentException(
                    "The response was less than the allowed minimum: " +
                        getSurveyItemId());
        }

        // If a 'max' exists, check that the response conforms.
        if((max != null) && (response.compareTo(max) > 0)) {
            throw
                new InvalidArgumentException(
                    "The response was greater than the allowed maximum: " +
                        getSurveyItemId());
        }

        // If decimals are not allowed, check that the response conforms.
        if(wholeNumbersOnly && (! isWholeNumber(response))) {
            throw
                new InvalidArgumentException(
                    "The response must be a whole number: " +
                        getSurveyItemId());
        }

        return response;
    }

    /**
     * Returns whether or not a given value is a whole number.
     *
     * @param value
     *        The value to check.
     *
     * @return True if the BigDecimal is a whole number; false, otherwise.
     */
    protected static boolean isWholeNumber(final BigDecimal value) {
        try {
            return value.setScale(0, RoundingMode.DOWN).compareTo(value) == 0;
        }
        catch(ArithmeticException e) {
                return false;
        }
    }
}