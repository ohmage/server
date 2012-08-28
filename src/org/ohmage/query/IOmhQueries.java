package org.ohmage.query;

import java.util.Map;

import org.ohmage.exception.DataAccessException;

/**
 * The queries for OMH information.
 *
 * @author John Jenkins
 */
public interface IOmhQueries {
	/**
	 * Retrieves the credentials for a given domain.
	 * 
	 * @param domain The domain for which the desired credentials.
	 * 
	 * @return A map of key-value pairs that describe the user's credentials.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Map<String, String> getCredentials(
		final String domain)
		throws DataAccessException;
}
