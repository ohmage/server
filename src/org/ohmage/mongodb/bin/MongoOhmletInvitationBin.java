package org.ohmage.mongodb.bin;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.ohmage.bin.OhmletInvitationBin;
import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InconsistentDatabaseException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.ohmlet.OhmletInvitation;
import org.ohmage.mongodb.domain.MongoOhmletInvitation;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

/**
 * <p>
 * The MongoDB implementation of the database-backed ohmlet invitation
 * repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoOhmletInvitationBin extends OhmletInvitationBin {
    /**
     * The name of the collection that contains all of the streams.
     */
    public static final String COLLECTION_NAME = "ohmlet_invitation_bin";

    /**
     * Get the connection to the ohlmet invitation bin with the Jackson
     * wrapper.
     */
    private static final JacksonDBCollection<OhmletInvitation, String> COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                OhmletInvitation.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Get the connection to the ohmlet invitation bin with the Jackson
     * wrapper, specifically for {@link MongoOhmletInvitation} objects.
     */
    private static final JacksonDBCollection<MongoOhmletInvitation, String> MONGO_COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                MongoOhmletInvitation.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Default constructor.
     */
    protected MongoOhmletInvitationBin() {
        // Ensure that there is an unique index on the ID.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(OhmletInvitation.JSON_KEY_INVITATION_ID, 1),
                COLLECTION_NAME + "_" +
                    OhmletInvitation.JSON_KEY_INVITATION_ID,
                true);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.OhmletInvitationBin#addInvitation(org.ohmage.domain.ohmlet.OhmletInvitation)
     */
    @Override
    public void addInvitation(final OhmletInvitation invitation)
        throws IllegalArgumentException, InvalidArgumentException {

        // Validate the input.
        if(invitation == null) {
            throw new IllegalArgumentException("The ohmlet invitation is null.");
        }

        // Attempt to add the user.
        try {
            COLLECTION.insert(invitation);
        }
        // If the user already exists, throw an exception.
        catch(MongoException.DuplicateKey e) {
            throw
                new InvalidArgumentException(
                    "Another ohmlet invitation has the same ID.",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.OhmletInvitationBin#getInvitation(java.lang.String)
     */
    @Override
    public OhmletInvitation getInvitation(final String invitationId)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(invitationId == null) {
            throw
                new IllegalArgumentException(
                    "The ohmlet invitation's ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the authentication token to the query
        queryBuilder
            .and(OhmletInvitation.JSON_KEY_INVITATION_ID)
            .is(invitationId);

        // Execute the query.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }

    @Override
    public void updateInvitation(final OhmletInvitation invitation)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(invitation == null) {
            throw new IllegalArgumentException("The user invitation is null.");
        }

        // Create the query.
        // Limit the query only to this user.
        Query query =
            DBQuery
                .is(OhmletInvitation.JSON_KEY_INVITATION_ID,
                    invitation.getId());

        // Ensure that the user has not been updated elsewhere.
        query =
            query
                .is(OhmageDomainObject.JSON_KEY_INTERNAL_VERSION,
                    invitation.getInternalReadVersion());

        // Commit the update and don't return until the collection has heard
        // the result.
        WriteResult<OhmletInvitation, String> result =
            COLLECTION
                .update(
                    query,
                    invitation,
                    false,
                    false,
                    WriteConcern.REPLICA_ACKNOWLEDGED);

        // Be sure that at least one document was updated.
        if(result.getN() == 0) {
            throw
                new InconsistentDatabaseException(
                    "A conflict occurred. Please, try again.");
        }
    }
}