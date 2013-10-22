package org.ohmage.mongodb.bin;

import java.util.List;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.StreamBin;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.Stream;

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
	
	// Get the connection to the registry with the Jackson wrapper.
	private static final JacksonDBCollection<Stream, Object> COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				Stream.class,
				Object.class,
				MongoBinController.getObjectMapper());
	
	/**
	 * Default constructor.
	 */
	protected MongoStreamBin() {
		// Ensure that there is an index on the ID.
		COLLECTION
			.ensureIndex(
				new BasicDBObject(Stream.JSON_KEY_ID, 1),
				COLLECTION_NAME + "_" + Stream.JSON_KEY_ID,
				false);
		
		// Ensure that there is an index on the version.
		COLLECTION
			.ensureIndex(
				new BasicDBObject(Stream.JSON_KEY_VERSION, 1),
				COLLECTION_NAME + "_" + Stream.JSON_KEY_VERSION,
				false);
		
		// Create the set of indexes.
		DBObject indexes = new BasicDBObject();
		// Index the ID.
		indexes.put(Stream.JSON_KEY_ID, 1);
		// Index the version.
		indexes.put(Stream.JSON_KEY_VERSION, 1);

		// Ensure that there is a unique index on the ID and version.
		COLLECTION
			.ensureIndex(
				indexes, 
				COLLECTION_NAME + "_" +
					Stream.JSON_KEY_ID + "_" +
					Stream.JSON_KEY_VERSION + "_unique",
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
					"A stream with the same ID-version pair already exists.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#getStreamIds()
	 */
	@Override
	public List<String> getStreamIds() {
		// Get the list of results.
		@SuppressWarnings("unchecked")
		List<String> result = COLLECTION.distinct(Stream.JSON_KEY_ID);
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamBin#getStreamVersions(java.lang.String)
	 */
	@Override
	public List<Long> getStreamVersions(
		final String streamId)
		throws IllegalArgumentException {
		
		// Validate the input.
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}
		
		// Get the list of results.
		@SuppressWarnings("unchecked")
		List<Long> result =
			COLLECTION
				.distinct(
					Stream.JSON_KEY_VERSION,
					new BasicDBObject(Stream.JSON_KEY_ID, streamId));
		
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
		
		// Build the query
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the schema ID.
		queryBuilder.and(Stream.JSON_KEY_ID).is(streamId);
		
		// Add the schema version.
		queryBuilder.and(Stream.JSON_KEY_VERSION).is(streamVersion);
		
		// Execute query.
		return COLLECTION.findOne(queryBuilder.get());
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
		BasicDBObject query = new BasicDBObject(Stream.JSON_KEY_ID, streamId);
		
		// Add the stream version to the query, if given.
		if(streamVersion != null) {
			query.put(Stream.JSON_KEY_VERSION, streamVersion);
		}
		
		// The result is based on whether or not any results were found.
		return (COLLECTION.count(query) > 0);
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
		
		// Build the query
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the schema ID.
		queryBuilder.and(Stream.JSON_KEY_ID).is(streamId);
		
		// Create the sort.
		DBObject sort = new BasicDBObject(Stream.JSON_KEY_VERSION, -1);

		// Make the query.
		DBCursor<Stream> result =
			COLLECTION.find(queryBuilder.get()).sort(sort).limit(1);
		
		// Return null or the schema based on what the query returned.
		if(result.count() == 0) {
			return null;
		}
		else {
			return result.next();
		}
	}
}