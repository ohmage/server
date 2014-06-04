package org.ohmage.mongodb.bin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.StreamBin;
import org.ohmage.domain.Schema;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.mongodb.domain.stream.MongoStream;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The MongoDB implementation of the database-backed stream repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoStreamBin extends StreamBin {
	/**
	 * The name of the collection that contains all of the streams.
	 */
	public static final String COLLECTION_NAME = "stream_bin";

	/**
	 * Get the connection to the stream bin with the Jackson wrapper.
	 */
	private static final JacksonDBCollection<Stream, String> COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				Stream.class,
				String.class,
				MongoBinController.getObjectMapper());

	/**
	 * Get the connection to the stream bin with the Jackson wrapper,
	 * specifically for {@link MongoStream} objects.
	 */
	private static final JacksonDBCollection<MongoStream, String> MONGO_COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				MongoStream.class,
				String.class,
				MongoBinController.getObjectMapper());

	/**
	 * Default constructor.
	 */
	protected MongoStreamBin() {
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
	 * @see org.ohmage.bin.StreamBin#addStream(org.ohmage.domain.stream.Stream)
	 */
	@Override
	public void addStream(
		final Stream stream)
		throws IllegalArgumentException, InvalidArgumentException {

		// Validate the parameter.
		if(stream == null) {
			throw new IllegalArgumentException("The stream is null.");
		}

		// Save it.
		try {
			COLLECTION.insert(stream);
		}
		catch(MongoException.DuplicateKey e) {
			throw
				new InvalidArgumentException(
					"A stream with the same ID-version pair already exists.",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#getStreamIds(java.lang.String, long, long)
	 */
	@Override
	public MultiValueResult<String> getStreamIds(
	    final String query,
        final boolean omhVisibleOnly,
	    final long numToSkip,
	    final long numToReturn) {
		ArrayList<String> results = new ArrayList<String>();
		MultiValueResult<Stream> streams = getStreams(query, omhVisibleOnly, numToSkip, numToReturn);
		for(Stream stream : streams) {
			results.add(stream.getId());
		}
		return new MongoMultiValueResultList<String>(results, results.size());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#getStreams(java.lang.String, long, long)
	 */
	@Override
	public MultiValueResult<Stream> getStreams(
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

		// Check if the stream must be visible to the Open mHealth APIs.
		if(omhVisibleOnly) {
		    queryBuilder.and(Schema.JSON_KEY_OMH_VISIBLE).is(true);
		}

        BasicDBObjectBuilder fields = BasicDBObjectBuilder
            .start()
            .add(Schema.JSON_KEY_ID , 1 )
            .add(Schema.JSON_KEY_NAME , 1 )
            .add(Schema.JSON_KEY_DESCRIPTION, "");

        // Get the list of results.
        @SuppressWarnings("unchecked")
        DBCursor<Stream> results =
            COLLECTION.find(queryBuilder.get()).skip((int)numToSkip).limit((int)numToReturn);

        // Sort the results.
        results.sort(BasicDBObjectBuilder.start().add(Schema.JSON_KEY_ID,1).get());

        // Create a MultiValueResult.
        MultiValueResult<Stream> result =
            new MongoMultiValueResultList<Stream>(results.toArray(), results.size());

        // Return the list.
        return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#getStreamVersions(java.lang.String, java.lang.String, long, long)
	 */
	@Override
	public MultiValueResult<Long> getStreamVersions(
		final String streamId,
		final String query,
        final boolean omhVisibleOnly,
        final long numToSkip,
        final long numToReturn)
		throws IllegalArgumentException {

		// Validate the input.
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the ID.
		queryBuilder.and(Schema.JSON_KEY_ID).is(streamId);

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

        // Check if the stream must be visible to the Open mHealth APIs.
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
	 * @see org.ohmage.bin.StreamBin#getStream(java.lang.String, java.lang.Long)
	 */
	@Override
	public Stream getStream(
		final String streamId,
		final Long streamVersion,
        final boolean omhVisibleOnly)
		throws IllegalArgumentException {

		// Validate the input.
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}
		if(streamVersion == null) {
			throw new IllegalArgumentException("The stream version is null.");
		}

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the schema ID.
		queryBuilder.and(Schema.JSON_KEY_ID).is(streamId);

		// Add the schema version.
		queryBuilder.and(Schema.JSON_KEY_VERSION).is(streamVersion);

        // Check if the stream must be visible to the Open mHealth APIs.
        if(omhVisibleOnly) {
            queryBuilder.and(Schema.JSON_KEY_OMH_VISIBLE).is(true);
        }

		// Execute query.
		return MONGO_COLLECTION.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#exists(java.lang.String, java.lang.Long)
	 */
	@Override
	public boolean exists(
		final String streamId,
		final Long streamVersion,
        final boolean omhVisibleOnly)
		throws IllegalArgumentException {

		// Validate the input.
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the stream ID to the query.
		queryBuilder.and(Schema.JSON_KEY_ID).is(streamId);

		// Add the stream version to the query, if given.
		if(streamVersion != null) {
		    queryBuilder.and(Schema.JSON_KEY_VERSION).is(streamVersion);
		}

        // Check if the stream must be visible to the Open mHealth APIs.
        if(omhVisibleOnly) {
            queryBuilder.and(Schema.JSON_KEY_OMH_VISIBLE).is(true);
        }

		// The result is based on whether or not any results were found.
		return (MONGO_COLLECTION.findOne(queryBuilder.get()) != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#getLatestStream(java.lang.String)
	 */
	@Override
	public Stream getLatestStream(
		final String streamId,
        final boolean omhVisibleOnly)
		throws IllegalArgumentException {

		// Validate the input.
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the schema ID.
		queryBuilder.and(Schema.JSON_KEY_ID).is(streamId);

        // Check if the stream must be visible to the Open mHealth APIs.
        if(omhVisibleOnly) {
            queryBuilder.and(Schema.JSON_KEY_OMH_VISIBLE).is(true);
        }

		// Create the sort.
		DBObject sort = new BasicDBObject(Schema.JSON_KEY_VERSION, -1);

		// Make the query.
		DBCursor<MongoStream> result =
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