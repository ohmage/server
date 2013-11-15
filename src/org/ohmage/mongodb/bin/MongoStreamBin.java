package org.ohmage.mongodb.bin;

import java.util.List;
import java.util.regex.Pattern;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.StreamBin;
import org.ohmage.domain.Schema;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.mongodb.domain.stream.MongoStream;

import com.mongodb.BasicDBObject;
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
	 * @see org.ohmage.bin.StreamBin#getStreamIds(java.lang.String)
	 */
	@Override
	public List<String> getStreamIds(final String query) {
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

		// Get the list of results.
		@SuppressWarnings("unchecked")
		List<String> result =
			MONGO_COLLECTION.distinct(Schema.JSON_KEY_ID, queryBuilder.get());

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#getStreamVersions(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Long> getStreamVersions(
		final String streamId,
		final String query)
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

		// Get the list of results.
		@SuppressWarnings("unchecked")
		List<Long> result =
			MONGO_COLLECTION
				.distinct(Schema.JSON_KEY_VERSION, queryBuilder.get());

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#getStream(java.lang.String, java.lang.Long)
	 */
	@Override
	public Stream getStream(
		final String streamId,
		final Long streamVersion)
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
		final Long streamVersion)
		throws IllegalArgumentException {

		// Validate the input.
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}

		// Add the stream ID to the query.
		BasicDBObject query = new BasicDBObject(Schema.JSON_KEY_ID, streamId);

		// Add the stream version to the query, if given.
		if(streamVersion != null) {
			query.put(Schema.JSON_KEY_VERSION, streamVersion);
		}

		// The result is based on whether or not any results were found.
		return (MONGO_COLLECTION.findOne() != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#getLatestStream(java.lang.String)
	 */
	@Override
	public Stream getLatestStream(
		final String streamId)
		throws IllegalArgumentException {

		// Validate the input.
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the schema ID.
		queryBuilder.and(Schema.JSON_KEY_ID).is(streamId);

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