package org.ohmage.mongodb.bin;

import java.util.List;

import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.OauthClientBin;
import org.ohmage.domain.auth.OauthClient;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.mongodb.domain.MongoOauthClient;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The MongoDB implementation of the database-backed OAuth client repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoOauthClientBin extends OauthClientBin {
    /**
     * The name of the collection that contains all of the third-parties.
     */
    public static final String COLLECTION_NAME = "oauth_client_bin";

    /**
     * Get the connection to the OAuth client bin with the Jackson wrapper.
     */
    private static final JacksonDBCollection<OauthClient, String> COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                OauthClient.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Get the connection to the OAuth client bin with the Jackson wrapper,
     * specifically for {@link MongoOauthClient} objects.
     */
    private static final JacksonDBCollection<MongoOauthClient, Object> MONGO_COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                MongoOauthClient.class,
                Object.class,
                MongoBinController.getObjectMapper());

    /**
     * Default constructor.
     */
    protected MongoOauthClientBin() {
        // Ensure that there is an index on the OAuth client's ID.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(OauthClient.JSON_KEY_ID, 1),
                COLLECTION_NAME + "_" + OauthClient.JSON_KEY_ID + "_unique",
                true);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.OauthClientBin#addOauthClient(org.ohmage.domain.OauthClient)
     */
    @Override
    public void addOauthClient(final OauthClient oauthClient)
        throws IllegalArgumentException, InvalidArgumentException {

        // Validate the parameter.
        if(oauthClient == null) {
            throw new IllegalArgumentException("The OAuth client is null.");
        }

        // Save it.
        try {
            COLLECTION.insert(oauthClient);
        }
        catch(MongoException.DuplicateKey e) {
            throw
                new InvalidArgumentException(
                    "A OAuth client with the same ID already exists.");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.OauthClientBin#getClientIds(java.lang.String)
     */
    @Override
    public MultiValueResult<String> getClientIds(final String userId)
        throws IllegalArgumentException {

        // Build the query
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the user's ID.
        queryBuilder.and(OauthClient.JSON_KEY_OWNER).is(userId);

        // Get the list of results.
        @SuppressWarnings("unchecked")
        List<String> results =
            MONGO_COLLECTION
                .distinct(OauthClient.JSON_KEY_ID, queryBuilder.get());

        // Create and return a MultiValueResult.
        return
            new MongoMultiValueResultList<String>(results, results.size());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.OauthClientBin#getOauthClient(java.lang.String)
     */
    @Override
    public OauthClient getOauthClient(final String oauthClientId)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(oauthClientId == null) {
            throw new IllegalArgumentException("The OAuth client ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the OAuth client ID to the query.
        queryBuilder
            .and(OauthClient.JSON_KEY_ID)
            .is(oauthClientId);

        // Execute query.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }
}