package org.ohmage.domain;

import java.util.Collections;
import java.util.List;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.OhmageException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A project is a group of schemas, which define the data being collected from
 * and/or about the user, and triggers, which define when a user should be
 * prompted to interface with the application.
 * </p>
 * 
 * @author John Jenkins
 */
public class Project extends OhmageDomainObject {
	/**
	 * <p>
	 * A builder for {@link Project}s.
	 * </p>
	 * 
	 * @author John Jenkins
	 */
	public static class Builder extends OhmageDomainObject.Builder<Project> {
		/**
		 * The version of this project.
		 */
		private long version;
		/**
		 * The name of this project.
		 */
		private String name;
		/**
		 * The description of this project.
		 */
		private String description;
		/**
		 * The owner of this project.
		 */
		private String owner;
		/**
		 * The streams for this project.
		 */
		private List<SchemaReference> streams;
		/**
		 * The schemas for this project.
		 */
		private List<SchemaReference> surveys;
		/**
		 * The triggers for this project that define when certain surveys should be
		 * prompted for the user.
		 */
		private List<String> triggers;
		
		/**
		 * Creates a new Project builder object.
		 * 
		 * @param version
		 *        The version of this project.
		 * 
		 * @param name
		 *        The name of this project.
		 * 
		 * @param description
		 *        The description of this project.
		 * 
		 * @param owner
		 *        The creator and subsequent owner of this project.
		 * 
		 * @param streams
		 *        The list of streams that compose this project.
		 * 
		 * @param surveys
		 *        The list of surveys that compose this project.
		 * 
		 * @param triggers
		 *        The list of default triggers for users that download this
		 *        project.
		 */
		public Builder(
			@JsonProperty(JSON_KEY_VERSION) final long version,
			@JsonProperty(JSON_KEY_NAME) final String name,
			@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
			@JsonProperty(JSON_KEY_OWNER) final String owner,
			@JsonProperty(JSON_KEY_STREAMS)
				final List<SchemaReference> streams,
			@JsonProperty(JSON_KEY_SURVEYS)
				final List<SchemaReference> surveys,
			@JsonProperty(JSON_KEY_TRIGGERS) final List<String> triggers) {
			
			super(null);
			
			this.version = version;
			this.name = name;
			this.description = description;
			this.owner = owner;
			this.streams = streams;
			this.surveys = surveys;
			this.triggers = triggers;
		}

		/**
		 * Creates a new builder based on an existing Project object.
		 * 
		 * @param project
		 *        The existing Project object on which this Builder should be
		 *        based.
		 */
		public Builder(final Project project) {
			super(project);
			
			this.version = project.version;
			this.name = project.name;
			this.description = project.description;
			this.owner = project.owner;
			this.streams = project.streams;
			this.surveys = project.surveys;
			this.triggers = project.triggers;
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
		 * Returns the currently set owner.
		 * 
		 * @return The currently set owner.
		 */
		public String getOwner() {
			return owner;
		}
		
		/**
		 * Sets the owner. Use null to remove the currently set owner.
		 * 
		 * @param owner
		 *        The new owner or null to remove the currently set owner.
		 */
		public void setOwner(final String owner) {
			this.owner = owner;
		}
		
		/**
		 * Creates a new Project object from the state of this builder.
		 * 
		 * @return A new Project object from the state of this builder.
		 * 
		 * @throws OhmageException
		 *         The state of the builder contained invalid fields.
		 */
		public Project build() {
			return
				new Project(
					getRandomId(),
					version, 
					name, 
					description, 
					owner, 
					streams,
					surveys, 
					triggers,
					internalReadVersion,
					internalWriteVersion);
		}
	}
	
	/**
	 * <p>
	 * A reference to a specific schema. Optionally, it may designate a
	 * specific version of that schema. If not, the latest version will always
	 * be used.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class SchemaReference {
		/**
		 * The JSON key for the schema ID.
		 */
		public static final String JSON_KEY_SCHEMA_ID = "schema_id";
		/**
		 * The JSON key for the schema version.
		 */
		public static final String JSON_KEY_VERSION = "version";
		
		/**
		 * The schema ID.
		 */
		@JsonProperty(JSON_KEY_SCHEMA_ID)
		private final String schemaId;
		/**
		 * The schema version.
		 */
		@JsonProperty(JSON_KEY_VERSION)
		private final Long version;
		
		/**
		 * Creates or recreates a reference to a schema. The version may be
		 * null, indicating that the latest version should always be used.
		 * 
		 * @param schemaId
		 *        The schema's unique identifier.
		 * 
		 * @param version
		 *        The specific version that is being referenced or null if no
		 *        version is being referenced.
		 * 
		 * @throws IllegalArgumentException
		 *         The schema ID is null.
		 */
		@JsonCreator
		public SchemaReference(
			@JsonProperty(JSON_KEY_SCHEMA_ID) final String schemaId,
			@JsonProperty(JSON_KEY_VERSION) final Long version)
			throws IllegalArgumentException {
			
			if(schemaId == null) {
				throw new IllegalArgumentException("The schema ID is null.");
			}
			
			this.schemaId = schemaId;
			this.version = version;
		}
		
		/**
		 * Returns the unique identifier of the schema.
		 * 
		 * @return The unique identifier of the schema.
		 */
		public String getSchemaId() {
			return schemaId;
		}
		
		/**
		 * Returns the specific version of the schema or null if no version was
		 * specified.
		 * 
		 * @return The specific version of the schema or null if no version was
		 *         specified.
		 */
		public Long getVersion() {
			return version;
		}
	}
	
	/**
	 * The JSON key for the ID.
	 */
	public static final String JSON_KEY_ID = "project_id";
	/**
	 * The JSON key for the version.
	 */
	public static final String JSON_KEY_VERSION = "project_version";
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
	 * The JSON key for the streams.
	 */
	public static final String JSON_KEY_STREAMS = "streams";
	/**
	 * The JSON key for the surveys.
	 */
	public static final String JSON_KEY_SURVEYS = "surveys";
	/**
	 * The JSON key for the triggers.
	 */
	public static final String JSON_KEY_TRIGGERS = "triggers";

	/**
	 * The unique identifier for this project.
	 */
	@JsonProperty(JSON_KEY_ID)
	private final String projectId;
	/**
	 * The version of this project.
	 */
	@JsonProperty(JSON_KEY_VERSION)
	private final long version;
	/**
	 * The name of this project.
	 */
	@JsonProperty(JSON_KEY_NAME)
	private final String name;
	/**
	 * The description of this project.
	 */
	@JsonProperty(JSON_KEY_DESCRIPTION)
	private final String description;
	/**
	 * The owner of this project.
	 */
	@JsonProperty(JSON_KEY_OWNER)
	private final String owner;
	/**
	 * The streams for this project.
	 */
	@JsonProperty(JSON_KEY_STREAMS)
	private final List<SchemaReference> streams;
	/**
	 * The surveys for this project.
	 */
	@JsonProperty(JSON_KEY_SURVEYS)
	private final List<SchemaReference> surveys;
	/**
	 * The triggers for this project that define when certain surveys should be
	 * prompted for the user.
	 */
	@JsonProperty(JSON_KEY_TRIGGERS)
	private final List<String> triggers;
	
	/**
	 * Creates a new project.
	 * 
	 * @param version
	 *        The version of this project.
	 * 
	 * @param name
	 *        The name of this project.
	 * 
	 * @param description
	 *        The description of this project.
	 * 
	 * @param owner
	 *        The creator and subsequent owner of this project.
	 * 
	 * @param streams
	 *        The list of streams that compose this project.
	 * 
	 * @param surveys
	 *        The list of surveys that compose this project.
	 * 
	 * @param triggers
	 *        The list of default triggers for users that download this
	 *        project.
	 * 
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	public Project(
		final long version,
		final String name,
		final String description,
		final String owner,
		final List<SchemaReference> streams,
		final List<SchemaReference> surveys,
		final List<String> triggers)
		throws IllegalArgumentException {
		
		// Pass through to the builder constructor.
		this(
			getRandomId(),
			version,
			name,
			description,
			owner,
			streams,
			surveys,
			triggers,
			null);
	}

	/**
	 * Recreates an existing project.
	 * 
	 * @param id
	 *        The ID for the project or null for a randomly generated one.
	 * 
	 * @param version
	 *        The version of this project.
	 * 
	 * @param name
	 *        The name of this project.
	 * 
	 * @param description
	 *        The description of this project.
	 * 
	 * @param owner
	 *        The owner of this project.
	 * 
	 * @param streams
	 *        The list of streams that compose this project.
	 * 
	 * @param surveys
	 *        The list of surveys that compose this project.
	 * 
	 * @param triggers
	 *        The triggers for this project.
	 * 
	 * @param internalVersion
	 *        The internal version of this project.
	 * 
	 * @throws IllegalArgumentException
	 *         The ID is invalid.
	 * 
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	@JsonCreator
	protected Project(
		@JsonProperty(JSON_KEY_ID) final String id,
		@JsonProperty(JSON_KEY_VERSION) final long version,
		@JsonProperty(JSON_KEY_NAME) final String name,
		@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
		@JsonProperty(JSON_KEY_OWNER) final String owner,
		@JsonProperty(JSON_KEY_STREAMS) final List<SchemaReference> streams,
		@JsonProperty(JSON_KEY_SURVEYS) final List<SchemaReference> surveys,
		@JsonProperty(JSON_KEY_TRIGGERS) final List<String> triggers,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException, InvalidArgumentException {
		
		// Pass through to the builder constructor.
		this(
			id, 
			version, 
			name, 
			description, 
			owner, 
			streams, 
			surveys, 
			triggers, 
			internalVersion, 
			null);
	}
	
	/**
	 * Builds the Project object.
	 * 
	 * @param id
	 *        The ID for the project or null for a randomly generated one.
	 * 
	 * @param version
	 *        The version of this project.
	 * 
	 * @param name
	 *        The name of this project.
	 * 
	 * @param description
	 *        The description of this project.
	 * 
	 * @param owner
	 *        The creator and subsequent owner of this project.
	 * 
	 * @param streams
	 *        The list of streams that compose this project.
	 * 
	 * @param surveys
	 *        The list of surveys that compose this project.
	 * 
	 * @param triggers
	 *        The list of default triggers for users that download this
	 *        project.
	 * 
	 * @param internalReadVersion
	 *        The internal version of this project when it was read from the
	 *        database.
	 * 
	 * @param internalWriteVersion
	 *        The new internal version of this project when it will be written
	 *        back to the database.
	 * 
	 * @throws IllegalArgumentException
	 *         The ID is invalid.
	 * 
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	private Project(
		final String id,
		final long version,
		final String name,
		final String description,
		final String owner,
		final List<SchemaReference> streams,
		final List<SchemaReference> surveys,
		final List<String> triggers,
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
		if(
			((streams == null) || (streams.size() == 0)) &&
			((surveys == null) || (surveys.size() == 0))) {
			
			throw
				new InvalidArgumentException(
					"At least one stream or survey must be declared.");
		}

		// Save the state.
		this.projectId = id;
		this.version = version;
		this.name = name;
		this.description = description;
		this.owner = owner;
		this.streams =
			((streams == null) ?
				Collections.<SchemaReference>emptyList() :
				Collections.unmodifiableList(streams));
		this.surveys =
			((surveys == null) ?
				Collections.<SchemaReference>emptyList() :
				Collections.unmodifiableList(surveys));
		this.triggers =
			((triggers == null) ?
				Collections.<String>emptyList() :
				Collections.unmodifiableList(triggers));
	}
	
	/**
	 * Returns the version of this project.
	 * 
	 * @return The version of this project.
	 */
	public long getVersion() {
		return version;
	}
	
	/**
	 * Returns the user-name of the user that owns this project.
	 * 
	 * @return The user-name of the user that owns this project.
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * Returns an unmodifiable list of the streams.
	 * 
	 * @return An unmodifiable list of the streams.
	 */
	public List<SchemaReference> getStreams() {
		return streams;
	}
	
	/**
	 * Returns an unmodifiable list of the surveys.
	 * 
	 * @return An unmodifiable list of the surveys.
	 */
	public List<SchemaReference> getSurveys() {
		return surveys;
	}
}