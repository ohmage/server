package org.ohmage.domain;

import org.ohmage.bin.UserBin;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.jackson.OhmageObjectMapper;
import org.ohmage.domain.jackson.OhmageObjectMapper.JsonFilterField;
import org.ohmage.domain.user.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A user's authentication token.
 * </p>
 *
 * <p>
 * This class is immutable.
 * </p>
 *
 * @author John Jenkins
 */
@JsonFilter(AuthorizationToken.JACKSON_FILTER_GROUP_ID)
public class AuthorizationToken extends OhmageDomainObject {
    /**
     * <p>
     * A builder class for {@link AuthorizationToken} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder
        extends OhmageDomainObject.Builder<AuthorizationToken> {

        /**
         * The authentication token.
         */
        private final String accessToken;
        /**
         * The refresh token.
         */
        private final String refreshToken;
        /**
         * The token that was being refreshed.
         */
        private String nextToken;
        /**
         * The unique ID of the user to whom the token applies.
         */
        private final String userId;
        /**
         * The number of milliseconds since the epoch at which time the token was
         * granted.
         */
        private final long granted;
        /**
         * The number of milliseconds since the epoch at which time the token will
         * expire.
         */
        private final long expires;
        /**
         * Whether or not a token is valid.
         */
        private boolean valid;

        /**
         * Constructs a new builder based on a given authorization token.
         *
         * @param original
         *        The original authorization token to base this builder off of.
         */
        public Builder(final AuthorizationToken original) {
            super(original);

            accessToken = original.accessToken;
            refreshToken = original.refreshToken;
            nextToken = original.nextToken;
            userId = original.userId;
            granted = original.granted;
            expires = original.expires;
            valid = original.valid;
        }

        /**
         * Sets the value of the token that was used to refresh this token.
         *
         * @param nextToken The value of the token that was used to refresh
         * this token.
         *
         * @return This Builder to facilitate chaining.
         */
        public Builder setNextToken(final String nextToken) {
            this.nextToken = nextToken;

            return this;
        }

        /**
         * Sets whether or not this token should be considered valid.
         *
         * @param valid
         *        Whether or not this token is valid.
         *
         * @return This Builder to facilitate chaining.
         */
        public Builder setValid(final boolean valid) {
            this.valid = valid;

            return this;
        }

        /**
         * Constructs a new {@link AuthorizationToken} object based on the
         * state of this builder.
         *
         * @return The new {@link AuthorizationToken}.
         *
         * @throws IllegalArgumentException The state of this builder is invalid
         * for building a new authorization token.
         */
        public AuthorizationToken build() throws IllegalArgumentException {
            return
                new AuthorizationToken(
                    accessToken,
                    refreshToken,
                    nextToken,
                    userId,
                    granted,
                    expires,
                    valid,
                    internalReadVersion,
                    internalWriteVersion);
        }
    }

    /**
     * The group ID for the Jackson filter. This must be unique to our class,
     * whatever the value is.
     */
    protected static final String JACKSON_FILTER_GROUP_ID =
        "org.ohmage.domain.AuthorizationToken";
    // Register this class with the ohmage object mapper.
    static {
        OhmageObjectMapper.register(AuthorizationToken.class);
    }

    /**
     * The name of the header that contains the authorization information.
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

	/**
	 * The JSON key for the authentication token.
	 */
	public static final String JSON_KEY_ACCESS_TOKEN = "access_token";
	/**
	 * The JSON key for the refresh token.
	 */
	public static final String JSON_KEY_REFRESH_TOKEN = "refresh_token";
	/**
	 * The JSON key for the token that was used to replace this token.
	 */
	public static final String JSON_KEY_NEW_TOKEN = "next_token";
	/**
	 * The JSON key for the time the token was granted.
	 */
	public static final String JSON_KEY_GRANTED = "granted";
	/**
	 * The JSON key for the time the token expires.
	 */
	public static final String JSON_KEY_EXPIRES = "expires";
	/**
	 * The JSON key for the time the token expires.
	 */
	public static final String JSON_KEY_VALID = "valid";

	/**
	 * The default duration of the authentication token.
	 */
	public static final long AUTH_TOKEN_LIFETIME = 1000 * 60 * 30;

	/**
	 * The authentication token.
	 */
	@JsonProperty(JSON_KEY_ACCESS_TOKEN)
	private final String accessToken;
	/**
	 * The refresh token.
	 */
	@JsonProperty(JSON_KEY_REFRESH_TOKEN)
	private final String refreshToken;
	/**
	 * The token that was being refreshed.
	 */
    @JsonProperty(JSON_KEY_NEW_TOKEN)
    @JsonFilterField
	private final String nextToken;
	/**
	 * The unique ID of the user to whom the token applies.
	 */
	@JsonProperty(User.JSON_KEY_ID)
	private final String userId;
	/**
	 * The number of milliseconds since the epoch at which time the token was
	 * granted.
	 */
	@JsonProperty(JSON_KEY_GRANTED)
	private final long granted;
	/**
	 * The number of milliseconds since the epoch at which time the token will
	 * expire.
	 */
	@JsonProperty(JSON_KEY_EXPIRES)
	private final long expires;
	/**
	 * Whether or not a token is valid.
	 */
	@JsonProperty(JSON_KEY_VALID)
	private final boolean valid;

	/**
	 * Creates a new authentication token for a user.
	 *
	 * @param user
	 *        The user about whom the authentication token should apply.
	 *
	 * @throws IllegalArgumentException
	 *         The user is null.
	 */
	public AuthorizationToken(
		final User user)
		throws IllegalArgumentException {

		// Pass through to the builder constructor.
		this(
			getRandomId(),
			getRandomId(),
			null,
			((user == null) ? null : user.getId()),
			System.currentTimeMillis(),
			System.currentTimeMillis() + AUTH_TOKEN_LIFETIME,
			true,
			null);
	}

	/**
	 * Refreshes an existing authentication token by creating a new one in its
	 * place. After calling this, the old token will be invalidated.
	 *
	 * @param oldToken
	 *        The original token on which this token should be based.
	 *
	 * @throws IllegalArgumentException
	 *         The original token was null.
	 */
	public AuthorizationToken(
		final AuthorizationToken oldToken)
		throws IllegalArgumentException {

		// Pass through to the builder constructor.
		this(
			getRandomId(),
			getRandomId(),
			null,
			((oldToken == null) ? null : oldToken.getUserId()),
			System.currentTimeMillis(),
			System.currentTimeMillis() + AUTH_TOKEN_LIFETIME,
			true,
			null);
	}

    /**
     * Recreates an existing authentication token.
     *
     * @param accessToken
     *        The authentication token.
     *
     * @param refreshToken
     *        The refresh token.
     *
     * @param nextToken
     *        The token that was issued when this token was refreshed.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @param granted
     *        The time when the token was granted.
     *
     * @param expires
     *        The time when the token expires.
     *
     * @param valid
     *        Whether or not this token is valid.
     *
     * @param internalVersion
     *        The internal version of this authentication token.
     *
     * @throws IllegalArgumentException
     *         The token and/or unique identifier for the user are null, the
     *         token is being granted in the future, or the token is being
     *         granted after it has expired.
     */
	@JsonCreator
	protected AuthorizationToken(
		@JsonProperty(JSON_KEY_ACCESS_TOKEN) final String accessToken,
		@JsonProperty(JSON_KEY_REFRESH_TOKEN) final String refreshToken,
		@JsonProperty(JSON_KEY_NEW_TOKEN) final String newToken,
		@JsonProperty(User.JSON_KEY_ID) final String userId,
		@JsonProperty(JSON_KEY_GRANTED) final long granted,
		@JsonProperty(JSON_KEY_EXPIRES) final long expires,
		@JsonProperty(JSON_KEY_VALID) final boolean valid,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException {

		// Pass through to the builder constructor.
		this(
			accessToken,
			refreshToken,
			newToken,
			userId,
			granted,
			expires,
			valid,
			internalVersion,
			null);
	}

    /**
     * Builds the AuthorizationToken object.
     *
     * @param accessToken
     *        The authentication token.
     *
     * @param refreshToken
     *        The refresh token.
     *
     * @param nextToken
     *        The token that was issued when this token was refreshed.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @param granted
     *        The time when the token was granted.
     *
     * @param expires
     *        The time when the token expires.
     *
     * @param valid
     *        Whether or not this token is valid.
     *
     * @param internalReadVersion
     *        The internal version of this authentication token when it was
     *        read from the database.
     *
     * @param internalWriteVersion
     *        The new internal version of this authentication token when it
     *        will be written back to the database.
     *
     * @throws IllegalArgumentException
     *         The token and/or unique identifier for the user are null, the
     *         token is being granted in the future, or the token is being
     *         granted after it has expired.
     */
	private AuthorizationToken(
		final String accessToken,
		final String refreshToken,
		final String nextToken,
		final String userId,
		final long granted,
		final long expires,
		final boolean valid,
		final Long internalReadVersion,
		final Long internalWriteVersion)
		throws IllegalArgumentException {

		// Pass the versioning parameters to the parent.
		super(internalReadVersion, internalWriteVersion);

		// Validate the parameters.
		if(accessToken == null) {
			throw
				new IllegalArgumentException(
					"The authentication token is null.");
		}
		if(refreshToken == null) {
			throw
				new IllegalArgumentException(
					"The refresh token is null.");
		}
		if(userId == null) {
			throw
			    new IllegalArgumentException(
			        "The user's unique identifier is null.");
		}
		if(granted > System.currentTimeMillis()) {
			throw
				new IllegalArgumentException(
					"An authentication token cannot be granted in the " +
						"future.");
		}
		if(granted > expires) {
			throw
				new IllegalArgumentException(
					"A token cannot expire before it was granted.");
		}

		// Save the state.
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.nextToken = nextToken;
		this.userId = userId;
		this.granted = granted;
		this.expires = expires;
		this.valid = valid;
	}

	/**
	 * Returns the authentication token.
	 *
	 * @return The authentication token.
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * Returns the refresh token.
	 *
	 * @return The refresh token.
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

    /**
     * Returns the unique identifier for the user associated with this
     * authentication token.
     *
     * @return The unique identifier for the user associated with this
     *         authentication token.
     */
	public String getUserId() {
		return userId;
	}

	/**
	 * Returns the user associated with this authentication token.
	 *
	 * @return The user associated with this authentication token.
	 *
	 * @throws IllegalStateException
	 *         There is an internal error or the user associated with this
	 *         token no longer exists.
	 */
	public User getUser() throws IllegalArgumentException {
		// Attempt to get the user.
		User user = UserBin.getInstance().getUser(userId);

		// If the user no longer exists, throw an exception.
		if(user == null) {
			throw
				new IllegalStateException(
					"The user that is associated with this token no longer " +
						"exists.");
		}

		// Return the user.
		return user;
	}

	/**
	 * Returns the number of milliseconds since the epoch when this token was
	 * granted.
	 *
	 * @return The number of milliseconds since the epoch when this token was
	 *         granted.
	 */
	public long getGranted() {
		return granted;
	}

	/**
	 * Returns the number of milliseconds since the epoch when this token
	 * (will) expire(d).
	 *
	 * @return The number of milliseconds since the epoch when this token
	 * 		   (will) expire(d).
	 */
	public long getExpires() {
		return expires;
	}

	/**
	 * Returns whether or not this token has expired.
	 *
	 * @return Whether or not this token has expired.
	 */
	public boolean isExpired() {
		return expires < System.currentTimeMillis();
	}

	/**
	 * Returns whether or not this token has been invalidated.
	 *
	 * @return Whether or not this token has been invalidated.
	 */
	public boolean wasInvalidated() {
		return ! valid;
	}

	/**
	 * Returns whether or not this token was used to create a new token via its
	 * refresh token, which means that this token is no longer valid.
	 *
	 * @return Whether or not this token has been refreshed.
	 */
	public boolean wasRefreshed() {
		return nextToken != null;
	}

    /**
     * If refreshed, returns the access token value for the token that was
     * returned when refreshing; otherwise, null is returned.
     *
     * @return The access token value for the token that was returned to
     *         refresh this token, or null if this token has not been
     *         refreshed.
     */
	public String getNextToken() {
	    return nextToken;
	}

	/**
	 * Returns whether or not this token is still valid.
	 *
	 * @return Whether or not this token is still valid.
	 */
	@JsonIgnore
	public boolean isValid() {
		return ! (isExpired() || wasInvalidated() || wasRefreshed());
	}

	/**
     * Parses the authorization header by verifying that it conforms to our
     * format and is for our domain.
     *
     * @param header
     *        The Authorization header string.
     *
     * @return The user-supplied token.
     *
     * @throws InsufficientPermissionsException
     *         The header is missing, unintelligible, or not for our domain.
     */
	public static String getTokenFromHeader(
	    final String header)
	    throws InsufficientPermissionsException {

        if(header == null) {
            throw
                new InsufficientPermissionsException(
                    "No auth information was given.");
        }
        String[] authHeaderParts = header.split(" ");
        if(authHeaderParts.length != 2) {
            throw
                new InsufficientPermissionsException(
                    "The auth header is malformed.");
        }
        if(! "ohmage".equals(authHeaderParts[0])) {
            throw
                new InsufficientPermissionsException(
                    "The auth header is not for 'ohmage'.");
        }

        return authHeaderParts[1];
	}
}