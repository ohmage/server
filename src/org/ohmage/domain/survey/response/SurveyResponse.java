package org.ohmage.domain.survey.response;

import java.util.HashMap;
import java.util.Map;

import org.ohmage.domain.MetaData;
import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Survey;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A response to a survey.
 * </p>
 *
 * @author John Jenkins
 */
public class SurveyResponse extends OhmageDomainObject {
    public static class Builder
        extends OhmageDomainObject.Builder<SurveyResponse> {

        /**
         * The owner of the survey response.
         */
        protected String owner;
        /**
         * The unique identifier to which these survey responses belong.
         */
        protected String surveyId;
        /**
         * The version for the survey to which this survey response conforms.
         */
        protected Long surveyVersion;
        /**
         * The meta-data associated with this survey response.
         */
        protected MetaData metaData;
        /**
         * The responses that compose this survey response.
         */
        protected Map<String, Object> responses;

        /**
         * Creates a new builder.
         *
         * @param metaData
         *        The survey response's meta-data.
         *
         * @param responses
         *        The list of survey item responses.
         */
        @JsonCreator
        public Builder(
            @JsonProperty(JSON_KEY_META_DATA) final MetaData metaData,
            @JsonProperty(JSON_KEY_RESPONSES)
                final Map<String, Object> responses) {

            super(null);

            this.metaData = metaData;
            this.responses = responses;
        }

        /**
         * Sets the owner of the survey response.
         *
         * @param owner
         *        The owner of the survey response.
         *
         * @return This to facilitate chaining.
         */
        public Builder setOwner(final String owner) {
            this.owner = owner;

            return this;
        }

        /**
         * Sets the identifier for the survey to which this data conforms.
         *
         * @param surveyId
         *        The unique identifier for the survey to which this survey
         *        response conforms.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setSurveyId(final String surveyId) {
            this.surveyId = surveyId;

            return this;
        }

        /**
         * Sets the version of the survey to which this survey response
         * conforms.
         *
         * @param version
         *        The version of the stream to which this data conforms.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setSurveyVersion(final Long version) {
            surveyVersion = version;

            return this;
        }

        /**
         * Builds a new {@link SurveyResponse} object based on the current
         * state of this builder.
         *
         * @return the built {@link SurveyResponse} object.
         *
         * @throws InvalidArgumentException
         *         The state of this builder is insufficient to build a new
         *         {@link SurveyResponse} object.
         */
        public SurveyResponse build() throws InvalidArgumentException {
            return
                new SurveyResponse(
                    owner,
                    surveyId,
                    surveyVersion,
                    metaData,
                    responses);
        }

        /**
         * First, validates the survey responses, then builds a new
         * {@link SurveyResponse} based on the current state of this builder.
         * The resulting object's survey reference will use the given
         * {@link Survey}, thereby overriding any survey references that are
         * part of this builder.
         *
         * @param survey
         *        The {@link Survey} object that should be used to validate
         *        this data and whose reference should be used to build the
         *        {@link SurveyResponse} result.
         *
         * @return The built {@link SurveyResponse} object.
         *
         * @throws InvalidArgumentException
         *         The state of this builder is insufficient to build a new
         *         {@link SurveyResponse} object or the survey response is
         *         invalid.
         */
        public SurveyResponse build(
            final Survey survey)
            throws InvalidArgumentException {

            // Validate the survey responses.
            survey.validate(metaData, responses);

            // Update this builder's survey reference.
            surveyId = survey.getId();
            surveyVersion = survey.getVersion();

            // Build and return the SurveyResponse object.
            return build();
        }
    }

    /**
     * The JSON key for the owner.
     */
    public static final String JSON_KEY_OWNER = "owner";
    /**
     * The JSON key for the survey's unique identifier.
     */
    public static final String JSON_KEY_SURVEY_ID = "survey_id";
    /**
     * The JSON key for the survey's unique identifier.
     */
    public static final String JSON_KEY_SURVEY_VERSION = "survey_version";
    /**
     * The JSON key for the meta-data.
     */
    public static final String JSON_KEY_META_DATA = "meta_data";
    /**
     * The JSON key for the responses.
     */
    public static final String JSON_KEY_RESPONSES = "data";

    /**
     * The owner of the survey response.
     */
    @JsonProperty(JSON_KEY_OWNER)
    private final String owner;
    /**
     * The unique identifier to which these survey responses belong.
     */
    @JsonProperty(JSON_KEY_SURVEY_ID)
    private final String surveyId;
    /**
     * The version of the survey to which this survey response belongs.
     */
    @JsonProperty(JSON_KEY_SURVEY_VERSION)
    private final long surveyVersion;
    /**
     * The meta-data associated with this survey response.
     */
    @JsonProperty(JSON_KEY_META_DATA)
    private final MetaData metaData;
    /**
     * The responses that compose this survey response.
     */
    @JsonProperty(JSON_KEY_RESPONSES)
    private final Map<String, Object> responses;

    /**
     * Creates a new survey response.
     *
     * @param owner
     *        The user that is creating this survey response.
     *
     * @param surveyId
     *        The unique identifier for the survey.
     *
     * @param surveyVersion
     *        The version of the survey to which this survey response belongs.
     *
     * @param metaData
     *        The meta-data associated with the survey response.
     *
     * @param responses
     *        The list of survey responses.
     */
    public SurveyResponse(
        final String owner,
        final String surveyId,
        final Long surveyVersion,
        final MetaData metaData,
        final Map<String, Object> responses) {

        this(owner, surveyId, surveyVersion, metaData, responses, null);
    }

    /**
     * Rebuilds an existing survey response.
     *
     * @param owner
     *        The user that created and owns this survey response.
     *
     * @param surveyId
     *        The unique identifier of the survey to which this response
     *        belongs. belongs.
     *
     * @param surveyVersion
     *        The version of the survey to which this survey response belongs.
     *
     * @param metaData
     *        The meta-data associated with the survey response.
     *
     * @param responses
     *        The list of survey responses that correspond to the survey.
     *
     * @param internalVersion
     *        The internal version of this survey response.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    protected SurveyResponse(
        @JsonProperty(JSON_KEY_OWNER) final String owner,
        @JsonProperty(JSON_KEY_SURVEY_ID) final String surveyId,
        @JsonProperty(JSON_KEY_SURVEY_VERSION) final Long surveyVersion,
        @JsonProperty(JSON_KEY_META_DATA) final MetaData metaData,
        @JsonProperty(JSON_KEY_RESPONSES) final Map<String, Object> responses,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws InvalidArgumentException {

        this(
            owner,
            surveyId,
            surveyVersion,
            metaData,
            responses,
            internalVersion,
            null);
    }

    /**
     * Builds a SurveyResponse object.
     *
     * @param owner
     *        The user that created and owns this survey response.
     *
     * @param surveyId
     *        The unique identifier of the survey to which this response
     *        belongs.
     *
     * @param surveyVersion
     *        The version of the survey to which this survey response belongs.
     *
     * @param metaData
     *        The meta-data associated with the survey response.
     *
     * @param responses
     *        The list of survey responses that correspond to the survey.
     *
     * @param internalReadVersion
     *        The internal version of this survey response when it was read
     *        from the database.
     *
     * @param internalWriteVersion
     *        The new internal version of this survey response when it will be
     *        written back to the database.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    private SurveyResponse(
        final String owner,
        final String surveyId,
        final Long surveyVersion,
        final MetaData metaData,
        final Map<String, Object> responses,
        final Long internalReadVersion,
        final Long internalWriteVersion)
        throws InvalidArgumentException {

        super(internalReadVersion, internalWriteVersion);

        if(owner == null) {
            throw new InvalidArgumentException("The owner is null.");
        }
        if(surveyId == null) {
            throw new InvalidArgumentException("The survey ID is null.");
        }
        if(surveyVersion == null) {
            throw new InvalidArgumentException("The survey version is null.");
        }
        if(metaData == null) {
            throw new InvalidArgumentException("The meta-data is null.");
        }
        if(responses == null) {
            throw new InvalidArgumentException("The responses list is null.");
        }

        this.owner = owner;
        this.surveyId = surveyId;
        this.surveyVersion = surveyVersion;
        this.metaData = metaData;
        this.responses = new HashMap<String, Object>(responses);
    }
}