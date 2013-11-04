package org.ohmage.mongodb.domain;

import java.util.List;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.Community;
import org.ohmage.mongodb.bin.MongoCommunityBin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link Community} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoCommunityBin.COLLECTION_NAME)
public class MongoCommunity extends Community implements MongoDbObject {
	/**
	 * The database ID for this object.
	 */
	@ObjectId
	private final String dbId;

	/**
	 * Creates a {@link Community} object via Jackson from the data layer.
	 * 
	 * @param dbId
	 *        The database ID for this authentication token.
	 * 
	 * @param communityId
	 *        The ID for the community or null for a randomly generated one.
	 * 
	 * @param name
	 *        The name of this community.
	 * 
	 * @param description
	 *        The description of this community.
	 * 
	 * @param owner
	 *        The owner of this community.
	 * 
	 * @param schemas
	 *        The schemas that define this community.
	 * 
	 * @param triggers
	 *        The triggers for this community.
	 * 
	 * @throws IllegalArgumentException
	 *         A required parameter was missing or any parameter was invalid.
	 */
	protected MongoCommunity(
		@Id @ObjectId final String dbId, 
		@JsonProperty(JSON_KEY_ID) final String communityId,
		@JsonProperty(JSON_KEY_NAME) final String name,
		@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
		@JsonProperty(JSON_KEY_OWNER) final String owner,
		@JsonProperty(JSON_KEY_STREAMS) final List<SchemaReference> streams,
		@JsonProperty(JSON_KEY_SURVEYS) final List<SchemaReference> surveys,
		@JsonProperty(JSON_KEY_TRIGGERS) final List<String> triggers,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final long internalVersion) {
		
		super(
			communityId, 
			name, 
			description, 
			owner, 
			streams, 
			surveys, 
			triggers, 
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