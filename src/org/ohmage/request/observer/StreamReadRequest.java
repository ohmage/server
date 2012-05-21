package org.ohmage.request.observer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.Observer;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ObserverServices;
import org.ohmage.validator.ObserverValidators;

public class StreamReadRequest extends UserRequest {
	private static final Logger LOGGER = 
		Logger.getLogger(StreamReadRequest.class);
	
	/**
	 * The maximum number of records that can be returned.
	 */
	public static final long MAX_NUMBER_TO_RETURN = 100;
	
	/**
	 * This is being used to facilitate an n-ary tree.
	 *
	 * @author John Jenkins
	 */
	public static class ColumnNode<T> {
		private Map<T, ColumnNode<T>> node;
		
		/**
		 * Creates an empty node.
		 */
		public ColumnNode() {
			node = null;
		}
		
		/**
		 * Adds a sub-node to this node.
		 * 
		 * @param value The value of the sub-node.
		 */
		public void addNode(final T value) {
			if(node == null) {
				node = new HashMap<T, ColumnNode<T>>();
			}
			
			node.put(value, new ColumnNode<T>());
		}
		
		/**
		 * Checks if this node has a sub-node with the given value.
		 * 
		 * @param value The value to check for.
		 * 
		 * @return True if this node has a sub-node with the given value; 
		 * 		   false, otherwise.
		 */
		public boolean hasNode(final T value) {
			if(node == null) {
				return false;
			}
			
			return node.containsKey(value);
		}
		
		/**
		 * Returns the sub-node with the given value.
		 * 
		 * @param value The value.
		 * 
		 * @return The sub-node or null if no such sub-node exists.
		 */
		public ColumnNode<T> getNode(final T value) {
			if(node == null) {
				return null;
			}
			
			return node.get(value);
		}
		
		/**
		 * The values for this node's sub-nodes.
		 * 
		 * @return An unmodifiable collection of this node's sub-nodes. 
		 */
		public Collection<T> getValues() {
			if(node == null) {
				return Collections.emptyList();
			}
			
			return Collections.unmodifiableSet(node.keySet());
		}
	}
	
	// Part of the URI.
	private final String observerId;
	
	// Optional.
	private final Long observerVersion;
	
	// Required parameters.
	private final String streamId;
	private final Long streamVersion;
	
	// Optional parameters.
	private final DateTime startDate;
	private final DateTime endDate;
	private final ColumnNode<String> columnsRoot;
	
	// Optional parameters, but they must be given a value.
	private final long numToSkip;
	private final long numToReturn;
	
	private final Collection<DataStream> results;
	
	/**
	 * Creates a stream read request.
	 * 
	 * @param httpRequest The HTTP request.
	 */
	public StreamReadRequest(final HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		String tObserverId = null;
		Long tObserverVersion = null;
		String tStreamId = null;
		Long tStreamVersion = null;
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		ColumnNode<String> tColumnsRoot = null;
		long tNumToSkip = 0;
		long tNumToReturn = MAX_NUMBER_TO_RETURN;
		
		if(! isFailed()) {
			LOGGER.info("Creating a stream read request.");
			String[] t;
			
			try {
				String[] uriParts = httpRequest.getRequestURI().split("/");
				
				tObserverId = 
					ObserverValidators.validateObserverId(
						uriParts[uriParts.length - 1]);
				if(tObserverId == null) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_ID,
						"The observer's ID is missing.");
				}
				
				t = getParameterValues(InputKeys.OBSERVER_VERSION);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_VERSION,
						"Multiple observer versions were given: " +
							InputKeys.OBSERVER_VERSION);
				}
				else if(t.length == 1) {
					tObserverVersion = 
						ObserverValidators.validateObserverVersion(t[0]);
				}
				
				t = getParameterValues(InputKeys.STREAM_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_STREAM_ID,
						"Multiple stream IDs were given: " + 
							InputKeys.STREAM_ID);
				}
				else if(t.length == 1) {
					tStreamId = ObserverValidators.validateStreamId(t[0]);
				}
				if(tStreamId == null) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_STREAM_ID,
						"The stream ID is missing: " + InputKeys.STREAM_ID);
				}
				
				t = getParameterValues(InputKeys.STREAM_VERSION);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_STREAM_VERSION,
						"Multiple stream versions were given: " + 
							InputKeys.STREAM_VERSION);
				}
				else if(t.length == 1) {
					tStreamVersion = 
						ObserverValidators.validateStreamVersion(t[0]);
				}
				if(tStreamVersion == null) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_STREAM_VERSION,
						"The stream version is missing: " + 
							InputKeys.STREAM_VERSION);
				}
				
				t = getParameterValues(InputKeys.START_DATE);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.SERVER_INVALID_DATE,
						"Multiple start dates were given: " + 
							InputKeys.START_DATE);
				}
				else if(t.length == 1) {
					tStartDate = 
						ObserverValidators.validateDate(t[0]);
				}
				
				t = getParameterValues(InputKeys.END_DATE);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.SERVER_INVALID_DATE,
						"Multiple end dates were given: " + 
							InputKeys.END_DATE);
				}
				else if(t.length == 1) {
					tEndDate = 
						ObserverValidators.validateDate(t[0]);
				}
				
				t = getParameterValues(InputKeys.COLUMN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_COLUMN_LIST,
						"Multiple column lists were given: " + 
							InputKeys.COLUMN_LIST);
				}
				else if(t.length == 1) {
					tColumnsRoot = 
						ObserverValidators.validateColumnList(t[0]);
				}
				
				t = getParameterValues(InputKeys.NUM_TO_SKIP);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
						"Multiple skip counts were given: " + 
							InputKeys.NUM_TO_SKIP);
				}
				else if(t.length == 1) {
					tNumToSkip = ObserverValidators.validateNumToSkip(t[0]);
				}
				
				t = getParameterValues(InputKeys.NUM_TO_RETURN);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
						"Multiple return counts were given: " + 
							InputKeys.NUM_TO_RETURN);
				}
				else if(t.length == 1) {
					tNumToReturn = 
						ObserverValidators.validateNumToReturn(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		observerId = tObserverId;
		observerVersion = tObserverVersion;
		streamId = tStreamId;
		streamVersion = tStreamVersion;
		startDate = tStartDate;
		endDate = tEndDate;
		columnsRoot = tColumnsRoot;
		numToSkip = tNumToSkip;
		numToReturn = tNumToReturn;
		
		results = new LinkedList<DataStream>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a stream read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Retrieving the stream definition.");
			Observer.Stream stream = 
				ObserverServices.instance().getStream(
					observerId, 
					streamId, 
					streamVersion);
			
			if(stream == null) {
				LOGGER.info(
					"The stream doesn't exist, so we will return no data.");
				return;
			}
			
			LOGGER.info("Gathering the data.");
			results.addAll(
				ObserverServices.instance().getStreamData(
					stream,
					getUser().getUsername(),
					observerId,
					observerVersion,
					startDate,
					endDate,
					numToSkip,
					numToReturn));
			LOGGER.info("Returning " + results.size() + " points.");
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {

		// Check for failure.
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
			return;
		}
		
		// Refresh the token cookie.
		refreshTokenCookie(httpResponse);
		
		// Expire the response, but this may be a bad idea.
		expireResponse(httpResponse);
		
		// Set the content type to JSON.
		httpResponse.setContentType("application/json");
		
		// Connect a stream to the response.
		OutputStream outputStream;
		try {
			outputStream = getOutputStream(httpRequest, httpResponse);
		}
		catch(IOException e) {
			LOGGER.warn("Could not connect to the output stream.", e);
			return;
		}
		
		// Be sure to close the stream before leaving.
		try {
			// Create the generator that will stream to the requester.
			JsonGenerator generator;
			try {
				generator =
					(new JsonFactory()).createJsonGenerator(outputStream);
			}
			catch(IOException e) {
				LOGGER.error("Could not create the JSON generator.", e);
				return;
			}
			
			/*
			 * Example output:
			 * 
			 * 	{
			 * 		"result":"success",
			 * 		"data":[
			 * 			{
			 * 				"metadata":{},
			 * 				"data":{} // Data based on the columns.
			 * 			},
			 * 			...
			 * 		],
			 * 		"metadata":{
			 * 			"count":<A number representing the number of results.>,
			 * 			"prev":"<The URL for the previous set of results.>",
			 * 			"next":"<The URL for the next set of results.>"
			 * 		}
			 * 	}
			 */
			
			// Start the resulting object.
			generator.writeStartObject();
			
			// Add the result to the object.
			generator.writeObjectField("result", "success");
			
			// TODO: Add the meta-data.
			
			// Add a "data" key that is an array of the results.
			generator.writeArrayFieldStart("data");
			for(DataStream dataStream : results) {
				// Begin this data stream.
				generator.writeStartObject();
				
				// TODO: Write the meta-data.
				
				// TODO: Write the data.
				handleGeneric(generator, dataStream.getData(), columnsRoot);
				
				// End this data stream.
				generator.writeEndObject();
			}
			generator.writeEndArray();
			
			// End the overall object.
			generator.writeEndObject();
		}
		catch(JsonProcessingException e) {
			LOGGER.error("The JSON could not be processed.", e);
			return;
		}
		catch(IOException e) {
			LOGGER.error(
				"The response could no longer be writtent to the response",
				e);
			return;
		}
		finally {
			try {
				outputStream.flush();
			}
			catch(IOException e) {
				LOGGER.warn(
					"Could not flush the writer.",
					e);
			}

			try {
				outputStream.close();
			}
			catch(IOException e) {
				LOGGER.warn(
					"Could not close the writer.",
					e);
			}
		}
	}
	
	/**
	 * Feeds a generic object into an output stream and only includes the
	 * specified columns.
	 * 
	 * @param generator The output stream to feed the object into.
	 * 
	 * @param generic The object to be fed out.
	 * 
	 * @param columns The columns to restrict the output. If this is null, all
	 *				  data in the object will be output.
	 *
	 * @throws JsonGenerationException Could not serialize the data into JSON.
	 * 
	 * @throws IOException Could not write to the output stream.
	 */
	@SuppressWarnings("unchecked")
	private static void handleGeneric(
			final JsonGenerator generator,
			final Object generic,
			final ColumnNode<String> columns)
			throws JsonGenerationException, IOException {
		
		// If the object is null, then someone is requesting something that
		// never existed, so we can safely just ignore this.
		if(generic == null) {
			return;
		}
		
		// If it's an array, then process it as such.
		if(generic instanceof GenericData.Array) {
			handleArray(
				generator, 
				(GenericData.Array<GenericContainer>) generic,
				columns);
		}
		// If it's an object, then process it as such.
		else if(generic instanceof GenericData.Record) {
			handleObject(generator, (GenericData.Record) generic, columns);
		}
		// Otherwise, it must be a primitive type, so just add it to the 
		// result.
		else {
			generator.writeObject(generic);
		}
	}
	
	/**
	 * Feeds an array into an output stream and limits the objects of the array
	 * based on the given columns.
	 * 
	 * @param generator The output stream to feed the array into.
	 * 
	 * @param array The array to be fed out.
	 * 
	 * @param currColumn The columns to restrict the output. If this is null,
	 * 					 all data in the array will be output.
	 *
	 * @throws JsonGenerationException Could not serialize the data into JSON.
	 * 
	 * @throws IOException Could not write to the output stream.
	 */
	private static void handleArray(
			final JsonGenerator generator,
			final GenericData.Array<GenericContainer> array,
			final ColumnNode<String> currColumn) 
			throws JsonGenerationException, IOException {
		
		// Start the array.
		generator.writeStartArray();
		
		// Iterate over the elements and add them to the result.
		int numElements = array.size();
		for(int i = 0; i < numElements; i++) {
			handleGeneric(generator, array.get(i), currColumn);
		}
		
		// End the array.
		generator.writeEndArray();
	}
	
	/**
	 * Feeds an object into an output stream and limits the keys in the object
	 * based on the given columns.
	 * 
	 * @param generator The output stream to feed the object into.
	 * 
	 * @param object The object to be fed out.
	 * 
	 * @param currColumn The columns to restrict the output. If this is null,
	 * 					 all data in the object will be output.
	 *
	 * @throws JsonGenerationException Could not serialize the data into JSON.
	 * 
	 * @throws IOException Could not write to the output stream.
	 */
	private static void handleObject(
			final JsonGenerator generator,
			final GenericData.Record object,
			final ColumnNode<String> currColumn) 
			throws JsonGenerationException, IOException {
		
		// Start the object.
		generator.writeStartObject();
		
		// Add the values of all of the requested keys.
		if(currColumn == null) {
			generator.writeObject(object);
		}
		// Add only the requested columns.
		else {
			for(String key : currColumn.getValues()) {
				handleGeneric(
					generator, 
					object.get(key), 
					currColumn.getNode(key));
			}
		}
		
		// End the object.
		generator.writeEndObject();
	}
}