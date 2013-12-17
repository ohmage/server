package org.ohmage.mongodb.domain;

import java.util.List;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.Ohmlet;
import org.ohmage.mongodb.bin.MongoOhmletBin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link Ohmlet} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoOhmletBin.COLLECTION_NAME)
public class MongoOhmlet extends Ohmlet implements MongoDbObject {
	/**
	 * The database ID for this object.
	 */
	@ObjectId
	private final String dbId;

	/**
	 * Creates a {@link Ohmlet} object via Jackson from the data layer.
	 *
	 * @param dbId
	 *        The database ID for this authentication token.
	 *
	 * @param ohmletId
	 *        The ID for the ohmlet or null for a randomly generated one.
	 *
	 * @param name
	 *        The name of this ohmlet.
	 *
	 * @param description
	 *        The description of this ohmlet.
	 *
	 * @param owner
	 *        The owner of this ohmlet.
	 *
	 * @param schemas
	 *        The schemas that define this ohmlet.
	 *
	 * @param reminders
	 *        The list of default reminders for users that download this
	 *        ohmlet.
	 *
	 * @param members
	 *        The members of this ohmlet.
	 *
	 * @param privacyState
	 *        The {@link PrivacyState} of this ohmlet.
	 *
	 * @param inviteRole
	 *        The minimum required {@link Role} to invite other users to this
	 *        ohmlet.
	 *
	 * @param visibilityRole
	 *        The minimum required {@link Role} to view data supplied by the
	 *        members of this ohmlet.
     *
     * @param iconId
     *        The media ID for the icon image.
	 *
	 * @param internalVersion
	 *        The internal version of this entity used for checking for update
	 *        collisions.
	 *
	 * @throws IllegalArgumentException
	 *         A required parameter was missing or any parameter was invalid.
	 */
	protected MongoOhmlet(
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
        @JsonProperty(JSON_KEY_ICON_ID) final String iconId,
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
			iconId,
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