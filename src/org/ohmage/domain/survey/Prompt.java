package org.ohmage.domain.survey;

import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.response.Respondable;

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
     * The JSON key used to define the prompt type.
     */
    public static final String JSON_KEY_PROMPT_TYPE = "prompt_type";

    /**
     * The JSON key for the text.
     */
    public static final String JSON_KEY_TEXT = "text";
    /**
     * The JSON key for the skippable flag.
     */
    public static final String JSON_KEY_SKIPPABLE = "skippable";
    /**
     * The JSON key for the default response.
     */
    public static final String JSON_KEY_DEFAULT_RESPONSE = "default_response";

    /**
     * The text to show to the user.
     */
    @JsonProperty(JSON_KEY_TEXT)
    private final String text;
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
    public Prompt(
        final String surveyItemId,
        final String condition,
        final String text,
        final boolean skippable,
        final ResponseType defaultResponse)
        throws InvalidArgumentException {

        super(surveyItemId, condition);

        if(text == null) {
            throw new InvalidArgumentException("The prompt text is null.");
        }

        this.text = text;
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
        final Map<String, Object> previousResponses)
        throws InvalidArgumentException {

        // TODO: Check if the response should have been displayed and compare
        // that to whether or not a response exists.
        /* if */
        // If the response should have been displayed, then, if the response is
        // missing, check if this prompt is skippable.
        /* else */ if(response == null) {
            if(! skippable()) {
                throw
                    new InvalidArgumentException(
                        "A prompt that was not skippable was skipped: " +
                            getSurveyItemId());
            }
        }
        else {
            try {
                validateResponse((ResponseType) response);
            }
            catch(ClassCastException e) {
                throw
                    new InvalidArgumentException(
                        "The response is of the wrong type for prompt '" +
                            getSurveyItemId() +
                        "'.");
            }
        }
    }

    /**
     * Validates that some prompt response conforms to the given prompt's
     * definition.
     *
     * @param response
     *        The prompt response to validate.
     */
    public abstract void validateResponse(
        final ResponseType response)
        throws InvalidArgumentException;
}