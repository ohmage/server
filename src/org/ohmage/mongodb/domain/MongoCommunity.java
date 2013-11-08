package org.ohmage.mongodb.domain;

import java.util.List;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.Community;
import org.ohmage.domain.Community.PrivacyState;
import org.ohmage.domain.Community.Role;
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
	 * @param reminders
	 *        The list of default reminders for users that download this
	 *        community.
	 * 
	 * @param members
	 *        The members of this community.
	 * 
	 * @param privacyState
	 *        The {@link PrivacyState} of this community.
	 * 
	 * @param inviteRole
	 *        The minimum required {@link Role} to invite other users to this
	 *        community.
	 * 
	 * @param visibilityRole
	 *        The minimum required {@link Role} to view data supplied by the
	 *        members of this community.
	 * 
	 * @param internalVersion
	 *        The internal version of this entity used for checking for update
	 *        collisions.
	 * 
	 * @throws IllegalArgumentException
	 *         A required parameter was missing or any parameter was invalid.
	 */
	protected MongoCommunity(
		@Id @ObjectId final String dbId, 
		@JsonProperty(JSON_KEY_ID) final String id,
		@JsonProperty(JSON_KEY_NAME) final String name,
		@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
		@JsonProperty(JSON_KEY_STREAMS) final List<SchemaReference> streams,
		@JsonProperty(JSON_KEY_SURVEYS) final List<SchemaReference> surveys,
		@JsonProperty(JSON_KEY_REMINDERS) final List<String> reminders,
		@JsonProperty(JSON_KEY_MEMBERS) final List<Member> members,
		@JsonProperty(JSON_KEY_PRIVACY_STATE) final PrivacyState privacyState,
		@JsonProperty(JSON_KEY_INVITE_ROLE) final Role inviteRole,
		@JsonProperty(JSON_KEY_VISIBILITY_ROLE) final Role visibilityRole,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion) {
		
		super(
			id, 
			name, 
			description,
			streams, 
			surveys, 
			reminders,
			members,
			privacyState,
			inviteRole,
			visibilityRole,
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