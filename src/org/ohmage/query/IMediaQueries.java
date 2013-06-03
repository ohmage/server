package org.ohmage.query;

import java.net.URL;
import java.util.UUID;

import org.ohmage.exception.DataAccessException;

/**
 * The interfaces for all of the queries against media.
 *
 * @author John Jenkins
 */
public interface IMediaQueries {
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
