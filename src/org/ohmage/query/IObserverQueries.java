package org.ohmage.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	 * Retrieves the ID of the creator of the observer or null if no such 
	 * observer exists.
	 * 
	 * @param observerId The observers unique identifier.
	 * 
	 * @return The owner of this observer.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public String getOwner(
		final String observerId)
		throws DataAccessException;
	
	/**
	 * Retrieves the observer.
	 * 
	 * @param id The observer's unique identifier.
	 * 
	 * @param version The observer's version. If this is null, the latest 
	 * 				  version is returned.
	 * 
	 * @return The Observer or null if no observer with that ID/version exists.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Observer getObserver(
		final String id, 
		final Long version) 
		throws DataAccessException;
	
	/**
	 * Gathers all observers that match the given criteria. If all parameters
	 * are null, then all parameters visible to the user are returned.
	 * 
	 * @param id Limits the results to only those with this ID. Optional.
	 * 
	 * @param version Limits the results to only those with this version. 
	 * 				  Optional.
	 * 
	 * @param numToSkip The number of observers to skip for paging.
	 * 
	 * @param numToReturn The number of observers to return for paging.
	 * 
	 * @return The collection of observers limited by the parameters.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public List<Observer> getObservers(
		final String id,
		final Long version,
		final long numToSkip,
		final long numToReturn)
		throws DataAccessException;
	
	/**
	 * Returns the greatest version number for an observer.
	 * 
	 * @param id The observer's unique identifier.
	 * 
	 * @return The greatest observer version.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Long getGreatestObserverVersion(
		final String id) 
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
		final Long streamVersion) 
		throws DataAccessException;
	
	/**
	 * Retrieves the streams that match the given criteria. All parameters are
	 * optional.
	 * 
	 * @param username Limits the results to only those streams for which the   
	 * 				   user submitted some data. 
	 * 
	 * @param observerId Limits the results to only those whose observer has 
	 * 					 this ID.
	 * 
	 * @param observerVersion Limits the results to only those whose observer
	 * 						  has this version.
	 * 
	 * @param streamId Limits the results to only those streams that have this
	 * 				   ID.
	 * 
	 * @param streamVersion Limits the results to only those streams that have
	 * 						this version.
	 * 
	 * @param numToSkip The number of streams to skip.
	 * 
	 * @param numToReturn The number of streams to return.
	 * 
	 * @return A map of observer IDs to its respective their of streams.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Map<String, Collection<Observer.Stream>> getStreams(
		final String username,
		final String observerId,
		final Long observerVersion,
		final String streamId,
		final Long streamVersion,
		final long numToSkip,
		final long numToReturn)
		throws DataAccessException;
	
	/**
	 * Returns the greatest version number for a stream.
	 * 
	 * @param observerId The observer's unique identifier.
	 * 
	 * @param streamId The stream's unique identifier.
	 * 
	 * @return The greatest stream identifier.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Long getGreatestStreamVersion(
		final String observerId,
		final String streamId)
		throws DataAccessException;
	
	/**
	 * Returns a map of observer IDs to all versions of all of their streams.
	 * 
	 * @param numToSkip The number of streams to skip.
	 * 
	 * @param numToReturn The number of streams to return.
	 * 
	 * @return The map of observer IDs to their streams.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public Map<String, Collection<Stream>> getObserverIdToStreamsMap(
		final long numToSkip,
		final long numToReturn)
		throws DataAccessException;
	
	/**
	 * Compares a list of IDs to the existing IDs for a user for a stream and
	 * returns the collection of IDs that match.
	 * 
	 * @param username The user's username.
	 * 
	 * @param observerId The observer's unique identifier.
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
		final String observerId,
		final String streamId,
		final Collection<String> idsToCheck)
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
		final Observer observer,
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
	 * @param observerId The observer's unique identifier. Optional.
	 * 
	 * @param observerVersion The observer's version. Optional.
	 * 
	 * @param startDate The earliest data point to return. Optional.
	 * 
	 * @param endDate The latest point data point to return. Optional.
	 * 
	 * @param chronological If true, the values will be sorted chronologically.
	 * 						If false, the values will be sorted reverse
	 * 						chronologically. Required.
	 * 
	 * @param numToSkip The number of data points to skip. Required.
	 * 
	 * @param numToReturn The number of data points to return. Required.
	 * 
	 * @return A collection of data points that match the query.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public List<DataStream> readData(
		final Stream stream,
		final String username,
		final String observerId,
		final Long observerVersion,
		final DateTime startDate,
		final DateTime endDate,
		final boolean chronological,
		final long numToSkip,
		final long numToReturn) 
		throws DataAccessException;
	
	/**
	 * Updates an observer.
	 * 
	 * @param username The username of the user that is updating the observer.
	 * 
	 * @param observer The new observer.
	 * 
	 * @param unchangedStreamIds The IDs of the streams in the new observer 
	 * 							 whose version didn't change and their version.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public void updateObserver(
		final String username,
		final Observer observer,
		final Map<String, Long> unchangedStreamIds)
		throws DataAccessException;
}