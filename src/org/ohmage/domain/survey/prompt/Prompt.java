package org.ohmage.domain.survey.prompt;

import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.NoResponse;
import org.ohmage.domain.survey.Respondable;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The base class for all prompts.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class Prompt<ResponseType>
    extends SurveyItem
    implements Respondable {

    /**
     * <p>
     * The allowed display type values.
     * </p>
     *
     * @author John Jenkins
     */
    public static enum DisplayType {
        /**
         * A calendar to allow users to select specific dates.
         */
        CALENDAR,
        /**
         * Launches the camera.
         */
        CAMERA,
        /**
         * Opens a new window / application.
         */
        LAUNCHER,
        /**
         * Allows a user to select one or more elements from a list of
         * elements.
         */
        LIST,
        /**
         * Like a text box except with additional UI elements to help the user
         * adjust their response, e.g. up and down arrows.
         */
        PICKER,
        /**
         * Launches the audio recorder.
         */
        RECORDER,
        /**
         * Allows a user to move a pointer across a discrete set of values.
         */
        SLIDER,
        /**
         * A simple text box where a user may enter their response directly.
         */
        TEXTBOX;

        /**
         * Creates the user-friendly variant of the display type.
         */
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    /**
     * The JSON key for the display type.
     */
    public static final String JSON_KEY_DISPLAY_TYPE = "display_type";
    /**
     * The JSON key for the text.
     */
    public static final String JSON_KEY_TEXT = "text";
    /**
     * The JSON key for the display label.
     */
    public static final String JSON_KEY_DISPLAY_LABEL = "display_label";
    /**
     * The JSON key for the skippable flag.
     */
    public static final String JSON_KEY_SKIPPABLE = "skippable";
    /**
     * The JSON key for the default response.
     */
    public static final String JSON_KEY_DEFAULT_RESPONSE = "default_response";

    /**
     * The display type used to describe how it will be displayed to the user.
     */
    @JsonProperty(JSON_KEY_DISPLAY_TYPE)
    private final DisplayType displayType;
    /**
     * The text to show to the user.
     */
    @JsonProperty(JSON_KEY_TEXT)
    private final String text;
    /**
     * The text to use as a short name in visualizations.
     */
    @JsonProperty(JSON_KEY_DISPLAY_LABEL)
    private final String displayLabel;
    /**
     * Whether or not this prompt may be skipped.
     */
    @JsonProperty(JSON_KEY_SKIPPABLE)
    private final boolean skippable;
    /**
     * The default response for this prompt or null if a default response is
     * not allowed.
     */
    @JsonProperty(JSON_KEY_DEFAULT_RESPONSE)
    private final ResponseType defaultResponse;

    /**
     * Creates a new prompt.
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
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    public Prompt(
        final String surveyItemId,
        final Condition condition,
        final DisplayType displayType,
        final String text,
        final String displayLabel,
        final boolean skippable,
        final ResponseType defaultResponse)
        throws InvalidArgumentException {

        super(surveyItemId, condition);

        if(text == null) {
            throw new InvalidArgumentException("The prompt text is null.");
        }
        if(displayLabel == null) {
            throw new InvalidArgumentException("The display label is null.");
        }

        this.displayType = displayType;
        this.text = text;
        this.displayLabel = displayLabel;
        this.skippable = skippable;
        this.defaultResponse = defaultResponse;
    }

    /**
     * Returns the text for this prompt.
     *
     * @return The text for this prompt.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns whether or not this prompt may be skipped.
     *
     * @return Whether or not this prompt may be skipped.
     */
    public boolean skippable() {
        return skippable;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.response.Respondable#validateResponse(java.lang.Object, java.util.Map)
     */
    @Override
    @SuppressWarnings("unchecked")
    public final void validateResponse(
        final Object response,
        final Map<String, Object> previousResponses,
        final Map<String, Media> media)
        throws InvalidArgumentException {

        // Check if the response should have been displayed and compare that to
        // whether or not a response exists.
        Condition condition = getCondition();
        if((condition != null) && (! condition.evaluate(previousResponses))) {
            // If it shouldn't have been displayed but an answer exists, report
            // an error.
            if(response != null) {
                throw
                    new InvalidArgumentException(
                        "A prompt should not have been displayed, yet a " +
                            "response was provided: " +
                            getSurveyItemId());
            }

            // Otherwise, add the NOT_DISPLAYED response to the set of previous
            // responses.
            previousResponses.put(getSurveyItemId(), NoResponse.NOT_DISPLAYED);
        }
        // If the response should have been displayed, then, if the response is
        // missing, check if this prompt is skippable.
        else if(response == null) {
            // If it is not skippable, report an error.
            if(! skippable()) {
                throw
                    new InvalidArgumentException(
                        "A prompt that was not skippable was skipped: " +
                            getSurveyItemId());
            }

            // Otherwise, add the SKIPPED response to the set of previous
            // responses.
            previousResponses.put(getSurveyItemId(), NoResponse.SKIPPED);
        }
        // Otherwise, evaluate the response.
        else {
            // Get the response object and, if it is of the wrong type, report
            // an error.
            try {
                // Attempt the cast. This will not throw a ClassCastException
                // even if the classes are not compatible.
                ResponseType responseObject = (ResponseType) response;

                // Validate the response.
                responseObject = validateResponse(responseObject, media);

                // Add the response to the list of previous responses.
                previousResponses.put(getSurveyItemId(), responseObject);
            }
            catch(ClassCastException e) {
                throw
                    new InvalidArgumentException(
                        "The response is of the wrong type: " +
                            getSurveyItemId());
            }
        }
    }

    /**
     * Validates that some prompt response conforms to the given prompt's
     * definition.
     *
     * @param response
     *        The prompt response to validate.
     *
     * @param media
     *        The map of unique identifiers to InputStreams for media that was
     *        uploaded with the request.
     *
     * @return The updated validated response.
     */
    public abstract ResponseType validateResponse(
        final ResponseType response,
        final Map<String, Media> media)
        throws InvalidArgumentException;
}