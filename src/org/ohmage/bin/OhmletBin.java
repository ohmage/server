package org.ohmage.bin;

import java.util.Collection;
import java.util.Set;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.ohmlet.Ohmlet;

/**
 * <p>
 * The interface to the database-backed ohmlet repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class OhmletBin {
	/**
	 * The instance of this OhmletBin to use.
	 */
	protected static OhmletBin instance;

	/**
	 * Default constructor.
	 */
	protected OhmletBin() {
		instance = this;
	}

	/**
	 * Returns the singular instance of this class.
	 *
	 * @return The singular instance of this class.
	 */
	public static OhmletBin getInstance() {
		return instance;
	}

	/**
	 * Stores a new ohmlet.
	 *
	 * @param ohmlet
	 *        The ohmlet to be saved.
	 *
	 * @throws IllegalArgumentExceptoin
	 *         The ohmlet is null.
	 *
	 * @throws InvalidArgumentException
	 *         Another ohmlet already exists with the same ID.
	 */
	public abstract void addOhmlet(
		final Ohmlet ohmlet)
		throws IllegalArgumentException, InvalidArgumentException;

    /**
     * Returns a list of the visible ohmlet IDs.
     *
     * @param userId
     *        The unique identifier for the user making the request or null if
     *        the request is being made anonymously.
     *
     * @param query
     *        A value that should appear in either the name or description.
     *
     * @param numToSkip
     *        The number of ohmlet IDs to skip.
     *
     * @param numToReturn
     *        The number of ohmlet IDs to return.
     *
     * @return A list of the visible ohmlet IDs.
     */
	public abstract MultiValueResult<String> getOhmletIds(
		final String userId,
		final String query,
        final long numToSkip,
        final long numToReturn);

    /**
     * Returns all of the ohlmet IDs where the user can read stream data about
     * other users.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @param streamId
     *        The stream's unique identifier.
     *
     * @param streamVersion
     *        The stream's version.
     *
     * @param allowNull
     *        Allow the stream's reference to have a null version.
     *
     * @return The list of ohmlet unique identifiers where the user has
     *         sufficient permissions to read data about other users.
     *
     * @throws IllegalArgumentException
     *         The user ID or stream ID are null.
     */
    public abstract Set<String> getOhmletIdsWhereUserCanReadStreamData(
        final String userId,
        final String streamId,
        final long streamVersion,
        final boolean allowNull)
        throws IllegalArgumentException;

    /**
     * Returns all of the ohlmet IDs where the user can read survey responses
     * from other users.
     *
     * @param userId
     *        The user's unique identifier.
     *
     * @param streamId
     *        The survey's unique identifier.
     *
     * @param streamVersion
     *        The survey's version.
     *
     * @param allowNull
     *        Allow the survey's reference to have a null version.
     *
     * @return The list of ohmlet unique identifiers where the user has
     *         sufficient permissions to read data about other users.
     *
     * @throws IllegalArgumentException
     *         The user ID or stream ID are null.
     */
    public abstract Set<String> getOhmletIdsWhereUserCanReadSurveyResponses(
        final String userId,
        final String streamId,
        final long streamVersion,
        final boolean allowNull)
        throws IllegalArgumentException;

	/**
	 * Returns a Ohmlet object for the desired ohmlet.
	 *
	 * @param ohmletId
	 *        The unique identifier for the ohmlet.
	 *
	 * @return A Ohmlet object that represents this ohmlet.
	 *
	 * @throws IllegalArgumentException
	 *         The ohmlet ID is null.
	 */
	public abstract Ohmlet getOhmlet(
		final String ohmletId)
		throws IllegalArgumentException;

    /**
     * Returns the set of member IDs for the members of all of the ohmlets.
     *
     * @param ohmletIds
     *        The ohmlets' unique identifier.
     *
     * @return The set of member IDs.
     *
     * @throws IllegalArgumentException
     *         The collection of ohmlet IDs is null.
     */
	public abstract Set<String> getMemberIds(
	    final Collection<String> ohmletIds)
	    throws IllegalArgumentException;

	/**
	 * Updates the ohmlet in the database.
	 *
	 * @param ohmlet
	 *        The updated Ohmlet object.
	 *
	 * @throws IllegalArgumentException
	 *         The ohmlet is null.
	 */
	public abstract void updateOhmlet(
		final Ohmlet ohmlet)
		throws IllegalArgumentException;

	/**
	 * Deletes the given ohmlet.
	 *
	 * @param ohmletId
	 *        The unique identifier for the ohmlet to delete.
	 *
	 * @throws IllegalArgumentException
	 *         The ohmlet identifier is null.
	 */
	public abstract void deleteOhmlet(
		final String ohmletId)
		throws IllegalArgumentException;
}