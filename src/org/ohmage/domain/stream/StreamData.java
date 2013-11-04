package org.ohmage.domain.stream;

import org.joda.time.DateTime;
import org.ohmage.domain.ISOW3CDateTimeFormat;
import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * <p>
 * A single data point that conforms to some stream.
 * </p>
 * 
 * @author John Jenkins
 */
public class StreamData extends OhmageDomainObject {
	/**
	 * <p>
	 * The meta-data associated with a {@link StreamData} point.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	@JsonInclude(Include.NON_NULL)
	public static class MetaData {
		/**
		 * The JSON key for the ID.
		 */
		public static final String JSON_KEY_ID = "id";
		/**
		 * The JSON key for the time-stamp.
		 */
		public static final String JSON_KEY_TIMESTAMP = "timestamp";
		
		/**
		 * The unique ID for this point.
		 */
		@JsonProperty(JSON_KEY_ID)
		@JsonInclude(Include.NON_NULL)
		private final String id;
		/**
		 * The time-stamp for this point.
		 */
		@JsonProperty(JSON_KEY_TIMESTAMP)
		@JsonInclude(Include.NON_NULL)
		@JsonSerialize(using = ToStringSerializer.class)
		@JsonDeserialize(using = ISOW3CDateTimeFormat.Deserializer.class)
		private final DateTime timestamp;
		
		/**
		 * Creates a new MetaData object.
		 * 
		 * @param id
		 *        The unique identifier or null if it does not have a unique
		 *        identifier.
		 * 
		 * @param timestamp
		 *        The point in time to which this data should be associated or
		 *        null if it is not associated with time in any way.
		 * 
		 * @throws InvalidArgumentException
		 *         The ID is null.
		 */
		@JsonCreator
		public MetaData(
			@JsonProperty(JSON_KEY_ID) final String id,
			@JsonProperty(JSON_KEY_TIMESTAMP) final DateTime timestamp)
			throws InvalidArgumentException {
			
			if(id == null) {
				throw new InvalidArgumentException("The ID is null.");
			}
			
			this.id = id;
			this.timestamp = timestamp;
		}
		
		/**
		 * Returns the unique identifier associated with this
		 * {@link StreamData} point or null if this point has no unique
		 * identifier.
		 * 
		 * @return The unique identifier associated with this
		 *         {@link StreamData} point or null.
		 */
		public String getId() {
			return id;
		}
		
		/**
		 * Returns the date and time associated with this {@link StreamData}
		 * point or null if it is not associated with any specific date and
		 * time.
		 * 
		 * @return The date and time associated with this {@link StreamData}
		 *         point or null.
		 */
		public DateTime getTimestamp() {
			return timestamp;
		}
	}
	
	/**
	 * <p>
	 * A builder for {@link StreamData}.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class Builder
		extends OhmageDomainObject.Builder<StreamData> {

		/**
		 * The user-name of the user that owns this data point.
		 */
		protected String owner;

		/**
		 * The unique ID for the stream to which this data conforms.
		 */
		protected String streamId;

		/**
		 * The version for the stream to which this data conforms.
		 */
		protected Long streamVersion;
		
		/**
		 * The meta-data associated with this point.
		 */
		protected MetaData metaData;
		
		/**
		 * The data for this point.
		 */
		protected JsonNode data;
		
		/**
		 * Creates a new {@link StreamData} Builder object.
		 * 
		 * @param metaData
		 *        The meta-data associated with this data.
		 * 
		 * @param data
		 *        The actual data for this {@link StreamData}.
		 */
		public Builder(
			@JsonProperty(JSON_KEY_META_DATA) final MetaData metaData,
			@JsonProperty(JSON_KEY_DATA) final JsonNode data) {
			
			super(null);
			
			this.metaData = metaData;
			this.data = data;
		}
		
		/**
		 * Creates a new Builder object based on an existing {@link StreamData}
		 * object.
		 * 
		 * @param streamData
		 *        The existing {@link StreamData} object on which this Builder
		 *        should be based.
		 */
		public Builder(final StreamData streamData) {
			super(streamData);
			
			this.metaData = streamData.metaData;
			this.data = streamData.data;
		}
		
		/**
		 * Sets the user-name of the user that owns this data point.
		 * 
		 * @param owner
		 *        The user-name of the user that owns this data point.
		 * 
		 * @return Returns this build to facilitate chaining.
		 */
		public Builder setOwner(final String owner) {
			this.owner = owner;
			
			return this;
		}
		
		/**
		 * Sets the identifier for the stream to which this data conforms.
		 * 
		 * @param streamId
		 *        The unique identifier for the stream to which this data
		 *        conforms.
		 * 
		 * @return Returns this builder to facilitate chaining.
		 */
		public Builder setStreamId(final String streamId) {
			this.streamId = streamId;
			
			return this;
		}
		
		/**
		 * Sets the version of the stream to which this data conforms.
		 * 
		 * @param streamVersion
		 *        The version of the stream to which this data conforms.
		 * 
		 * @return Returns this builder to facilitate chaining.
		 */
		public Builder setStreamVersion(final Long streamVersion) {
			this.streamVersion = streamVersion;
			
			return this;
		}
		
		/**
		 * Builds a new {@link StreamData} object based on the current state of
		 * this builder.
		 * 
		 * @return The built {@link StreamData} object.
		 * 
		 * @throws InvalidArgumentException
		 *         The state of this builder is insufficient to build a new
		 *         {@link StreamData} object.
		 */
		public StreamData build() throws InvalidArgumentException {
			return
				new StreamData(
					owner,
					streamId, 
					streamVersion, 
					metaData, 
					data, 
					internalReadVersion, 
					internalWriteVersion);
		}
		
		/**
		 * First, validates the data, then builds a new {@link StreamData}
		 * object based on the current state of this builder. The resulting
		 * object's stream reference will use the given {@link Stream}, thereby
		 * overriding any stream references that are part of this builder.
		 * 
		 * @param stream
		 *        The {@link Stream} object that should be used to validate
		 *        this data and whose reference should be used to build the
		 *        {@link StreamData} result.
		 * 
		 * @return The built {@link StreamData} object.
		 * 
		 * @throws InvalidArgumentException
		 *         The state of this builder is insufficient to build a new
		 *         {@link StreamData} object or the data is invalid.
		 */
		public StreamData build(
			final Stream stream)
			throws InvalidArgumentException {
			
			// Validate that the data is valid.
			stream.validate(metaData, data);
			
			// Update this builder's stream reference.
			streamId = stream.getId();
			streamVersion = stream.getVersion();
			
			// Build and return the StreamData object.
			return build();
		}
	}
	
	/**
	 * The JSON key for the data point's owner's user-name.
	 */
	public static final String JSON_KEY_OWNER = "owner";
	
	/**
	 * The JSON key for the stream's ID.
	 */
	public static final String JSON_KEY_STREAM_ID = "stream_id";
	
	/**
	 * The JSON key for the stream's version.
	 */
	public static final String JSON_KEY_STREAM_VERSION = "stream_version";
	
	/**
	 * The JSON key for the meta-data.
	 */
	public static final String JSON_KEY_META_DATA = "meta_data";
	
	/**
	 * The JSON key for the data.
	 */
	public static final String JSON_KEY_DATA = "data";
	
	/**
	 * The user-name of the user that owns this data.
	 */
	@JsonProperty(JSON_KEY_OWNER)
	private final String owner;

	/**
	 * The unique ID for the stream to which this data conforms.
	 */
	@JsonProperty(JSON_KEY_STREAM_ID)
	private final String streamId;

	/**
	 * The version for the stream to which this data conforms.
	 */
	@JsonProperty(JSON_KEY_STREAM_VERSION)
	private final long streamVersion;
	
	/**
	 * The meta-data associated with this point.
	 */
	@JsonProperty(JSON_KEY_META_DATA)
	private final MetaData metaData;
	
	/**
	 * The data for this point.
	 */
	@JsonProperty(JSON_KEY_DATA)
	private final JsonNode data;

	/**
	 * Creates a new stream data point.
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
	 */
	public StreamData(
		final String owner,
		final String streamId,
		final long streamVersion,
		final MetaData metaData,
		final JsonNode data) {
		
		this(owner, streamId, streamVersion, metaData, data, null);
	}
	
	/**
	 * Rebuilds an existing data point.
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
	 *        The internal version of this point.
	 */
	@JsonCreator
	protected StreamData(
		@JsonProperty(JSON_KEY_OWNER) final String owner,
		@JsonProperty(JSON_KEY_STREAM_ID) final String streamId,
		@JsonProperty(JSON_KEY_STREAM_VERSION) final long streamVersion,
		@JsonProperty(JSON_KEY_META_DATA) final MetaData metaData,
		@JsonProperty(JSON_KEY_DATA) final JsonNode data,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion) {
		
		this(
			owner, 
			streamId, 
			streamVersion, 
			metaData, 
			data, 
			internalVersion, 
			null);
	}
	
	/**
	 * Builder constructor that validates the state of the object.
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
	 * @param internalReadVersion
	 *        The internal version of this object when it was read from the
	 *        storage.
	 * 
	 * @param internalWriteVersion
	 *        The new internal version of this object when it is written back
	 *        to the storage.
	 */
	private StreamData(
		final String owner,
		final String streamId,
		final long streamVersion,
		final MetaData metaData,
		final JsonNode data,
		final Long internalReadVersion,
		final Long internalWriteVersion) {
		
		super(internalReadVersion, internalWriteVersion);

		if(owner == null) {
			throw new InvalidArgumentException("The owner is null.");
		}
		if(streamId == null) {
			throw new InvalidArgumentException("The stream ID is null.");
		}
		if(data == null) {
			throw new InvalidArgumentException("The data is null.");
		}

		this.owner = owner;
		this.streamId = streamId;
		this.streamVersion = streamVersion;
		this.metaData = metaData;
		this.data = data;
	}
}