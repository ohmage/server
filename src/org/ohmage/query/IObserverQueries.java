package org.ohmage.query;

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
}
