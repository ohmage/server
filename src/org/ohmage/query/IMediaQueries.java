package org.ohmage.query;

import java.net.URL;
import java.util.UUID;

import org.ohmage.exception.DataAccessException;

/**
 * The interfaces for all of the queries against media.
 *
 * @author John Jenkins
 * @author Hongsuda T.
 */
public interface IMediaQueries {
	
	/**
	 * Checks whether the media already exists. 
	 * 
	 * @param id The media's unique identifier.
	 * 
	 * @return true if the media exists, false if the media doesn't exist.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Boolean getMediaExists(UUID id) throws DataAccessException;
	
	/**
	 * Gets the URL for the media.
	 * 
	 * @param id The media's unique identifier.
	 * 
	 * @return The media's URL or null if no such media exists.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	URL getMediaUrl(final UUID id) throws DataAccessException;
}
