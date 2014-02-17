package org.ohmage.mongodb.domain;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.user.UserInvitation;
import org.ohmage.mongodb.bin.MongoUserInvitationBin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link UserInvitation} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoUserInvitationBin.COLLECTION_NAME)
public class MongoUserInvitation
    extends UserInvitation
    implements MongoDbObject {

    /**
     * The database ID for this object.
     */
    @ObjectId
    private final String dbId;

    /**
     * Creates an {@link UserInvitation} object via Jackson from the data
     * layer.
     *
     * @param dbId
     *        The database ID for this user.
     *
     * @param invitationId
     *        The invitation's unique identifier.
     *
     * @param email
     *        The user's email address.
     *
     * @param ohmletInvitationId
     *        A verification ID to use to automatically verify an email
     *        address.
     *
     * @param creationTimestamp
     *        The number of milliseconds since the Unix epoch when this
     *        invitation was created.
     *
     * @param usedTimestamp
     *        The number of milliseconds since the Unix epoch when this
     *        invitation was used.
     *
     * @param internalVersion
     *        The internal version of this object when it was read from the
     *        database to facilitate updating it.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    public MongoUserInvitation(
        @Id @ObjectId final String dbId,
        @JsonProperty(JSON_KEY_INVITATION_ID) final String invitationId,
        @JsonProperty(JSON_KEY_EMAIL) final String email,
        @JsonProperty(JSON_KEY_OHMLET_INVITATION_ID)
            final String ohmletInvitationId,
        @JsonProperty(JSON_KEY_CREATION_TIMESTAMP)
            final long creationTimestamp,
        @JsonProperty(JSON_KEY_USED_TIMESTAMP) final Long usedTimestamp,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws InvalidArgumentException, IllegalArgumentException {

        super(
            invitationId,
            email,
            ohmletInvitationId,
            creationTimestamp,
            usedTimestamp,
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