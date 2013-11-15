package org.ohmage.mongodb.domain.survey;

import java.util.List;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Survey;
import org.ohmage.domain.survey.SurveyItem;
import org.ohmage.mongodb.bin.MongoSurveyBin;
import org.ohmage.mongodb.domain.MongoDbObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The MongoDB implementation of the {@link Survey} class.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoSurveyBin.COLLECTION_NAME)
public class MongoSurvey extends Survey implements MongoDbObject {
    /**
     * The database ID for this object.
     */
    @ObjectId
    private final String dbId;

    /**
     * Rebuild an existing Survey object.
     *
     * @param dbId
     *        The database ID for this user.
     *
     * @param id
     *        The unique identifier for this object. If null, a default value
     *        is given.
     *
     * @param version
     *        The version of this survey.
     *
     * @param name
     *        The name of this survey.
     *
     * @param description
     *        The description of this survey.
     *
     * @param owner
     *        The owner of this survey.
     *
     * @param surveyItems
     *        The ordered list of survey items that compose this survey.
     *
     * @param internalVersion
     *        The internal version of this survey.
     *
     * @throws IllegalArgumentException
     *         The ID is invalid.
     *
     * @throws InvalidArgumentException
     *         A parameter is invalid.
     */
    @JsonCreator
    protected MongoSurvey(
        @Id @ObjectId final String dbId,
        @JsonProperty(JSON_KEY_ID) final String id,
        @JsonProperty(JSON_KEY_VERSION) final long version,
        @JsonProperty(JSON_KEY_NAME) final String name,
        @JsonProperty(JSON_KEY_DESCRIPTION) final String description,
        @JsonProperty(JSON_KEY_OWNER) final String owner,
        @JsonProperty(JSON_KEY_SURVEY_ITEMS)
            final List<SurveyItem> surveyItems,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws IllegalArgumentException, InvalidArgumentException {

        super(
            id,
            version,
            name,
            description,
            owner,
            surveyItems,
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