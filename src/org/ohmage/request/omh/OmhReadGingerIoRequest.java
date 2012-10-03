package org.ohmage.request.omh;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;
import org.ohmage.service.OmhServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;

public class OmhReadGingerIoRequest
		extends UserRequest
		implements OmhReadResponder {
	
	/**
	 * The Logger for this request.
	 */
	private static final Logger LOGGER = 
		Logger.getLogger(OmhReadGingerIoRequest.class);
	
	/**
	 * A mapping JSON factory for parsing and outputting JSON.
	 */
	private static final JsonFactory JSON_FACTORY = new MappingJsonFactory();
	
	/**
	 * DateTimeFormatter for GingerIO.
	 */
	private static final DateTimeFormatter DATE_TIME_FORMATTER_GINGER_IO = 
		ISODateTimeFormat.dateHourMinuteSecond().withZone(DateTimeZone.UTC);
	/**
	 * DateTimeFormatter for ohmage.
	 */
	private static final DateTimeFormatter DATE_TIME_FORMATTER_OHMAGE =
		ISODateTimeFormat.dateTime();
	
	/**
	 * This class represents a single day of data from the GBI API.
	 *
	 * @author John Jenkins
	 */
	private static class Result {
		private static final String JSON_KEY_LOCATION_COUNT = "location_count";
		private static final String JSON_KEY_MOBILITY = "mobility";
		private static final String JSON_KEY_MOBILITY_RADIUS = 
			"mobility_radius";
		private static final String JSON_KEY_MISSED_INTERACTIONS = 
			"missed_interactions";
		private static final String JSON_KEY_INTERACTION_DIVERSITY = 
			"interaction_diversity";
		private static final String JSON_KEY_INTERACTION_DURATION = 
			"interaction_duration";
		private static final String JSON_KEY_INTERACTION_BALANCE = 
			"interaction_balance";
		private static final String JSON_KEY_SMS_COUNT = "sms_count";
		private static final String JSON_KEY_SMS_LENGTH = "sms_length";
		private static final String JSON_KEY_AGGREGATE_COMMUNICATION = 
			"aggregate_communication";
		private static final String JSON_KEY_RESPONSIVENESS = "responsiveness";
		private static final String JSON_KEY_CALL_DURATION = "call_duration";
		private static final String JSON_KEY_CALL_COUNT = "call_count";
		
		/**
		 * The timestamp for this record.
		 */
		private DateTime timestamp;
		
		// The data for this record.
		@JsonProperty(JSON_KEY_LOCATION_COUNT)
		private Double locationCount = null;
		@JsonProperty(JSON_KEY_MOBILITY)
		private Double mobility = null;
		@JsonProperty(JSON_KEY_MOBILITY_RADIUS)
		private Double mobilityRadius = null;
		@JsonProperty(JSON_KEY_MISSED_INTERACTIONS)
		private Double missedInteractions = null;
		@JsonProperty(JSON_KEY_INTERACTION_DIVERSITY)
		private Double interactionDiversity = null;
		@JsonProperty(JSON_KEY_INTERACTION_DURATION)
		private Double interactionDuration = null;
		@JsonProperty(JSON_KEY_INTERACTION_BALANCE)
		private Double interactionBalance = null;
		@JsonProperty(JSON_KEY_SMS_COUNT)
		private Double smsCount = null;
		@JsonProperty(JSON_KEY_SMS_LENGTH)
		private Double smsLength = null;
		@JsonProperty(JSON_KEY_AGGREGATE_COMMUNICATION)
		private Double aggregateCommuniaction = null;
		@JsonProperty(JSON_KEY_RESPONSIVENESS)
		private Double responsiveness = null;
		@JsonProperty(JSON_KEY_CALL_DURATION)
		private Double callDuration = null;
		@JsonProperty(JSON_KEY_CALL_COUNT)
		private Double callCount = null;
		
		/**
		 * Generates the Concordia schema for this Result object.
		 * 
		 * @param generator The generator to use to write the definition.
		 * 
		 * @return The 'generator' that was passed in to facilitate chaining.
		 * 
		 * @throws JsonGenerationException There was a problem generating the
		 * 								   JSON.
		 * 
		 * @throws IOException There was a problem writing to the generator.
		 */
		public static JsonGenerator toConcordia(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			// Start the definition.
			generator.writeStartObject();
			
			// The data will always be a JSON object.
			generator.writeStringField("type", "object");
			generator.writeArrayFieldStart("schema");
			
			generator.writeStartObject();
			generator.writeStringField("name", Result.JSON_KEY_LOCATION_COUNT);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField("name", Result.JSON_KEY_MOBILITY);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The phone is polled every 10 minutes and, as long as you haven't traveled more than a half of a mile, added to the running total for the day. This is that total.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField("name", Result.JSON_KEY_MOBILITY_RADIUS);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The distance between the farthest two points based on the user's position in a day. This is along the curve of a circle to approximate the globe and not a straight-line distance.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField(
				"name", 
				Result.JSON_KEY_MISSED_INTERACTIONS);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The number of missed calls for the day.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField(
				"name", 
				Result.JSON_KEY_INTERACTION_DIVERSITY);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The number of unqiue contacts for phone calls and SMS messages for the day, excluding missed calls.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField(
				"name", 
				Result.JSON_KEY_INTERACTION_DURATION);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The total number of seconds the user was on the phone plus the number of characters in all of their sent SMS messages.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField(
				"name", 
				Result.JSON_KEY_INTERACTION_BALANCE);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The number of received calls, not missed or made, divided by the total number of calls.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField("name", Result.JSON_KEY_SMS_COUNT);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The number of SMS messages received and sent.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField("name", Result.JSON_KEY_SMS_LENGTH);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The sum of the length of all SMS messages.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField(
				"name", 
				Result.JSON_KEY_AGGREGATE_COMMUNICATION);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The sum of all sent and received SMS messages plus all calls except those that were missed.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField("name", Result.JSON_KEY_RESPONSIVENESS);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The number of missed calls that the user has not responded to.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField("name", Result.JSON_KEY_CALL_DURATION);
			generator.writeStringField("type", "number");
			generator
				.writeStringField("doc", "The number of minutes in calls.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			generator.writeStartObject();
			generator.writeStringField("name", Result.JSON_KEY_CALL_COUNT);
			generator.writeStringField("type", "number");
			generator
				.writeStringField(
					"doc", 
					"The total number of calls made and received, excluding missed calls.");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// End the overall schema array.
			generator.writeEndArray();
			
			// End the definition.
			generator.writeEndObject();
			
			// Return the generator.
			return generator;
		}
	}
	private final List<Result> results = new LinkedList<Result>();
	
	private final String owner;
	private final DateTime startDate;
	private final DateTime endDate;
	private final long numToSkip;
	private final long numToReturn;
	
	/**
	 * Creates a request to read a HealthVault API.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The parameters already decoded from the HTTP request.
	 * 
	 * @param hashPassword Whether or not to hash the user's password on
	 * 					   authentication. If null, username and password are
	 * 					   not allowed for this API.
	 * 
	 * @param tokenLocation Where to search for the user's token. If null, a
	 * 						token is not allowed for this API.
	 * 
	 * @param callClientRequester Refers to the "client" parameter as the
	 * 							  "requester".
	 * 
	 * @param owner The user whose data is being requested. If null, the 
	 * 				requester is requesting data about themselves.
	 * 
	 * @param startDate Limits the results to only those on or after this date.
	 * 
	 * @param endDate Limits the results to only those on or before this date.
	 * 
	 * @param columns Limits the data output based on the given columns.
	 * 
	 * @param numToSkip The number of responses to skip. Responses are in 
	 * 					reverse-chronological order.
	 * 
	 * @param numToReturn The number of responses to return after the required
	 * 					  responses have been skipped.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 */
	public OmhReadGingerIoRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final String owner,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws IOException, InvalidRequestException {
		
		super(
			httpRequest, 
			hashPassword, 
			tokenLocation, 
			parameters, 
			callClientRequester);
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH read request for GingerIO.");
		}
		
		this.owner = owner;
		this.startDate = startDate;
		this.endDate = endDate;
		this.numToSkip = numToSkip;
		this.numToReturn = numToReturn;
	}
	
	/**
	 * Creates the registry entry for GingerIO.
	 * 
	 * @param generator The generator to use to write the definition.
	 * 
	 * @throws JsonGenerationException There was an error creating the 
	 * 								   JSON.
	 * 
	 * @throws IOException There was an error writing to the generator.
	 */
	public static void writeRegistryEntry(
			final JsonGenerator generator)
			throws JsonGenerationException, IOException {
		
		// Root of the definition
		generator.writeStartObject();
		
		// Output the chunk size which will be the same for all 
		// observers.
		generator.writeNumberField(
			"chunk_size", 
			StreamReadRequest.MAX_NUMBER_TO_RETURN);
		
		// There are no external IDs yet. This may change to
		// link to observer/read, but there are some
		// discrepancies in the parameters.
		
		// Set the local timezone as authoritative.
		generator.writeBooleanField(
			"local_tz_authoritative",
			true);
		
		// Set the summarizable as false for the time being.
		generator.writeBooleanField("summarizable", false);
		
		// Set the payload ID.
		generator.writeStringField(
			"payload_id", 
			"urn:ginger_io");
		
		// Set the payload version. For now, all surveys have 
		// the same version, 1.
		generator.writeStringField(
			"payload_version", 
			"1");
		
		// Set the payload definition.
		generator.writeFieldName("payload_definition"); 
		Result.toConcordia(generator);

		// End the root of the definition.
		generator.writeEndObject();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH read request for GingerIO.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// Verify that the user is allowed to query this data through 
			// ohmage.
			if((owner != null) && (! owner.equals(getUser().getUsername()))) {
				try {
					LOGGER.info("Checking if the user is an admin.");
					UserServices.instance().verifyUserIsAdmin(
						getUser().getUsername());
				}
				catch(ServiceException notAdmin) {
					LOGGER.info("The user is not an admin.");

					LOGGER.info(
						"Checking if reading data about another user is even allowed.");
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
							"Checking if the requester is allowed to read data about this user.");
						UserClassServices
							.instance()
							.userIsPrivilegedInAnotherUserClass(
								getUser().getUsername(), 
								owner);
					}
					else {
						throw new ServiceException(
							ErrorCode.OMH_INSUFFICIENT_PERMISSIONS,
							"This user is not allowed to query data about the requested user.");
					}
				}
			}
			
			// Get the authentication information from the database.
			LOGGER
				.info(
					"Getting the authentication credentials for GingerIO.");
			Map<String, String> healthVaultCredentials =
				OmhServices.instance().getCredentials("ginger_io");
			
			// Retrieve the user's GingerIO ID.
			String userId = 
				healthVaultCredentials
					.get(getUser().getUsername() + "_id");
			if(userId == null) {
				throw new ServiceException(
					"The user's GingerIO ID has not been stored: " +
						getUser().getUsername());
			}
			
			// Switch on either the requester or the given username.
			String requestee = 
				((owner == null) ? getUser().getUsername() : owner);
			
			// Retrieve the user's GingerIO authentication token.
			String authToken = 
				healthVaultCredentials.get(requestee + "_token");
			if(authToken == null) {
				throw new ServiceException(
					"The user's GingerIO auth token has not been stored: " +
						requestee);
			}
			
			// Get the data and massage it into a form we like.
			try {
				LOGGER.info("Building the GingerIO request.");
				StringBuilder urlBuilder = 
					new StringBuilder(
						"https://data.ginger.io/api/v1/participants/");
				urlBuilder.append(userId);
				urlBuilder.append("/gbi/?auth_token=");
				urlBuilder.append(authToken);
				
				LOGGER.info("Calling the GingerIO server.");
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(urlBuilder.toString());
				
				// Make the request.
				HttpResponse response;
				try {
					response = client.execute(request);
				}
				catch(ClientProtocolException e) {
					throw new DomainException("There was an HTTP error.", e);
				}
				catch(IOException e) {
					throw new DomainException(
						"There was an error communicating with the server.",
						e);
				}
				
				// Process the response for its content.
				String responseString;
				try {
					responseString =
						(new BasicResponseHandler()).handleResponse(response);
				}
				catch(HttpResponseException e) {
					throw new DomainException(
						"The server returned an error.",
						e);
				}
				catch(IOException e) {
					throw new DomainException(
						"There was an error commmunicating with the server.",
						e);
				}
				
				Map<DateTime, Result> resultMap = 
					new HashMap<DateTime, Result>();
				try {
					LOGGER.info("Parsing the response.");
					JsonParser parser = 
						JSON_FACTORY.createJsonParser(responseString);
					
					// Ensure that the response is a JSON object.
					if(parser.nextToken() != JsonToken.START_OBJECT) {
						throw 
							new DomainException(
								"The response was not a JSON object.");
					}
					
					// Cycle through each of the columns.
					while(parser.nextToken() != JsonToken.END_OBJECT) {
						// Get the column's name.
						String fieldName = parser.getCurrentName();
						
						// Ensure that the value of this field is an array.
						if(parser.nextToken() != JsonToken.START_ARRAY) {
							throw 
								new DomainException(
									"The '" +
										fieldName +
										"' field was not a JSON array.");
						}

						// Now, cycle through each of the column's data points
						// and add them to their appropriate Result object.
						JsonToken currToken;
						while((currToken = parser.nextToken()) != JsonToken.END_ARRAY) {
							// Ensure that each element in the array is an 
							// array.
							if(currToken != JsonToken.START_ARRAY) {
								throw
									new DomainException(
										"The element was not an array.");
							}
							
							// Get the timestamp.
							String timestampString = parser.nextTextValue();
							if(timestampString == null) {
								throw 
									new DomainException(
										"The timestamp is missing.");
							}
							DateTime timestamp = 
								DATE_TIME_FORMATTER_GINGER_IO
									.parseDateTime(timestampString);
							
							// Advance the token.
							parser.nextToken();
							// Get the value.
							double value = parser.getDoubleValue();
							
							// Advance the token to consume the JsonToken.END_ARRAY.
							parser.nextToken();
							
							// If this point is before the start date or after
							// the end date, don't add it to the results.
							if(	(
									(startDate != null)
									&& 
									timestamp.isBefore(startDate)
								)
								||
								(
									(endDate != null)
									&&
									timestamp.isAfter(endDate)
								)) {
								continue;
							}
							
							// Get the result based on the timestamp or add it
							// if it doesn't already exist.
							Result result = resultMap.get(timestamp);
							if(result == null) {
								result = new Result();
								result.timestamp = timestamp;
								resultMap.put(timestamp, result);
							}
							
							// Switch to the correct value based on the current
							// field and set this value.

							if(Result.JSON_KEY_LOCATION_COUNT.equals(fieldName)) {
								result.locationCount = value;
							}
							else if(Result.JSON_KEY_MOBILITY.equals(fieldName)) {
								result.mobility = value;
							}
							else if(Result.JSON_KEY_MOBILITY_RADIUS.equals(fieldName)) {
								result.mobilityRadius = value;
							}
							else if(Result.JSON_KEY_MISSED_INTERACTIONS.equals(fieldName)) {
								result.missedInteractions = value;
							}
							else if(Result.JSON_KEY_INTERACTION_DIVERSITY.equals(fieldName)) {
								result.interactionDiversity = value;
							}
							else if(Result.JSON_KEY_INTERACTION_DURATION.equals(fieldName)) {
								result.interactionDuration = value;
							}
							else if(Result.JSON_KEY_INTERACTION_BALANCE.equals(fieldName)) {
								result.interactionBalance = value;
							}
							else if(Result.JSON_KEY_SMS_COUNT.equals(fieldName)) {
								result.smsCount = value;
							}
							else if(Result.JSON_KEY_SMS_LENGTH.equals(fieldName)) {
								result.smsLength = value;
							}
							else if(Result.JSON_KEY_AGGREGATE_COMMUNICATION.equals(fieldName)) {
								result.aggregateCommuniaction = value;
							}
							else if(Result.JSON_KEY_RESPONSIVENESS.equals(fieldName)) {
								result.responsiveness = value;
							}
							else if(Result.JSON_KEY_CALL_DURATION.equals(fieldName)) {
								result.callDuration = value;
							}
							else if(Result.JSON_KEY_CALL_COUNT.equals(fieldName)) {
								result.callCount = value;
							}
						}
					}
				}
				catch(JsonParseException e) {
					throw 
						new DomainException(
							"There was an error reading the JSON.",
							e);
				}
				catch(IOException e) {
					throw 
						new DomainException(
							"There was an error writing to the generator.",
							e);
				}
				results.addAll(resultMap.values());
			}
			catch(DomainException e) {
				throw new ServiceException("Could not retrieve the data.", e);
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.omh.OmhReadResponder#getNumDataPoints()
	 */
	@Override
	public long getNumDataPoints() {
		long maxToOutput = results.size() - numToSkip;
		if(maxToOutput < 0) {
			maxToOutput = 0;
		}
		
		return Math.min(maxToOutput, numToReturn);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.omh.OmhReadResponder#respond(org.codehaus.jackson.JsonGenerator, org.ohmage.request.observer.StreamReadRequest.ColumnNode)
	 */
	@Override
	public void respond(
			final JsonGenerator generator, 
			final ColumnNode<String> columns)
			throws JsonGenerationException, IOException, DomainException {

		LOGGER.info("Responding to an OMH read request for GingerIO data.");
		
		// Keep track of the number of records processed.
		long numProcessed = 0;
		long totalNumToProcess = numToSkip + numToReturn;
		
		// For each object,
		for(Result result : results) {
			numProcessed++;
			if(numProcessed <= numToSkip) {
				continue;
			}
			if(numProcessed > totalNumToProcess) {
				break;
			}
			
			// Start the overall object.
			generator.writeStartObject();
			
			// Write the metadata.
			generator.writeObjectFieldStart("metadata");
			
			// Write the timestamp.
			generator
				.writeStringField(
					"timestamp", 
					DATE_TIME_FORMATTER_OHMAGE.print(result.timestamp));
			
			// End the metadata object.
			generator.writeEndObject();
			
			// Write the data field name.
			generator.writeFieldName("data");
			
			// Write the data.
			generator.writeObject(result);
			
			// End the overall object.
			generator.writeEndObject();
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

		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
		}
		else {
			throw new UnsupportedOperationException(
				"HTTP requests are invalid for this request.");
		}
	}

}
