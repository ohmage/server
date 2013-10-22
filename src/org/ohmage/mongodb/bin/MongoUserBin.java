/*******************************************************************************
 * Copyright 2013 Open mHealth
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.mongodb.bin;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.mongojack.internal.MongoJackModule;
import org.ohmage.bin.UserBin;
import org.ohmage.domain.ProviderUserInformation;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.InconsistentDatabaseException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.mongodb.domain.MongoUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

/**
 * <p>
 * The MongoDB implementation of the interface to the database-backed
 * collection of users.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoUserBin extends UserBin {
	/**
	 * The name of the collection that contains all of the users.
	 */
	public static final String COLLECTION_NAME = "user_bin";
	
	/**
	 * The object mapper that should be used to parse {@link User}s.
	 */
	private static final ObjectMapper JSON_MAPPER;
	static {
		// Create the object mapper.
		ObjectMapper mapper = new ObjectMapper();
		
		// Create the FilterProvider.
		SimpleFilterProvider filterProvider = new SimpleFilterProvider();
		filterProvider.setFailOnUnknownId(false);
		mapper.setFilters(filterProvider);
		
		// Finally, we must configure the mapper to work with the MongoJack
		// configuration.
		JSON_MAPPER = MongoJackModule.configure(mapper);
	}	
	
	/**
	 * Default constructor that creates any tables and indexes if necessary.
	 */
	protected MongoUserBin() {
		// Get the collection to add indexes to.
		DBCollection collection =
			MongoBinController.getInstance().getDb().getCollection(COLLECTION_NAME);
		
		// Ensure that there is an index on the username.
		collection
			.ensureIndex(
				new BasicDBObject(User.JSON_KEY_USERNAME, 1),
				COLLECTION_NAME + "_" + User.JSON_KEY_USERNAME + "_unique",
				true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.UserBin#addUser(org.ohmage.domain.User)
	 */
	@Override
	public void addUser(
		final User user)
		throws IllegalArgumentException, InvalidArgumentException {
		
		// Validate the input.
		if(user == null) {
			throw new IllegalArgumentException("The user is null.");
		}
		
		// Get the user collection.
		JacksonDBCollection<User, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					User.class,
					Object.class,
					JSON_MAPPER);
		
		// Attempt to add the user.
		try {
			collection.insert(user, WriteConcern.REPLICA_ACKNOWLEDGED);
		}
		// If the user already exists, throw an exception.
		catch(MongoException.DuplicateKey e) {
			throw new InvalidArgumentException("The user already exists.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.UserBin#getUser(java.lang.String)
	 */
	@Override
	public User getUser(
		final String username)
		throws IllegalArgumentException {
		
		// Validate the parameter.
		if(username == null) {
			throw new IllegalArgumentException("The username is null.");
		}
		
		// Get the user collection.
		JacksonDBCollection<MongoUser, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					MongoUser.class,
					Object.class,
					JSON_MAPPER);
		
		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the authentication token to the query
		queryBuilder.and(MongoUser.JSON_KEY_USERNAME).is(username);
		
		// Execute the query.
		return collection.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.UserBin#getUserFromProvider(java.lang.String, java.lang.String)
	 */
	@Override
	public User getUserFromProvider(
		final String providerId,
		final String userId)
		throws IllegalArgumentException {
		
		if(providerId == null) {
			throw new IllegalArgumentException("The provider ID is null.");
		}
		if(userId == null) {
			throw new IllegalArgumentException("The user ID is null.");
		}
		
		// Get the user collection.
		JacksonDBCollection<MongoUser, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					MongoUser.class,
					Object.class,
					JSON_MAPPER);

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the provider's ID.
		queryBuilder
			.and(
				MongoUser.JSON_KEY_PROVIDERS + 
					"." + 
					ProviderUserInformation.JSON_KEY_PROVIDER_ID)
			.is(providerId);
		
		// Add the user's ID.
		queryBuilder
			.and(
				MongoUser.JSON_KEY_PROVIDERS + 
					"." + 
					ProviderUserInformation.JSON_KEY_USER_ID)
			.is(userId);
		
		// Execute the query.
		return collection.findOne(queryBuilder.get());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.UserBin#updateUser(org.ohmage.domain.User)
	 */
	@Override
	public void updateUser(final User user) throws IllegalArgumentException {
		if(user == null) {
			throw new IllegalArgumentException("The user is null.");
		}
		
		// Get the user collection.
		JacksonDBCollection<User, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					User.class,
					Object.class,
					JSON_MAPPER);
		
		// Create the query.
		// Limit the query only to this user.
		Query query = DBQuery.is(User.JSON_KEY_USERNAME, user.getUsername());
		// Ensure that the user has not been updated elsewhere.
		query =
			query
				.is(User.JSON_KEY_INTERNAL_VERSION,
					user.getInternalReadVersion());
		
		// Commit the update and don't return until the collection has heard
		// the result.
		WriteResult<User, Object> result =
			collection
				.update(
					query,
					user,
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
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.UserBin#disableUser(java.lang.String)
	 */
	@Override
	public void disableUser(
		final String username)
		throws IllegalArgumentException {

		// Validate the parameter.
		if(username == null) {
			throw new IllegalArgumentException("The username is null.");
		}
		
		// Get the user collection.
		JacksonDBCollection<MongoUser, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					MongoUser.class,
					Object.class,
					JSON_MAPPER);
		
		// FIXME: For testing purposes, this actually deletes the object. In
		// the future, we will probably want to simply mark the account as
		// deleted.
		collection.remove(DBQuery.is(User.JSON_KEY_USERNAME, username));
	}
}