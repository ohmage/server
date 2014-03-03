package org.ohmage.mongodb.domain;

import org.mongojack.Id;
import org.mongojack.MongoCollection;
import org.mongojack.ObjectId;
import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.mongodb.bin.MongoAuthorizationTokenBin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A MongoDB extension of the {@link AuthorizationToken} type.
 * </p>
 *
 * @author John Jenkins
 */
@MongoCollection(name = MongoAuthorizationTokenBin.COLLECTION_NAME)
public class MongoAuthorizationToken
	extends AuthorizationToken
	implements MongoDbObject {

	/**
	 * The database ID for this object.
	 */
	@ObjectId
	private final String dbId;

    /**
     * Creates an {@link AuthorizationToken} object via Jackson from the data
     * layer.
     *
     * @param dbId
     *        The database ID for this authorization token.
     *
     * @param accessToken
     *        The authorization token.
     *
     * @param refreshToken
     *        The refresh token.
     *
     * @param nextToken
     *        The token that was issued when this token was refreshed.
     *
     * @param userId
     *        The user's internal unique identifier.
     *
     * @param authorizationCode
     *        The authorization code that was used to authorize the creation of
     *        this token.
     *
     * @param granted
     *        The time when the token was granted.
     *
     * @param expires
     *        The time when the token expires.
     *
     * @param internalVersion
     *        The internal version of this entity used for checking for update
     *        collisions.
     *
     * @throws IllegalArgumentException
     *         The token and/or unique identifier for the user are null, the
     *         token is being granted in the future, or the token is being
     *         granted after it has expired.
     */
	@JsonCreator
	protected MongoAuthorizationToken(
		@Id @ObjectId final String dbId,
        @JsonProperty(JSON_KEY_ACCESS_TOKEN) final String accessToken,
        @JsonProperty(JSON_KEY_REFRESH_TOKEN) final String refreshToken,
        @JsonProperty(JSON_KEY_NEW_TOKEN) final String nextToken,
        @JsonProperty(JSON_KEY_USER_ID) final String userId,
        @JsonProperty(JSON_KEY_AUTHORIZATION_CODE)
            final String authorizationCode,
        @JsonProperty(JSON_KEY_GRANTED) final long granted,
        @JsonProperty(JSON_KEY_EXPIRES) final long expires,
        @JsonProperty(JSON_KEY_VALID) final boolean valid,
        @JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException {

		super(
			accessToken,
			refreshToken,
			nextToken,
			userId,
			authorizationCode,
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