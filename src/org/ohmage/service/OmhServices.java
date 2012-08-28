package org.ohmage.service;

import java.util.Map;

import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IOmhQueries;

/**
 * The services for OMH queries.
 *
 * @author John Jenkins
 */
public class OmhServices {
	private static OmhServices instance;
	private IOmhQueries omhQueries;
	
	/**
	 * Privately instantiated via reflection.
	 * 
	 * @param iOmhQueries The instance of the OmhQueries.
	 * 
	 * @throws IllegalStateException This class has already been setup.
	 * 
	 * @throws IllegalArgumentException A parameter is invalid.
	 */
	private OmhServices(final IOmhQueries iOmhQueries) {
		if(instance != null) {
			throw new IllegalStateException(
				"An instance of this class already exists.");
		}
		
		if(iOmhQueries == null) {
			throw new IllegalArgumentException(
				"An instance of IOmhQueries is required.");
		}
		
		omhQueries = iOmhQueries;
		instance = this;
	}
	
	/**
	 * Returns the instance of this service.
	 * 
	 * @return The instance of this service.
	 */
	public static OmhServices instance() {
		return instance;
	}
	
	/**
	 * Retrieves all of the authentication credentials for a given domain.
	 * 
	 * @param domain The ID for the domain.
	 * 
	 * @return A map of key-value pairs that contains all of the credentials.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Map<String, String> getCredentials(
			final String domain) 
			throws ServiceException {
		
		try {
			return omhQueries.getCredentials(domain);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}