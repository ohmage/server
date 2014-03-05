package org.ohmage.mongodb.bin;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.StreamDataBin;
import org.ohmage.domain.ColumnList;
import org.ohmage.domain.DataPoint;
import org.ohmage.domain.MetaData;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.StreamData;
import org.ohmage.mongodb.domain.stream.MongoStreamData;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The MongoDB implementation of the database-backed stream data repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoStreamDataBin extends StreamDataBin {
	/**
	 * The name of the collection that contains all of the stream data.
	 */
	public static final String COLLECTION_NAME = "stream_data_bin";

	/**
	 * Get the connection to the stream data bin with the Jackson wrapper.
	 */
	private static final JacksonDBCollection<StreamData, String> COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				StreamData.class,
				String.class,
				MongoBinController.getObjectMapper());

	/**
	 * Get the connection to the stream bin with the Jackson wrapper,
	 * specifically for {@link StreamData} objects.
	 */
	private static final JacksonDBCollection<MongoStreamData, String> MONGO_COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				MongoStreamData.class,
				String.class,
				MongoBinController.getObjectMapper());

	/**
	 * Default constructor.
	 */
	protected MongoStreamDataBin() {
		// Ensure that there is an index on the point's version.
		COLLECTION
			.ensureIndex(
				new BasicDBObject(
					DataPoint.JSON_KEY_META_DATA +
						"." + MetaData.JSON_KEY_ID,
					1),
				COLLECTION_NAME + "_" +
					DataPoint.JSON_KEY_META_DATA + "." +
						MetaData.JSON_KEY_ID,
				false);

		// Create the set of indexes.
		DBObject indexes = new BasicDBObject();
		// Index the stream ID.
		indexes.put(DataPoint.JSON_KEY_SCHEMA_ID, 1);
		// Index the stream version.
		indexes.put(DataPoint.JSON_KEY_SCHEMA_VERSION, 1);
		// Index the data point's unique ID.
		indexes
			.put(
				DataPoint.JSON_KEY_META_DATA + "." +
					MetaData.JSON_KEY_ID,
				1);

		// Ensure that there is a unique index on the stream ID and version and
		// the point's ID.
		COLLECTION
			.ensureIndex(
				indexes,
				COLLECTION_NAME + "_" +
					DataPoint.JSON_KEY_SCHEMA_ID + "_" +
					DataPoint.JSON_KEY_SCHEMA_VERSION + "_" +
					DataPoint.JSON_KEY_META_DATA + "." +
						MetaData.JSON_KEY_ID +
					"_unique",
				true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamDataBin#addStreamData(java.util.List)
	 */
	@Override
	public void addStreamData(
		final List<StreamData> streamData)
		throws IllegalArgumentException, InvalidArgumentException {

		// Validate the parameters.
		if(streamData == null) {
			throw new IllegalArgumentException("The stream data is null.");
		}

		// Save it.
		try {
			COLLECTION.insert(streamData);
		}
		catch(MongoException.DuplicateKey e) {
			throw
				new InvalidArgumentException(
					"A data point had the same ID as an existing data point.",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamDataBin#getStreamData(java.lang.String, long, java.util.Set, org.joda.time.DateTime, org.joda.time.DateTime, org.ohmage.domain.ColumnList, boolean, long, long)
	 */
	@Override
	public MultiValueResult<MongoStreamData> getStreamData(
		final String streamId,
		final long streamVersion,
        final Set<String> userIds,
        final DateTime startDate,
        final DateTime endDate,
        final ColumnList columnList,
        final boolean chronological,
        final long numToSkip,
        final long numToReturn)
		throws IllegalArgumentException {

		// Validate the parameters.
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}
        if(userIds == null) {
            throw
                new IllegalArgumentException(
                    "The set of users' unique identifier is null.");
        }

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the stream ID.
		queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_ID).is(streamId);

		// Add the stream version.
		queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_VERSION).is(streamVersion);

        // Add the user's unique identifier.
        queryBuilder.and(DataPoint.JSON_KEY_OWNER).in(userIds);

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
			new MongoMultiValueResultCursor<MongoStreamData>(
				MONGO_COLLECTION
				    .find(queryBuilder.get(), columns)
				    .sort(sort)
				    .skip((int) numToSkip)
				    .limit((int) numToReturn));
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.StreamDataBin#getStreamData(java.lang.String, java.lang.String, long, java.lang.String)
	 */
	@Override
	public StreamData getStreamData(
		final String userId,
		final String streamId,
		final long streamVersion,
		final String pointId)
		throws IllegalArgumentException {

		// Validate the parameters.
		if(userId == null) {
			throw
			    new IllegalArgumentException(
			        "The user's unique identifier is null.");
		}
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}
		if(pointId == null) {
			throw new IllegalArgumentException("The point ID is null.");
		}

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the user's unique identifier.
		queryBuilder.and(DataPoint.JSON_KEY_OWNER).is(userId);

		// Add the stream ID.
		queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_ID).is(streamId);

		// Add the stream version.
		queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_VERSION).is(streamVersion);

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
	 * @see org.ohmage.bin.StreamDataBin#deleteStreamData(java.lang.String, java.lang.String, long, java.lang.String)
	 */
	@Override
	public void deleteStreamData(
		final String userId,
		final String streamId,
		final long streamVersion,
		final String pointId)
		throws IllegalArgumentException {

		// Validate the parameters.
		if(userId == null) {
			throw
			    new IllegalArgumentException(
			        "The user's unique identifier is null.");
		}
		if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}
		if(pointId == null) {
			throw new IllegalArgumentException("The point ID is null.");
		}

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the user's unique identifier.
		queryBuilder.and(DataPoint.JSON_KEY_OWNER).is(userId);

		// Add the stream ID.
		queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_ID).is(streamId);

		// Add the stream version.
		queryBuilder.and(DataPoint.JSON_KEY_SCHEMA_VERSION).is(streamVersion);

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