package org.ohmage.domain.survey.prompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.jackson.MapValuesJsonSerializer;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * <p>
 * The parent class for all choice prompts.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class ChoicePrompt<ChoiceType, ResponseType>
    extends Prompt<ResponseType> {

    /**
     * <p>
     * A choice for the choice prompts.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Choice<ChoiceType> {
        /**
         * The JSON key for the text, which may be
         */
        public static final String JSON_KEY_TEXT = "text";
        /**
         * The JSON key for the value.
         */
        public static final String JSON_KEY_VALUE = "value";

        /**
         * The text to display to the user.
         */
        @JsonProperty(JSON_KEY_TEXT)
        private final String text;
        /**
         * An optional value for this prompt for data processors.
         */
        @JsonProperty(JSON_KEY_VALUE)
        private final ChoiceType value;

        /**
         * Creates a new choice.
         *
         * @param text
         *        The text to show to the user, which may be null.
         *
         * @param value
         *        The value that represents this choice, which may be null.
         */
        @JsonCreator
        public Choice(
            @JsonProperty(JSON_KEY_TEXT) final String text,
            @JsonProperty(JSON_KEY_VALUE) final ChoiceType value)
            throws InvalidArgumentException {

            if(value == null) {
                throw new InvalidArgumentException("The value is null.");
            }

            this.text = text;
            this.value = value;
        }

        /**
         * Returns the text for this choice, which will be shown to the user
         * if given.
         *
         * @return The text for this choice, which may be null.
         */
        public String getText() {
            return text;
        }

        /**
         * Returns the value of this choice.
         *
         * @return The value of this choice.
         */
        public ChoiceType getValue() {
            return value;
        }
    }

    /**
     * The default flag indicating whether or not custom values are allowed.
     */
    public static final boolean DEFAULT_ALLOW_CUSTOM = false;

    /**
     * The JSON key for the choices.
     */
    public static final String JSON_KEY_CHOICES = "choices";

    /**
     * The map of choices from their value to their actual {@link Choice}
     * object.
     */
    @JsonProperty(JSON_KEY_CHOICES)
    @JsonSerialize(using = MapValuesJsonSerializer.class)
    private final Map<ChoiceType, Choice<? extends ChoiceType>> choices;

    /**
     * Creates a parent choice prompt.
     *
     * @param surveyItemId
     *        The survey-unique identifier for this prompt.
     *
     * @param condition
     *        The condition on whether or not to show this prompt.
     *
     * @param displayType
     *        The display type to use to visualize the prompt.
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
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public ChoicePrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_DISPLAY_TYPE) final DisplayType displayType,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE)
            final ResponseType defaultResponse,
        @JsonProperty(JSON_KEY_CHOICES)
            final List<? extends Choice<? extends ChoiceType>> choices)
        throws InvalidArgumentException {

        super(
            surveyItemId,
            condition,
            displayType,
            text,
            displayLabel,
            skippable,
            defaultResponse);

        // Ensure the choices list is not null.
        if(choices == null) {
            throw
                new InvalidArgumentException(
                    "The list of chioces is missing: " + getSurveyItemId());
        }
        // Validate that at least one choice was given.
        if(choices.size() == 0) {
            throw
                new InvalidArgumentException(
                    "No choices were given: " + getSurveyItemId());
        }

        // Create a lookup table for the choices.
        this.choices =
            new HashMap<ChoiceType, Choice<? extends ChoiceType>>(
                choices.size());
        // Examine each choice.
        for(Choice<? extends ChoiceType> choice : choices) {
            // Be sure the value does not already exist.
            if(this.choices.put(choice.getValue(), choice) != null) {
                throw
                    new InvalidArgumentException(
                        "Two choices have the same value '" +
                            choice.getText() +
                            "': " +
                            getSurveyItemId());
            }
        }
    }

    /**
     * Returns a choice based on the
     *
     * @param key
     *        The key to use to find the desired choice.
     *
     * @return The {@link Choice} object or null if the key is unknown.
     */
    public Choice<? extends ChoiceType> getChoice(final ChoiceType key) {
        return choices.get(key);
    }
}