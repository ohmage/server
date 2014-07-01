package org.ohmage.mongodb.bin;

import java.util.List;

import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.bin.OAuthClientBin;
import org.ohmage.domain.auth.OAuthClient;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.mongodb.domain.MongoOAuthClient;

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
public class MongoOAuthClientBin extends OAuthClientBin {
    /**
     * The name of the collection that contains all of the third-parties.
     */
    public static final String COLLECTION_NAME = "oauth_client_bin";

    /**
     * Get the connection to the OAuth client bin with the Jackson wrapper.
     */
    private static final JacksonDBCollection<OAuthClient, String> COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                OAuthClient.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Get the connection to the OAuth client bin with the Jackson wrapper,
     * specifically for {@link org.ohmage.mongodb.domain.MongoOAuthClient} objects.
     */
    private static final JacksonDBCollection<MongoOAuthClient, Object> MONGO_COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                MongoOAuthClient.class,
                Object.class,
                MongoBinController.getObjectMapper());

    /**
     * Default constructor.
     */
    protected MongoOAuthClientBin() {
        // Ensure that there is an index on the OAuth client's ID.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(OAuthClient.JSON_KEY_ID, 1),
                COLLECTION_NAME + "_" + OAuthClient.JSON_KEY_ID + "_unique",
                true);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.OAuthClientBin#addOAuthClient(org.ohmage.domain.OAuthClient)
     */
    @Override
    public void addOAuthClient(final OAuthClient oAuthClient)
        throws IllegalArgumentException, InvalidArgumentException {

        // Validate the parameter.
        if(oAuthClient == null) {
            throw new IllegalArgumentException("The OAuth client is null.");
        }

        // Save it.
        try {
            COLLECTION.insert(oAuthClient);
        }
        catch(MongoException.DuplicateKey e) {
            throw
                new InvalidArgumentException(
                    "A OAuth client with the same ID already exists.");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.OAuthClientBin#getClientIds(java.lang.String)
     */
    @Override
    public MultiValueResult<String> getClientIds(final String userId)
        throws IllegalArgumentException {

        // Build the query
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the user's ID.
        queryBuilder.and(OAuthClient.JSON_KEY_OWNER).is(userId);

        // Get the list of results.
        @SuppressWarnings("unchecked")
        List<String> results =
            MONGO_COLLECTION
                .distinct(OAuthClient.JSON_KEY_ID, queryBuilder.get());

        // Create and return a MultiValueResult.
        return
            new MongoMultiValueResultList<String>(results, results.size());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.OAuthClientBin#getOauthClient(java.lang.String)
     */
    @Override
    public OAuthClient getOAuthClient(final String oAuthClientId)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(oAuthClientId == null) {
            throw new IllegalArgumentException("The OAuth client ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the OAuth client ID to the query.
        queryBuilder
            .and(OAuthClient.JSON_KEY_ID)
            .is(oAuthClientId);

        // Execute query.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }
}