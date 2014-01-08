package org.ohmage.domain.survey;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ohmage.domain.MetaData;
import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.jackson.OhmageObjectMapper.JsonFilterField;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A response to a survey.
 * </p>
 *
 * @author John Jenkins
 */
@JsonFilter(SurveyResponse.JACKSON_FILTER_GROUP_ID)
public class SurveyResponse extends OhmageDomainObject {
    /**
     * <p>
     * A builder for {@link SurveyResponse} objects.
     * </p>
     *
     * @author John Jenkins
     */
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
         * The set of filenames for the media associated with this survey
         * response.
         */
        protected Set<String> mediaFilenames;

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
                    responses,
                    mediaFilenames);
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
         * @param media
         *        A map of unique identifiers to {@link Media} objects for
         *        media that
         *        was uploaded with the survey responses.
         *
         * @return The built {@link SurveyResponse} object.
         *
         * @throws InvalidArgumentException
         *         The state of this builder is insufficient to build a new
         *         {@link SurveyResponse} object or the survey response is
         *         invalid.
         */
        public SurveyResponse build(
            final Survey survey,
            final Map<String, Media> media)
            throws InvalidArgumentException {

            // Validate the survey responses.
            responses = survey.validate(metaData, responses, media);

            // Update this builder's survey reference.
            surveyId = survey.getId();
            surveyVersion = survey.getVersion();

            // Update the list of media filenames.
            if(media != null) {
                mediaFilenames = new HashSet<String>();
                for(Media currMedia : media.values()) {
                    mediaFilenames.add(currMedia.getId());
                }
            }

            // Build and return the SurveyResponse object.
            return build();
        }
    }

    /**
     * The group ID for the Jackson filter. This must be unique to our class,
     * whatever the value is.
     */
    protected static final String JACKSON_FILTER_GROUP_ID =
        "org.ohmage.domain.survey.SurveyResponse";
    // Register this class with the ohmage object mapper.
    static {
        OhmageObjectMapper.register(SurveyResponse.class);
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
     * The JSON key for the media filenames.
     */
    public static final String JSON_KEY_MEDIA_FILENAMES = "media_filenames";

    /**
     * The owner of the survey response.
     */
    @JsonProperty(JSON_KEY_OWNER)
    @JsonFilterField
    private final String owner;

    /**
     * The unique identifier to which these survey responses belong.
     */
    @JsonProperty(JSON_KEY_SURVEY_ID)
    @JsonFilterField
    private final String surveyId;

    /**
     * The version of the survey to which this survey response belongs.
     */
    @JsonProperty(JSON_KEY_SURVEY_VERSION)
    @JsonFilterField
    private final Long surveyVersion;

    /**
     * The meta-data associated with this survey response.
     */
    @JsonProperty(JSON_KEY_META_DATA)
    @JsonInclude(Include.NON_NULL)
    private final MetaData metaData;

    /**
     * The responses that compose this survey response.
     */
    @JsonProperty(JSON_KEY_RESPONSES)
    @JsonInclude(Include.NON_NULL)
    private final Map<String, Object> responses;

    /**
     * The filenames of the media associated with this response.
     */
    @JsonProperty(JSON_KEY_MEDIA_FILENAMES)
    @JsonFilterField
    private final Set<String> mediaFilenames;

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
     *
     * @param mediaFilenames
     *        The list of filenames of the media files associated with this
     *        response.
     */
    public SurveyResponse(
        final String owner,
        final String surveyId,
        final Long surveyVersion,
        final MetaData metaData,
        final Map<String, Object> responses,
        final Set<String> mediaFilenames) {

        this(
            owner,
            surveyId,
            surveyVersion,
            metaData,
            responses,
            mediaFilenames,
            null);

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
     * @param mediaFilenames
     *        The list of filenames of the media files associated with this
     *        response.
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
        @JsonProperty(JSON_KEY_MEDIA_FILENAMES)
            final Set<String> mediaFilenames,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws InvalidArgumentException {

        this(
            owner,
            surveyId,
            surveyVersion,
            metaData,
            responses,
            mediaFilenames,
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
     * @param mediaFilenames
     *        The list of filenames of the media files associated with this
     *        response.
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
        final Set<String> mediaFilenames,
        final Long internalReadVersion,
        final Long internalWriteVersion)
        throws InvalidArgumentException {

        super(internalReadVersion, internalWriteVersion);

        this.owner = owner;
        this.surveyId = surveyId;
        this.surveyVersion = surveyVersion;
        this.metaData = metaData;
        this.responses =
            (responses == null) ?
                null :
                new HashMap<String, Object>(responses);
        this.mediaFilenames =
            (mediaFilenames == null) ?
                Collections.<String>emptySet() :
                new HashSet<String>(mediaFilenames);
    }

    /**
     * Returns the unique identifier for this survey response from its
     * meta-data.
     *
     * @return The unique identifier for this survey response from its
     *         meta-data.
     */
    public String getId() {
        return metaData.getId();
    }

    /**
     * Returns the owner.
     *
     * @return The owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Returns the set of media IDs for this survey response.
     *
     * @return The set of media IDs for this survey response.
     */
    public Set<String> getMediaIds() {
        return mediaFilenames;
    }
}