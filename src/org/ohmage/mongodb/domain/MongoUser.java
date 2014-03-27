package org.ohmage.mongodb.domain;

import java.util.List;
import java.util.Set;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.ohmlet.Ohmlet.SchemaReference;
import org.ohmage.domain.ohmlet.OhmletInvitation;
import org.ohmage.domain.ohmlet.OhmletReference;
import org.ohmage.domain.user.ProviderUserInformation;
import org.ohmage.domain.user.Registration;
import org.ohmage.domain.user.User;
import org.ohmage.mongodb.bin.MongoUserBin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link User} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoUserBin.COLLECTION_NAME)
public class MongoUser extends User implements MongoDbObject {
	/**
	 * The database ID for this object.
	 */
	@ObjectId
	private final String dbId;

	/**
	 * Creates a {@link User} object via Jackson from the data layer.
	 *
	 * @param dbId
	 *        The database ID for this user.
	 *
	 * @param userId
	 *        The internal unique identifier for this user.
	 *
	 * @param password
	 *        The hashed password of the user.
	 *
	 * @param email
	 *        The email address of the user.
	 *
	 * @param fullName
	 *        The full name of the user, which may be null.
	 *
	 * @param providers
	 *        The collection of information about providers that have
	 *        authenticated this user.
	 *
	 * @param communities
	 *        The set of communities to which the user is associated and their
	 *        specific view of that ohmlet.
	 *
	 * @param streams
	 *        A set of stream identifiers and, optionally, a version that this
	 *        user is tracking.
	 *
	 * @param surveys
	 *        A set of survey identifiers and, optionally, a version that this
	 *        user is tracking.
     *
     * @param registration
     *        The user's self-registration information if the user was
     *        self-registered; if not, null.
     *
     * @param invitationId
     *        The unique identifier of the user invitation that was used to
     *        create this account.
     *
     * @param ohmletInvitations
     *        The invitations to ohmlets that have been given to this user.
     *        They may or may not have been used.
	 *
	 * @param internalVersion
	 *        The internal version of this entity used for checking for update
	 *        collisions.
	 *
	 * @throws IllegalArgumentException
	 *         A required parameter is null or invalid.
	 */
	@JsonCreator
	public MongoUser(
		@Id @ObjectId final String dbId,
		@JsonProperty(JSON_KEY_ID) final String userId,
		@JsonProperty(JSON_KEY_PASSWORD) final String password,
		@JsonProperty(JSON_KEY_EMAIL) final String email,
		@JsonProperty(JSON_KEY_FULL_NAME) final String fullName,
		@JsonProperty(JSON_KEY_PROVIDERS)
			final List<ProviderUserInformation> providers,
		@JsonProperty(JSON_KEY_OHMLETS)
		    final Set<OhmletReference> communities,
		@JsonProperty(JSON_KEY_STREAMS) final Set<SchemaReference> streams,
		@JsonProperty(JSON_KEY_SURVEYS) final Set<SchemaReference> surveys,
        @JsonProperty(JSON_KEY_REGISTRATION) final Registration registration,
        @JsonProperty(JSON_KEY_INVITATION_ID) final String invitationId,
        @JsonProperty(JSON_KEY_OHMLET_INVITATIONS)
            final Set<OhmletInvitation> ohmletInvitations,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException {

		super(
		    userId,
			password,
			email,
			fullName,
			providers,
			communities,
			streams,
			surveys,
			registration,
			invitationId,
			ohmletInvitations,
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