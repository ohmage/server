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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.Location;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.Observer;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
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
	public static final long MAX_NUMBER_TO_RETURN = 2000;
	
	/**
	 * This is being used to facilitate an n-ary tree.
	 *
	 * @author John Jenkins
	 */
	public static class ColumnNode<T> {
		private Map<T, ColumnNode<T>> children;
		
		/**
		 * Creates a node with a value.
		 */
		public ColumnNode() {
			this.children = new HashMap<T, ColumnNode<T>>();
		}
		
		/**
		 * Adds a child to this node. Adding null has no effect.
		 * 
		 * @param value The value of the child.
		 */
		public void addChild(final T value) {
			if(value == null) {
				return;
			}
			
			children.put(value, new ColumnNode<T>());
		}
		
		/**
		 * Checks if this node has a child with the given value.
		 * 
		 * @param value The value to check for.
		 * 
		 * @return True if this node has a sub-node with the given value; 
		 * 		   false, otherwise.
		 */
		public boolean hasChild(final T value) {
			return children.containsKey(value);
		}
		
		/**
		 * Returns the child with the given value.
		 * 
		 * @param value The value.
		 * 
		 * @return The child or null if no such child exists.
		 */
		public ColumnNode<T> getChild(final T value) {
			return children.get(value);
		}
		
		/**
		 * The values for this node's children.
		 * 
		 * @return An unmodifiable collection of this node's children. 
		 */
		public Collection<T> getChildrenValues() {
			return Collections.unmodifiableSet(children.keySet());
		}
		
		/**
		 * Returns whether or not this node is a leaf node.
		 * 
		 * @return Whether or not this node is a leaf node.
		 */
		public boolean isLeaf() {
			return children.size() == 0;
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
	 * Creates a stream read request from the given parameters.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The parameters from the HTTP request that have already
	 * 					 been decoded.
	 * 
	 * @param observerId The observer's unique identifier. Required.
	 * 
	 * @param observerVersion The observer's version. Optional.
	 * 
	 * @param streamId The stream's unique identifier. Required.
	 * 
	 * @param streamVersion The stream's version. Required.
	 * 
	 * @param startDate Limits the results to only those on or after this date.
	 * 					Optional.
	 * 
	 * @param endDate Limits the results to only those on or before this date.
	 * 				  Optional.
	 * 
	 * @param columns A string representing the columns to return or null if
	 * 				  all columns should be returned. Optional.
	 * 
	 * @param numToSkip The number of entries to skip. Optional. Default is 0.
	 * 
	 * @param numToReturn The number of entries to return. Optional. Default is
	 * 					  {@value #MAX_NUMBER_TO_RETURN}.
	 */
	public StreamReadRequest(
			final HttpServletRequest httpRequest, 
			final Map<String, String[]> parameters,
			final String observerId,
			final Long observerVersion,
			final String streamId,
			final long streamVersion,
			final DateTime startDate,
			final DateTime endDate,
			final String columns,
			final Long numToSkip,
			final Long numToReturn) {
		
		super(httpRequest, TokenLocation.EITHER, false, parameters);
		
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
			
			if(observerId == null) {
				setFailed(
					ErrorCode.OBSERVER_INVALID_ID,
					"The observer ID is missing.");
			}
			if(streamId == null) {
				setFailed(
					ErrorCode.OBSERVER_INVALID_STREAM_ID,
					"The stream ID is missing.");
			}
			
			try {
				tObserverId = ObserverValidators.validateObserverId(observerId);
				tObserverVersion = observerVersion;
				tStreamId = ObserverValidators.validateStreamId(streamId);
				tStreamVersion = streamVersion;
				tStartDate = startDate;
				
				LOGGER.debug("Start Date: " + tStartDate);
				
				tEndDate = endDate;
				
				LOGGER.info("End Date: " + tEndDate);
				
				tColumnsRoot = ObserverValidators.validateColumnList(columns);

				if((numToSkip != null) && (numToSkip > 0)) {
					tNumToSkip = numToSkip;
				}
				if((numToReturn != null) && (
					(numToReturn < 0) || (numToReturn > MAX_NUMBER_TO_RETURN))) {
					
					tNumToReturn = MAX_NUMBER_TO_RETURN;
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		this.observerId = tObserverId;
		this.observerVersion = tObserverVersion;
		this.streamId = tStreamId;
		this.streamVersion = tStreamVersion;
		this.startDate = tStartDate;
		this.endDate = tEndDate;
		this.columnsRoot = tColumnsRoot;
		this.numToSkip = tNumToSkip;
		this.numToReturn = tNumToReturn;
		
		results = new LinkedList<DataStream>();
	}
	
	/**
	 * Creates a stream read request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public StreamReadRequest(
			final HttpServletRequest httpRequest) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest, TokenLocation.EITHER, false);
		
		String tObserverId = null;
		Long tObserverVersion = null;
		String tStreamId = null;
		Long tStreamVersion = null;
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		ColumnNode<String> tColumnsRoot = new ColumnNode<String>();
		long tNumToSkip = 0;
		long tNumToReturn = MAX_NUMBER_TO_RETURN;
		
		if(! isFailed()) {
			LOGGER.info("Creating a stream read request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.OBSERVER_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OBSERVER_INVALID_ID,
						"Multiple observer IDs were given: " +
							InputKeys.OBSERVER_ID);
				}
				else if(t.length == 1) {
					tObserverId = 
						ObserverValidators.validateObserverId(t[0]);
				}
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
	
	/**
	 * Returns an unmodifiable copy of the results. If {@link #service()} has
	 * not been call on this request, this will be an empty list.
	 * 
	 * @return The list of results generated thus far.
	 */
	public Collection<DataStream> getResults() {
		return Collections.unmodifiableCollection(results);
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

		// Create the generator that will stream to the requester.
		JsonGenerator generator;
		try {
			generator =
				(new JsonFactory()).createJsonGenerator(outputStream);
		}
		catch(IOException generatorException) {
			LOGGER.error(
				"Could not create the JSON generator.",
				generatorException);
			
			try {
				outputStream.close();
			}
			catch(IOException streamCloseException) {
				LOGGER.warn(
					"Could not close the output stream.",
					streamCloseException);
			}
			
			return;
		}
				
		/*
		 * Example output:
		 * 
		 * 	{
		 * 		"result":"success",
		 * 		"metadata":{
		 * 			"count":<A number representing the number of results.>,
		 * 			"prev":"<The URL for the previous set of results.>",
		 * 			"next":"<The URL for the next set of results.>"
		 * 		},
		 * 		"data":[
		 * 			{
		 * 				"metadata":{},
		 * 				"data":{} // Data based on the columns.
		 * 			},
		 * 			...
		 * 		]
		 * 	}
		 */
		try {
			// Start the resulting object.
			generator.writeStartObject();
			
			// Add the result to the object.
			generator.writeObjectField("result", "success");
			
			// Add the meta-data.
			generator.writeObjectFieldStart("metadata");
			generator.writeNumberField("count", results.size());
			// TODO: Add the "prev" and "next" fields.
			generator.writeEndObject();
			
			// Add a "data" key that is an array of the results.
			generator.writeArrayFieldStart("data");
			for(DataStream dataStream : results) {
				// Begin this data stream.
				generator.writeStartObject();
				
				// Write the meta-data.
				DataStream.MetaData metaData = dataStream.getMetaData();
				if(metaData != null) {
					generator.writeObjectFieldStart("metadata");
					
					String id = metaData.getId();
					if(id != null) {
						generator.writeStringField("id", id);
					}
					
					DateTime timestamp = metaData.getTimestamp();
					if(timestamp != null) {
						generator.writeStringField(
							"timestamp",
							ISODateTimeFormat.dateTime().print(timestamp));
					}
					
					Location location = metaData.getLocation();
					if(location != null) {
						generator.writeObjectFieldStart("location");
						location.streamJson(
							generator, 
							false, 
							LocationColumnKey.ALL_COLUMNS);
						generator.writeEndObject();
					}
					
					generator.writeEndObject();
				}
				
				// Write the data.
				handleGeneric(
					generator, 
					dataStream.getData(), 
					columnsRoot, 
					"data");
				
				// End this data stream.
				generator.writeEndObject();
			}
			generator.writeEndArray();
			
			// End the overall object.
			generator.writeEndObject();
		}
		catch(JsonProcessingException e) {
			LOGGER.error("The JSON could not be processed.", e);
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		catch(IOException e) {
			LOGGER.error(
				"The response could no longer be writtent to the response",
				e);
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		catch(DomainException e) {
			LOGGER.error("Could not read one of the objects.", e);
			httpResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		finally {
			// Flush and close the writer.
			try {
				generator.close();
			}
			catch(IOException e) {
				LOGGER.warn("Could not close the generator.", e);
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
			final ColumnNode<String> columns,
			final String fieldName)
			throws JsonGenerationException, IOException {
		
		// If it's an array, then process it as such.
		if(generic instanceof GenericData.Array) {
			handleArray(
				generator, 
				(GenericData.Array<GenericContainer>) generic,
				columns,
				fieldName);
		}
		// If it's an object, then process it as such.
		else if(generic instanceof GenericData.Record) {
			handleObject(
				generator, 
				(GenericData.Record) generic, 
				columns,
				fieldName);
		}
		// If it's a string, then we need to code around their string type.
		else if(generic instanceof Utf8) {
			String value = ((Utf8) generic).toString();
			
			if(fieldName == null) {
				generator.writeString(value);
			}
			else {
				generator.writeStringField(fieldName, value);
			}
		}
		// Otherwise, it can be added to its encapsulating object.
		else {
			if(fieldName == null) {
				generator.writeObject(generic);
			}
			else {
				generator.writeObjectField(fieldName, generic);
			}
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
			final ColumnNode<String> currColumn,
			final String fieldName) 
			throws JsonGenerationException, IOException {
		
		// Start the array.
		if(fieldName == null) {
			generator.writeStartArray();
		}
		else {
			generator.writeArrayFieldStart(fieldName);
		}
		
		// Iterate over the elements and add them to the result.
		int numElements = array.size();
		for(int i = 0; i < numElements; i++) {
			handleGeneric(generator, array.get(i), currColumn, null);
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
			final ColumnNode<String> currColumn,
			final String fieldName) 
			throws JsonGenerationException, IOException {
		
		// Start the object.
		if(fieldName == null) {
			generator.writeStartObject();
		}
		else {
			generator.writeObjectFieldStart(fieldName);
		}
		
		// Add the values of all of the requested keys.
		if(currColumn.isLeaf()) {
			for(Schema.Field field : object.getSchema().getFields()) {
				String currFieldName = field.name();
				
				handleGeneric(
					generator,
					object.get(currFieldName),
					currColumn,
					currFieldName);
			}
		}
		// Add only the requested columns.
		else {
			for(String key : currColumn.getChildrenValues()) {
				Object currObject = object.get(key);
				
				if(currObject != null) {
					handleGeneric(
						generator, 
						currObject, 
						currColumn.getChild(key),
						key);
				}
			}
		}
		
		// End the object.
		generator.writeEndObject();
	}
}