package org.ohmage.mongodb.bin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.ColumnList;
import org.ohmage.domain.DataPoint;
import org.ohmage.domain.MetaData;
import org.ohmage.domain.survey.SurveyResponse;
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
        // Create the unique index for a survey response data point
        DBObject uniqueIndex = new BasicDBObject();
        // Index the survey ID.
        uniqueIndex.put(DataPoint.JSON_KEY_OWNER, 1);
        // Index the survey ID.
        uniqueIndex.put(DataPoint.JSON_KEY_SCHEMA_ID, 1);
        // Index the survey version.
        uniqueIndex.put(DataPoint.JSON_KEY_SCHEMA_VERSION, 1);
        // Index the survey response's unique ID.
        uniqueIndex
            .put(
                    DataPoint.JSON_KEY_META_DATA + "." +
                            MetaData.JSON_KEY_ID,
                    1);

        // Ensure that there is a unique index on the user, survey ID, version
        // and the point's ID.
        COLLECTION
            .ensureIndex(
                uniqueIndex,
                COLLECTION_NAME + "_" +
                    DataPoint.JSON_KEY_OWNER + "_" +
                    DataPoint.JSON_KEY_SCHEMA_ID + "_" +
                    DataPoint.JSON_KEY_SCHEMA_VERSION + "_" +
                    DataPoint.JSON_KEY_META_DATA + "." +
                        MetaData.JSON_KEY_ID +
                    "_unique",
                true);

        // Ensure that there is a unique index on the media filenames.
        // Create the old-style of options due to a bug in MongoDB, see:
        // https://jira.mongodb.org/browse/SERVER-3934
        BasicDBObject options = new BasicDBObject();
        options
            .put(
                "name",
                COLLECTION_NAME + "_" +
                    SurveyResponse.JSON_KEY_MEDIA_FILENAMES +
                    "_unique");
        options.put("unique", true);
        // This means that some rows are not required to have the array or any
        // elements in the array.
        options.put("sparse", true);
        // This circumvents a bug in MongoDB by using their old indexing
        // strategy.
        options.put("v", 0);
        COLLECTION
            .ensureIndex(
                new BasicDBObject(SurveyResponse.JSON_KEY_MEDIA_FILENAMES, 1),
                options);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#addSurveyResponses(java.util.List)
     */
    @Override
    public void addSurveyResponses(
        final List<SurveyResponse> surveyResponses)
        throws IllegalArgumentException, IllegalStateException {

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
                new IllegalArgumentException(
                    "One of the media files had the same unique key as any " +
                        "other media file. However, we should be creating " +
                        "new IDs for the media files, so this should never " +
                        "happen.",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#getDuplicateIds(java.lang.String, java.lang.String, long, java.util.Set)
     */
    @Override
    @SuppressWarnings("unchecked")
    public MultiValueResult<String> getDuplicateIds(
        final String owner,
        final String surveyId,
        final long surveyVersion,
        final Set<String> candidateIds)
        throws IllegalArgumentException {

        // Validate the parameters.
        if(owner == null) {
            throw new IllegalArgumentException("The owner is null.");
        }
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }
        if(candidateIds == null) {
            throw
                new IllegalArgumentException("The candidate IDs set is null.");
        }
        else if(candidateIds.size() == 0) {
            return
                new MongoMultiValueResultList<String>(
                    Collections.<String>emptyList(),
                    0);
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the owner.
        queryBuilder.and(DataPoint.JSON_KEY_OWNER).is(owner);

        // Add the survey ID.
        queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_ID).is(surveyId);

        // Add the survey version.
        queryBuilder
            .and(DataPoint.JSON_KEY_SCHEMA_VERSION)
            .is(surveyVersion);

        // Add the candidate IDs.
        queryBuilder
            .and(
                DataPoint.JSON_KEY_META_DATA + "." + MetaData.JSON_KEY_ID)
            .in(candidateIds);

        // Get the results.
        List<String> results =
            COLLECTION
                .distinct(
                    DataPoint.JSON_KEY_META_DATA + "." +
                        MetaData.JSON_KEY_ID,
                    queryBuilder.get());

        // Get and return the duplicates.
        return new MongoMultiValueResultList<String>(results, results.size());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#getSurveyResponses(java.lang.String, long, java.util.Set, java.util.Collection, org.joda.time.DateTime, org.joda.time.DateTime, org.ohmage.domain.ColumnList, boolean, long, long)
     */
    @Override
    public MultiValueResult<? extends SurveyResponse> getSurveyResponses(
        final String surveyId,
        final long surveyVersion,
        final Set<String> userIds,
        final Collection<String> surveyResponseIds,
        final DateTime startDate,
        final DateTime endDate,
        final ColumnList columnList,
        final boolean chronological,
        final long numToSkip,
        final long numToReturn)
        throws IllegalArgumentException {

        // Validate the parameters.
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }
        if(userIds == null) {
            throw
                new IllegalArgumentException(
                    "The set of users' unique identifier is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the survey ID.
        queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_ID).is(surveyId);

        // Add the survey version.
        queryBuilder
            .and(DataPoint.JSON_KEY_SCHEMA_VERSION)
            .is(surveyVersion);

        // Add the user's unique identifier.
        queryBuilder.and(DataPoint.JSON_KEY_OWNER).in(userIds);

        // Add the survey response IDs, if given.
        if(surveyResponseIds != null) {
            queryBuilder
                .and(
                    DataPoint.JSON_KEY_META_DATA + "." +
                        MetaData.JSON_KEY_ID)
                .in(surveyResponseIds);
        }

        // Add the start date, if given.
        if(startDate != null) {
            queryBuilder
                .and(
                    DataPoint.JSON_KEY_META_DATA + "." +
                        MetaData.JSON_KEY_TIMESTAMP_MILLIS)
                .greaterThanEquals(startDate.getMillis());
        }

        // Add the end date, if given.
        if(endDate != null) {
            queryBuilder
                .and(
                    DataPoint.JSON_KEY_META_DATA + "." +
                        MetaData.JSON_KEY_TIMESTAMP_MILLIS)
                .lessThanEquals(endDate.getMillis());
        }

        // Create the projection.
        BasicDBObject columns = new BasicDBObject();
        if(columnList != null) {
            for(String column : columnList.toList()) {
                columns.put(column, 1);
            }
        }

        // Create the ordering.
        BasicDBObject sort =
            new BasicDBObject(
                DataPoint.JSON_KEY_META_DATA + "." +
                    MetaData.JSON_KEY_TIMESTAMP_MILLIS,
                ((chronological) ? 1 : -1));

        // Make the query and return the results.
        return
            new MongoMultiValueResultCursor<MongoSurveyResponse>(
                MONGO_COLLECTION
                    .find(queryBuilder.get(), columns)
                    .sort(sort)
                    .skip((int) numToSkip)
                    .limit((int) numToReturn));
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#getSurveyResponse(java.lang.String, java.lang.String, long, java.lang.String)
     */
    @Override
    public SurveyResponse getSurveyResponse(
        final String owner,
        final String surveyId,
        final long surveyVersion,
        final String pointId)
        throws IllegalArgumentException {

        // Validate the parameters.
        if(owner == null) {
            throw new IllegalArgumentException("The owner is null.");
        }
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }
        if(pointId == null) {
            throw new IllegalArgumentException("The point ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the user's unique identifier.
        queryBuilder.and(DataPoint.JSON_KEY_OWNER).is(owner);

        // Add the survey ID.
        queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_ID).is(surveyId);

        // Add the survey version.
        queryBuilder
            .and(DataPoint.JSON_KEY_SCHEMA_VERSION)
            .is(surveyVersion);

        // Add the point ID.
        queryBuilder
            .and(
                DataPoint.JSON_KEY_META_DATA +
                    "." +
                    MetaData.JSON_KEY_ID)
            .is(pointId);

        // Make the query and return the results.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#getSurveyResponseForMedia(java.lang.String)
     */
    @Override
    public SurveyResponse getSurveyResponseForMedia(final String mediaId) {
        // Validate the parameters.
        if(mediaId == null) {
            throw new IllegalArgumentException("The media ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the point ID.
        queryBuilder.and(SurveyResponse.JSON_KEY_MEDIA_FILENAMES).is(mediaId);

        // Make the query and return the results.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#deleteSurveyResponse(java.lang.String, java.lang.String, long, java.lang.String)
     */
    @Override
    public void deleteSurveyResponse(
        final String owner,
        final String surveyId,
        final long surveyVersion,
        final String pointId)
        throws IllegalArgumentException {

        // Validate the parameters.
        if(owner == null) {
            throw new IllegalArgumentException("The owner is null.");
        }
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }
        if(pointId == null) {
            throw new IllegalArgumentException("The point ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the user's unique identifier.
        queryBuilder.and(DataPoint.JSON_KEY_OWNER).is(owner);

        // Add the survey ID.
        queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_ID).is(surveyId);

        // Add the survey version.
        queryBuilder
            .and(DataPoint.JSON_KEY_SCHEMA_VERSION)
            .is(surveyVersion);

        // Add the point ID.
        queryBuilder
            .and(
                DataPoint.JSON_KEY_META_DATA +
                    "." +
                    MetaData.JSON_KEY_ID)
            .is(pointId);

        // Delete the data point.
        // TODO: In the future, we may want to simply mark the point as deleted
        // and not actually delete it.
        MONGO_COLLECTION.remove(queryBuilder.get());
    }
}