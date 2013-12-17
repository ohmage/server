package org.ohmage.domain.survey.prompt;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import name.jenkins.paul.john.concordia.exception.ConcordiaException;
import name.jenkins.paul.john.concordia.schema.ObjectSchema;
import name.jenkins.paul.john.concordia.schema.Schema;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.condition.Condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>
 * A prompt for the user to launch a remote activity.
 * </p>
 *
 * @author John Jenkins
 */
public class RemoteActivityPrompt extends Prompt<List<ObjectNode>> {
    /**
     * The string type of this survey item.
     */
    public static final String SURVEY_ITEM_TYPE = "remote_activity_prompt";

    /**
     * The default number of minimum runs.
     */
    public static final int DEFAULT_MIN_RUNS = 1;
    /**
     * The default number of retries.
     */
    public static final int DEFAULT_MAX_RUNS = Integer.MAX_VALUE;

    /**
     * The JSON key for the URI.
     */
    public static final String JSON_KEY_URI = "uri";
    /**
     * The JSON key for the minimum number of times the user must launch the
     * remote activity.
     */
    public static final String JSON_KEY_MIN_RUNS = "min_runs";
    /**
     * The JSON key for the maximum number of times the user may launch the
     * remote activity.
     */
    public static final String JSON_KEY_MAX_RUNS = "max_runs";

    /**
     * The URI to use to launch the remote activity. This may include query
     * parameters and/or fragments.
     */
    @JsonProperty(JSON_KEY_URI)
    private final URI uri;
    /**
     * The minimum number of times the user must launch the remote activity.
     */
    @JsonProperty(JSON_KEY_MIN_RUNS)
    private final int minRuns;
    /**
     * The maximum number of times the user may launch the remote activity.
     */
    @JsonProperty(JSON_KEY_MAX_RUNS)
    private final int maxRuns;

    /**
     * Creates a new remote activity prompt.
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
     * @param minRuns
     *        The minimum number of times the user must launch the remote
     *        activity. This must be greater than zero and, if null, will
     *        default to {@link DEFAULT_MIN_RUNS}.
     *
     * @param maxRuns
     *        The maximum number of times the user may launch the remote
     *        activity. This must be greater than 'minRuns' and, if null, will
     *        default to {@link DEFAULT_MAX_RUNS}.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public RemoteActivityPrompt(
        @JsonProperty(JSON_KEY_SURVEY_ITEM_ID) final String surveyItemId,
        @JsonProperty(JSON_KEY_CONDITION) final Condition condition,
        @JsonProperty(JSON_KEY_TEXT) final String text,
        @JsonProperty(JSON_KEY_DISPLAY_LABEL) final String displayLabel,
        @JsonProperty(JSON_KEY_SKIPPABLE) final boolean skippable,
        @JsonProperty(JSON_KEY_DEFAULT_RESPONSE)
            final List<ObjectNode> defaultResponse,
        @JsonProperty(JSON_KEY_URI) final URI uri,
        @JsonProperty(JSON_KEY_MIN_RUNS) final Integer minRuns,
        @JsonProperty(JSON_KEY_MAX_RUNS) final Integer maxRuns)
        throws InvalidArgumentException {

        super(
            surveyItemId,
            condition,
            text,
            displayLabel,
            skippable,
            defaultResponse);

        // Default values are not allowed.
        if(defaultResponse != null) {
            throw
                new InvalidArgumentException(
                    "Default values are not allowed for remote activities.");
        }

        // Validate the URI.
        if(uri == null) {
            throw new InvalidArgumentException("The URI is missing.");
        }
        else {
            this.uri = uri;
        }

        // Validate the minimum number of runs.
        if(minRuns == null) {
            this.minRuns = DEFAULT_MIN_RUNS;
        }
        else if(minRuns < 1) {
            throw
                new InvalidArgumentException(
                    "The minimum number of times the user must launch the " +
                        "remote activity must be a positive integer. If the " +
                        "user is not required to launch the remote " +
                        "activity, then set this value to any positive " +
                        "integer and mark it as 'skippable': " +
                        surveyItemId);
        }
        else {
            this.minRuns = minRuns;
        }

        // Validate the maximum number of runs.
        if(maxRuns == null) {
            this.maxRuns = DEFAULT_MAX_RUNS;
        }
        else if(maxRuns < this.minRuns) {
            throw
                new InvalidArgumentException(
                    "The maximum number of times the user may launch the " +
                        "remote activity must be equal to or gerater than " +
                        "the minimum number of runs: " +
                        surveyItemId);
        }
        else {
            this.maxRuns = maxRuns;
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
                new ObjectSchema(
                    getText(),
                    (skippable() || (getCondition() != null)),
                    getSurveyItemId(),
                    Collections.<Schema>emptyList());
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
    public List<ObjectNode> validateResponse(
        final List<ObjectNode> response,
        final Map<String, Media> media)
        throws InvalidArgumentException {

        // Ensure that the user ran the remote activity at least the minimum
        // number of times.
        if(response.size() < minRuns) {
            throw
                new InvalidArgumentException(
                    "The user only ran the remote activity " +
                        response.size() +
                        " times when they needed to run it at least " +
                        minRuns +
                        " times: " +
                        getSurveyItemId());
        }

        // Ensure that the user did not run the remote activity more than the
        // allotted number of times.
        if(response.size() > maxRuns) {
            throw
                new InvalidArgumentException(
                    "The user ran the remote activity " +
                        response.size() +
                        " times when they were only allowed to run it " +
                        maxRuns +
                        " times: " +
                        getSurveyItemId());
        }

        return response;
    }
}