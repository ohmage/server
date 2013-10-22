package org.ohmage.mongodb.bin;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.ohmage.bin.AuthenticationTokenBin;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.mongodb.domain.MongoAuthenticationToken;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandFailureException;
import com.mongodb.DBCollection;
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
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(MongoAuthenticationTokenBin.class.getName());
	
	/**
	 * Default constructor.
	 */
	protected MongoAuthenticationTokenBin() {
		// Get the collection to add indexes to.
		DBCollection collection =
			MongoBinController
			.getInstance()
			.getDb()
			.getCollection(COLLECTION_NAME);
		
		// Remove the old "token" index, if it still exists.
		try {
			collection.dropIndex(COLLECTION_NAME + "_" + "token" + "_unique");
		}
		catch(CommandFailureException e) {
			LOGGER
				.log(
					Level.INFO,
					"The 'token' index had already been removed.");
		}

		// Ensure that there is an index on the access token.
		collection
			.ensureIndex(
				new BasicDBObject(
					AuthenticationToken.JSON_KEY_ACCESS_TOKEN,
					1), 
				COLLECTION_NAME + "_" +
					AuthenticationToken.JSON_KEY_ACCESS_TOKEN + "_unique",
				true);
		// Ensure that there is an index on the refresh token.
		collection
			.ensureIndex(
				new BasicDBObject(
					AuthenticationToken.JSON_KEY_REFRESH_TOKEN,
					1), 
				COLLECTION_NAME + "_" +
					AuthenticationToken.JSON_KEY_REFRESH_TOKEN + "_unique",
				true);
		// Ensure that there is an index on the expiration time.
		collection
			.ensureIndex(
				new BasicDBObject(AuthenticationToken.JSON_KEY_EXPIRES, 1),
				COLLECTION_NAME + 
					"_" +
					AuthenticationToken.JSON_KEY_EXPIRES +
					"_index",
				false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.AuthenticationTokenBin#addToken(org.ohmage.domain.AuthenticationToken)
	 */
	@Override
	public void addToken(
		final AuthenticationToken token)
		throws IllegalArgumentException, InvalidArgumentException {
		
		// Validate the parameter.
		if(token == null) {
			throw new IllegalArgumentException("The token is null.");
		}
		
		// Get the authentication token collection.
		JacksonDBCollection<AuthenticationToken, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController
						.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					AuthenticationToken.class,
					Object.class,
					MongoBinController.getObjectMapper());
		
		// Save it.
		try {
			collection.insert(token);
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
		
		// Get the connection to the authentication token bin with the Jackson
		// wrapper.
		JacksonDBCollection<MongoAuthenticationToken, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController
						.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					MongoAuthenticationToken.class,
					Object.class,
					MongoBinController.getObjectMapper());
		
		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the authentication token to the query.
		queryBuilder
			.and(AuthenticationToken.JSON_KEY_ACCESS_TOKEN)
			.is(accessToken);
		
		// Execute query.
		return collection.findOne(queryBuilder.get());
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
		
		// Get the connection to the authentication token bin with the Jackson
		// wrapper.
		JacksonDBCollection<MongoAuthenticationToken, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController
						.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					MongoAuthenticationToken.class,
					Object.class,
					MongoBinController.getObjectMapper());
		
		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the authentication token to the query.
		queryBuilder
			.and(AuthenticationToken.JSON_KEY_REFRESH_TOKEN)
			.is(refreshToken);
		
		// Execute query.
		return collection.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.AuthenticationTokenBin#updateToken(org.ohmage.domain.AuthenticationToken)
	 */
	@Override
	public void updateToken(
		final AuthenticationToken token)
		throws IllegalArgumentException {
		
		// Validate the parameter.
		if(token == null) {
			throw new IllegalArgumentException("The token is null.");
		}

		// Get the authentication token collection.
		JacksonDBCollection<AuthenticationToken, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController
						.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					AuthenticationToken.class,
					Object.class,
					MongoBinController.getObjectMapper());
		
		// Ensure that we are only updating the given authentication token.
		Query query =
			DBQuery
				.is(AuthenticationToken.JSON_KEY_ACCESS_TOKEN,
					token.getAccessToken());
		
		// Update the token.
		collection.update(query, token);
	}
}