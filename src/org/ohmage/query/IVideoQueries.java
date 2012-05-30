package org.ohmage.query;

import java.net.URL;
import java.util.UUID;

import org.ohmage.exception.DataAccessException;

/**
 * The interfaces for all of the queries against videos.
 *
 * @author John Jenkins
 */
public interface IVideoQueries {
	/**
	 * Gets the URL for the video.
	 * 
	 * @param videoId The video's unique identifier.
	 * 
	 * @return The video's URL or null if no such video exists.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	URL getVideoUrl(final UUID videoId) throws DataAccessException;
}
