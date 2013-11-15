package org.ohmage.domain.survey.response;

import org.ohmage.domain.survey.Prompt;
import org.ohmage.domain.survey.TextPrompt;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * <p>
 * The parent class for all prompt responses.
 * </p>
 *
 * @author John Jenkins
 */
@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = Prompt.JSON_KEY_PROMPT_TYPE)
@JsonSubTypes({
    @JsonSubTypes.Type(
        value = TextPromptResponse.class,
        name = TextPrompt.SURVEY_ITEM_TYPE) })
public abstract class PromptResponse<PromptType extends Prompt<?>, ResponseType> {
//    /**
//     * The JSON key for the user's response.
//     */
//    public static final String JSON_KEY_RESPONSE = "response";
//    /**
//     * The JSON key for the "no response" value.
//     */
//    public static final String JSON_KEY_NO_RESPONSE = "no_response";
//
//    /**
//     * The reason an actual response was not given.
//     */
//    @JsonProperty(JSON_KEY_NO_RESPONSE)
//    private final NoResponse noResponse;

    /**
     * Creates a prompt response when the user did give a response.
     */
    public PromptResponse() {
//        this.noResponse = null;
    }

//    /**
//     * Creates a prompt response when the user didn't give an actual response
//     * to the prompt.
//     *
//     * @param noResponse
//     *        The {@link NoResponse} which indicates why the user did not give
//     *        an actual response to the prompt. This may be null to indicate
//     *        that a response was given.
//     */
//    public PromptResponse(final NoResponse noResponse) {
//        this.noResponse = noResponse;
//    }

//    /**
//     * Returns whether or not the prompt was skipped.
//     *
//     * @return Whether or not the prompt was skipped.
//     */
//    public boolean wasSkipped() {
//        return NoResponse.SKIPPED.equals(noResponse);
//    }
//
//    /**
//     * Returns whether or not this prompt was not displayed.
//     *
//     * @return Whether or not this prompt was not displayed.
//     */
//    public boolean wasNotDisplayed() {
//        return NoResponse.NOT_DISPLAYED.equals(noResponse);
//    }

//    /**
//     * Returns the {@link NoResponse reason} the user did not respond to the
//     * prompt or null if the user did respond to the prompt.
//     *
//     * @return The {@link NoResponse reason} the user did not respond to the
//     *         prompt.
//     *
//     * @see #getResponse()
//     */
//    public NoResponse getNoResponse() {
//        return noResponse;
//    }
//
//    /**
//     * Returns whether or not this prompt has a "no response" value.
//     *
//     * @return Whether or not this prompt has a "no response" value.
//     */
//    public boolean hasNoResponse() {
//        return noResponse != null;
//    }

    /**
     * Returns the user's response or null if the user did not respond to the
     * prompt.
     *
     * @return The user's response or null if the user did not respond to the
     *         prompt.
     *
     * @see #getNoResponse()
     */
//    @JsonProperty(JSON_KEY_RESPONSE)
    @JsonValue
    public abstract ResponseType getResponse();

//    /**
//     * Returns the prompt type of the prompt to which this response belongs.
//     *
//     * @return The prompt type of the prompt to which this response belongs.
//     */
//    @JsonProperty(Prompt.JSON_KEY_PROMPT_TYPE)
//    public abstract String getPromptType();
}