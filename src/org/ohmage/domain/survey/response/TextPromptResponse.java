package org.ohmage.domain.survey.response;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.TextPrompt;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * <p>
 * A response to a text prompt.
 * </p>
 *
 * @author John Jenkins
 */
public class TextPromptResponse extends PromptResponse<TextPrompt, String> {
    /**
     * The value of the response or null if there does not exist a valid
     * response.
     */
    @JsonIgnore
    public final String value;

//    /**
//     * Creates a response object in the event that the prompt was not actually
//     * responded to.
//     *
//     * @param noResponse
//     *        The reason the prompt was not actually responded to.
//     *
//     * @throws InvalidArgumentException
//     *         The "no response" is null.
//     */
//    public TextPromptResponse(
//        final NoResponse noResponse)
//        throws InvalidArgumentException {
//
//        this(noResponse, null);
//
//        if(value == null) {
//            throw new InvalidArgumentException(
//                "The 'no response' value is null.");
//        }
//    }

    /**
     * Creates a response response from the user.
     *
     * @param value
     *        The response from the user.
     *
     * @throws InvalidArgumentException
     *         The value is null.
     */
    public TextPromptResponse(
        final String value)
        throws InvalidArgumentException {

//        this(null, value);

        if(value == null) {
            throw new InvalidArgumentException("The value is null.");
        }

        this.value = value;
    }

//    /**
//     * Builds a response for a text prompt.
//     *
//     * @param noResponse
//     *        The "no response" value or null if the value is valid.
//     *
//     * @param response
//     *        The response or null if the value is a "no response" value.
//     */
//    @JsonCreator
//    protected TextPromptResponse(
//        @JsonProperty(JSON_KEY_NO_RESPONSE) final NoResponse noResponse,
//        @JsonProperty(JSON_KEY_RESPONSE) final String response) {
//
//        super(noResponse);
//
//        value = response;
//    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.response.PromptResponse#getResponse()
     */
    @Override
    public String getResponse() {
        return value;
    }

//    /*
//     * (non-Javadoc)
//     * @see org.ohmage.domain.survey.response.PromptResponse#getPromptType()
//     */
//    @Override
//    public String getPromptType() {
//        return TextPrompt.SURVEY_ITEM_TYPE;
//    }
}