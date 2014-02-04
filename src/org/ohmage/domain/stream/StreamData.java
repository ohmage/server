package org.ohmage.domain.stream;

import org.ohmage.domain.DataPoint;
import org.ohmage.domain.MetaData;
import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * <p>
 * A single data point that conforms to some stream.
 * </p>
 *
 * @author John Jenkins
 */
public class StreamData extends DataPoint<JsonNode> {
	/**
	 * <p>
	 * A builder for {@link StreamData}.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class Builder extends DataPoint.Builder<JsonNode> {
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

			super(metaData, data);
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
		@Override
        public StreamData build() throws InvalidArgumentException {
			return
				new StreamData(
					getOwner(),
					getSchemaId(),
					getSchemaVersion(),
					getMetaData(),
					getData(),
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
		public StreamData build(final Stream stream)
			throws InvalidArgumentException {

			// Validate that the data is valid.
			stream.validate(getMetaData(), getData());

			// Update this builder's stream reference.
			setSchemaId(stream.getId());
			setSchemaVersion(stream.getVersion());

			// Build and return the StreamData object.
			return build();
		}
	}

	/**
	 * Creates a new stream data point.
	 *
	 * @param owner
	 *        The unique identifier for the user that owns this data.
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
		final Long streamVersion,
		final MetaData metaData,
		final JsonNode data) {

		this(owner, streamId, streamVersion, metaData, data, null);
	}

	/**
	 * Rebuilds an existing data point.
	 *
	 * @param owner
	 *        The unique identifier for the user that owns this data.
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
		@JsonProperty(JSON_KEY_SCHEMA_ID) final String streamId,
        @JsonProperty(JSON_KEY_SCHEMA_VERSION) final Long streamVersion,
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
	 *        The unique identifier for the user that owns this data.
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
		final Long streamVersion,
		final MetaData metaData,
		final JsonNode data,
		final Long internalReadVersion,
		final Long internalWriteVersion) {

		super(
		    owner,
		    streamId,
		    streamVersion,
		    metaData,
		    data,
		    internalReadVersion,
		    internalWriteVersion);
	}
}