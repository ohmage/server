package org.ohmage.domain;

import org.joda.time.DateTime;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.StreamData;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * <p>
 * The meta-data associated with a {@link StreamData} point.
 * </p>
 *
 * @author John Jenkins
 */
public class MetaData {
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
     *        The point in time to which this data should be associated or null
     *        if it is not associated with time in any way.
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
     * Returns the unique identifier associated with this {@link StreamData}
     * point or null if this point has no unique identifier.
     *
     * @return The unique identifier associated with this {@link StreamData}
     *         point or null.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the date and time associated with this {@link StreamData} point
     * or null if it is not associated with any specific date and time.
     *
     * @return The date and time associated with this {@link StreamData} point
     *         or null.
     */
    public DateTime getTimestamp() {
        return timestamp;
    }
}