package org.ohmage.query;

import java.util.Collection;

import org.joda.time.DateTime;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.Observer;
import org.ohmage.domain.Observer.Stream;
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
	 * Retrieves the stream.
	 * 
	 * @param observerId The observer's unique identifier.
	 * 
	 * @param streamId The stream's unique identifier.
	 * 
	 * @param streamVersion The stream's version.
	 * 
	 * @return The Stream or null if no stream with that observer/ID/version 
	 * 		   exists.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Observer.Stream getStream(
		final String observerId,
		final String streamId, 
		final long streamVersion) 
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

	/**
	 * Retrieves the data for a stream.
	 * 
	 * @param stream The Stream object for the stream whose data is in 
	 * 				 question. Required.
	 * 
	 * @param username The username of the user to which the data must belong.
	 * 				   Required.
	 * 
	 * @param observerVersion The observer's version. Optional.
	 * 
	 * @param startDate The earliest data point to return. Optional.
	 * 
	 * @param endDate The latest point data point to return. Optional.
	 * 
	 * @param numToSkip The number of data points to skip. Optional.
	 * 
	 * @param numToReturn The number of data points to return. Optional.
	 * 
	 * @return A collection of data points that match the query.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Collection<DataStream> readData(
		final Stream stream,
		final String username,
		final String observerId,
		final Long observerVersion,
		final DateTime startDate,
		final DateTime endDate,
		final long numToSkip,
		final long numToReturn) 
		throws DataAccessException;
	
	/**
	 * Compares a list of IDs to the existing IDs for a user for a stream and
	 * returns the collection of IDs that match.
	 * 
	 * @param username The user's username.
	 * 
	 * @param streamId The stream's unique identifier.
	 * 
	 * @param idsToCheck The collection of IDs to compare against.
	 * 
	 * @return The collection of IDs that are already stored for this user for
	 * 		   this stream and were in the supplied list.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Collection<String> getDuplicateIds(
		final String username,
		final String streamId,
		final Collection<String> idsToCheck)
		throws DataAccessException;
}