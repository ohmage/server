package org.ohmage.bin;

import org.ohmage.domain.auth.OauthClient;
import org.ohmage.domain.exception.InvalidArgumentException;

/**
 * <p>
 * The interface to the database-based OAuth client repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class OauthClientBin {
    /**
     * The singular instance of this class.
     */
    private static OauthClientBin instance;

    /**
     * Initializes the singleton instance to this.
     */
    protected OauthClientBin() {
        instance = this;
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static final OauthClientBin getInstance() {
        return instance;
    }

    /**
     * Adds a new OAuth client to the repository.
     *
     * @param oauthClient
     *        The OAuth client to add.
     *
     * @throws IllegalArgumentException
     *         The OAuth client is null.
     *
     * @throws InvalidArgumentException
     *         A OAuth client with the same ID already exists.
     */
    public abstract void addOauthClient(final OauthClient oauthClient)
        throws IllegalArgumentException, InvalidArgumentException;

    /**
     * Retrieves the set of client unique identifiers that are owned by the
     * given user.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @return The set of client unique identifiers that are owned by the given
     *         user.
     *
     * @throws IllegalArgumentException
     *         The user's ID is null.
     */
    public abstract MultiValueResult<String> getClientIds(final String userId)
        throws IllegalArgumentException;

    /**
     * Returns a OauthClient object based on the given ID.
     *
     * @param oauthClientId
     *        The unique identifier for the OAuth client.
     *
     * @return A OauthClient object that represents this OAuth client.
     *
     * @throws IllegalArgumentException
     *         The OAuth client ID is null.
     */
    public abstract OauthClient getOauthClient(final String oauthClientId)
        throws IllegalArgumentException;
}