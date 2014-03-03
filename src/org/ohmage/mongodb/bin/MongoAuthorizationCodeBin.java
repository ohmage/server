package org.ohmage.mongodb.bin;

import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.AuthorizationCodeBin;
import org.ohmage.bin.MultiValueResult;
import org.ohmage.domain.auth.AuthorizationCode;
import org.ohmage.domain.auth.AuthorizationCodeResponse;
import org.ohmage.domain.exception.InconsistentDatabaseException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.mongodb.domain.MongoAuthorizationCode;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The MongoDB implementation of the database-backed authorization code
 * repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoAuthorizationCodeBin extends AuthorizationCodeBin {
    /**
     * The name of the collection that contains all of the authorization codes.
     */
    public static final String COLLECTION_NAME = "authorization_code_bin";

    /**
     * Get the connection to the authorization code bin with the Jackson
     * wrapper.
     */
    private static final JacksonDBCollection<AuthorizationCode, String> COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                AuthorizationCode.class,
                String.class,
                MongoBinController.getObjectMapper());

    /**
     * Get the connection to the authorization code bin with the Jackson
     * wrapper, specifically for {@link MongoAuthorizationCode} objects.
     */
    private static final JacksonDBCollection<MongoAuthorizationCode, Object> MONGO_COLLECTION =
        JacksonDBCollection
            .wrap(
                MongoBinController
                    .getInstance()
                    .getDb()
                    .getCollection(COLLECTION_NAME),
                MongoAuthorizationCode.class,
                Object.class,
                MongoBinController.getObjectMapper());

    /**
     * Default constructor.
     */
    protected MongoAuthorizationCodeBin() {
        // Ensure that there is an index on the code.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(
                    AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE,
                    1),
                COLLECTION_NAME + "_" +
                    AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE + "_unique",
                true);
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.AuthorizationCodeBin#addCode(org.ohmage.domain.auth.AuthorizationCode)
     */
    @Override
    public void addCode(final AuthorizationCode code)
        throws IllegalArgumentException, InvalidArgumentException {

        // Validate the parameter.
        if(code == null) {
            throw new IllegalArgumentException("The code is null.");
        }

        // Save it.
        try {
            COLLECTION.insert(code);
        }
        catch(MongoException.DuplicateKey e) {
            throw new InvalidArgumentException("The code already exists.");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.AuthorizationCodeBin#getCodes(java.lang.String)
     */
    @Override
    public MultiValueResult<String> getCodes(final String userId)
        throws IllegalArgumentException {

        // Build the query
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the user's ID.
        queryBuilder
            .and(
                AuthorizationCode.JSON_KEY_RESPONSE +
                    "." +
                    AuthorizationCodeResponse.JSON_KEY_USER_ID)
            .is(userId);

        // Make sure the user approved it.
        queryBuilder
            .and(
                AuthorizationCode.JSON_KEY_RESPONSE +
                    "." +
                    AuthorizationCodeResponse.JSON_KEY_GRANTED)
            .is(true);

        // Make sure the user hasn't invalidated it.
        queryBuilder
            .and(
                AuthorizationCode.JSON_KEY_RESPONSE +
                    "." +
                    AuthorizationCodeResponse.JSON_KEY_INVALIDATION_TIMESTAMP)
            .is(null);

        // Get the list of results.
        @SuppressWarnings("unchecked")
        List<String> results =
            MONGO_COLLECTION
                .distinct(
                    AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE,
                    queryBuilder.get());

        // Create and return a MultiValueResult.
        return
            new MongoMultiValueResultList<String>(results, results.size());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.AuthorizationCodeBin#getCode(java.lang.String)
     */
    @Override
    public AuthorizationCode getCode(final String code)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(code == null) {
            throw new IllegalArgumentException("The code is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the authorization code to the query.
        queryBuilder
            .and(AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE)
            .is(code);

        // Execute query.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.AuthorizationCodeBin#updateCode(org.ohmage.domain.auth.AuthorizationCode)
     */
    @Override
    public void updateCode(final AuthorizationCode code)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(code == null) {
            throw new IllegalArgumentException("The code is null.");
        }

        // Ensure that we are only updating the given authorization token.
        Query query =
            DBQuery
                .is(AuthorizationCode.JSON_KEY_AUTHORIZATION_CODE,
                    code.getCode());

        // Update the token.
        if(COLLECTION.update(query, code).getN() == 0) {
            throw
                new InconsistentDatabaseException(
                    "A conflict occurred. Please, try again.");
        }
    }
}