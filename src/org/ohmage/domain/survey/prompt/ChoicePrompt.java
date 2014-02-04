package org.ohmage.domain.survey.prompt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public abstract class ChoicePrompt<ResponseType> extends Prompt<ResponseType> {
    /**
     * <p>
     * A choice for the choice prompts.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Choice {
        /**
         * The JSON key for the key.
         */
        public static final String JSON_KEY_KEY = "key";
        /**
         * The JSON key for the label.
         */
        public static final String JSON_KEY_LABEL = "label";
        /**
         * The JSON key for the value.
         */
        public static final String JSON_KEY_VALUE = "value";

        /**
         * The key for this choice, which will be used for ordering when
         * displaying it to the user.
         */
        @JsonProperty(JSON_KEY_KEY)
        private final int key;
        /**
         * The label to display to the user.
         */
        @JsonProperty(JSON_KEY_LABEL)
        private final String label;
        /**
         * An optional value for this prompt for data processors.
         */
        @JsonProperty(JSON_KEY_VALUE)
        private final Number value;

        /**
         * Creates a new choice.
         *
         * @param key
         *        The key for this choice which will be used for ordering.
         *
         * @param label
         *        The label to show to the user.
         *
         * @param value
         *        The value that represents this choice, which may be null.
         */
        @JsonCreator
        public Choice(
            @JsonProperty(JSON_KEY_KEY) final int key,
            @JsonProperty(JSON_KEY_LABEL) final String label,
            @JsonProperty(JSON_KEY_VALUE) final Number value)
            throws InvalidArgumentException {

            if(label == null) {
                throw new InvalidArgumentException("The label is null.");
            }

            this.key = key;
            this.label = label;
            this.value = value;
        }

        /**
         * Returns the key for this choice, which will be used for ordering.
         *
         * @return The key for this choice.
         */
        public int getKey() {
            return key;
        }

        /**
         * Returns the label for this choice, which will be shown to the user.
         *
         * @return The label for this choice.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Returns the value of this choice or null. The value can be a hint to
         * data processors.
         *
         * @return The value of this choice or null.
         */
        public Number getValue() {
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
     * The JSON key for whether or not custom choices are allowed.
     */
    public static final String JSON_KEY_ALLOW_CUSTOM = "allow_custom";

    /**
     * The map of choices from their label to their actual {@link Choice}
     * object.
     */
    @JsonProperty(JSON_KEY_CHOICES)
    @JsonSerialize(using = MapValuesJsonSerializer.class)
    private final Map<String, Choice> choices;
    /**
     * Whether or not custom choices are allowed to be added by the user.
     */
    @JsonProperty(JSON_KEY_ALLOW_CUSTOM)
    private final boolean allowCustom;

    /**
     * Creates a parent choice prompt.
     *
     * @param displayType
     *        The display type to use to visualize the prompt.
     *
     * @param surveyItemId
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
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public ChoicePrompt(
        @JsonProperty(JSON_KEY_DISPLAY_TYPE) final String displayType,
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE)
            final ResponseType defaultResponse,
        @JsonProperty(JSON_KEY_CHOICES) final List<Choice> choices,
        @JsonProperty(JSON_KEY_ALLOW_CUSTOM) final Boolean allowCustom)
        throws InvalidArgumentException {

        super(
            displayType,
            surveyItemId,
            condition,
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

        // Create a lookup table for the choices.
        this.choices = new HashMap<String, Choice>(choices.size());
        // Keep track of the keys to ensure there are no duplicates.
        Set<Integer> uniqueKeys = new HashSet<Integer>(choices.size());
        // Examine each choice.
        for(Choice choice : choices) {
            // Be sure the key does not already exist.
            if(! uniqueKeys.add(choice.getKey())) {
                throw
                    new InvalidArgumentException(
                        "Two choices have the same key '" +
                            choice.getKey() +
                            "': " +
                            getSurveyItemId());
            }

            // Be sure the label does not already exist.
            if(this.choices.put(choice.getLabel(), choice) != null) {
                throw
                    new InvalidArgumentException(
                        "Two choices have the same label '" +
                            choice.getLabel() +
                            "': " +
                            getSurveyItemId());
            }
        }

        // Validate the "allow custom" flag.
        this.allowCustom =
            (allowCustom == null) ? DEFAULT_ALLOW_CUSTOM : allowCustom;

        // Validate that, if no choices were given, the "allow custom" flag is
        // set to true.
        if((choices.size() == 0) && (! this.allowCustom)) {
            throw
                new InvalidArgumentException(
                    "No choices were given and custom choices are " +
                        "disallowed: " +
                        getSurveyItemId());
        }
    }

    /**
     * Returns a choice based on its key value.
     *
     * @param key
     *        The key to use to find the desired choice.
     *
     * @return The {@link Choice} object or null if the key is unknown.
     */
    public Choice getChoice(final int key) {
        // Check all choices.
        for(Choice choice : choices.values()) {
            // If a choice with a matching key is found, return it.
            if(choice.key == key) {
                return choice;
            }
        }

        // If no choice was found, return null.
        return null;
    }

    /**
     * Returns a choice based on its label value.
     *
     * @param label
     *        The label to use to find the desired choice.
     *
     * @return The {@link Choice} object or null if the label is unknown.
     */
    public Choice getChoice(final String label) {
        return choices.get(label);
    }

    /**
     * Returns whether or not this prompt allows custom values.
     *
     * @return Whether or not this prompt allows custom values.
     */
    public boolean allowsCustom() {
        return allowCustom;
    }
}