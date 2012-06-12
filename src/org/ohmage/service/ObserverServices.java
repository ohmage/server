package org.ohmage.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
	public void verifyNewObserverIsValid(
			final Observer observer)
			throws ServiceException {
		
		if(observer == null) {
			throw new ServiceException(
				"The observer is null.");
		}
		
		/*
		try {
			// Get the current observer.
			Observer currentObserver = getObserver(observer.getId(), null);
		
			// Compare the observer versions. If the new version is less than or
			// equal to the existing version, then this is not a valid update 
			// attempt.
			if(observer.getVersion() <= currentObserver.getVersion()) {
				throw new ServiceException(
					ErrorCode.OBSERVER_INVALID_VERSION,
					"The new observer's version must increase: " +
						observer.getVersion());
			}
			
			// Compare each of the streams.
			for(Stream stream : observer.getStreams().values()) {
				
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		*/
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
						"ID: " + observerId + 
						"Version: " + observerVersion);
			}
			
			return result;
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
			final long streamVersion)
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
						data.remove(dataStream);
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
	 * @param numToSkip The number of data points to skip. Optional.
	 * 
	 * @param numToReturn The number of data points to return. Optional.
	 * 
	 * @return A collection of data points that match the query.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Collection<DataStream> getStreamData(
			final Stream stream,
			final String username,
			final String observerId,
			final Long observerVersion,
			final DateTime startDate,
			final DateTime endDate,
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
					numToSkip,
					numToReturn);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}