package org.ohmage.bin;

import org.ohmage.domain.auth.AuthorizationToken;
import org.ohmage.domain.exception.InvalidArgumentException;

/**
 * <p>
 * The interface to the database-backed authorization token repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class AuthorizationTokenBin {
	/**
	 * The instance of this AuthorizationTokenBin to use.
	 */
	protected static AuthorizationTokenBin instance;

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
	 * Stores an existing authorization token.
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
		throws IllegalArgumentException, InvalidArgumentException;

	/**
	 * Retrieves the {@link AuthorizationToken} object based on the given
	 * access token string.
	 *
	 * @param accessToken
	 *        The authorization token.
	 *
	 * @return The {@link AuthorizationToken} or null if the authorization
	 *         token string does not exist or is expired.
	 *
	 * @throws IllegalArgumentException
	 *         The token is null.
	 */
	public abstract AuthorizationToken getTokenFromAccessToken(
		final String accessToken)
		throws IllegalArgumentException;

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
        throws IllegalArgumentException;

    /**
     * Retrieves the oldest {@link AuthorizationToken} object associated with
     * the authorization code.
     *
     * @param authorizationCode
     *        The authorization code that corresponds to this
     *
     * @return The {@link AuthorizationToken} or null if the authorization code
     *         string does not exist.
     *
     * @throws IllegalArgumentException
     *         The authorization code is null.
     */
    public abstract AuthorizationToken getTokenFromAuthorizationCode(
        final String authorizationCode)
        throws IllegalArgumentException;

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