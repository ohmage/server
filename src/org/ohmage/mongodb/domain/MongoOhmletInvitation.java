package org.ohmage.mongodb.domain;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.ohmlet.OhmletInvitation;
import org.ohmage.mongodb.bin.MongoOhmletInvitationBin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link OhmletInvitation} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoOhmletInvitationBin.COLLECTION_NAME)
public class MongoOhmletInvitation
    extends OhmletInvitation
    implements MongoDbObject {

    /**
     * The database ID for this object.
     */
    @ObjectId
    private final String dbId;

    /**
     * Creates an {@link OhmletInvitation} object via Jackson from the data
     * layer.
     *
     * @param dbId
     *        The database ID for this user.
     *
     * @param invitationId
     *        The invitation's unique identifier.
     *
     * @param ohmletId
     *        The ohmlet's unique identifier.
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
     * @throws IllegalArgumentException
     *         A required parameter is null or invalid.
     */
    public MongoOhmletInvitation(
        @Id @ObjectId final String dbId,
        @JsonProperty(JSON_KEY_INVITATION_ID) final String invitationId,
        @JsonProperty(JSON_KEY_OHMLET_ID) final String ohmletId,
        @JsonProperty(JSON_KEY_CREATION_TIMESTAMP)
            final Long creationTimestamp,
        @JsonProperty(JSON_KEY_USED_TIMESTAMP) final Long usedTimestamp,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
        throws IllegalArgumentException {

        super(
            invitationId,
            ohmletId,
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