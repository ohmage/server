package org.ohmage.bin;

import org.ohmage.domain.auth.AuthorizationCode;
import org.ohmage.domain.exception.InvalidArgumentException;

/**
 * <p>
 * The interface to the database-backed authorization code repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class AuthorizationCodeBin {
    /**
     * The instance of this AuthorizationTokenBin to use.
     */
    protected static AuthorizationCodeBin instance;

    /**
     * Default constructor.
     */
    protected AuthorizationCodeBin() {
        instance = this;
    }

    /**
     * Returns the singular instance of this class.
     *
     * @return The singular instance of this class.
     */
    public static AuthorizationCodeBin getInstance() {
        return instance;
    }

    /**
     * Stores an existing authorization code.
     *
     * @param code
     *        The code to be saved.
     *
     * @throws IllegalArgumentException
     *         The code is null.
     *
     * @throws InvalidArgumentException
     *         A code with the same unique identifier already exists.
     */
    public abstract void addCode(final AuthorizationCode code)
        throws IllegalArgumentException, InvalidArgumentException;

    /**
     * Retrieves the set of codes that the given user has accepted.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @return The set of codes that the user has approved.
     *
     * @throws IllegalArgumentException
     *         The user ID is null.
     */
    public abstract MultiValueResult<String> getCodes(final String userId)
        throws IllegalArgumentException;

    /**
     * Retrieves the {@link AuthorizationCode} based on the given code value.
     *
     * @param code
     *        The authorization code's value.
     *
     * @return The {@link AuthorizationCode} with the given value or null if no
     *         such code exists.
     *
     * @throws IllegalArgumentException
     *         The code is null.
     */
    public abstract AuthorizationCode getCode(final String code)
        throws IllegalArgumentException;

    /**
     * Updates an existing code.
     *
     * @param code
     *        The code to update.
     *
     * @throws IllegalArgumentException
     *         The code is null.
     */
    public abstract void updateCode(final AuthorizationCode code);
}