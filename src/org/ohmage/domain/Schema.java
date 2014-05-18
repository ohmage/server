package org.ohmage.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import name.jenkins.paul.john.concordia.Concordia;
import name.jenkins.paul.john.concordia.exception.ConcordiaException;
import name.jenkins.paul.john.concordia.validator.ValidationController;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.jackson.OhmageObjectMapper.JsonFilterField;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;

/**
 * <p>
 * A generic schema in the system.
 * </p>
 *
 * @author John Jenkins
 */
@JsonFilter(Schema.JACKSON_FILTER_GROUP_ID)
public abstract class Schema extends OhmageDomainObject {
	/**
	 * <p>
	 * A builder for {@link Schema}s.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static abstract class Builder
		extends OhmageDomainObject.Builder<Schema> {

	    /**
	     * The unique identifier for this schema. This may be set if we are
	     * creating a new schema from an existing one.
	     */
	    protected String schemaId;
		/**
		 * The version of this schema.
		 */
		protected long version;
		/**
		 * The name of this schema.
		 */
		protected String name;
		/**
		 * The description of this schema.
		 */
		protected String description;
		/**
		 * The owner of this schema.
		 */
		protected String owner;
		/**
		 * The media ID for the icon image.
		 */
		protected String iconId;
	    /**
	     * Whether or not a schema is visible to the Open mHealth APIs.
	     */
	    protected Boolean omhVisible;

        /**
         * Creates a new Schema builder object.
         *
         * @param version
         *        The version of this schema.
         *
         * @param name
         *        The name of this schema.
         *
         * @param description
         *        The description of this schema.
         *
         * @param definition
         *        The definition of this schema.
         *
         * @param iconId
         *        The media ID for the icon image.
         *
         * @param omhVisible
         *        Whether or not this schema is visible to the Open mHealth
         *        APIs.
         */
		@JsonCreator
		public Builder(
			@JsonProperty(JSON_KEY_VERSION) final long version,
			@JsonProperty(JSON_KEY_NAME) final String name,
            @JsonProperty(JSON_KEY_DESCRIPTION) final String description,
            @JsonProperty(JSON_KEY_ICON_ID) final String iconId,
            @JsonProperty(JSON_KEY_OMH_VISIBLE) final Boolean omhVisible) {

			super(null);

			this.version = version;
			this.name = name;
			this.description = description;
			this.iconId = iconId;
			this.omhVisible = omhVisible;
		}

		/**
		 * Creates a new builder based on an existing Schema object.
		 *
		 * @param schema
		 *        The existing Schema object on which this Builder should be
		 *        based.
		 */
		public Builder(final Schema schema) {
			super(schema);

			schemaId = schema.schemaId;
			version = schema.version;
			name = schema.name;
			description = schema.description;
			owner = schema.owner;
			omhVisible = schema.omhVisible;
		}

        /**
         * Sets the ID for this schema.
         *
         * @param schemaId
         *        The new, unique identifier for this schema.
         *
         * @return This builder to facilitate chaining.
         */
		public Builder setSchemaId(final String schemaId) {
		    this.schemaId = schemaId;

		    return this;
		}

		/**
		 * Returns the currently set version.
		 *
		 * @return The currently set version.
		 */
		public long getVersion() {
			return version;
		}

    public void setVersion(long version) {
      this.version = version;
    }

        /**
         * Sets the owner of the schema.
         *
         * @param owner
         *        The unique identifier for the ohmage user that owns this
         *        schema.
         *
         * @return This builder to facilitate chaining.
         */
		public Builder setOwner(final String owner) {
			this.owner = owner;

			return this;
		}

        /**
         * Returns the identifier of the icon.
         *
         * @return The identifier of the icon.
         */
        public String getIconId() {
            return iconId;
        }

        /**
         * Sets the identifier of the icon.
         *
         * @param iconId
         *        The icon's identifier.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setIconId(final String iconId) {
            this.iconId = iconId;

            return this;
        }

        /**
         * Returns whether or not this schema is visible to the Open mHealth
         * APIs.
         *
         * @return Whether or not this schema is visible to the Open mHealth
         *         APIs.
         */
        public Boolean getOmhVisible() {
            return omhVisible;
        }

        /**
         * Sets whether or not this schema is visible to the Open mHealth APIs.
         *
         * @param iconId
         *        Whether or not this schema is visible to the Open mHealth
         *        APIs.
         *
         * @return This builder to facilitate chaining.
         */
        public Builder setOmhVisible(final Boolean omhVisible) {
            this.omhVisible = omhVisible;

            return this;
        }
	}

	/**
	 * The JSON key for the ID.
	 */
	public static final String JSON_KEY_ID = "schema_id";
	/**
	 * The JSON key for the version.
	 */
	public static final String JSON_KEY_VERSION = "schema_version";
	/**
	 * The JSON key for the name.
	 */
	public static final String JSON_KEY_NAME = "name";
	/**
	 * The JSON key for the description.
	 */
	public static final String JSON_KEY_DESCRIPTION = "description";
	/**
	 * The JSON key for the owner.
	 */
	public static final String JSON_KEY_OWNER = "owner";
	/**
	 * The JSON key for the icon ID.
	 */
	public static final String JSON_KEY_ICON_ID = "icon_id";
	/**
	 * The JSON key for whether or not a schema is visible to the Open mHealth
	 * APIs.
	 */
	public static final String JSON_KEY_OMH_VISIBLE = "omh_visible";

    /**
     * The JSON key for the definition.
     */
    public static final String JSON_KEY_DEFINITION = "definition";

    /**
     * The group ID for the Jackson filter. This must be unique to our class,
     * whatever the value is.
     */
    protected static final String JACKSON_FILTER_GROUP_ID =
        "org.ohmage.domain.Schema";
    // Register this class with the ohmage object mapper.
    static {
        OhmageObjectMapper.register(Schema.class);
    }

	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(Schema.class.getName());

	/**
	 * The default value of whether or not a schema should be visible to Open
	 * mHealth APIs.
	 */
	private static final boolean DEFAULT_OMH_VISIBLE = true;

	/**
	 * The unique identifier for this schema.
	 */
	@JsonProperty(JSON_KEY_ID)
	private final String schemaId;
	/**
	 * The version of this schema.
	 */
	@JsonProperty(JSON_KEY_VERSION)
	private final long version;
	/**
	 * The name of this schema.
	 */
	@JsonProperty(JSON_KEY_NAME)
	private final String name;
	/**
	 * The description of this schema.
	 */
	@JsonProperty(JSON_KEY_DESCRIPTION)
	private final String description;
	/**
	 * The owner of this schema.
	 */
	@JsonProperty(JSON_KEY_OWNER)
	private final String owner;
    /**
     * The media ID for the icon image.
     */
    @JsonProperty(JSON_KEY_ICON_ID)
    private final String iconId;
    /**
     * Whether or not a schema is visible to the Open mHealth APIs.
     */
    @JsonProperty(JSON_KEY_OMH_VISIBLE)
    @JsonFilterField
    private final boolean omhVisible;

    /**
     * Creates a new Schema object.
     *
     * @param version
     *        The version of this schema.
     *
     * @param name
     *        The name of this schema.
     *
     * @param description
     *        The description of this schema.
     *
     * @param owner
     *        The owner of this schema.
     *
     * @param iconId
     *        The media ID for the icon image.
     *
     * @param omhVisible
     *        Whether or not this schema is visible to the Open mHealth APIs.
     *
     * @throws InvalidArgumentException
     *         A parameter is invalid.
     */
	public Schema(
		final long version,
		final String name,
		final String description,
		final String owner,
		final String iconId,
		final boolean omhVisible)
		throws InvalidArgumentException {

		// Pass through to the builder constructor.
		this(
			getRandomId(),
			version,
			name,
			description,
			owner,
			iconId,
			omhVisible,
			null);
	}

	/**
	 * Rebuilds an existing Schema object.
	 *
	 * @param id
	 *        The unique identifier for this object.
	 *
	 * @param version
	 *        The version of this schema.
	 *
	 * @param name
	 *        The name of this schema.
	 *
	 * @param description
	 *        The description of this schema.
	 *
	 * @param owner
	 *        The owner of this schema.
     *
     * @param iconId
     *        The media ID for the icon image.
     *
     * @param omhVisible
     *        Whether or not this schema is visible to the Open mHealth APIs.
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
	protected Schema(
		@JsonProperty(JSON_KEY_ID) final String id,
		@JsonProperty(JSON_KEY_VERSION) final long version,
		@JsonProperty(JSON_KEY_NAME) final String name,
		@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
		@JsonProperty(JSON_KEY_OWNER) final String owner,
		@JsonProperty(JSON_KEY_ICON_ID) final String iconId,
		@JsonProperty(JSON_KEY_OMH_VISIBLE) final Boolean omhVisible,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException, InvalidArgumentException {

		// Pass through to the builder constructor.
		this(
			id,
			version,
			name,
			description,
			owner,
			iconId,
			omhVisible,
			internalVersion,
			internalVersion);
	}

	/**
	 * Builds the Schema object.
	 *
	 * @param id
	 *        The unique identifier for this object.
	 *
	 * @param version
	 *        The version of this schema.
	 *
	 * @param name
	 *        The name of this schema.
	 *
	 * @param description
	 *        The description of this schema.
	 *
	 * @param owner
	 *        The owner of this schema.
     *
     * @param iconId
     *        The media ID for the icon image.
     *
     * @param omhVisible
     *        Whether or not this schema is visible to the Open mHealth APIs.
	 *
	 * @param internalReadVersion
	 *        The internal version of this schema when it was read from the
	 *        database.
	 *
	 * @param internalWriteVersion
	 *        The new internal version of this schema when it will be written
	 *        back to the database.
	 *
	 * @throws IllegalArgumentException
	 *         The ID is invalid.
	 *
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	protected Schema(
		final String id,
		final long version,
		final String name,
		final String description,
		final String owner,
		final String iconId,
		final Boolean omhVisible,
		final Long internalReadVersion,
		final Long internalWriteVersion)
		throws IllegalArgumentException, InvalidArgumentException {

		// Initialize the parent.
		super(internalReadVersion, internalWriteVersion);

		// Validate the parameters.
		if(id == null) {
			throw new IllegalArgumentException("The ID is null.");
		}
		if(name == null) {
			throw new InvalidArgumentException("The name is null.");
		}
		if(description == null) {
			throw new InvalidArgumentException("The description is null.");
		}
		if(owner == null) {
			throw new InvalidArgumentException("The owner is null.");
		}

		schemaId = id;
		this.version = version;
		this.name = name;
		this.description = description;
		this.owner = owner;
		this.iconId = iconId;
		this.omhVisible =
		    (omhVisible == null) ? DEFAULT_OMH_VISIBLE : omhVisible;
	}

	/**
	 * Returns the unique ID for this schema.
	 *
	 * @return The unique ID for this schema.
	 */
	public String getId() {
		return schemaId;
	}

	/**
	 * Returns the version of this schema.
	 *
	 * @return The version of this schema.
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * Returns the name of this schema.
	 *
	 * @return The name of this schema.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description of this schema.
	 *
	 * @return The description of this schema.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the unique identifier for the user that owns this schema.
	 *
	 * @return The unique identifier for the user that owns this schema.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Returns the given identifier for the icon.
	 *
	 * @return The given identifier for the icon.
	 */
	public String getIconId() {
	    return iconId;
	}

	/**
	 * Returns the definition of this schema.
	 *
	 * @return The definition of this schema.
	 */
    @JsonProperty(JSON_KEY_DEFINITION)
	public abstract Concordia getDefinition();

	/**
	 * Parses a string into a Concordia object.
	 *
	 * @param definition
	 *        The definition to parse.
	 *
	 * @return The validated Concordia object.
	 *
	 * @throws InvalidArgumentException
	 *         The definition was not valid JSON or not a valid Concordia
	 *         definition.
	 *
	 * @throws IllegalStateException
	 *         There was a problem handling our own streams.
	 */
	protected static Concordia parseDefinition(
		final String definition)
		throws InvalidArgumentException, IllegalStateException {

		// Validate the input.
		if(definition == null) {
			throw new InvalidArgumentException("The definition is null.");
		}

		// Create an input stream for the input.
		ByteArrayInputStream definitionInput =
			new ByteArrayInputStream(definition.getBytes());
		try {
			// Build and return the Concordia object.
			return
				new Concordia(
					definitionInput,
					ValidationController.BASIC_CONTROLLER);
		}
		// If it was invalid JSON.
		catch(JsonParseException e) {
			throw
				new InvalidArgumentException(
					"The definition is not valid JSON.",
					e);
		}
		// If it was invalid Concordia.
		catch(ConcordiaException e) {
			throw
				new InvalidArgumentException(
					"The definition is invalid: " + e.getMessage(),
					e);
		}
		// If we couldn't read from our own input stream, which should never
		// happen.
		catch(IOException e) {
			throw
				new IllegalStateException(
					"Could not read from our own input stream.",
					e);
		}
		// Always be sure to close our own stream.
		finally {
			try {
				definitionInput.close();
			}
			catch(IOException e) {
				LOGGER
					.log(
						Level.WARNING,
						"Could not close our own input stream.",
						e);
			}
		}
	}
}