package org.ohmage.mongodb.bin;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.AuthorizationTokenBin;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.exception.InconsistentDatabaseException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.mongodb.domain.MongoAuthorizationToken;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The MongoDB implementation of the database-backed authorization token
 * repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoAuthorizationTokenBin extends AuthorizationTokenBin {
	/**
	 * The name of the collection that contains all of the authorization
	 * tokens.
	 */
	public static final String COLLECTION_NAME = "authorization_token_bin";

	/**
	 * Get the connection to the authorization token bin with the Jackson
	 * wrapper.
	 */
	private static final JacksonDBCollection<AuthorizationToken, String> COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				AuthorizationToken.class,
				String.class,
				MongoBinController.getObjectMapper());

	/**
	 * Get the connection to the authorization token bin with the Jackson
	 * wrapper, specifically for {@link MongoAuthorizationToken} objects.
	 */
	private static final JacksonDBCollection<MongoAuthorizationToken, Object> MONGO_COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				MongoAuthorizationToken.class,
				Object.class,
				MongoBinController.getObjectMapper());

	/**
	 * Default constructor.
	 */
	protected MongoAuthorizationTokenBin() {
		// Ensure that there is an index on the access token.
		COLLECTION
			.ensureIndex(
				new BasicDBObject(
					AuthorizationToken.JSON_KEY_ACCESS_TOKEN,
					1),
				COLLECTION_NAME + "_" +
					AuthorizationToken.JSON_KEY_ACCESS_TOKEN + "_unique",
				true);
		// Ensure that there is an index on the refresh token.
		COLLECTION
			.ensureIndex(
				new BasicDBObject(
					AuthorizationToken.JSON_KEY_REFRESH_TOKEN,
					1),
				COLLECTION_NAME + "_" +
					AuthorizationToken.JSON_KEY_REFRESH_TOKEN + "_unique",
				true);
		// Ensure that there is an index on the expiration time.
		COLLECTION
			.ensureIndex(
				new BasicDBObject(AuthorizationToken.JSON_KEY_EXPIRES, 1),
				COLLECTION_NAME +
					"_" +
					AuthorizationToken.JSON_KEY_EXPIRES +
					"_index",
				false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.AuthorizationTokenBin#addToken(org.ohmage.domain.AuthorizationToken)
	 */
	@Override
	public void addToken(
		final AuthorizationToken token)
		throws IllegalArgumentException, InvalidArgumentException {

		// Validate the parameter.
		if(token == null) {
			throw new IllegalArgumentException("The token is null.");
		}

		// Save it.
		try {
			COLLECTION.insert(token);
		}
		catch(MongoException.DuplicateKey e) {
			throw new InvalidArgumentException("The token already exists.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.AuthorizationTokenBin#getToken(java.lang.String)
	 */
	@Override
	public MongoAuthorizationToken getTokenFromAccessToken(
		final String accessToken)
		throws IllegalArgumentException {

		// Validate the parameter.
		if(accessToken == null) {
			throw new IllegalArgumentException("The access token is null.");
		}

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the authorization token to the query.
		queryBuilder
			.and(AuthorizationToken.JSON_KEY_ACCESS_TOKEN)
			.is(accessToken);

		// Execute query.
		return MONGO_COLLECTION.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.AuthorizationTokenBin#getTokenFromRefreshToken(java.lang.String)
	 */
	@Override
	public MongoAuthorizationToken getTokenFromRefreshToken(
		final String refreshToken)
		throws IllegalArgumentException {

		// Validate the parameter.
		if(refreshToken == null) {
			throw new IllegalArgumentException("The refresh token is null.");
		}

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the authorization token to the query.
		queryBuilder
			.and(AuthorizationToken.JSON_KEY_REFRESH_TOKEN)
			.is(refreshToken);

		// Execute query.
		return MONGO_COLLECTION.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.AuthorizationTokenBin#getTokenFromAuthorizationCode(java.lang.String)
	 */
    @Override
    public AuthorizationToken getTokenFromAuthorizationCode(
        final String authorizationCode)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(authorizationCode == null) {
            throw
                new IllegalArgumentException(
                    "The authorization code is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the authorization token to the query.
        queryBuilder
            .and(AuthorizationToken.JSON_KEY_AUTHORIZATION_CODE)
            .is(authorizationCode);

        // Make sure it is the oldest one (the first one that was granted).
        DBObject sort =
            new BasicDBObject(AuthorizationToken.JSON_KEY_GRANTED, 1);

        // Execute query.
        DBCursor<MongoAuthorizationToken> tokens =
            MONGO_COLLECTION.find(queryBuilder.get()).sort(sort).limit(1);

        // If no tokens were returned, return null.
        if(tokens.size() == 0) {
            return null;
        }
        // Otherwise, return the first token that was found.
        else {
            return tokens.next();
        }
    }

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.AuthorizationTokenBin#updateToken(org.ohmage.domain.AuthorizationToken)
	 */
	@Override
	public void updateToken(
		final AuthorizationToken token)
		throws IllegalArgumentException {

		// Validate the parameter.
		if(token == null) {
			throw new IllegalArgumentException("The token is null.");
		}

		// Ensure that we are only updating the given authorization token.
		Query query =
			DBQuery
				.is(AuthorizationToken.JSON_KEY_ACCESS_TOKEN,
					token.getAccessToken());

		// Update the token.
		if(COLLECTION.update(query, token).getN() == 0) {
            throw
                new InconsistentDatabaseException(
                    "A conflict occurred. Please, try again.");
		}
	}
}