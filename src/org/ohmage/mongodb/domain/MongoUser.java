package org.ohmage.mongodb.domain;

import java.util.Map;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.ProviderUserInformation;
import org.ohmage.domain.User;
import org.ohmage.mongodb.bin.MongoUserBin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link User} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoUserBin.COLLECTION_NAME)
public class MongoUser extends User implements MongoDbObject {
	/**
	 * The database ID for this object.
	 */
	@ObjectId
	private final String dbId;

	/**
	 * Creates a {@link User} object via Jackson from the data layer.
	 * 
	 * @param dbId
	 *        The database ID for this user.
	 * 
	 * @param username
	 *        The user-name of the user.
	 * 
	 * @param password
	 *        The hashed password of the user.
	 * 
	 * @param email
	 *        The email address of the user.
	 * 
	 * @param fullName
	 *        The full name of the user, which may be null.
	 * 
	 * @param providers
	 *        The collection of information about providers that have
	 *        authenticated this user.
	 * 
	 * @throws IllegalArgumentException
	 *         A required parameter is null or invalid.
	 */
	@JsonCreator
	public MongoUser(
		@Id @ObjectId final String dbId,
		@JsonProperty(JSON_KEY_USERNAME) final String username,
		@JsonProperty(JSON_KEY_PASSWORD) final String password,
		@JsonProperty(JSON_KEY_EMAIL) final String email,
		@JsonProperty(JSON_KEY_FULL_NAME) final String fullName,
		@JsonProperty(JSON_KEY_PROVIDERS)
			final Map<String, ProviderUserInformation> providers,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException {
		
		super(username, password, email, fullName, providers, internalVersion);
		
		// Store the MongoDB ID.
		if(dbId == null) {
			throw new IllegalArgumentException("The MongoDB ID is missing.");
		}
		else {
			this.dbId = dbId;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.mongodb.domain.MongoDbObject#getDatabaseId()
	 */
	@Override
	@ObjectId
	public String getDbId() {
		return dbId;
	}
}