package org.ohmage.query;

import java.util.Collection;

import org.ohmage.domain.DataStream;
import org.ohmage.domain.Observer;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;

public interface IObserverQueries {
	/**
	 * Creates a new observer in the system and associates it with a user.
	 * 
	 * @param username The user's username.
	 * 
	 * @param observer The observer.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public void createObserver(
		final String username,
		final Observer observer) 
		throws DataAccessException;
	
	/**
	 * Returns whether or not an observer with the given ID already exists.
	 * 
	 * @param observerId The observer's unique identifier.
	 * 
	 * @return Whether or not the observer exists.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public boolean doesObserverExist(
		final String observerId) 
		throws DataAccessException;
	
	/**
	 * Retrieves the observer.
	 * 
	 * @param id The observer's unique identifier.
	 * 
	 * @param version The observer's version.
	 * 
	 * @return The Observer or null if no observer with that ID/version exists.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Observer getObserver(
		final String id, 
		final long version) 
		throws DataAccessException;
	
	/**
	 * Stores the data stream data.
	 * 
	 * @param username The user who is uploading the data.
	 * 
	 * @param data The data to be stored.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public void storeData(
		final String username,
		final Collection<DataStream> data)
		throws DataAccessException;
}
