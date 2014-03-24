package org.ohmage.mongodb.bin;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.ohmage.bin.UserInvitationBin;
import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InconsistentDatabaseException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.user.UserInvitation;
import org.ohmage.mongodb.domain.MongoUserInvitation;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The MongoDB implementation of the interface to the database-backed
 * collection of user invitations.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoUserInvitationBin extends UserInvitationBin {
    /**
     * The name of the collection that contains all of the streams.
     */
    public static final String COLLECTION_NAME = "user_invitation_bin";

    /**
     * Get the connection to the user invitation bin with the Jackson wrapper.
     */
    private static final JacksonDBCollection<UserInvitation, String> COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                UserInvitation.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Get the connection to the user invitation bin with the Jackson wrapper,
     * specifically for {@link MongoUserInvitation} objects.
     */
    private static final JacksonDBCollection<MongoUserInvitation, String> MONGO_COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                MongoUserInvitation.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Default constructor.
     */
    protected MongoUserInvitationBin() {
        // Ensure that there is an unique index on the ID.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(UserInvitation.JSON_KEY_INVITATION_ID, 1),
                COLLECTION_NAME + "_" +
                    UserInvitation.JSON_KEY_INVITATION_ID,
                true);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.UserInvitationBin#addInvitation(org.ohmage.domain.user.UserInvitation)
     */
    @Override
    public void addInvitation(final UserInvitation userInvitation)
        throws IllegalArgumentException, InvalidArgumentException {

        // Validate the input.
        if(userInvitation == null) {
            throw new IllegalArgumentException("The user invitation is null.");
        }

        // Attempt to add the user.
        try {
            COLLECTION.insert(userInvitation);
        }
        // If the user already exists, throw an exception.
        catch(MongoException.DuplicateKey e) {
            throw
                new InvalidArgumentException(
                    "Another user invitation has the same ID.",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.UserInvitationBin#getInvitation(java.lang.String)
     */
    @Override
    public UserInvitation getInvitation(final String invitationId)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(invitationId == null) {
            throw
                new IllegalArgumentException(
                    "The user invitation ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the authentication token to the query
        queryBuilder
            .and(UserInvitation.JSON_KEY_INVITATION_ID)
            .is(invitationId);

        // Execute the query.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.UserInvitationBin#updateInvitation(org.ohmage.domain.user.UserInvitation)
     */
    @Override
    public void updateInvitation(final UserInvitation invitation)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(invitation == null) {
            throw new IllegalArgumentException("The user invitation is null.");
        }

        // Create the query.
        // Limit the query only to this user.
        Query query =
            DBQuery
                .is(UserInvitation.JSON_KEY_INVITATION_ID, invitation.getId());
        // Ensure that the user has not been updated elsewhere.
        query =
            query
                .is(OhmageDomainObject.JSON_KEY_INTERNAL_VERSION,
                    invitation.getInternalReadVersion());

        // Commit the update and don't return until the collection has heard
        // the result.
        WriteResult<UserInvitation, String> result =
            COLLECTION.update(query, invitation);

        // Be sure that at least one document was updated.
        if(result.getN() == 0) {
            throw
                new InconsistentDatabaseException(
                    "A conflict occurred. Please, try again.");
        }
    }
}