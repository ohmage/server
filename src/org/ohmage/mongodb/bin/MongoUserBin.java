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
import org.ohmage.domain.OhmageDomainObject;
import org.ohmage.domain.exception.InconsistentDatabaseException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyResponse;
import org.ohmage.domain.user.ProviderUserInformation;
import org.ohmage.domain.user.Registration;
import org.ohmage.domain.user.User;
import org.ohmage.mongodb.domain.MongoOhmlet;
import org.ohmage.mongodb.domain.MongoUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

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
		ObjectMapper mapper = MongoBinController.getObjectMapper();

		// Create the FilterProvider.
		SimpleFilterProvider filterProvider = new SimpleFilterProvider();
		filterProvider.setFailOnUnknownId(false);
		mapper.setFilters(filterProvider);

		// Finally, we must configure the mapper to work with the MongoJack
		// configuration.
		JSON_MAPPER = MongoJackModule.configure(mapper);
	}

	/**
	 * Get the connection to the ohmlet bin with the Jackson wrapper.
	 */
	private static final JacksonDBCollection<User, Object> COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				User.class,
				Object.class,
				JSON_MAPPER);

	/**
	 * Get the connection to the ohmlet bin with the Jackson wrapper,
	 * specifically for {@link MongoOhmlet} objects.
	 */
	private static final JacksonDBCollection<MongoUser, Object> MONGO_COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				MongoUser.class,
				Object.class,
				JSON_MAPPER);

	/**
	 * Default constructor that creates any tables and indexes if necessary.
	 */
	protected MongoUserBin() {
        // Ensure that there is an index on the user's unique identifier.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(User.JSON_KEY_ID, 1),
                COLLECTION_NAME + "_" + User.JSON_KEY_ID + "_unique",
                true);

        // Ensure that there is an index on the user's email address.
        COLLECTION
            .ensureIndex(
                new BasicDBObject(User.JSON_KEY_EMAIL, 1),
                COLLECTION_NAME + "_" + User.JSON_KEY_EMAIL + "_unique",
                true);

        // Ensure that there is a unique index on the media filenames.
        // Create the old-style of options due to a bug in MongoDB, see:
        // https://jira.mongodb.org/browse/SERVER-3934
        BasicDBObject options = new BasicDBObject();
        options
            .put(
                "name",
                COLLECTION_NAME + "_" +
                    User.JSON_KEY_PROVIDERS + "_" +
                    ProviderUserInformation.JSON_KEY_PROVIDER_ID + "_" +
                    ProviderUserInformation.JSON_KEY_USER_ID + "_" +
                    "unique");
        options.put("unique", true);
        // This means that some rows are not required to have the array or any
        // elements in the array.
        options.put("sparse", true);
        // This circumvents a bug in MongoDB by using their old indexing
        // strategy.
        options.put("v", 0);
        // Ensure that there is an index on the provider ID and token.
		BasicDBObject providerIndex = new BasicDBObject();
		providerIndex
            .put(
                User.JSON_KEY_PROVIDERS + "." +
                    ProviderUserInformation.JSON_KEY_PROVIDER_ID,
                1);
		providerIndex
            .put(
                User.JSON_KEY_PROVIDERS + "." +
                    ProviderUserInformation.JSON_KEY_USER_ID,
                1);
        COLLECTION
            .ensureIndex(
                new BasicDBObject(SurveyResponse.JSON_KEY_MEDIA_FILENAMES, 1),
                options);
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

		// Attempt to add the user.
		try {
			COLLECTION.insert(user);
		}
		// If the user already exists, throw an exception.
		catch(MongoException.DuplicateKey e) {
			throw
			    new InvalidArgumentException(
			        "A user with the given email address already exists or " +
			            "a user has already linked this provider account to " +
			            "their ohmage account.",
			        e);
		}
	}

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.UserBin#getUser(java.lang.String)
     */
    @Override
    public User getUser(final String id)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(id == null) {
            throw
                new IllegalArgumentException(
                    "The user's unique identifier is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the authentication token to the query
        queryBuilder.and(User.JSON_KEY_ID).is(id);

        // Execute the query.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.UserBin#getUserFromEmail(java.lang.String)
     */
    @Override
    public User getUserFromEmail(final String email)
        throws IllegalArgumentException {

        // Validate the parameter.
        if(email == null) {
            throw
                new IllegalArgumentException(
                    "The user's email address is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the authentication token to the query
        queryBuilder.and(User.JSON_KEY_EMAIL).is(email);

        // Execute the query.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
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

		// Build the query.
		QueryBuilder queryBuilder = QueryBuilder.start();

		// Add the provider's ID.
		queryBuilder
			.and(
				User.JSON_KEY_PROVIDERS +
					"." +
					ProviderUserInformation.JSON_KEY_PROVIDER_ID)
			.is(providerId);

		// Add the user's ID.
		queryBuilder
			.and(
				User.JSON_KEY_PROVIDERS +
					"." +
					ProviderUserInformation.JSON_KEY_USER_ID)
			.is(userId);

		// Execute the query.
		return MONGO_COLLECTION.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.UserBin#getUserFromActivationId(java.lang.String)
	 */
    @Override
    public User getUserFromActivationId(final String activationId)
        throws IllegalArgumentException {

        if(activationId == null) {
            throw new IllegalArgumentException("The activation ID is null.");
        }

        // Build the query.
        QueryBuilder queryBuilder = QueryBuilder.start();

        // Add the activation ID.
        queryBuilder
            .and(
                User.JSON_KEY_REGISTRATION + "." +
                    Registration.JSON_KEY_ACTIVATION_ID)
            .is(activationId);

        // Execute the query.
        return MONGO_COLLECTION.findOne(queryBuilder.get());
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

		// Create the query.
		// Limit the query only to this user.
		Query query = DBQuery.is(User.JSON_KEY_ID, user.getId());
		// Ensure that the user has not been updated elsewhere.
		query =
			query
				.is(OhmageDomainObject.JSON_KEY_INTERNAL_VERSION,
					user.getInternalReadVersion());

		// Commit the update and don't return until the collection has heard
		// the result.
		WriteResult<User, Object> result = COLLECTION.update(query, user);

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
		final String userId)
		throws IllegalArgumentException {

		// Validate the parameter.
		if(userId == null) {
			throw new IllegalArgumentException("The user ID is null.");
		}

		// FIXME: For testing purposes, this actually deletes the object. In
		// the future, we will probably want to simply mark the account as
		// deleted.
		COLLECTION.remove(DBQuery.is(User.JSON_KEY_ID, userId));
	}
}