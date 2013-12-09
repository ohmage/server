package org.ohmage.bin;

import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.exception.InvalidArgumentException;

/**
 * <p>
 * The interface to the database-backed authentication token repository.
 * </p>
 * 
 * @author John Jenkins
 */
public abstract class AuthenticationTokenBin {
	/**
	 * The instance of this AuthenticationTokenBin to use.
	 */
	protected static AuthenticationTokenBin instance;

	/**
	 * Default constructor.
	 */
	protected AuthenticationTokenBin() {
		instance = this;
	}

	/**
	 * Returns the singular instance of this class.
	 * 
	 * @return The singular instance of this class.
	 */
	public static AuthenticationTokenBin getInstance() {
		return instance;
	}

	/**
	 * Stores an existing authentication token.
	 * 
	 * @param token
	 *        The token to be saved.
	 * 
	 * @throws IllegalArgumentException
	 *         The token is null.
	 * 
	 * @throws InvalidArgumentException
	 *         A token with the same value already exists.
	 */
	public abstract void addToken(
		final AuthorizationToken token)
		throws IllegalArgumentException, IllegalStateException;

	/**
	 * Retrieves the {@link AuthorizationToken} object based on the given
	 * access token string.
	 * 
	 * @param accessToken
	 *        The authentication token.
	 * 
	 * @return The {@link AuthorizationToken} or null if the authentication
	 *         token string does not exist or is expired.
	 * 
	 * @throws IllegalArgumentException
	 *         The token is null.
	 */
	public abstract AuthorizationToken getTokenFromAccessToken(
		final String accessToken)
		throws IllegalArgumentException, IllegalStateException;

	/**
	 * Retrieves the {@link AuthorizationToken} object based on the given
	 * refresh token string.
	 * 
	 * @param refreshToken
	 *        The refresh token.
	 * 
	 * @return The {@link AuthorizationToken} or null if the refresh token
	 *         string does not exist.
	 * 
	 * @throws IllegalArgumentException
	 *         The token is null.
	 */
	public abstract AuthorizationToken getTokenFromRefreshToken(
		final String refreshToken)
		throws IllegalArgumentException, IllegalStateException;

	/**
	 * Updates an existing token.
	 * 
	 * @param token
	 *        The token to update.
	 * 
	 * @throws IllegalArgumentException
	 *         The token is null.
	 */
	public abstract void updateToken(final AuthorizationToken token);
}