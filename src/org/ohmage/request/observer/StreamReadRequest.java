package org.ohmage.request.observer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.DataStream;
import org.ohmage.domain.Location;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.Observer;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.RequestBuilder;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ObserverServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.ObserverValidators;
import org.ohmage.validator.UserValidators;

/**
 * <p>Reads uploaded data for a stream.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OBSERVER_ID}</td>
 *     <td>The unique ID for the observer that contains the stream.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OBSERVER_VERSION}</td>
 *     <td>The version of the observer to limit the results to only those that
 *       were generated with this version of the observer.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#STREAM_ID}</td>
 *     <td>The unique ID for the stream.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#STREAM_VERSION}</td>
 *     <td>A specific version of the stream.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#START_DATE}</td>
 *     <td>An ISO-8601 date-time-zone that limits the results to only those on
 *       or after this date.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>An ISO-8601 date-time-zone that limits the results to only those on
 *       or before this date.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#COLUMN_LIST}</td>
 *     <td>The list of columns to return from the data. The columns are 
 *       formatted as such: 
 *       "<tt>&lt;RootID&gt;:&lt;SubID&gt;:&lt;SubSubID&gt;...</tt>",
 *       so, for example, if the data was formatted with a two top level keys,
 *       "r1" and "r2", and "r1" had two sub-keys, "s1" and "s2", a user could
 *       retrieve only "r2" and "s2" with the following column list, 
 *       "<tt>r2,r1:s2</tt>".</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NUM_TO_SKIP}</td>
 *     <td>The number of data points that match the given query that should be
 *       skipped. This is used to facilitate paging.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NUM_TO_RETURN}</td>
 *     <td>The number of data points that match the given query that should be
 *       returned after skipping. This is used to facilitate paging.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class StreamReadRequest extends UserRequest {
	private static final Logger LOGGER = 
		Logger.getLogger(StreamReadRequest.class);
	
	/**
	 * The single factory instance for the writer.
	 */
	private static final JsonFactory JSON_FACTORY = 
		(new MappingJsonFactory()).configure(Feature.AUTO_CLOSE_TARGET, true);
	
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
		
		/**
		 * Creates a string that is a list of all of the descendants of this
		 * node. For example, if this node had one leaf child, "leaf", and
		 * another child, "other", and "other" had one leaf child, "otherLeaf",
		 * the result of this function would be a string with two nodes:
		 * "leaf,other:otherLeaf".
		 * 
		 * @return The comma-separated string representation of the list of the
		 * 		   descendants of this node, each prepended with their parent's
		 * 		   name separated by a colon.
		 * 
		 * @throws IllegalStateException This is a leaf node which has no 
		 * 								 descendants.
		 */
		public String toListString() {
			StringBuilder result = new StringBuilder();
			
			boolean firstPass = true;
			for(String node : toList()) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					result.append(',');
				}
				
				result.append(node);
			}
			
			return result.toString();
		}
		
		/**
		 * Creates a list of column nodes for each of this node's descendants.
		 * For example, if this node had one leaf child, "leaf", and another 
		 * child, "other", and "other" had one leaf child, "otherLeaf", the 
		 * result of this function would be a list with two nodes: "leaf" and 
		 * "other:otherLeaf".
		 * 
		 * @return The list of children and their children where the grand
		 * 		   children and beyond are prepended with the child's name.  
		 * 
		 * @throws IllegalStateException This is a leaf node which has no 
		 * 								 children.
		 */
		private List<String> toList() {
			if(isLeaf()) {
				throw new IllegalStateException(
					"This is a leaf node, which doesn't have a column list.");
			}
			
			List<String> result = new LinkedList<String>();
			for(T child : children.keySet()) {
				ColumnNode<T> childNode = children.get(child);
				
				if(childNode.isLeaf()) {
					result.add(child.toString());
				}
				else {
					List<String> subChildren = childNode.toList();
					for(String subChild : subChildren) {
						result.add(child.toString() + ":" + subChild);
					}
				}
			}
			return result;
		}
	}
	
	// The username of the user whose data is being read.
	private final String username;
	
	// Required.
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
	
	// The stream created during the servicing of the request.
	private Observer.Stream stream;
	
	// The collection results from this request.
	private final List<DataStream> results;
	
	/**
	 * Creates a stream read request from the given parameters.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The parameters from the HTTP request that have already
	 * 					 been decoded.
	 * 
	 * @param hashPassword Whether or not to hash the user's password. If it is
	 * 					   null, username/password combinations will not be
	 * 					   allowed.
	 * 
	 * @param tokenLocation Where to look for the token. If it is null, the 
	 * 						token for authentication will not be allowed.
	 * 
	 * @param callClientRequester Use the name "requester" in place of 
	 * 							  "client".
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
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws IllegalArgumentException Thrown if a required parameter is 
	 * 									missing.
	 */
	public StreamReadRequest(
			final HttpServletRequest httpRequest, 
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final String username,
			final String observerId,
			final Long observerVersion,
			final String streamId,
			final long streamVersion,
			final DateTime startDate,
			final DateTime endDate,
			final ColumnNode<String> columns,
			final Long numToSkip,
			final Long numToReturn)
			throws IOException, InvalidRequestException {
		
		super(httpRequest, hashPassword, tokenLocation, parameters, callClientRequester);
		
		if(observerId == null) {
			throw new IllegalArgumentException("The observer ID is null.");
		}
		else if(streamId == null) {
			throw new IllegalArgumentException("The stream ID is null.");
		}
		
		this.username = username;
		this.observerId = observerId;
		this.observerVersion = observerVersion;
		this.streamId = streamId;
		this.streamVersion = streamVersion;
		this.startDate = startDate;
		this.endDate = endDate;
		this.columnsRoot = columns;
		
		if(numToSkip == null) {
			this.numToSkip = 0;
		}
		else {
			this.numToSkip = numToSkip;
		}
		
		if(numToReturn == null) {
			this.numToReturn = MAX_NUMBER_TO_RETURN;
		}
		else {
			this.numToReturn = numToReturn;
		}
		
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
		
		super(httpRequest, false, TokenLocation.EITHER, null);
		
		String tUsername = null;
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
				t = getParameterValues(InputKeys.USERNAME);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.USER_INVALID_USERNAME,
						"Multiple usernames were given: " +
							InputKeys.USERNAME);
				}
				else if(t.length == 1) {
					tUsername = UserValidators.validateUsername(t[0]);
				}
				
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
						ObserverValidators
							.validateNumToReturn(t[0], MAX_NUMBER_TO_RETURN);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		username = tUsername;
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
			if((username != null) && (! username.equals(getUser().getUsername()))) {
				try {
					LOGGER.info("Checking if the user is an admin.");
					UserServices.instance().verifyUserIsAdmin(
						getUser().getUsername());
				}
				catch(ServiceException notAdmin) {
					LOGGER.info("The user is not an admin.");

					LOGGER.info(
						"Checking if reading stream data about another user is even allowed.");
					boolean isPlausible;
					try {
						isPlausible = 
							StringUtils.decodeBoolean(
								PreferenceCache.instance().lookup(
									PreferenceCache.KEY_PRIVILEGED_USER_IN_CLASS_CAN_VIEW_MOBILITY_FOR_EVERYONE_IN_CLASS));
					}
					catch(CacheMissException e) {
						throw new ServiceException(e);
					}
					
					if(isPlausible) {
						LOGGER.info(
							"Checking if the requester is allowed to read stream data about the user.");
						UserClassServices
							.instance()
							.userIsPrivilegedInAnotherUserClass(
								getUser().getUsername(), 
								username);
					}
					else {
						throw new ServiceException(
							ErrorCode.OBSERVER_INSUFFICIENT_PERMISSIONS,
							"This user is not allowed to query stream data about the requested user.");
					}
				}
			}
			
			LOGGER.info("Retrieving the stream definition.");
			stream = 
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
					(username == null) ? getUser().getUsername() : username,
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
		
		// Set the CORS headers.
		handleCORS(httpRequest, httpResponse);
		
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
			generator = JSON_FACTORY.createJsonGenerator(outputStream);
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
			
			// Add the count to the meta-data.
			generator.writeNumberField("count", results.size());

			// Get the URL that will be the base for the "previous" and "next"
			// URLs.
			StringBuilder prevAndNextUrlBuilder = buildNextAndPrevUrl();
			
			// If the number of entries skipped was non-zero, add a previous
			// pointer.
			if(numToSkip != 0) {
				// Create a copy of the existing string builder.
				StringBuilder prevUrl = 
					new StringBuilder(prevAndNextUrlBuilder);
				
				// Calculate the number of results to skip and return for the
				// "previous" URL.
				long prevNumToSkip = numToSkip - numToReturn - 1;
				boolean returnNumToSkipAsNumToReturn = false;
				if(prevNumToSkip < 0) {
					returnNumToSkipAsNumToReturn = true;
					prevNumToSkip = 0;
				}
				
				// Add the number of results to skip and return.
				prevUrl
					.append('&')
					.append(InputKeys.NUM_TO_SKIP)
					.append('=')
					.append(prevNumToSkip);
				prevUrl
					.append('&')
					.append(InputKeys.NUM_TO_RETURN)
					.append('=')
					.append((returnNumToSkipAsNumToReturn) ? numToSkip - 1 : numToReturn);
				
				// Add the "previous" URL to the meta-data.
				generator.writeStringField("previous", prevUrl.toString());
			}
			
			// Generate and add the "next" URL if the number of results is 
			// to the number requested. The only reason it would be less is if
			// there weren't that many to return. If there were more than that
			if(numToReturn == results.size()) {
				StringBuilder nextUrl = prevAndNextUrlBuilder;
				
				// Calculate the number to skip.
				long nextNumToSkip = numToSkip + numToReturn;
				
				// Add the number of results to skip and return to the "next"
				// URL.
				nextUrl
					.append('&')
					.append(InputKeys.NUM_TO_SKIP)
					.append('=')
					.append(nextNumToSkip);
				nextUrl
					.append('&')
					.append(InputKeys.NUM_TO_RETURN)
					.append('=')
					.append(numToReturn);
				
				// Add the "next" URL to the meta-data.
				generator.writeStringField("next", nextUrl.toString());
			}
			
			// End the meta-data.
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
			LOGGER.info(
				"The response could no longer be written to the response",
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
				LOGGER.info("Could not close the generator.", e);
			}
		}
	}
	
	/**
	 * Generates a URL for the "previous" and "next" URLs in the result's 
	 * meta-data. This includes all of the given parameters except the number 
	 * of results to skip and return. These should be computed and added before
	 * adding this URL to the result's meta-data.
	 * 
	 * @return The URL for the "previous" and "next" URLs without the number of
	 * 		   results to skip or return.
	 */
	private StringBuilder buildNextAndPrevUrl() {
		StringBuilder result = new StringBuilder();
		
		// Get the server's fully qualified domain name.
		String fqdn;
		try {
			fqdn =
				PreferenceCache.instance().lookup(
					PreferenceCache.KEY_FULLY_QUALIFIED_DOMAIN_NAME);
		}
		catch(CacheMissException e) {
			LOGGER.warn(
				"Error retrieving the system's fully qualified domain name.",
				e);
			return null;
		}
		// Trim any tailing '/'s.
		while(fqdn.endsWith("/")) {
			fqdn = fqdn.substring(0, fqdn.length() - 1);
		}
		
		// Add the fully qualified domain name.
		result.append(fqdn);
		
		// Add the stream/read URI.
		result.append(RequestBuilder.getInstance().getApiStreamRead());
		
		// Add the parameters.
		result.append('?');
		
		// If the token was provided as a parameter, add it as a 
		// parameter. If it was not, we don't want to start echoing 
		// back passwords, so we will make it a requirement that the 
		// caller re-supply the password.
		String token = getUser().getToken();
		if(token != null) {
			result
				.append(InputKeys.AUTH_TOKEN)
				.append('=')
				.append(token)
				// This is so that the next component doesn't have to
				// worry about checking if this exists or not.
				.append('&');
		}
		
		// Add the client value.
		result.append(InputKeys.CLIENT).append('=').append(getClient());
		
		// Add the observer ID.
		result
			.append('&')
			.append(InputKeys.OBSERVER_ID)
			.append('=')
			.append(observerId);
		
		// Add the observer version if it was given.
		if(observerVersion != null) {
			result
				.append('&')
				.append(InputKeys.OBSERVER_VERSION)
				.append('=')
				.append(observerVersion);
		}
		
		// Add the stream ID and version.
		result
			.append('&')
			.append(InputKeys.STREAM_ID)
			.append('=')
			.append(streamId);
		result
			.append('&')
			.append(InputKeys.STREAM_VERSION)
			.append('=')
			.append(streamVersion);
		
		// Add the start date if it was given.
		if(startDate != null) {
			result
				.append('&')
				.append(InputKeys.START_DATE)
				.append('=')
				.append(ISODateTimeFormat.dateTime().print(startDate));
		}

		// Add the end date if it was given.
		if(endDate != null) {
			result
				.append('&')
				.append(InputKeys.END_DATE)
				.append('=')
				.append(ISODateTimeFormat.dateTime().print(endDate));
		}
		
		// Add the columns list if it was given.
		if(! columnsRoot.isLeaf()) {
			result
				.append('&')
				.append(InputKeys.COLUMN_LIST)
				.append('=')
				.append(columnsRoot.toListString());
		}
		
		return result;
	}
	
	/**
	 * Feeds a generic object into an output stream and only includes the
	 * specified columns.
	 * 
	 * @param generator The output stream to feed the object into.
	 * 
	 * @param encoder The encoder to convert Avro data to JSON data.
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
	private static void handleGeneric(
			final JsonGenerator generator,
			final JsonNode generic,
			final ColumnNode<String> columns,
			final String fieldName)
			throws JsonGenerationException, IOException {
		
		// If it's an array, then process it as such.
		if(generic.isArray()) {
			handleArray(
				generator,
				(ArrayNode) generic,
				columns,
				fieldName);
		}
		// If it's an object, then process it as such.
		else if(generic.isObject()) {
			handleObject(
				generator,
				(ObjectNode) generic, 
				columns,
				fieldName);
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
	 * @param encoder The encoder to convert Avro data to JSON data.
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
			final ArrayNode array,
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
			handleGeneric(
				generator,
				array.get(i), 
				currColumn, 
				null);
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
	 * @param encoder The encoder to convert Avro data to JSON data.
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
			final ObjectNode object,
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
			Iterator<String> fields = object.getFieldNames();
			while(fields.hasNext()) {
				String field = fields.next();
				
				handleGeneric(
					generator,
					object.get(field),
					currColumn,
					field);
			}
		}
		// Add only the requested columns.
		else {
			for(String key : currColumn.getChildrenValues()) {
				JsonNode currObject = object.get(key);
				
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