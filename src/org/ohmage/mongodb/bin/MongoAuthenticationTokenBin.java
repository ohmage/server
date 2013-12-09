package org.ohmage.mongodb.bin;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.AuthenticationTokenBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.mongodb.domain.MongoAuthenticationToken;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * <p>
 * The MongoDB implementation of the database-backed authentication token
 * repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoAuthenticationTokenBin extends AuthenticationTokenBin {
	/**
	 * The name of the collection that contains all of the authentication
	 * tokens.
	 */
	public static final String COLLECTION_NAME = "authentication_token_bin";
	
	/** 
	 * Get the connection to the authentication token bin with the Jackson
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
	 * Get the connection to the authentication token bin with the Jackson
	 * wrapper, specifically for {@link MongoAuthenticationToken} objects.
	 */
	private static final JacksonDBCollection<MongoAuthenticationToken, Object> MONGO_COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				MongoAuthenticationToken.class,
				Object.class,
				MongoBinController.getObjectMapper());
	
	/**
	 * Default constructor.
	 */
	protected MongoAuthenticationTokenBin() {
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
	 * @see org.ohmage.bin.AuthenticationTokenBin#addToken(org.ohmage.domain.AuthorizationToken)
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
	 * @see org.ohmage.bin.AuthenticationTokenBin#getToken(java.lang.String)
	 */
	@Override
	public MongoAuthenticationToken getTokenFromAccessToken(
		final String accessToken)
		throws IllegalArgumentException, IllegalStateException {
		
		// Validate the parameter.
		if(accessToken == null) {
			throw new IllegalArgumentException("The access token is null.");
		}
		
		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the authentication token to the query.
		queryBuilder
			.and(AuthorizationToken.JSON_KEY_ACCESS_TOKEN)
			.is(accessToken);
		
		// Execute query.
		return MONGO_COLLECTION.findOne(queryBuilder.get());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.AuthenticationTokenBin#getTokenFromRefreshToken(java.lang.String)
	 */
	@Override
	public MongoAuthenticationToken getTokenFromRefreshToken(
		final String refreshToken)
		throws IllegalArgumentException {
		
		// Validate the parameter.
		if(refreshToken == null) {
			throw new IllegalArgumentException("The refresh token is null.");
		}
		
		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the authentication token to the query.
		queryBuilder
			.and(AuthorizationToken.JSON_KEY_REFRESH_TOKEN)
			.is(refreshToken);
		
		// Execute query.
		return MONGO_COLLECTION.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.AuthenticationTokenBin#updateToken(org.ohmage.domain.AuthorizationToken)
	 */
	@Override
	public void updateToken(
		final AuthorizationToken token)
		throws IllegalArgumentException {
		
		// Validate the parameter.
		if(token == null) {
			throw new IllegalArgumentException("The token is null.");
		}
		
		// Ensure that we are only updating the given authentication token.
		Query query =
			DBQuery
				.is(AuthorizationToken.JSON_KEY_ACCESS_TOKEN,
					token.getAccessToken());
		
		// Update the token.
		COLLECTION.update(query, token);
	}
}