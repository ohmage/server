package org.ohmage.bin;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.user.User;

/**
 * <p>
 * The interface to the database-backed user repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class UserBin {
	/**
	 * The singular instance of this class.
	 */
	private static UserBin instance;

	/**
	 * Initializes the singleton instance to this.
	 */
	protected UserBin() {
		instance = this;
	}

	/**
	 * Retrieves the singleton instance of this class.
	 *
	 * @return The singleton instance of this class.
	 */
	public static final UserBin getInstance() {
		return instance;
	}

	/**
	 * Adds a new user to the repository.
	 *
	 * @param user
	 *        The user to add.
	 *
	 * @throws IllegalArgumentException
	 *         The user is null.
	 *
	 * @throws InvalidArgumentException
	 *         A user with the given user-name already exists.
	 */
	public abstract void addUser(
		final User user)
		throws IllegalArgumentException, InvalidArgumentException;

	/**
	 * Retrieves the user with the given user-name.
	 *
	 * @param username
	 *        The user-name of the desired user.
	 *
	 * @return The desired user or null if no such user exists.
	 *
	 * @throws IllegalArgumentException
	 *         The user is null.
	 */
	public abstract User getUser(
		final String username)
		throws IllegalArgumentException;

	/**
	 * Retrieves the user that has been authenticated with a provider and had
	 * the given user ID returned.
	 *
	 * @param providerId
	 *        The provider's unique identifier.
	 *
	 * @param userId
	 *        The user's unique identifier according to the provider.
	 *
	 * @return The User object that corresponds with the given parameters or
	 *         null if on such user exists.
	 *
	 * @throws IllegalArgumentException
	 *         A parameter is null.
	 */
	public abstract User getUserFromProvider(
		final String providerId,
		final String userId)
		throws IllegalArgumentException;

    /**
     * Retrieves the user based on their activation ID.
     *
     * @param activationId
     *        The activation ID.
     *
     * @return The user with the given activation ID or null if the activation
     *         ID is unknown.
     *
     * @throws IllegalArgumentException
     *         The activation ID is null.
     */
    public abstract User getUserFromActivationId(
        final String activationId)
        throws IllegalArgumentException;

	/**
	 * Updates the user in the database.
	 *
	 * @param user
	 *        The updated User object.
	 *
	 * @throws IllegalArgumentException
	 *         The user is null.
	 */
	public abstract void updateUser(
		final User user)
		throws IllegalArgumentException;

	/**
	 * Disables a user's account.
	 *
	 * @param username
	 *        The user-name of the user's account.
	 *
	 * @throws IllegalArgumentException
	 *         The user is null.
	 */
	public abstract void disableUser(
		final String username)
		throws IllegalArgumentException;
}