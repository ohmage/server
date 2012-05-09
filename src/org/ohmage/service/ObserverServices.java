package org.ohmage.service;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Observer;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IObserverQueries;

public class ObserverServices {
	private static ObserverServices instance;
	private IObserverQueries observerQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iObserverQueries is null
	 */
	private ObserverServices(final IObserverQueries iObserverQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		
		if(iObserverQueries == null) {
			throw new IllegalArgumentException("An instance of IObserverQueries is required.");
		}
		
		observerQueries = iObserverQueries;
		instance = this;
	}
	
	/**
	 * The instance of this service.
	 * 
	 * @return  Returns the singleton instance of this class.
	 */
	public static ObserverServices instance() {
		return instance;
	}
	
	/**
	 * Creates a new observer in the system and associates it with a user.
	 * 
	 * @param 
	 * 
	 * @param observer The observer.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public void createObserver(
			final String username,
			final Observer observer) 
			throws ServiceException {
		
		try {
			observerQueries.createObserver(username, observer);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to create an observer.
	 * 
	 * @param username The user's username.
	 * 
	 * @param observerId The observer's unique identifier.
	 * 
	 * @throws ServiceException The user is not allowed to create the observer
	 * 							or there was an error.
	 */
	public void verifyUserCanCreateObserver(
			final String username,
			final String observerId)
			throws ServiceException {
		
		try {
			// First, the observer cannot already exist.
			if(observerQueries.doesObserverExist(observerId)) {
				throw new ServiceException(
					ErrorCode.OBSERVER_INSUFFICIENT_PERMISSIONS,
					"An observer with the given ID already exists.");
			}
			
			// Other than that, anyone is allowed to create them.
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}
