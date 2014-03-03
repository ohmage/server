package org.ohmage.domain.survey.prompt;

import java.net.URI;
import java.util.Map;

import name.jenkins.paul.john.concordia.Concordia;
import name.jenkins.paul.john.concordia.exception.ConcordiaException;
import name.jenkins.paul.john.concordia.schema.Schema;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

/**
 * <p>
 * A prompt for the user to launch a remote activity.
 * </p>
 *
 * @author John Jenkins
 */
public class RemoteActivityPrompt extends Prompt<Object> {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "remote_activity_prompt";

    /**
     * The JSON key for the URI.
     */
    public static final String JSON_KEY_URI = "uri";

    /**
     * The JSON key for the response's definition.
     */
    public static final String JSON_KEY_DEFINITION = "definition";

    /**
     * The static object mapper to use to convert the given objects into JSON
     * nodes.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * The URI to use to launch the remote activity. This may include query
     * parameters and/or fragments.
     */
    @JsonProperty(JSON_KEY_URI)
    private final URI uri;

    /**
     * The definition that a valid response must follow.
     */
    @JsonProperty(JSON_KEY_DEFINITION)
    private final Concordia definition;

    /**
     * Creates a new remote activity prompt.
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
     * @param uri
     *        The URI that the client should use to launch the remote activity.
     *
     * @param definition
     *        The {@link Concordia} definition to which the remote activity
     *        response must adhere.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public RemoteActivityPrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_DISPLAY_TYPE) final DisplayType displayType,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE)
            final JsonNode defaultResponse,
        @JsonProperty(JSON_KEY_URI) final URI uri,
        @JsonProperty(JSON_KEY_DEFINITION) final Concordia definition)
        throws InvalidArgumentException {

        super(
            surveyItemId,
            condition,
            displayType,
            text,
            displayLabel,
            skippable,
            defaultResponse);

        // Validate the display type.
        if(! DisplayType.LAUNCHER.equals(displayType)) {
            throw
                new InvalidArgumentException(
                    "The display type '" +
                        displayType.toString() +
                        "' is not valid for the prompt, which must be '" +
                        DisplayType.LAUNCHER.toString() +
                        "': " +
                        getSurveyItemId());
        }

        // Default values are not allowed.
        if(
            (defaultResponse != null) &&
            (! (defaultResponse instanceof NullNode))) {

            throw
                new InvalidArgumentException(
                    "Default values are not allowed for remote activities: " +
                        getSurveyItemId());
        }

        // Validate the URI.
        if(uri == null) {
            throw
                new InvalidArgumentException(
                    "The URI is missing: " + getSurveyItemId());
        }
        else {
            this.uri = uri;
        }

        // Validate the definition.
        if(definition == null) {
            throw
                new InvalidArgumentException(
                    "The definition is missing: " + getSurveyItemId());
        }
        else {
            this.definition = definition;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.Respondable#getResponseSchema()
     */
    @Override
    public Schema getResponseSchema() {
        try {
            return
                definition
                    .getSchema()
                    .getBuilder()
                    .setDoc(getText())
                    .setOptional(skippable() || (getCondition() != null))
                    .setName(getSurveyItemId())
                    .build();
        }
        catch(ConcordiaException e) {
            throw
                new IllegalStateException(
                    "There was a problem creating a an empty object schema.",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.domain.survey.prompt.Prompt#validateResponse(java.lang.Object, java.util.Map)
     */
    @Override
    public JsonNode validateResponse(
        final Object response,
        final Map<String, Media> media)
        throws InvalidArgumentException {

        JsonNode result = OBJECT_MAPPER.valueToTree(response);

        try {
            definition.validateData(result);
        }
        catch(ConcordiaException e) {
            throw
                new InvalidArgumentException(
                    "The data was invalid, \"" +
                        e.getLocalizedMessage() +
                        "\": " +
                        getSurveyItemId());
        }

        return result;
    }
}