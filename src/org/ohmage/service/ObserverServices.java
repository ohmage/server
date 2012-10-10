package org.ohmage.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.DataStream.MetaData;
import org.ohmage.domain.Observer;
import org.ohmage.domain.Observer.Stream;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
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
	
	/**
	 * Verifies that a user is allowed to update an observer.
	 * 
	 * @param username The user's username.
	 * 
	 * @param observerId The observer's unique identifier.
	 * 
	 * @throws ServiceException The user is not allowed to update the observer
	 * 							or there was an error.
	 */
	public void verifyUserCanUpdateObserver(
			final String username,
			final String observerId)
			throws ServiceException {
		
		try {
			String owner = observerQueries.getOwner(observerId);
			// If there is no owner, that is because the stream doesn't exist.
			if(owner == null) {
				throw new ServiceException(
					ErrorCode.OBSERVER_INSUFFICIENT_PERMISSIONS,
					"An observer with the given ID does not exist.");
			}
			// If the requester is not the owner, then they do not have 
			// permission to update it.
			// If we open up the ACLs to allow others to update the observer,
			// this check would be altered and this is where those ACLs would
			// be applied.
			else if(! owner.equals(username)) {
				throw new ServiceException(
					ErrorCode.OBSERVER_INSUFFICIENT_PERMISSIONS,
					"The requester is not the owner of the observer.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a new observer conforms to the requirements of the 
	 * existing observer.
	 * 
	 * @param observer The observer that must, at the very least, have an
	 * 				   increased version number and may contain other fixes.
	 * 
	 * @throws ServiceException The observer is invalid as a new observer or
	 * 							there was an error.
	 */
	public Map<String, Long> verifyNewObserverIsValid(
			final Observer observer)
			throws ServiceException {
		
		if(observer == null) {
			throw new ServiceException(
				"The observer is null.");
		}
		
		try {
			// Compare the observer versions. If the new version is less than
			// or equal to the existing version, then this is not a valid 
			// update attempt.
			String observerId = observer.getId();
			if(observer.getVersion() <= 
				observerQueries.getGreatestObserverVersion(observerId)) {
				
				throw new ServiceException(
					ErrorCode.OBSERVER_INVALID_VERSION,
					"The new observer's version must increase: " +
						observer.getVersion());
			}
			
			// The set of stream IDs whose version did not increase. 
			Map<String, Long> result = new HashMap<String, Long>();
			
			// Compare each of the streams.
			for(Stream stream : observer.getStreams().values()) {
				// Get the stream's version.
				Long streamVersion =
					observerQueries.getGreatestStreamVersion(
						observer.getId(), 
						stream.getId());
				
				// Get the new stream's version.
				long newStreamVersion = stream.getVersion();
				
				// If the stream didn't exist before, it is fine.
				if(streamVersion == null) {
					continue;
				}
				// If the new stream's version is less than the existing 
				// stream's version, that is an error.
				else if(newStreamVersion < streamVersion) {
					throw new ServiceException(
						ErrorCode.OBSERVER_INVALID_STREAM_VERSION,
						"The version of this stream, '" +
							stream.getId() +
							"', is less than the existing stream's version, '" +
							streamVersion +
							"': " + stream.getVersion());
				}
				// If the version didn't change, we add it to the set of stream
				// IDs.
				else if(newStreamVersion == streamVersion) {
					result.put(stream.getId(), streamVersion);
				}
				// Otherwise, the stream ID increased, in which case a new 
				// stream entry will be created.
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the observer.
	 * 
	 * @param observerId The observer's unique identifier.
	 * 
	 * @return The observer.
	 * 
	 * @throws ServiceException The observer doesn't exist.
	 */
	public Observer getObserver(
			final String observerId,
			final Long observerVersion) 
			throws ServiceException {
		
		try {
			Observer result = 
				observerQueries.getObserver(observerId, observerVersion);
			
			if(result == null) {
				throw new ServiceException(
					ErrorCode.OBSERVER_INVALID_ID,
					"No such observer exists: " + 
						"ID: " + observerId + ", " + 
						"Version: " + observerVersion);
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
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
	public Collection<Observer> getObservers(
			final String id,
			final Long version,
			final long numToSkip,
			final long numToReturn) 
			throws ServiceException {
		
		try {
			return 
				observerQueries
					.getObservers(id, version, numToSkip, numToReturn);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
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
	 * @throws ServiceException There was an error.
	 */
	public Observer.Stream getStream(
			final String observerId,
			final String streamId,
			final Long streamVersion)
			throws ServiceException {
		
		try {
			return 
				observerQueries.getStream(observerId, streamId, streamVersion);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
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
	 * @return A map of observer IDs to their respective set of streams.
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
			throws ServiceException {
		
		try {
			return 
				observerQueries.getStreams(
					username,
					observerId, 
					observerVersion,
					streamId, 
					streamVersion,
					numToSkip,
					numToReturn);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a map of observer IDs to all versions of all of their streams.
	 * 
	 * @param The number of streams to skip.
	 * 
	 * @param The number of streams to return.
	 * 
	 * @return The map of observer IDs to their streams.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Map<String, Collection<Stream>> getObserverIdToStreamsMap(
			final long numToSkip,
			final long numToReturn) 
			throws ServiceException {
		
		try {
			return 
				observerQueries
					.getObserverIdToStreamsMap(numToSkip, numToReturn);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Validates that the uploaded data is valid by comparing it to its stream
	 * schema and creating DataStream objects.
	 * 
	 * @param observer The observer that contains the streams.
	 * 
	 * @param data The data to validate.
	 * 
	 * @return A collection of DataStreams where each stream represents a 
	 * 		   different piece of data.
	 * 
	 * @throws ServiceException The data was invalid.
	 */
	public Collection<DataStream> validateData(
			final Observer observer,
			final JsonParser data)
			throws ServiceException {
		
		JsonNode nodes;
		try {
			nodes = data.readValueAsTree();
		}
		catch(JsonProcessingException e) {
			throw new ServiceException(
				ErrorCode.OBSERVER_INVALID_STREAM_DATA,
				"The data was not well-formed JSON.",
				e);
		}
		catch(IOException e) {
			throw new ServiceException(
				ErrorCode.OBSERVER_INVALID_STREAM_DATA,
				"Could not read the data from the parser.",
				e);
		}
		int numNodes = nodes.size();
		
		Collection<DataStream> result = new ArrayList<DataStream>(numNodes);
		for(int i = 0; i < numNodes; i++) {
			try {
				result.add(observer.getDataStream(nodes.get(i)));
			}
			catch(DomainException e) {
				throw new ServiceException(
					ErrorCode.OBSERVER_INVALID_STREAM_DATA,
					"The data was malformed: " + e.getMessage(),
					e);
			}
		}
		
		
		return result;
	}
	
	/**
	 * Prunes the duplicates from the collection of data elements. A duplicate
	 * is defined as a point with an ID whose ID already exists for the given
	 * user and for the associated stream. This will not remove duplicates in
	 * a single upload.
	 *  
	 * @param username The username of the user that will own these points.
	 * 
	 * @param observerId The observer's unique identifier.
	 * 
	 * @param data The data that has been uploaded.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public void removeDuplicates(
			final String username,
			final String observerId,
			final Collection<DataStream> data)
			throws ServiceException {
		
		try {
			// Get the IDs for each stream from this upload's data.
			Map<String, Collection<String>> uploadIds = 
				new HashMap<String, Collection<String>>();
			for(DataStream dataStream : data) {
				MetaData dataStreamMetaData = dataStream.getMetaData();
				
				if(dataStreamMetaData != null) {
					String id = dataStreamMetaData.getId();
				
					if(id != null) {
						Stream stream = dataStream.getStream();
						
						Collection<String> streamIds = 
							uploadIds.get(stream.getId());
						if(streamIds == null) {
							streamIds = new LinkedList<String>();
							uploadIds.put(stream.getId(), streamIds);
						}
						streamIds.add(id);
					}
				}
			}
			
			// Get the existing IDs for each stream that are also in this 
			// upload's IDs.
			Collection<String> duplicateIds = new HashSet<String>();
			for(String streamId : uploadIds.keySet()) {
				duplicateIds.addAll( 
					observerQueries.getDuplicateIds(
						username,
						observerId,
						streamId,
						uploadIds.get(streamId)));
			}
			
			// Remove any of this upload's IDs that already exist.
			Iterator<DataStream> dataIter = data.iterator();
			while(dataIter.hasNext()) {
				DataStream dataStream = dataIter.next();
				MetaData dataStreamMetaData = dataStream.getMetaData();
				
				if(dataStreamMetaData != null) {
					String id = dataStreamMetaData.getId();
				
					if((id != null) && (duplicateIds.contains(id))) {
						dataIter.remove();
					}
				}
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Stores the data stream data.
	 * 
	 * @param username The user who is uploading the data.
	 * 
	 * @param observer The observer to which the data belong.
	 * 
	 * @param data The data to be stored.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public void storeData(
			final String username,
			final Observer observer,
			final Collection<DataStream> data) 
			throws ServiceException {
		
		try {
			observerQueries.storeData(username, observer, data);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Retrieves the data for a stream.
	 * 
	 * @param stream The Stream object for the stream whose data is in 
	 * 				 question. Required.
	 * 
	 * @param username The username of the user to which the data must belong.
	 * 				   Required.
	 * 
	 * @param observerId The observer's unique identifier. Required.
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
	 * @return A list of data points in chronological order that match the 
	 * 		   query.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public List<DataStream> getStreamData(
			final Stream stream,
			final String username,
			final String observerId,
			final Long observerVersion,
			final DateTime startDate,
			final DateTime endDate,
			final boolean chronological,
			final long numToSkip,
			final long numToReturn) 
			throws ServiceException {
		
		try {
			return 
				observerQueries.readData(
					stream,
					username,
					observerId,
					observerVersion,
					startDate,
					endDate,
					chronological,
					numToSkip,
					numToReturn);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
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
	 * @throws ServiceException There was an error.
	 */
	public void updateObserver(
			final String username,
			final Observer observer,
			final Map<String, Long> unchangedStreamIds)
			throws ServiceException {

		try {
			observerQueries.updateObserver(
				username,
				observer,
				unchangedStreamIds);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}