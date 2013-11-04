package org.ohmage.domain;

import org.ohmage.bin.UserBin;

import com.fasterxml.jackson.annotation.JsonCreator;
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
public class AuthenticationToken extends OhmageDomainObject {
	/**
	 * The JSON key for the authentication token.
	 */
	public static final String JSON_KEY_ACCESS_TOKEN = "access_token";
	/**
	 * The JSON key for the refresh token.
	 */
	public static final String JSON_KEY_REFRESH_TOKEN = "refresh_token";
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
	 * The JSON key for the time the token expires.
	 */
	public static final String JSON_KEY_REFRESHED = "refreshed";
	
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
	 * The user-name of the user to whom the token applies.
	 */
	@JsonProperty(User.JSON_KEY_USERNAME)
	private final String username;
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
	private boolean valid;
	/**
	 * Whether or not a token is has been used to create a new token, in which
	 * case it should no longer be considered valid.
	 */
	@JsonProperty(JSON_KEY_REFRESHED)
	private boolean refreshed;
	
	/**
	 * Creates a new authentication token for a user.
	 * 
	 * @param user
	 *        The user about whom the authentication token should apply.
	 * 
	 * @throws IllegalArgumentException
	 *         The user is null.
	 */
	public AuthenticationToken(
		final User user)
		throws IllegalArgumentException {
		
		// Pass through to the builder constructor.
		this(
			getRandomId(),
			getRandomId(),
			((user == null) ? null : user.getUsername()),
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
	public AuthenticationToken(
		final AuthenticationToken oldToken)
		throws IllegalArgumentException {
		
		// Pass through to the builder constructor.
		this(
			getRandomId(),
			getRandomId(),
			((oldToken == null) ? null : oldToken.getUsername()),
			System.currentTimeMillis(),
			System.currentTimeMillis() + AUTH_TOKEN_LIFETIME,
			true,
			null);
		
		// Mark the old token as having been used to create a new token.
		oldToken.refreshed = true;
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
	 * @param username
	 *        The user's user-name.
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
	 *         The token and/or user-name are null, the token is being granted
	 *         in the future, or the token is being granted after it has
	 *         expired.
	 */
	@JsonCreator
	public AuthenticationToken(
		@JsonProperty(JSON_KEY_ACCESS_TOKEN) final String accessToken,
		@JsonProperty(JSON_KEY_REFRESH_TOKEN) final String refreshToken,
		@JsonProperty(User.JSON_KEY_USERNAME) final String username,
		@JsonProperty(JSON_KEY_GRANTED) final long granted,
		@JsonProperty(JSON_KEY_EXPIRES) final long expires,
		@JsonProperty(JSON_KEY_VALID) final boolean valid,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion) 
		throws IllegalArgumentException {

		// Pass through to the builder constructor.
		this(
			accessToken,
			refreshToken,
			username,
			granted,
			expires,
			valid,
			internalVersion,
			null);
	}
	
	/**
	 * Builds the AuthenticationToken object.
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
	 *         The token and/or user-name are null, the token is being granted
	 *         in the future, or the token is being granted after it has
	 *         expired.
	 */
	private AuthenticationToken(
		final String accessToken,
		final String refreshToken,
		final String username,
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
		if(username == null) {
			throw new IllegalArgumentException("The user-name is null.");
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
		this.username = username;
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
	 * Returns the user-name of the user associated with this authentication
	 * token.
	 * 
	 * @return The user-name of the user associated with this authentication
	 *         token.
	 */
	public String getUsername() {
		return username;
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
		User user = UserBin.getInstance().getUser(username);
		
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
		return refreshed;
	}
	
	/**
	 * Returns whether or not this token is still valid.
	 * 
	 * @return Whether or not this token is still valid.
	 */
	public boolean isValid() {
		return ! (isExpired() || wasInvalidated() || wasRefreshed());
	}
	
	/**
	 * Sets the validity of this token to false.
	 */
	public void invalidate() {
		this.valid = false;
	}
}