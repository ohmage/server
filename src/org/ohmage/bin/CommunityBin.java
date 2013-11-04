package org.ohmage.bin;

import java.util.List;

import org.ohmage.domain.Community;
import org.ohmage.domain.exception.InvalidArgumentException;

/**
 * <p>
 * The interface to the database-backed community repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class CommunityBin {
	/**
	 * The instance of this CommunityBin to use.
	 */
	protected static CommunityBin instance;

	/**
	 * Default constructor.
	 */
	protected CommunityBin() {
		instance = this;
	}

	/**
	 * Returns the singular instance of this class.
	 * 
	 * @return The singular instance of this class.
	 */
	public static CommunityBin getInstance() {
		return instance;
	}

	/**
	 * Stores a new community.
	 * 
	 * @param community
	 *        The community to be saved.
	 * 
	 * @throws IllegalArgumentExceptoin
	 *         The community is null.
	 * 
	 * @throws InvalidArgumentException
	 *         Another community already exists with the same ID.
	 */
	public abstract void addCommunity(
		final Community community)
		throws IllegalArgumentException, InvalidArgumentException;
	
	/**
	 * Returns a list of the visible community IDs.
	 * 
	 * @param query
	 *        A value that should appear in either the name or description.
	 * 
	 * @return A list of the visible community IDs.
	 */
	public abstract List<String> getCommunityIds(final String query);
	
	/**
	 * Returns a Community object for the desired community.
	 * 
	 * @param communityId
	 *        The unique identifier for the community.
	 * 
	 * @return A Community object that represents this community.
	 * 
	 * @throws IllegalArgumentException
	 *         The community ID is null.
	 */
	public abstract Community getCommunity(
		final String communityId)
		throws IllegalArgumentException;
}