package org.ohmage.domain;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.jackson.OhmageObjectMapper.JsonFilterField;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The parent class for all data points, e.g. stream data, data points.
 * </p>
 *
 * @author John Jenkins
 */
@JsonFilter(DataPoint.JACKSON_FILTER_GROUP_ID)
public abstract class DataPoint<T> extends OhmageDomainObject {
    /**
     * <p>
     * A super-class for inherited {@link DataPoint} Builder classes.
     * </p>
     *
     * @author John Jenkins
     */
    public static abstract class Builder<T>
        extends OhmageDomainObject.Builder<DataPoint<T>> {

        /**
         * The unique identifier of the user that owns this point.
         */
        private String ownerId;

        /**
         * The unique identifier for the schema to which this data conforms.
         */
        private String schemaId;
        /**
         * The version of the schema to which this data conforms.
         */
        private Long schemaVersion;

        /**
         * The meta-data about this data point.
         */
        private MetaData metaData;
        /**
         * The actual data that backs this data point.
         */
        private T data;

        /**
         * Creates a new Builder.
         *
         * @param metaData
         *        The data point's meta-data.
         *
         * @param data
         *        The data that backs this data point.
         */
        public Builder(final MetaData metaData, final T data) {
            super(null);

            this.metaData = metaData;
            this.data = data;
        }

        /**
         * Creates a new Builder based on the given data point.
         *
         * @param dataPoint
         *        The data point to base this Builder off of.
         */
        public Builder(final DataPoint<T> dataPoint) {
            super(dataPoint);

            this.ownerId = dataPoint.ownerId;
            this.schemaId = dataPoint.schemaId;
            this.schemaVersion = dataPoint.schemaVersion;
            this.metaData = dataPoint.metaData;
            this.data = dataPoint.data;
        }

        /**
         * Returns the data point's owner's unique identifier.
         *
         * @return The data point's owner's unique identifier.
         */
        public String getOwner() {
            return ownerId;
        }

        /**
         * Sets the owner of the data point.
         *
         * @param ownerId
         *        The unique identifier for the owner of the data point.
         *
         * @return This to facilitate chaining.
         */
        public Builder<T> setOwner(final String ownerId) {
            this.ownerId = ownerId;

            return this;
        }

        /**
         * Returns the unique identifier for the schema to which this data
         * point conforms.
         *
         * @return The unqiue identifier for the schema to which this data
         *         conforms.
         */
        public String getSchemaId() {
            return schemaId;
        }

        /**
         * Sets the identifier for the schema to which this data conforms.
         *
         * @param schemaId
         *        The unique identifier for the schema to which this schema
         *        response conforms.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder<T> setSchemaId(final String schemaId) {
            this.schemaId = schemaId;

            return this;
        }

        /**
         * Returns the version of the schema to which this data conforms.
         *
         * @return The version of the schema to which this data conforms.
         */
        public Long getSchemaVersion() {
            return schemaVersion;
        }

        /**
         * Sets the version of the schema to which this data point
         * conforms.
         *
         * @param version
         *        The version of the stream to which this data conforms.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder<T> setSchemaVersion(final Long version) {
            schemaVersion = version;

            return this;
        }

        /**
         * Returns the data point's meta-data.
         *
         * @return The data point's meta-data.
         */
        public MetaData getMetaData() {
            return metaData;
        }

        /**
         * Sets the meta-data associated with this data point.
         *
         * @param metaData
         *        The meta-data associated with this data point.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder<T> setMetaData(final MetaData metaData) {
            this.metaData = metaData;

            return this;
        }

        /**
         * Returns the data point's data.
         *
         * @return The data point's data.
         */
        public T getData() {
            return data;
        }

        /**
         * Sets the data associated with this data point.
         *
         * @param data
         *        The data associated with this data point.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder<T> setData(final T data) {
            this.data = data;

            return this;
        }

        /**
         * Builds a new concrete DataPoint object.
         *
         * @return An object of a concrete DataPoint subclass.
         *
         * @throws InvalidArgumentException
         *         The state of this builder is insufficient to build a new
         *         {@link DataPoint} object.
         */
        @Override
        public abstract DataPoint<T> build() throws InvalidArgumentException;
    }

    /**
     * The group ID for the Jackson filter. This must be unique to our class,
     * whatever the value is.
     */
    protected static final String JACKSON_FILTER_GROUP_ID =
        "org.ohmage.domain.DataPoint";
    // Register this class with the ohmage object mapper.
    static {
        OhmageObjectMapper.register(DataPoint.class);
    }

    /**
     * The JSON key for the owner.
     */
    public static final String JSON_KEY_OWNER = "owner";
    /**
     * The JSON key for the schema's unique identifier.
     */
    public static final String JSON_KEY_SCHEMA_ID = "schema_id";
    /**
     * The JSON key for the schema's version.
     */
    public static final String JSON_KEY_SCHEMA_VERSION = "schema_version";
    /**
     * The JSON key for which ohmlets this data point is shared with.
     */
    public static final String JSON_KEY_OHMLETS = "ohmlets";
    /**
     * The JSON key for the owner.
     */
    public static final String JSON_KEY_USER_ID = "user_id";
    /**
     * The JSON key for the meta-data.
     */
    public static final String JSON_KEY_META_DATA = "meta_data";
    /**
     * The JSON key for the data.
     */
    public static final String JSON_KEY_DATA = "data";

    /**
     * The unique identifier of the user that owns this point.
     */
    @JsonProperty(JSON_KEY_OWNER)
    private final String ownerId;

    /**
     * The unique identifier for the schema to which this data conforms.
     */
    @JsonProperty(JSON_KEY_SCHEMA_ID)
    @JsonFilterField
    private final String schemaId;
    /**
     * The version of the schema to which this data conforms.
     */
    @JsonProperty(JSON_KEY_SCHEMA_VERSION)
    @JsonFilterField
    private final long schemaVersion;

    /**
     * The meta-data about this data point.
     */
    @JsonProperty(JSON_KEY_META_DATA)
    @JsonInclude(Include.NON_NULL)
    private final MetaData metaData;
    /**
     * The actual data that backs this data point.
     */
    @JsonProperty(JSON_KEY_DATA)
    @JsonInclude(Include.NON_NULL)
    private final T data;

    /**
     * Builds a DataPoint object.
     *
     * @param ownerId
     *        The user that created and owns this data point.
     *
     * @param schemaId
     *        The unique identifier of the schema to which this data belongs.
     *
     * @param schemaVersion
     *        The version of the schema to which this data belongs.
     *
     * @param metaData
     *        The meta-data associated with the data.
     *
     * @param data
     *        The data that corresponds to this data point.
     *
     * @param internalReadVersion
     *        The internal version of this data point when it was read
     *        from the database.
     *
     * @param internalWriteVersion
     *        The new internal version of this data point when it will be
     *        written back to the database.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    protected DataPoint(
        final String ownerId,
        final String schemaId,
        final Long schemaVersion,
        final MetaData metaData,
        final T data,
        final Long internalReadVersion,
        final Long internalWriteVersion)
        throws InvalidArgumentException {

        super(internalReadVersion, internalWriteVersion);

        if(ownerId == null) {
            throw new InvalidArgumentException("The owner is null.");
        }
        if(schemaId == null) {
            throw new InvalidArgumentException("The schema ID is null.");
        }
        if(schemaVersion == null) {
            throw new InvalidArgumentException("The schema version is null.");
        }
        if(metaData == null) {
            throw new InvalidArgumentException("The meta-data is null.");
        }
        if(data == null) {
            throw new InvalidArgumentException("The data is null.");
        }

        this.ownerId = ownerId;
        this.schemaId = schemaId;
        this.schemaVersion = schemaVersion;
        this.metaData = metaData;
        this.data = data;
    }

    /**
     * Returns the unique identifier for this data point from its
     * meta-data.
     *
     * @return The unique identifier for this data point from its
     *         meta-data.
     */
    public String getId() {
        return metaData.getId();
    }

    /**
     * Returns the owner.
     *
     * @return The owner's unique identifier.
     */
    public String getOwner() {
        return ownerId;
    }

    /**
     * Returns the unique identifier of the schema to which this data point
     * belongs.
     *
     * @return The unique identifier of the schema to which this data point
     *         belongs.
     */
    public String getSchemaId() {
        return schemaId;
    }

    /**
     * Returns the version of the to which this data point belongs.
     *
     * @return The version of the to which this data point belongs.
     */
    public long getSchemaVersion() {
        return schemaVersion;
    }
}