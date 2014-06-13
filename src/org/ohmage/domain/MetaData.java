package org.ohmage.domain;

import org.joda.time.DateTime;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.jackson.OhmageObjectMapper.JsonFilterField;
import org.ohmage.domain.stream.StreamData;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The meta-data associated with a {@link StreamData} point.
 * </p>
 *
 * @author John Jenkins
 */
@JsonFilter(MetaData.JACKSON_FILTER_GROUP_ID)
public class MetaData {
    /**
     * The group ID for the Jackson filter. This must be unique to our class,
     * whatever the value is.
     */
    protected static final String JACKSON_FILTER_GROUP_ID =
        "org.ohmage.domain.MetaData";
    // Register this class with the ohmage object mapper.
    static {
        OhmageObjectMapper.register(MetaData.class);
    }

    /**
     * The JSON key for the ID.
     */
    public static final String JSON_KEY_ID = "id";
    /**
     * The JSON key for the time-stamp.
     */
    public static final String JSON_KEY_TIMESTAMP = "timestamp";
    /**
     * The JSON key for the time-stamp as a millisecond value.
     */
    public static final String JSON_KEY_TIMESTAMP_MILLIS = "timestamp_millis";
    /**
     * The JSON key for the location.
     */
    public static final String JSON_KEY_LOCATION = "location";

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
    private final DateTime timestamp;
    /**
     * The number of milliseconds since the Unix epoch as a time-stamp for this
     * point.
     */
    @JsonProperty(JSON_KEY_TIMESTAMP_MILLIS)
    @JsonFilterField
    private final Long timestampMillis;
    /**
     * The location for this point.
     */
    @JsonProperty(JSON_KEY_LOCATION)
    @JsonInclude(Include.NON_NULL)
    private final Location location;

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
     * @param timestampMillis
     *        The time-stamp associated with the data point.
     *
     * @param location
     *        The location of the user or null if no location was able to be
     *        retrieved.
     *
     * @throws InvalidArgumentException
     *         The ID is null.
     */
    @JsonCreator
    public MetaData(
        @JsonProperty(JSON_KEY_ID) final String id,
        @JsonProperty(JSON_KEY_TIMESTAMP) final DateTime timestamp,
        @JsonProperty(JSON_KEY_TIMESTAMP_MILLIS) final Long timestampMillis,
        @JsonProperty(JSON_KEY_LOCATION) final Location location)
        throws InvalidArgumentException {

        if(id == null || "".equals(id.trim())) {
            throw new InvalidArgumentException("The ID is null or all whitespace.");
        }

        this.id = id;
        this.timestamp = timestamp;
        if(timestampMillis == null) {
            if(timestamp == null) {
                this.timestampMillis = null;
            }
            else {
                this.timestampMillis = timestamp.getMillis();
            }
        }
        else {
            this.timestampMillis = timestampMillis;
        }
        this.location = location;
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

    /**
     * Returns the {@link Location} of the user.
     *
     * @return The {@link Location} of the user.
     */
    public Location getLocation() {
        return location;
    }
}