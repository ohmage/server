package org.ohmage.mongodb.domain.survey.response;

import java.util.Map;
import java.util.Set;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.MetaData;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyResponse;
import org.ohmage.mongodb.bin.MongoSurveyResponseBin;
import org.ohmage.mongodb.domain.MongoDbObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The MongoDB implementation of the {@link SurveyResponse} class.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoSurveyResponseBin.COLLECTION_NAME)
public class MongoSurveyResponse
    extends SurveyResponse
    implements MongoDbObject {

    /**
     * The database ID for this object.
     */
    @ObjectId
    private final String dbId;

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
    protected MongoSurveyResponse(
        @Id @ObjectId final String dbId,
        @JsonProperty(JSON_KEY_OWNER) final String owner,
        @JsonProperty(JSON_KEY_SURVEY_ID) final String surveyId,
        @JsonProperty(JSON_KEY_SURVEY_VERSION) final Long surveyVersion,
        @JsonProperty(JSON_KEY_META_DATA) final MetaData metaData,
        @JsonProperty(JSON_KEY_RESPONSES) final Map<String, Object> responses,
        @JsonProperty(JSON_KEY_MEDIA_FILENAMES)
            final Set<String> mediaFilenames,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws InvalidArgumentException {

        super(
            owner,
            surveyId,
            surveyVersion,
            metaData,
            responses,
            mediaFilenames,
            internalVersion);

        // Store the MongoDB ID.
        if(dbId == null) {
            throw new IllegalArgumentException("The MongoDB ID is missing.");
        }
        else {
            this.dbId = dbId;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.mongodb.domain.MongoDbObject#getDatabaseId()
     */
    @Override
    @ObjectId
    public String getDbId() {
        return dbId;
    }
}