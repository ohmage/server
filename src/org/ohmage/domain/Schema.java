package org.ohmage.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import name.jenkins.paul.john.concordia.Concordia;
import name.jenkins.paul.john.concordia.exception.ConcordiaException;
import name.jenkins.paul.john.concordia.validator.ValidationController;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.OhmageException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;

/**
 * <p>
 * A generic schema in the system.
 * </p>
 * 
 * @author John Jenkins
 */
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
		 * The definition of this schema.
		 */
		protected Concordia definition;
		
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
		 */
		@JsonCreator
		public Builder(
			@JsonProperty(JSON_KEY_VERSION) final long version,
			@JsonProperty(JSON_KEY_NAME) final String name,
			@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
			@JsonProperty(JSON_KEY_DEFINITION) final Concordia definition) {
			
			super(null);
			
			this.version = version;
			this.name = name;
			this.description = description;
			this.definition = definition;
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
			
			this.version = schema.version;
			this.name = schema.name;
			this.description = schema.description;
			this.owner = schema.owner;
			this.definition = schema.definition;
		}
		
		/**
		 * Returns the currently set version.
		 * 
		 * @return The currently set version.
		 */
		public long getVersion() {
			return version;
		}
		
		/**
		 * Sets the owner of the schema.
		 * 
		 * @param owner The user-name of the ohmage user that owns this schema.
		 */
		public void setOwner(final String owner) {
			this.owner = owner;
		}
		
		/**
		 * Creates a new object from this builder.
		 * 
		 * @return A new object from this builder.
		 * 
		 * @throws OhmageException
		 *         The state of the builder contained invalid fields.
		 */
		public abstract Schema build();
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
	 * The JSON key for the definition.
	 */
	public static final String JSON_KEY_DEFINITION = "definition";
	
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(Schema.class.getName());

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
	 * The definition of this schema.
	 */
	@JsonProperty(JSON_KEY_DEFINITION)
	private final Concordia definition;
	
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
	 * @param definition
	 *        The definition of this schema.
	 * 
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	public Schema(
		final long version,
		final String name,
		final String description,
		final String owner,
		final Concordia definition)
		throws InvalidArgumentException {
		
		// Pass through to the builder constructor.
		this(
			getRandomId(),
			version,
			name,
			description,
			owner,
			definition,
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
	 * @param definition
	 *        The definition of this schema.
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
		@JsonProperty(JSON_KEY_DEFINITION) final Concordia definition,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException, InvalidArgumentException {
		
		// Pass through to the builder constructor.
		this(
			id, 
			version, 
			name, 
			description, 
			owner, 
			definition, 
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
	 * @param definition
	 *        The definition of this schema.
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
		final Concordia definition,
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
		if(definition == null) {
			throw new InvalidArgumentException("The definition is null.");
		}

		this.schemaId = id;
		this.version = version;
		this.name = name;
		this.description = description;
		this.owner = owner;
		this.definition = definition;
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
	 * Returns the user-name of the user that owns this schema.
	 * 
	 * @return The user-name of the user that owns this schema.
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * Returns the definition of this schema.
	 * 
	 * @return The definition of this schema.
	 */
	public Concordia getDefinition() {
		return definition;
	}
	
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