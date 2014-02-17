package org.ohmage.mongodb.bin;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.SurveyBin;
import org.ohmage.domain.Schema;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Survey;
import org.ohmage.mongodb.domain.survey.MongoSurvey;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The MongoDB implementation of the database-backed survey repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoSurveyBin extends SurveyBin {
    /**
     * The name of the collection that contains all of the surveys.
     */
    public static final String COLLECTION_NAME = "survey_bin";

    /**
     * Get the connection to the survey bin with the Jackson wrapper.
     */
    private static final JacksonDBCollection<Survey, String> COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                Survey.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Get the connection to the survey bin with the Jackson wrapper,
     * specifically for {@link MongoSurvey} objects.
     */
    private static final JacksonDBCollection<MongoSurvey, String> MONGO_COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                MongoSurvey.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Default constructor.
     */
    protected MongoSurveyBin() {
        // Ensure that there is an index on the ID.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(Schema.JSON_KEY_ID, 1),
                COLLECTION_NAME + "_" + Schema.JSON_KEY_ID,
                false);

        // Ensure that there is an index on the version.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(Schema.JSON_KEY_VERSION, 1),
                COLLECTION_NAME + "_" + Schema.JSON_KEY_VERSION,
                false);

        // Create the set of indexes.
        DBObject indexes = new BasicDBObject();
        // Index the ID.
        indexes.put(Schema.JSON_KEY_ID, 1);
        // Index the version.
        indexes.put(Schema.JSON_KEY_VERSION, 1);

        // Ensure that there is a unique index on the ID and version.
        COLLECTION
            .ensureIndex(
                indexes,
                COLLECTION_NAME + "_" +
                    Schema.JSON_KEY_ID + "_" +
                    Schema.JSON_KEY_VERSION + "_unique",
                true);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyBin#addSurvey(org.ohmage.domain.survey.Survey)
     */
    @Override
    public void addSurvey(final Survey survey)
        throws IllegalArgumentException, InvalidArgumentException {

        // Validate the parameter.
        if(survey == null) {
            throw new IllegalArgumentException("The survey is null.");
        }

        // Save it.
        try {
            COLLECTION.insert(survey);
        }
        catch(MongoException.DuplicateKey e) {
            throw
                new InvalidArgumentException(
                    "A survey with the same ID-version pair already exists.",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyBin#getSurveyIds(java.lang.String, long, long)
     */
    @Override
    public MultiValueResult<String> getSurveyIds(
        final String query,
        final boolean omhVisibleOnly,
        final long numToSkip,
        final long numToReturn) {

        // Build the query
        QueryBuilder queryBuilder = QueryBuilder.start();

        // If given, add the query for the name and description.
        if(query != null) {
            // Build the query pattern.
            Pattern queryPattern = Pattern.compile(".*" + query + ".*");

            // Create a query builder for the name portion.
            QueryBuilder nameQueryBuilder = QueryBuilder.start();

            // Add the name.
            nameQueryBuilder.and(Schema.JSON_KEY_NAME).regex(queryPattern);

            // Create a query builder for the version protion.
            QueryBuilder versionQueryBuilder = QueryBuilder.start();

            // Add the version.
            versionQueryBuilder
                .and(Schema.JSON_KEY_VERSION)
                .regex(queryPattern);

            // Add the name and version queries to the root query.
            queryBuilder.or(nameQueryBuilder.get(), versionQueryBuilder.get());
        }

        // Check if the survey must be visible to the Open mHealth APIs.
        if(omhVisibleOnly) {
            queryBuilder.and(Schema.JSON_KEY_OMH_VISIBLE).is(true);
        }

        // Get the list of results.
        @SuppressWarnings("unchecked")
        List<String> results =
            MONGO_COLLECTION.distinct(Schema.JSON_KEY_ID, queryBuilder.get());

        // Remember the total number of results.
        int numResults = results.size();

        // Sort the results.
        Collections.sort(results);

        // Get the lower index.
        int lowerIndex =
            (new Long(Math.min(numToSkip, results.size()))).intValue();
        // Get the upper index.
        int upperIndex =
            (new Long(Math.min(numToSkip + numToReturn, results.size())))
                .intValue();

        // Get the results based on the upper and lower bounds.
        results = results.subList(lowerIndex, upperIndex);

        // Create a MultiValueResult.
        MultiValueResult<String> result =
            new MongoMultiValueResultList<String>(results, numResults);

        // Return the list.
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyBin#getSurveyVersions(java.lang.String, java.lang.String, long, long)
     */
    @Override
    public MultiValueResult<Long> getSurveyVersions(
        final String surveyId,
        final String query,
        final boolean omhVisibleOnly,
        final long numToSkip,
        final long numToReturn)
        throws IllegalArgumentException {

        // Validate the input.
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the ID.
        queryBuilder.and(Schema.JSON_KEY_ID).is(surveyId);

        // If given, add the query for the name and description.
        if(query != null) {
            // Build the query pattern.
            Pattern queryPattern = Pattern.compile(".*" + query + ".*");

            // Create a query builder for the name portion.
            QueryBuilder nameQueryBuilder = QueryBuilder.start();

            // Add the name.
            nameQueryBuilder.and(Schema.JSON_KEY_NAME).regex(queryPattern);

            // Create a query builder for the version portion.
            QueryBuilder versionQueryBuilder = QueryBuilder.start();

            // Add the version.
            versionQueryBuilder
                .and(Schema.JSON_KEY_VERSION)
                .regex(queryPattern);

            // Add the name and version queries to the root query.
            queryBuilder.or(nameQueryBuilder.get(), versionQueryBuilder.get());
        }

        // Check if the survey must be visible to the Open mHealth APIs.
        if(omhVisibleOnly) {
            queryBuilder.and(Schema.JSON_KEY_OMH_VISIBLE).is(true);
        }

        // Get the list of results.
        @SuppressWarnings("unchecked")
        List<Long> results =
            MONGO_COLLECTION
                .distinct(Schema.JSON_KEY_VERSION, queryBuilder.get());

        // Remember the total number of results.
        int numResults = results.size();

        // Sort the results.
        Collections.sort(results);

        // Get the lower index.
        int lowerIndex =
            (new Long(Math.min(numToSkip, results.size()))).intValue();
        // Get the upper index.
        int upperIndex =
            (new Long(Math.min(numToSkip + numToReturn, results.size())))
                .intValue();

        // Get the results based on the upper and lower bounds.
        results = results.subList(lowerIndex, upperIndex);

        // Create a MultiValueResult.
        MultiValueResult<Long> result =
            new MongoMultiValueResultList<Long>(results, numResults);

        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyBin#getSurvey(java.lang.String, java.lang.Long)
     */
    @Override
    public Survey getSurvey(
        final String surveyId,
        final Long surveyVersion,
        final boolean omhVisibleOnly)
        throws IllegalArgumentException {

        // Validate the input.
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }
        if(surveyVersion == null) {
            throw new IllegalArgumentException("The survey version is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the survey ID.
        queryBuilder.and(Schema.JSON_KEY_ID).is(surveyId);

        // Add the survey version.
        queryBuilder.and(Schema.JSON_KEY_VERSION).is(surveyVersion);

        // Check if the survey must be visible to the Open mHealth APIs.
        if(omhVisibleOnly) {
            queryBuilder.and(Schema.JSON_KEY_OMH_VISIBLE).is(true);
        }

        // Execute query.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyBin#exists(java.lang.String, java.lang.Long)
     */
    @Override
    public boolean exists(
        final String surveyId,
        final Long surveyVersion,
        final boolean omhVisibleOnly)
        throws IllegalArgumentException {

        // Validate the input.
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the survey ID to the query.
        queryBuilder.and(Schema.JSON_KEY_ID).is(surveyId);

        // Add the survey version to the query, if given.
        if(surveyVersion != null) {
            queryBuilder.and(Schema.JSON_KEY_VERSION).is(surveyVersion);
        }

        // Check if the survey must be visible to the Open mHealth APIs.
        if(omhVisibleOnly) {
            queryBuilder.and(Schema.JSON_KEY_OMH_VISIBLE).is(true);
        }

        // The result is based on whether or not any results were found.
        return (MONGO_COLLECTION.findOne(queryBuilder.get()) != null);
    }

    @Override
    public Survey getLatestSurvey(
        final String surveyId,
        final boolean omhVisibleOnly)
        throws IllegalArgumentException {

        // Validate the input.
        if(surveyId == null) {
            throw new IllegalArgumentException("The survey ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the survey ID.
        queryBuilder.and(Schema.JSON_KEY_ID).is(surveyId);

        // Check if the survey must be visible to the Open mHealth APIs.
        if(omhVisibleOnly) {
            queryBuilder.and(Schema.JSON_KEY_OMH_VISIBLE).is(true);
        }

        // Create the sort.
        DBObject sort = new BasicDBObject(Schema.JSON_KEY_VERSION, -1);

        // Make the query.
        DBCursor<MongoSurvey> result =
            MONGO_COLLECTION.find(queryBuilder.get()).sort(sort).limit(1);

        // Return null or the schema based on what the query returned.
        if(result.count() == 0) {
            return null;
        }
        else {
            return result.next();
        }
    }
}