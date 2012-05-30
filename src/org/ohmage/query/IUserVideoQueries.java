package org.ohmage.query;

import java.util.UUID;

import org.ohmage.exception.DataAccessException;

public interface IUserVideoQueries {
	/**
	 * Gets the username of the user that created this video.
	 * 
	 * @param videoId The video's unique identifier.
	 * 
	 * @return The user's username.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	String getVideoOwner(final UUID videoId) throws DataAccessException;
}
