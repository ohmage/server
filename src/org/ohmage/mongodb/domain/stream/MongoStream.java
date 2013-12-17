package org.ohmage.mongodb.domain.stream;

import name.jenkins.paul.john.concordia.Concordia;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.mongodb.bin.MongoStreamBin;
import org.ohmage.mongodb.domain.MongoDbObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link Stream} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoStreamBin.COLLECTION_NAME)
public class MongoStream extends Stream implements MongoDbObject {
	/**
	 * The database ID for this object.
	 */
	@ObjectId
	private final String dbId;

	/**
	 * Creates a {@link Stream} object via Jackson from the data layer.
	 *
	 * @param dbId
	 *        The database ID for this stream.
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
	protected MongoStream(
		@Id @ObjectId final String dbId,
		@JsonProperty(JSON_KEY_ID) final String id,
		@JsonProperty(JSON_KEY_VERSION) final long version,
		@JsonProperty(JSON_KEY_NAME) final String name,
		@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
		@JsonProperty(JSON_KEY_OWNER) final String owner,
        @JsonProperty(JSON_KEY_ICON_ID) final String iconId,
		@JsonProperty(JSON_KEY_DEFINITION) final Concordia definition,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException {

		super(
			id,
			version,
			name,
			description,
			owner,
			iconId,
			definition,
			internalVersion);

		// Store the MongoDB ID.
		if(dbId == null) {
			throw new IllegalArgumentException("The MongoDB ID is missing.");
		}
		else {
			this.dbId = dbId;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.mongodb.domain.MongoDbObject#getDatabaseId()
	 */
	@Override
	@ObjectId
	public String getDbId() {
		return dbId;
	}
}