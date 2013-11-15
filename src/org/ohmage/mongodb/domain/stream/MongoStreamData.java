package org.ohmage.mongodb.domain.stream;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.MetaData;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.domain.stream.StreamData;
import org.ohmage.mongodb.bin.MongoStreamDataBin;
import org.ohmage.mongodb.domain.MongoDbObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * <p>
 * A MongoDB extension of the {@link StreamData} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoStreamDataBin.COLLECTION_NAME)
public class MongoStreamData extends StreamData implements MongoDbObject {
	/**
	 * The database ID for this object.
	 */
	@ObjectId
	private final String dbId;

	/**
	 * Creates a {@link Stream} object via Jackson from the data layer.
	 * 
	 * @param dbId
	 *        The database ID for this stream.
	 * 
	 * @param owner
	 *        The user-name of the user that owns this data.
	 * 
	 * @param streamId
	 *        The unique identifier for the stream to which this data conforms.
	 * 
	 * @param streamVersion
	 *        The version of the stream to which this data conforms.
	 * 
	 * @param metaData
	 *        The meta-data about this data, which may be null.
	 * 
	 * @param data
	 *        The actual data for this data point.
	 * 
	 * @param internalVersion
	 *        The internal version of this schema.
	 * 
	 * @throws IllegalArgumentException
	 *         The ID is invalid.
	 * 
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	@JsonCreator
	protected MongoStreamData(
		@Id @ObjectId final String dbId, 
		@JsonProperty(JSON_KEY_OWNER) final String owner,
		@JsonProperty(JSON_KEY_STREAM_ID) final String streamId,
		@JsonProperty(JSON_KEY_STREAM_VERSION) final long streamVersion,
		@JsonProperty(JSON_KEY_META_DATA) final MetaData metaData,
		@JsonProperty(JSON_KEY_DATA) final JsonNode data,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion) 
		throws IllegalArgumentException {

		super(owner, streamId, streamVersion, metaData, data, internalVersion);
		
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