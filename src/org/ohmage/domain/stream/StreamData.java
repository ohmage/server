package org.ohmage.domain.stream;

import org.joda.time.DateTime;

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
public class StreamData {
	/**
	 * The JSON key for the stream.
	 */
	public static final String JSON_KEY_STREAM = "stream";
	/**
	 * The JSON key for the ID.
	 */
	public static final String JSON_KEY_ID = "id";
	/**
	 * The JSON key for the time-stamp.
	 */
	public static final String JSON_KEY_TIMESTAMP = "timestamp";
	/**
	 * The JSON key for the data.
	 */
	public static final String JSON_KEY_DATA = "data";

	/**
	 * The stream to which this data conforms.
	 */
	@JsonProperty(JSON_KEY_STREAM)
	private final Stream stream;
	/**
	 * The unique ID for this point.
	 */
	@JsonProperty(JSON_KEY_ID)
	private final String id;
	/**
	 * The time-stamp for this point.
	 */
	@JsonProperty(JSON_KEY_TIMESTAMP)
	private final DateTime timestamp;
	/**
	 * The data for this point.
	 */
	@JsonProperty(JSON_KEY_DATA)
	private final JsonNode data;

	/**
	 * Creates a new stream data point.
	 * 
	 * @param stream
	 *        The stream to which this data conforms.
	 * 
	 * @param id
	 *        The unique identifier for this point.
	 * 
	 * @param timestamp
	 *        The time-stamp for this point.
	 * 
	 * @param data
	 *        The data for this point.
	 */
	@JsonCreator
	public StreamData(
		@JsonProperty(JSON_KEY_STREAM) final Stream stream,
		@JsonProperty(JSON_KEY_ID) final String id,
		@JsonProperty(JSON_KEY_TIMESTAMP) final DateTime timestamp,
		@JsonProperty(JSON_KEY_DATA) final JsonNode data) {

		if(stream == null) {
			throw new IllegalArgumentException("The stream is null.");
		}
		if(id == null) {
			throw new IllegalArgumentException("The ID is null.");
		}
		if(data == null) {
			throw new IllegalArgumentException("The data is null.");
		}

		this.stream = stream;
		this.id = id;
		this.timestamp = timestamp;
		this.data = data;
	}
}