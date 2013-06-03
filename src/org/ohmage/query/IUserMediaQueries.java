package org.ohmage.query;

import java.util.UUID;

import org.ohmage.exception.DataAccessException;

public interface IUserMediaQueries {
	/**
	 * Gets the username of the user that created this media.
	 * 
	 * @param id The media's unique identifier.
	 * 
	 * @return The user's username.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	String getMediaOwner(final UUID id) throws DataAccessException;
}
