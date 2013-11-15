package org.ohmage.domain.stream;

import name.jenkins.paul.john.concordia.Concordia;
import name.jenkins.paul.john.concordia.exception.ConcordiaException;

import org.ohmage.domain.MetaData;
import org.ohmage.domain.Schema;
import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * <p>
 * A stream of passively collected data.
 * </p>
 *
 * @author John Jenkins
 */
public class Stream extends Schema {
	/**
	 * <p>
	 * A builder for a {@link Stream}.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class Builder extends Schema.Builder {
        /**
         * The definition of this schema.
         */
        protected Concordia definition;

		/**
		 * Creates a new Stream builder object.
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

			super(version, name, description);

			this.definition = definition;
		}

		/**
		 * Creates a new builder based on an existing Stream object.
		 *
		 * @param stream
		 *        The existing Stream object on which this Builder should be
		 *        based.
		 */
		public Builder(final Stream stream) {
			super(stream);

			definition = stream.definition;
		}

		/**
		 * Creates a new Stream object from the state of this builder.
		 *
		 * @throws InvalidArgumentException
		 *         The state of this builder is insufficient to build a new
		 *         {@link Stream} object.
		 */
		@Override
		public Stream build() throws InvalidArgumentException {
			return
				new Stream(
				    (schemaId == null) ? getRandomId() : schemaId,
					version,
					name,
					description,
					owner,
					definition,
					internalReadVersion,
					internalWriteVersion);
		}
	}

    /**
     * The JSON key for the definition.
     */
    public static final String JSON_KEY_DEFINITION = "definition";

    /**
     * The definition of this schema.
     */
    @JsonProperty(JSON_KEY_DEFINITION)
    private final Concordia definition;

	/**
	 * Creates a new Stream object.
	 *
	 * @param version
	 *        The version of this stream.
	 *
	 * @param name
	 *        The name of this stream.
	 *
	 * @param description
	 *        The description of this stream.
	 *
	 * @param owner
	 *        The owner of this stream.
	 *
	 * @param definition
	 *        The definition of this stream.
	 *
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	public Stream(
		final long version,
		final String name,
		final String description,
		final String owner,
		final Concordia definition)
		throws InvalidArgumentException {

		this(null, version, name, description, owner, definition, null);
	}

	/**
	 * Rebuild an existing Schema object.
	 *
	 * @param id
	 *        The unique identifier for this object. If null, a default value
	 *        is given.
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
	protected Stream(
		@JsonProperty(JSON_KEY_ID) final String id,
		@JsonProperty(JSON_KEY_VERSION) final long version,
		@JsonProperty(JSON_KEY_NAME) final String name,
		@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
		@JsonProperty(JSON_KEY_OWNER) final String owner,
		@JsonProperty(JSON_KEY_DEFINITION) final Concordia definition,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException, InvalidArgumentException {

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
	private Stream(
		final String id,
		final long version,
		final String name,
		final String description,
		final String owner,
		final Concordia definition,
		final Long internalReadVersion,
		final Long internalWriteVersion)
		throws IllegalArgumentException, InvalidArgumentException {

		super(
			id,
			version,
			name,
			description,
			owner,
			internalReadVersion,
			internalWriteVersion);

        if(definition == null) {
            throw new InvalidArgumentException("The definition is null.");
        }

        this.definition = definition;
	}

	/**
	 * Validates that some meta-data and data conform to this stream's
	 * definition.
	 *
	 * @param metaData
	 *        The {@link MetaData} to validate.
	 *
	 * @param data
	 *        The data to validate.
	 *
	 * @throws InvalidArgumentException
	 *         The meta-data or data were invalid.
	 */
	public void validate(
		final MetaData metaData,
		final JsonNode data)
		throws InvalidArgumentException {

	    if(data == null) {
	        throw new InvalidArgumentException("The stream data is null.");
	    }

		try {
			getDefinition().validateData(data);
		}
		catch(ConcordiaException e) {
			throw
				new InvalidArgumentException(
					"The data does not conform to the schema.",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.Schema#getDefinition()
	 */
	@Override
    public Concordia getDefinition() {
	    return definition;
	}
}