package org.ohmage.mongodb.domain;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.User;
import org.ohmage.mongodb.bin.MongoAuthenticationTokenBin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link AuthenticationToken} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoAuthenticationTokenBin.COLLECTION_NAME)
public class MongoAuthenticationToken
	extends AuthenticationToken
	implements MongoDbObject {
	
	/**
	 * The database ID for this object.
	 */
	@ObjectId
	private final String dbId;

	/**
	 * Creates an {@link AuthenticationToken} object via Jackson from the data
	 * layer.
	 * 
	 * @param dbId
	 *        The database ID for this authentication token.
	 * 
	 * @param accessToken
	 *        The authentication token.
	 * 
	 * @param refreshToken
	 *        The refresh token.
	 * 
	 * @param username
	 *        The user's user-name.
	 * 
	 * @param granted
	 *        The time when the token was granted.
	 * 
	 * @param expires
	 *        The time when the token expires.
	 * 
	 * @param internalVersion
	 *        The internal version of this authentication token.
	 * 
	 * @throws IllegalArgumentException
	 *         The token and/or user-name are null, the token is being granted
	 *         in the future, or the token is being granted after it has
	 *         expired.
	 */
	@JsonCreator
	protected MongoAuthenticationToken(
		@Id @ObjectId final String dbId, 
		@JsonProperty(JSON_KEY_ACCESS_TOKEN) final String accessToken,
		@JsonProperty(JSON_KEY_REFRESH_TOKEN) final String refreshToken,
		@JsonProperty(User.JSON_KEY_USERNAME) final String username,
		@JsonProperty(JSON_KEY_GRANTED) final long granted,
		@JsonProperty(JSON_KEY_EXPIRES) final long expires,
		@JsonProperty(JSON_KEY_VALID) final boolean valid,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion) 
		throws IllegalArgumentException {
		
		super(
			accessToken, 
			refreshToken, 
			username, 
			granted, 
			expires, 
			valid, 
			internalVersion);
		
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