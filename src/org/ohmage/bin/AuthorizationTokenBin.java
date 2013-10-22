package org.ohmage.bin;

import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.exception.InvalidArgumentException;

/**
 * <p>
 * The collection of authorization tokens.
 * </p>
 * 
 * @author John Jenkins
 */
public abstract class AuthorizationTokenBin {
	/**
	 * The instance of this AuthorizationTokenBin to use.
	 */
	private static AuthorizationTokenBin instance;

	/**
	 * Default constructor.
	 */
	protected AuthorizationTokenBin() {
		instance = this;
	}

	/**
	 * Returns the singular instance of this class.
	 * 
	 * @return The singular instance of this class.
	 */
	public static AuthorizationTokenBin getInstance() {
		return instance;
	}

	/**
	 * Stores an existing authorization code.
	 * 
	 * @param token
	 *        The code to be saved.
	 * 
	 * @throws IllegalArgumentException
	 *         The token is null.
	 * 
	 * @throws InvalidArgumentException
	 *         A token with the same value already exists.
	 */
	public abstract void addToken(
		final AuthorizationToken token)
		throws IllegalArgumentException, InvalidArgumentException;

	/**
	 * Retrieves the {@link AuthorizationToken} object based on the given
	 * access token string. It will still be returned even if it has expired.
	 * 
	 * @param accessToken
	 *        The authorization token's access token string.
	 * 
	 * @return The {@link AuthorizationToken} or null if no authorization token
	 *         has the given access token associated with it.
	 * 
	 * @throws IllegalArgumentException
	 *         The token is null.
	 */
	public abstract AuthorizationToken getTokenFromAccessToken(
		final String accessToken)
		throws IllegalArgumentException;

	/**
	 * Retrieves the {@link AuthorizationToken} object based on the given
	 * refresh token string. It will still be returned even if it has expired.
	 * 
	 * @param refreshToken
	 *        The authorization token's refresh token string.
	 * 
	 * @return The {@link AuthorizationToken} or null if no authorization token
	 *         has the given refresh token associated with it.
	 * 
	 * @throws IllegalArgumentException
	 *         The token is null.
	 */
	public abstract AuthorizationToken getTokenFromRefreshToken(
		final String refreshToken)
		throws IllegalArgumentException;
}