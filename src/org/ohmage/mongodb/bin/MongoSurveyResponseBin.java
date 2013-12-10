package org.ohmage.mongodb.bin;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.SurveyResponseBin;
import org.ohmage.domain.MetaData;
import org.ohmage.domain.MultiValueResult;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.Media;
import org.ohmage.domain.survey.SurveyResponse;
import org.ohmage.mongodb.domain.MongoCursorMultiValueResult;
import org.ohmage.mongodb.domain.survey.response.MongoSurveyResponse;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

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
     * The name of the collection that contains all of the media for the survey
     * responses.
     */
    private static final String SURVEY_RESPONSE_MEDIA_COLLECTION_NAME =
        "survey_response_media";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(MongoSurveyResponseBin.class.getName());

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
     * A connection to the container for the media within survey responses.
     */
    private final GridFS surveyResponseMediaConnection;

    /**
     * Default constructor.
     */
    protected MongoSurveyResponseBin() {
        // Create the set of indexes.
        DBObject indexes = new BasicDBObject();
        // Index the survey ID.
        indexes.put(SurveyResponse.JSON_KEY_OWNER, 1);
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

        // Ensure that there is a unique index on the user, survey ID, version
        // and the point's ID.
        COLLECTION
            .ensureIndex(
                indexes,
                COLLECTION_NAME + "_" +
                    SurveyResponse.JSON_KEY_OWNER + "_" +
                    SurveyResponse.JSON_KEY_SURVEY_ID + "_" +
                    SurveyResponse.JSON_KEY_SURVEY_VERSION + "_" +
                    SurveyResponse.JSON_KEY_META_DATA + "." +
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

        // Connect to the container for the media for survey responses.
        surveyResponseMediaConnection =
            new GridFS(
                MongoBinController.getInstance().getDb(),
                SURVEY_RESPONSE_MEDIA_COLLECTION_NAME);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.SurveyResponseBin#addSurveyResponses(java.util.List, java.util.Map)
     */
    @Override
    public void addSurveyResponses(
        final List<SurveyResponse> surveyResponses,
        final Map<String, MultipartFile> media)
        throws IllegalArgumentException, InvalidArgumentException {

        // Validate the parameters.
        if(surveyResponses == null) {
            throw
                new IllegalArgumentException("The survey responses are null.");
        }

        // Create a handle for all of the files that were successfully
        // saved.
        List<String> savedKeys = new LinkedList<String>();

        // Create a catch block to determine when things have failed.
        boolean failed = false;
        try {
            // Save it.
            try {
                COLLECTION.insert(surveyResponses);
            }
            catch(MongoException.DuplicateKey e) {
                failed = true;
                throw
                    new InvalidArgumentException(
                        "A survey response had the same unique key as " +
                            "another survey response for the same user and " +
                            "survey, or one of the media files had the same " +
                            "unique key as any other media file.",
                        e);
            }

            // Save all of the relevant files.
            if(media != null) {
                // Save each media file.
                for(String key : media.keySet()) {
                    // Get the media file.
                    MultipartFile currMedia = media.get(key);

                    // Get the InputStream handle to the file.
                    InputStream in;
                    try {
                        in = currMedia.getInputStream();
                    }
                    // If an error occurs, throw an exception.
                    catch(IOException e) {
                        failed = true;
                        throw
                            new IllegalArgumentException(
                                "Could not connect to a media input stream: " +
                                    key,
                                e);
                    }

                    // Create the file.
                    GridFSInputFile file =
                        surveyResponseMediaConnection.createFile(in, key);
                    file.setContentType(currMedia.getContentType());

                    // Save the file.
                    try {
                        file.save();
                    }
                    catch(MongoException e) {
                        failed = true;
                        throw
                            new IllegalArgumentException(
                                "Could not save the media file: " + key,
                                e);
                    }

                    // Add the file to the list.
                    savedKeys.add(key);
                }
            }
        }
        finally {
            // If we have failed, delete the saved files.
            if(failed) {
                for(String savedKey : savedKeys) {
                    try {
                        surveyResponseMediaConnection.remove(savedKey);
                    }
                    catch(MongoException e) {
                        LOGGER
                            .log(
                                Level.SEVERE,
                                "Error rolling back and deleting file: " +
                                    savedKey,
                                e);
                    }
                }
            }
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
        final long surveyVersion)
        throws IllegalArgumentException {

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
        final String pointId)
        throws IllegalArgumentException {

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
     * @see org.ohmage.bin.SurveyResponseBin#getMedia(java.lang.String)
     */
    @Override
    public Media getMedia(final String mediaId) {
        // Get all of the files with the given filename.
        List<GridFSDBFile> files = surveyResponseMediaConnection.find(mediaId);

        // If no files were found, return null.
        if(files.size() == 0) {
            return null;
        }
        // If multiple files were found, that is a violation of the system.
        if(files.size() > 1) {
            throw
                new IllegalStateException(
                    "Multiple files have the same filename: " + mediaId);
        }

        // Get the file.
        GridFSDBFile file = files.get(0);

        // Create and return the Media object.
        return
            new Media(
                file.getInputStream(),
                file.getLength(),
                file.getContentType());
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
        final String pointId)
        throws IllegalArgumentException {

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