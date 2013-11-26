package org.ohmage.mongodb.bin;

import java.util.List;

import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.MetaData;
import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyResponse;
import org.ohmage.mongodb.domain.MongoCursorMultiValueResult;
import org.ohmage.mongodb.domain.survey.response.MongoSurveyResponse;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The MongoDB implementation of the database-backed survey response
 * repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoSurveyResponseBin extends SurveyResponseBin {
    /**
     * The name of the collection that contains all of the survey responses.
     */
    public static final String COLLECTION_NAME = "survey_response_bin";

    /**
     * Get the connection to the survey response bin with the Jackson wrapper.
     */
    private static final JacksonDBCollection<SurveyResponse, String> COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                SurveyResponse.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Get the connection to the survey response bin with the Jackson wrapper,
     * specifically for {@link SurveyResponse} objects.
     */
    private static final JacksonDBCollection<MongoSurveyResponse, String> MONGO_COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                MongoSurveyResponse.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Default constructor.
     */
    protected MongoSurveyResponseBin() {
        // Ensure that there is an index on the point's version.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(
                    SurveyResponse.JSON_KEY_META_DATA + "." +
                        MetaData.JSON_KEY_ID,
                    1),
                COLLECTION_NAME + "_" +
                    SurveyResponse.JSON_KEY_META_DATA + "." +
                        MetaData.JSON_KEY_ID,
                false);

        // Create the set of indexes.
        DBObject indexes = new BasicDBObject();
        // Index the survey ID.
        indexes.put(SurveyResponse.JSON_KEY_SURVEY_ID, 1);
        // Index the survey version.
        indexes.put(SurveyResponse.JSON_KEY_SURVEY_VERSION, 1);
        // Index the survey response's unique ID.
        indexes
            .put(
                SurveyResponse.JSON_KEY_META_DATA + "." +
                    MetaData.JSON_KEY_ID,
                1);

        // Ensure that there is a unique index on the survey ID and version and
        // the point's ID.
        COLLECTION
            .ensureIndex(
                indexes,
                COLLECTION_NAME + "_" +
                    SurveyResponse.JSON_KEY_SURVEY_ID + "_" +
                    SurveyResponse.JSON_KEY_SURVEY_VERSION + "_" +
                    SurveyResponse.JSON_KEY_META_DATA + "." +
                        MetaData.JSON_KEY_ID +
                    "_unique",
                true);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#addSurveyResponses(java.util.List)
     */
    @Override
    public void addSurveyResponses(final List<SurveyResponse> surveyResponses)
        throws IllegalArgumentException,
        InvalidArgumentException {

        // Validate the parameters.
        if(surveyResponses == null) {
            throw
                new IllegalArgumentException("The survey responses are null.");
        }

        // Save it.
        try {
            COLLECTION.insert(surveyResponses);
        }
        catch(MongoException.DuplicateKey e) {
            throw
                new InvalidArgumentException(
                    "Two survey responses had the same ID.",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#getSurveyResponses(java.lang.String, java.lang.String, long)
     */
    @Override
    public MultiValueResult<? extends SurveyResponse> getSurveyResponses(
        final String username,
        final String surveyId,
        final long surveyVersion) throws IllegalArgumentException {

        // Validate the parameters.
        if(username == null) {
            throw new IllegalArgumentException("The username is null.");
        }
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the user-name.
        queryBuilder.and(SurveyResponse.JSON_KEY_OWNER).is(username);

        // Add the survey ID.
        queryBuilder.and(SurveyResponse.JSON_KEY_SURVEY_ID).is(surveyId);

        // Add the survey version.
        queryBuilder
            .and(SurveyResponse.JSON_KEY_SURVEY_VERSION)
            .is(surveyVersion);

        // Make the query and return the results.
        return
            new MongoCursorMultiValueResult<MongoSurveyResponse>(
                MONGO_COLLECTION.find(queryBuilder.get()));
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#getSurveyResponse(java.lang.String, java.lang.String, long, java.lang.String)
     */
    @Override
    public SurveyResponse getSurveyResponse(
        final String username,
        final String surveyId,
        final long surveyVersion,
        final String pointId) throws IllegalArgumentException {

        // Validate the parameters.
        if(username == null) {
            throw new IllegalArgumentException("The username is null.");
        }
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }
        if(pointId == null) {
            throw new IllegalArgumentException("The point ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the user-name.
        queryBuilder.and(SurveyResponse.JSON_KEY_OWNER).is(username);

        // Add the survey ID.
        queryBuilder.and(SurveyResponse.JSON_KEY_SURVEY_ID).is(surveyId);

        // Add the survey version.
        queryBuilder
            .and(SurveyResponse.JSON_KEY_SURVEY_VERSION)
            .is(surveyVersion);

        // Add the point ID.
        queryBuilder
            .and(
                SurveyResponse.JSON_KEY_META_DATA +
                    "." +
                    MetaData.JSON_KEY_ID)
            .is(pointId);

        // Make the query and return the results.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#deleteSurveyResponse(java.lang.String, java.lang.String, long, java.lang.String)
     */
    @Override
    public void deleteSurveyResponse(
        final String username,
        final String surveyId,
        final long surveyVersion,
        final String pointId) throws IllegalArgumentException {

        // Validate the parameters.
        if(username == null) {
            throw new IllegalArgumentException("The username is null.");
        }
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }
        if(pointId == null) {
            throw new IllegalArgumentException("The point ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the user-name.
        queryBuilder.and(SurveyResponse.JSON_KEY_OWNER).is(username);

        // Add the survey ID.
        queryBuilder.and(SurveyResponse.JSON_KEY_SURVEY_ID).is(surveyId);

        // Add the survey version.
        queryBuilder
            .and(SurveyResponse.JSON_KEY_SURVEY_VERSION)
            .is(surveyVersion);

        // Add the point ID.
        queryBuilder
            .and(
                SurveyResponse.JSON_KEY_META_DATA +
                    "." +
                    MetaData.JSON_KEY_ID)
            .is(pointId);

        // Delete the data point.
        // TODO: In the future, we may want to simply mark the point as deleted
        // and not actually delete it.
        MONGO_COLLECTION.remove(queryBuilder.get());
    }
}