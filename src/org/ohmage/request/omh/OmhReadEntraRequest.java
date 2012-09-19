package org.ohmage.request.omh;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XMLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;
import org.ohmage.service.OmhServices;
import org.ohmage.validator.OmhValidators;

public class OmhReadEntraRequest
		extends UserRequest
		implements OmhReadResponder {

	private static final Logger LOGGER = 
		Logger.getLogger(OmhReadEntraRequest.class);
	
	/**
	 * The superclass for all of the Entra methods.
	 *
	 * @author John Jenkins
	 */
	public abstract static class EntraMethod implements OmhReadResponder {
		/**
		 * The API to which all calls are made. This is a single ASP page, and
		 * the different methods are parameters to this API.
		 */
		public static final String API_URI = 
			"https://secure.myglucohealth.net/api/MGHApi3.asp";
		
		/**
		 * The pattern used to create the formatter representing how dates are
		 * formatted by Entra.
		 */
		public static final String DATE_TIME_PATTERN = "MM/dd/yyyy";
		/**
		 * Formatter for formating a date/time object into the Entra-specific
		 * format.
		 */
		public static final DateTimeFormatter DATE_TIME_FORMATTER =
			DateTimeFormat.forPattern(DATE_TIME_PATTERN);
		
		/**
		 * The parameter name for the application's ID.
		 */
		protected static final String PARAM_APP_ID = "uid";
		/**
		 * The parameter name for the application's password.
		 */
		protected static final String PARAM_APP_PASSWORD = "pw";
		/**
		 * The parameter name for the application's 'source'.
		 */
		protected static final String PARAM_APP_SOURCE = "source";
		/**
		 * The parameter name for the user's username.
		 */
		protected static final String PARAM_USER_NAME = "mID";
		/**
		 * The parameter name for the user's password.
		 */
		protected static final String PARAM_USER_PASSWORD = "mPW";
		/**
		 * The parameter name for the method.
		 */
		protected static final String PARAM_METHOD = "method";
		
		/**
		 * The method String for this EntraMethod.
		 */
		private final String method;
		
		/**
		 * A flag to indicate if this request has been made yet or not.
		 */
		private boolean madeRequest = false;
		
		/**
		 * Creates a new abstract Entra method.
		 * 
		 * @param method The method String for this Entra method.
		 */
		private EntraMethod(final String method) {
			this.method = method;
		}
		
		/**
		 * Creates the base URI for this method. Implementing methods should
		 * override this method, call this within their implementation, and 
		 * append their additional parameters, if any. This implementation will
		 * always add at least one parameter, so sub-implementations should 
		 * always add a '&' before their first parameter.
		 * 
		 * @return The URI for this method.
		 * 
		 * @throws DomainException There was a problem creating the URI object.
		 */
		public URI getUri(
				final String appId,
				final String appPassword,
				final String appSource,
				final String userName,
				final String userPassword)
				throws DomainException {
			
			if(appId == null) {
				throw new DomainException("The application ID was null.");
			}
			else if(appId.trim().length() == 0) {
				throw
					new DomainException(
						"The application ID was only whitespace.");
			}
			else if(appPassword == null) {
				throw
					new DomainException("The application password was null.");
			}
			else if(appPassword.trim().length() == 0) {
				throw
					new DomainException(
						"The application password was only whitespace.");
			}
			else if(appSource == null) {
				throw new DomainException("The application source was null.");
			}
			else if(appSource.trim().length() == 0) {
				throw
					new DomainException(
						"The application source was only whitespace.");
			}
			else if(userName == null) {
				throw new DomainException("The username was null.");
			}
			else if(userName.trim().length() == 0) {
				throw
					new DomainException("The username was only whitespace.");
			}
			else if(userPassword == null) {
				throw new DomainException("The user's password was null.");
			}
			else if(userPassword.trim().length() == 0) {
				throw
					new DomainException(
						"The user's password was only whitespace.");
			}
			
			// Create the builder based on the base API URI.
			StringBuilder builder = new StringBuilder(API_URI);
			builder.append('?');
			
			// Add the application ID.
			builder.append(PARAM_APP_ID).append(appId);
			
			// Add the application password.
			builder.append('&').append(PARAM_APP_PASSWORD).append(appPassword);
			
			// Add the application source.
			builder.append('&').append(PARAM_APP_SOURCE).append(appSource);
			
			// Add the username.
			builder.append('&').append(PARAM_USER_NAME).append(userName);
			
			// Add the password.
			builder
				.append('&').append(PARAM_USER_PASSWORD).append(userPassword);
			
			// Add the method.
			builder.append('&').append(PARAM_METHOD).append(method);
			
			try {
				return new URI(builder.toString());
			}
			catch(URISyntaxException e) {
				throw
					new DomainException(
						"There was a problem building the URI.",
						e);
			}
		}
		
		/**
		 * Returns whether or not the records for this method will have an ID 
		 * associated with each one.
		 * 
		 * @return Whether or not these records have IDs.
		 */
		public abstract boolean hasId();
		
		/**
		 * Returns whether or not the records for this method will have a timestamp
		 * associated with each one.
		 * 
		 * @return Whether or not these records have timestamps.
		 */
		public abstract boolean hasTimestamp();
		
		/**
		 * Returns whether or not the records for this method will have a location
		 * associated with each one.
		 * 
		 * @return Whether or not these records have locations.
		 */
		public abstract boolean hasLocation();
		
		/**
		 * Creates the registry entry for this Entra method.
		 * 
		 * @param generator The generator to use to write the definition.
		 * 
		 * @throws JsonGenerationException There was an error creating the 
		 * 								   JSON.
		 * 
		 * @throws IOException There was an error writing to the generator.
		 */
		public void writeRegistryEntry(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			// Entra definition
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
			StringBuilder surveyPayloadIdBuilder = 
				new StringBuilder("urn:entra:");
			surveyPayloadIdBuilder.append(method);
			generator.writeStringField(
				"payload_id", 
				surveyPayloadIdBuilder.toString());
			
			// Set the payload version. For now, all surveys have 
			// the same version, 1.
			generator.writeStringField(
				"payload_version", 
				"1");
			
			// Set the payload definition.
			generator.writeFieldName("payload_definition"); 
			toConcordia(generator);

			// End the Entra definition.
			generator.writeEndObject();
		}
		
		/**
		 * Generates the Concordia schema for this path.
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
		public abstract JsonGenerator toConcordia(
			final JsonGenerator generator)
			throws JsonGenerationException, IOException;
		
		/**
		 * Returns the method String.
		 * 
		 * @return The method String.
		 */
		public String getMethod() {
			return method;
		}
		
		/**
		 * Makes the request to the API and stores the received data.
		 * 
		 * @param appId The Entra-generated ID for ohmage.
		 * 
		 * @param appPassword The Entra-generated password for ohmage.
		 * 
		 * @param appSource The Entra-generated 'source' value for ohmage.
		 * 
		 * @param userName The username of the user whose data is desired.
		 * 
		 * @param userPassword The password for the user whose data is desired.
		 * 
		 * @param startDate Limits the data to only those points on or after
		 * 					this date and time.
		 * 
		 * @param endDate Limits the data to only those points on or before 
		 * 				  this date and time.
		 * 
		 * @param numToSkip This represents the number of records that will be
		 * 					skipped. Records are in reverse-chronological
		 * 					order.
		 * 
		 * @param numToReturn This represents the number of records that will 
		 * 					  be returned. This is processed after records have
		 * 					  been skipped.
		 * 
		 * @throws DomainException There was an error making the call.
		 */
		public final void service(
				final String appId,
				final String appPassword,
				final String appSource,
				final String userName,
				final String userPassword,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException {
			
			if(madeRequest) {
				return;
			}
			
			makeRequest( 
				appId,
				appPassword,
				appSource,
				userName,
				userPassword,
				startDate, 
				endDate, 
				numToSkip, 
				numToReturn);
			
			madeRequest = true;
		}
		
		/**
		 * Makes the request to the API and stores the received data. This will
		 * be called while the {@link #service(DateTime, DateTime, long, long)}
		 * call is being made. This allows the superclass to do some work
		 * before the subclasses make their call.
		 * 
		 * @param appId The Entra-generated ID for ohmage.
		 * 
		 * @param appPassword The Entra-generated password for ohmage.
		 * 
		 * @param appSource The Entra-generated 'source' value for ohmage.
		 * 
		 * @param userName The username of the user whose data is desired.
		 * 
		 * @param userPassword The password for the user whose data is desired.
		 * 
		 * @param startDate Limits the data to only those points on or after
		 * 					this date and time.
		 * 
		 * @param endDate Limits the data to only those points on or before 
		 * 				  this date and time.
		 * 
		 * @param numToSkip This represents the number of records that will be
		 * 					skipped. Records are in reverse-chronological
		 * 					order.
		 * 
		 * @param numToReturn This represents the number of records that will 
		 * 					  be returned. This is processed after records have
		 * 					  been skipped.
		 * 
		 * @throws DomainException There was an error making the call.
		 */
		protected abstract void makeRequest(
			final String appId,
			final String appPassword,
			final String appSource,
			final String userName,
			final String userPassword,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws DomainException;
		
		/**
		 * Builds and makes the HTTP GET request. This will return the data as
		 * a string.
		 * 
		 * @return The response from Entra as a String.
		 * 
		 * @throws DomainException There was a problem making the request.
		 */
		protected final String makeRequest(
				final URI uri)
				throws DomainException {
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(uri);
			
			HttpResponse httpResponse;
			try {
				httpResponse = httpClient.execute(httpGet);
			}
			catch(ClientProtocolException e) {
				throw new DomainException("There was an HTTP error.", e);
			}
			catch(IOException e) {
				throw new DomainException(
					"There was an error communicating with the server.",
					e);
			}
			
			try {
				return 
					(new BasicResponseHandler()).handleResponse(httpResponse);
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
		}
	}
	
	/**
	 * A {@link EntraMethod} for the user's glucose meter readings.
	 *
	 * @author John Jenkins
	 */
	public static class GlucoseMethod extends EntraMethod {
		/**
		 * The name of the method to be called.
		 */
		private static final String METHOD = "getGlucose";
		
		/**
		 * The parameter that indicates the type of glucose reading.
		 */
		private static final String PARAM_TYPE_ID = "typeID";
		/**
		 * The parameter that indicates the earliest point at which results
		 * should be returned.
		 */
		private static final String PARAM_START_DATE = "fromDate";
		/**
		 * The parameter that indicates the earliest point at which results
		 * should be returned.
		 */
		private static final String PARAM_END_DATE = "toDate";
		
		/**
		 * The date-time formatter for our concatenation of the date and time.
		 */
		private static final DateTimeFormatter DATE_TIME_CONCAT_FORMATTER =
			DateTimeFormat.forPattern(DATE_TIME_PATTERN + "'T'" + "HH:mm");
		
		/**
		 * This class represents a single data point returned from the API.
		 *
		 * @author John Jenkins
		 */
		private static final class Result {
			private static final String JSON_KEY_GLUCOSE = "glucose";
			private static final String JSON_KEY_TEST_EVENT = "testevent";
			private static final String JSON_KEY_COMMENT = "comment";
			
			/**
			 * The different types of test events. This enum contains both the
			 * code for each event and a human-readable string value.
			 *
			 * @author John Jenkins
			 */
			private static enum TestEvent {
				BEFORE_BREAKFAST (1, "Before breakfast"),
				AFTER_BREAKFAST (2, "After breakfast"),
				BEFORE_LUNCH (3, "Before lunch"),
				AFTER_LUNCH (4, "After lunch"),
				BEFORE_DINNER (5, "Before dinner"),
				AFTER_DINNER (6, "After dinner"),
				EVENING (7, "Evening"),
				AFTER_EXERCISE (9, "After exercise"),
				AFTER_TAKING_MEDICATION (10, "After taking medication");
				
				private final Integer code;
				private final String value;
				
				/**
				 * Create a lookup table for faster access.
				 */
				private static final Map<Integer, TestEvent> LOOKUP =
					new HashMap<Integer, TestEvent>();
				static {
					for(TestEvent testEvent : values()) {
						LOOKUP.put(testEvent.code, testEvent);
					}
				}
				
				/**
				 * Creates a lookup table with an integer code and a readable
				 * string value.
				 * 
				 * @param code The code to lookup.
				 * 
				 * @param value The value to display to the user.
				 */
				private TestEvent(final Integer code, final String value) {
					if(code == null) {
						throw
							new IllegalArgumentException("The code was null.");
					}
					if(value == null) {
						throw
							new IllegalArgumentException(
								"The value was null.");
					}
					if(value.trim().length() == 0) {
						throw
							new IllegalArgumentException(
								"The value was only whitespace.");
					}
					
					this.code = code;
					this.value = value;
				}
				
				/**
				 * Returns the human-readable value for this test event.
				 * 
				 * @return The human-readable value for this test event.
				 */
				public String getValue() {
					return value;
				}
				
				/**
				 * Looks up the test event based on its code.
				 * 
				 * @param code The code to use to lookup the test event.
				 * 
				 * @return The test event or null if none match the given code.
				 */
				public static final TestEvent getTestEvent(
						final Integer code) {
					
					return LOOKUP.get(code);
				}
			}
			
			private String id;
			private DateTime timestamp;
			private int glucose;
			private TestEvent testEvent;
			private String comment = null;
			
			/**
			 * Generates the Concordia schema for this method.
			 * 
			 * @param generator The generator to use to write the definition.
			 * 
			 * @return The 'generator' that was passed in to facilitate
			 * 		   chaining.
			 * 
			 * @throws JsonGenerationException There was a problem generating 
			 * 								   the JSON.
			 * 
			 * @throws IOException There was a problem writing to the 
			 * 					   generator.
			 */
			public static JsonGenerator toConcordia(
					final JsonGenerator generator)
					throws JsonGenerationException, IOException {
				
				// Begin the definition.
				generator.writeStartObject();
				
				// The type of the data is a JSON object.
				generator.writeStringField("type", "object");
				
				// Define that object.
				generator.writeArrayFieldStart("schema");
				
				// Add the "birthday" field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_GLUCOSE);
				generator.writeStringField("type", "number");
				generator.writeEndObject();
				
				// Add the "birthday" field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_TEST_EVENT);
				generator.writeStringField("type", "string");
				generator.writeEndObject();
				
				// Add the "birthday" field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_COMMENT);
				generator.writeStringField("type", "string");
				generator.writeBooleanField("optional", true);
				generator.writeEndObject();
				
				// Finish defining the overall object.
				generator.writeEndArray();
				
				// End the definition.
				generator.writeEndObject();
				
				// Return the generator to facilitate chaining.
				return generator;
			}
		}
		List<Result> results = new LinkedList<Result>();
		
		/**
		 * Creates a {@link EntraMethod} for the user's glucose readings.
		 */
		public GlucoseMethod() {
			super(METHOD);
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#hasId()
		 */
		@Override
		public boolean hasId() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#hasTimestamp()
		 */
		@Override
		public boolean hasTimestamp() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#hasLocation()
		 */
		@Override
		public boolean hasLocation() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#toConcordia(org.codehaus.jackson.JsonGenerator)
		 */
		@Override
		public JsonGenerator toConcordia(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			return Result.toConcordia(generator);
		}

		@Override
		protected void makeRequest(
				final String appId,
				final String appPassword,
				final String appSource,
				final String userName,
				final String userPassword,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException {
			
			// Build the request based on the API's URI.
			StringBuilder uriBuilder = new StringBuilder(API_URI);
			uriBuilder.append('?');
			
			// Add the ohamge ID.
			uriBuilder.append(PARAM_APP_ID).append('=').append(appId);
			
			// Add the ohmage password.
			uriBuilder
				.append('&')
					.append(PARAM_APP_PASSWORD)
						.append('=').append(appPassword);
			
			// Add the ohmage 'source'.
			uriBuilder
				.append('&')
					.append(PARAM_APP_SOURCE).append('=').append(appSource);
			
			// Add the user's username.
			uriBuilder
				.append('&')
					.append(PARAM_USER_NAME).append('=').append(userName);
			
			// Add the user's password.
			uriBuilder
				.append('&')
					.append(PARAM_USER_PASSWORD)
						.append('=').append(userPassword);
			
			// Add the method.
			uriBuilder
				.append('&')
					.append(PARAM_METHOD).append('=').append(getMethod());
			
			// The type ID must always be added, even though there is only one
			// value.
			uriBuilder
				.append('&').append(PARAM_TYPE_ID).append('=').append('1');
			
			// Add some start date.
			uriBuilder.append('&').append(PARAM_START_DATE).append('=');
			if(startDate == null) {
				uriBuilder.append(DATE_TIME_FORMATTER.print(new DateTime()));
			}
			else {
				uriBuilder.append(DATE_TIME_FORMATTER.print(startDate));
			}
			
			// Add the end date if it is present.
			if(endDate != null) {
				uriBuilder
					.append('&')
						.append(PARAM_END_DATE)
							.append('=')
								.append(DATE_TIME_FORMATTER.print(endDate));
			}
			
			// Get the result of the request.
			String result;
			try {
				result = makeRequest(new URI(uriBuilder.toString()));
			}
			catch(URISyntaxException e) {
				throw new DomainException("The URI was invalid.", e);
			}
			
			// Parse the result as XML. 
			Document document;
			try {
				document = (new Builder()).build(new StringReader(result));
			} 
			catch(IOException e) {
				// This should only be thrown if it can't read the 'xml', but
				// given that it is already in memory this should never happen.
				throw new DomainException("XML was unreadable.", e);
			}
			catch(XMLException e) {
				throw
					new DomainException(
						"No usable XML parser could be found.",
						e);
			}
			catch(ValidityException e) {
				throw new DomainException("The XML is invalid.", e);
			}
			catch(ParsingException e) {
				throw new DomainException("The XML is not well formed.", e);
			}
			
			// Get the set of "record" nodes.
			Element root = document.getRootElement();
			Nodes records = root.query("record");
			
			// Calculate the last index to return.
			long endIndex = numToSkip + numToReturn;
			int numRecords = records.size();
			if(endIndex > numRecords) {
				endIndex = numRecords;
			}
			
			// Cycle through the appropriate number of records and add them to
			// the set of results.
			for(long i = numToSkip; i < endIndex; i++) {
				// Get the node.
				Node node = records.get((int) i);
				
				// Create the Result object.
				Result currResult = new Result();
				
				// Convert the node into a Result object.
				// Get the ID.
				Nodes idNodes = node.query("id");
				if(idNodes.size() == 0) {
					throw
						new DomainException(
							"No ID was returned for the record.");
				}
				else if(idNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple IDs were returned for the record.");
				}
				else {
					currResult.id = idNodes.get(0).getValue().trim();
				}
				
				// Get the timestamp.
				String date, time;
				// First, get the date.
				Nodes dateNodes = node.query("testdate");
				if(dateNodes.size() == 0) {
					throw
						new DomainException(
							"No test date was returned for the record.");
				}
				else if(dateNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple test dates were returned for the record.");
				}
				else {
					date = dateNodes.get(0).getValue().trim();
				}
				// Then, get the time.
				Nodes timeNodes = node.query("testtime");
				if(timeNodes.size() == 0) {
					throw
						new DomainException(
							"No test time was returned for the record.");
				}
				else if(timeNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple test times were returned for the record.");
				}
				else {
					time = timeNodes.get(0).getValue().trim();
				}
				// Finally, concatenate the two and parse it using our parser.
				currResult.timestamp =
					DATE_TIME_CONCAT_FORMATTER
						.parseDateTime(date + "T" + time);
				
				// Get the glucose value.
				Nodes glucoseNodes = node.query("glucose");
				if(glucoseNodes.size() == 0) {
					throw
						new DomainException(
							"No glucose value was returned for the record.");
				}
				else if(glucoseNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple glucose values were returned for the record.");
				}
				else {
					try {
						currResult.glucose = 
							Integer
								.decode(glucoseNodes.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The glucose value was not a number.",
								e);
					}
				}
				
				// Get the test event.
				Nodes testEventNodes = node.query("testevent");
				if(testEventNodes.size() == 0) {
					throw
						new DomainException(
							"No test event value was returned for the record.");
				}
				else if(testEventNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple test event values were returned for the record.");
				}
				else {
					int testEventCode;
					try {
						testEventCode =
							Integer
								.decode(
									testEventNodes.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The glucose value was not a number.",
								e);
					}
					
					currResult.testEvent =
						Result.TestEvent.getTestEvent(testEventCode);
					if(currResult.testEvent == null) {
						throw
							new DomainException(
								"The test event code is unkown: " + 
									testEventCode);
					}
				}
				
				// Get the comment.
				Nodes commentNodes = node.query("comment");
				if(commentNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple comments were returned for the record.");
				}
				else if(commentNodes.size() == 1) {
					currResult.comment = commentNodes.get(0).getValue().trim();
				}
				
				results.add(currResult);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadResponder#getNumDataPoints()
		 */
		@Override
		public long getNumDataPoints() {
			return results.size();
		}

		@Override
		public void respond(
				final JsonGenerator generator,
				final ColumnNode<String> columns)
				throws JsonGenerationException, IOException, DomainException {
			
			// Create the reusable DateTimeFormatter. 
			DateTimeFormatter isoDateTimeFormatter = 
				ISODateTimeFormat.dateTime();
			
			// Output the results.
			for(Result result : results) {
				// Start the overall object.
				generator.writeStartObject();
				
				// Write the metadata.
				generator.writeObjectFieldStart("metadata");
				
				// Write the ID.
				generator.writeStringField("id", result.id);
				
				// Write the timestamp.
				generator
					.writeStringField(
						"timestamp", 
						isoDateTimeFormatter.print(result.timestamp));
				
				// End the metadata object.
				generator.writeEndObject();
				
				// Write the data.
				generator.writeObjectFieldStart("data");
				
				// Write the 'glucose' field.
				generator
					.writeNumberField(
						Result.JSON_KEY_GLUCOSE,
						result.glucose);
				
				// Write the 'test event' field.
				generator
					.writeStringField(
						Result.JSON_KEY_TEST_EVENT,
						result.testEvent.getValue());
				
				// Write the 'comments' field.
				if(result.comment != null) {
					generator
						.writeStringField(
							Result.JSON_KEY_COMMENT,
							result.comment);
				}
				
				// End the data.
				generator.writeEndObject();
				
				// End the overall object.
				generator.writeEndObject();
			}
		}
	}
	
	/**
	 * A {@link EntraMethod} for the user's Entra data.
	 *
	 * @author John Jenkins
	 */
	public abstract static class FitnessMethod extends EntraMethod {
		/**
		 * This Entra method's method name.
		 */
		private static final String METHOD = "getData";
		
		/**
		 * The parameter that indicates the earliest point at which results
		 * should be returned.
		 */
		private static final String PARAM_START_DATE = "fromDate";
		/**
		 * The parameter that indicates the earliest point at which results
		 * should be returned.
		 */
		private static final String PARAM_END_DATE = "toDate";
		
		/**
		 * The date-time formatter for our concatenation of the date and time.
		 */
		private static final DateTimeFormatter DATE_TIME_CONCAT_FORMATTER =
			DateTimeFormat.forPattern(DATE_TIME_PATTERN + "'T'" + "HH:mm");
		
		private static final String JSON_KEY_TYPE = "type";
		private static final String JSON_KEY_TIME_SLOT = "timeslot";
		private static final String JSON_KEY_COMMENT = "comment";
		/*
		 * The keys that represent the values. 
		 */
		private static final String JSON_KEY_VAL1 = "val1";
		private static final String JSON_KEY_VAL2 = "val2";
		private static final String JSON_KEY_VAL3 = "val3";
		private static final String JSON_KEY_VAL4 = "val4";
		private static final String JSON_KEY_VAL5 = "val5";
		private static final String JSON_KEY_VAL6 = "val6";
		
		/**
		 * The different types of data. This is used to decode what the data
		 * means.
		 */
		private static final Map<Integer, String> TYPES = 
			new HashMap<Integer, String>();
		static {
			TYPES.put(20, "General Wellness");
			TYPES.put(21, "Blood Pressure");
			TYPES.put(23, "Weight/Height");
			TYPES.put(24, "Diet");
			TYPES.put(26, "Body measurements");
			TYPES.put(29, "Walk/Run/Bike");
			TYPES.put(30, "Weights");
			TYPES.put(31, "Sports");
			TYPES.put(32, "Cardio exercise");
			TYPES.put(33, "Fitness class");
		}
		
		/**
		 * The different types of time slots.
		 */
		private static final Map<Integer, String> TIME_SLOTS =
			new HashMap<Integer, String>();
		static {
			TIME_SLOTS.put(1, "Before breakfast");
			TIME_SLOTS.put(2, "After breakfast");
			TIME_SLOTS.put(3, "Before lunch");
			TIME_SLOTS.put(4, "After lunch");
			TIME_SLOTS.put(5, "Before dinner");
			TIME_SLOTS.put(6, "After dinner");
			TIME_SLOTS.put(7, "Evening");
		}
		
		/**
		 * This defines what a result object must do in order to decode its own
		 * results and return them in a logical fashion.
		 *
		 * @author John Jenkins
		 */
		private static class Result {
			private String id;
			private DateTime timestamp;
			private String type;
			private String timeSlot;
			private String comment;
			private Double[] vals = new Double[6];
		}
		/**
		 * The results of this query are a list of subclasses of this object.
		 */
		private final List<Result> results = new LinkedList<Result>();
		
		/**
		 * Creates a {@link EntraMethod} for the user's Entra data.
		 */
		private FitnessMethod() {
			super(METHOD);
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#hasId()
		 */
		@Override
		public boolean hasId() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#hasTimestamp()
		 */
		@Override
		public boolean hasTimestamp() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#hasLocation()
		 */
		@Override
		public boolean hasLocation() {
			return false;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#toConcordia(org.codehaus.jackson.JsonGenerator)
		 */
		@Override
		public JsonGenerator toConcordia(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			// Start the definition.
			generator.writeStartObject();
			
			// The data will always be a JSON object.
			generator.writeStringField("type", "object");
			generator.writeArrayFieldStart("schema");
			
			// Add the 'type' field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_TYPE);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the 'timeslot' field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_TIME_SLOT);
			generator.writeStringField("type", "string");
			generator.writeEndObject();
			
			// Add the 'comment' field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_COMMENT);
			generator.writeStringField("type", "string");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the 'val1' field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_VAL1);
			generator.writeStringField("type", "number");
			generator.writeEndObject();
			
			// Add the 'val2' field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_VAL2);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the 'val3' field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_VAL3);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the 'val4' field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_VAL4);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the 'val5' field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_VAL5);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// Add the 'val6' field.
			generator.writeStartObject();
			generator.writeStringField("name", JSON_KEY_VAL6);
			generator.writeStringField("type", "number");
			generator.writeBooleanField("optional", true);
			generator.writeEndObject();
			
			// End the overall schema array.
			generator.writeEndArray();
			
			// End the definition.
			generator.writeEndObject();
			
			// Return the generator.
			return generator;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#makeRequest(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.joda.time.DateTime, org.joda.time.DateTime, long, long)
		 */
		@Override
		protected void makeRequest(
				final String appId,
				final String appPassword,
				final String appSource,
				final String userName,
				final String userPassword,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException {
			
			// Build the request based on the API's URI.
			StringBuilder uriBuilder = new StringBuilder(API_URI);
			uriBuilder.append('?');
			
			// Add the ohamge ID.
			uriBuilder.append(PARAM_APP_ID).append('=').append(appId);
			
			// Add the ohmage password.
			uriBuilder
				.append('&')
					.append(PARAM_APP_PASSWORD)
						.append('=').append(appPassword);
			
			// Add the ohmage 'source'.
			uriBuilder
				.append('&')
					.append(PARAM_APP_SOURCE).append('=').append(appSource);
			
			// Add the user's username.
			uriBuilder
				.append('&')
					.append(PARAM_USER_NAME).append('=').append(userName);
			
			// Add the user's password.
			uriBuilder
				.append('&')
					.append(PARAM_USER_PASSWORD)
						.append('=').append(userPassword);
			
			// Add the method.
			uriBuilder
				.append('&')
					.append(PARAM_METHOD).append('=').append(getMethod());
			
			// Add some start date.
			uriBuilder.append('&').append(PARAM_START_DATE).append('=');
			if(startDate == null) {
				uriBuilder.append(DATE_TIME_FORMATTER.print(new DateTime()));
			}
			else {
				uriBuilder.append(DATE_TIME_FORMATTER.print(startDate));
			}
			
			// Add the end date if it is present.
			if(endDate != null) {
				uriBuilder
					.append('&')
						.append(PARAM_END_DATE)
							.append('=')
								.append(DATE_TIME_FORMATTER.print(endDate));
			}
			
			// Get the result of the request.
			String result;
			try {
				result = makeRequest(new URI(uriBuilder.toString()));
			}
			catch(URISyntaxException e) {
				throw new DomainException("The URI was invalid.", e);
			}
			
			// FIXME: Remove this. This will check to see if the documentation
			// is incorrect or of the typeID is actually outside of the record
			// tags.
			LOGGER.debug("Result: " + result);
			
			// Parse the result as XML. 
			Document document;
			try {
				document = (new Builder()).build(new StringReader(result));
			} 
			catch(IOException e) {
				// This should only be thrown if it can't read the 'xml', but
				// given that it is already in memory this should never happen.
				throw new DomainException("XML was unreadable.", e);
			}
			catch(XMLException e) {
				throw
					new DomainException(
						"No usable XML parser could be found.",
						e);
			}
			catch(ValidityException e) {
				throw new DomainException("The XML is invalid.", e);
			}
			catch(ParsingException e) {
				throw new DomainException("The XML is not well formed.", e);
			}
			
			// Get the root of the XML.
			Element root = document.getRootElement();
			// Get all of the records.
			Nodes records = root.query("record");
			
			// Calculate the last index to return.
			long endIndex = numToSkip + numToReturn;
			int numRecords = records.size();
			if(endIndex > numRecords) {
				endIndex = numRecords;
			}
			
			// Cycle through the appropriate number of records and add them to
			// the set of results.
			for(long i = numToSkip; i < endIndex; i++) {
				// Get the node.
				Node node = records.get((int) i);
				
				// Create the Result object.
				Result currResult = new Result();
				
				// Convert the node into a Result object.
				// Get the ID.
				Nodes idNodes = node.query("id");
				if(idNodes.size() == 0) {
					throw
						new DomainException(
							"No ID was returned for the record.");
				}
				else if(idNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple IDs were returned for the record.");
				}
				else {
					currResult.id = idNodes.get(0).getValue().trim();
				}
				
				// Get the timestamp.
				String date, time;
				// First, get the date.
				Nodes dateNodes = node.query("date");
				if(dateNodes.size() == 0) {
					throw
						new DomainException(
							"No date was returned for the record.");
				}
				else if(dateNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple dates were returned for the record.");
				}
				else {
					date = dateNodes.get(0).getValue().trim();
				}
				// Then, get the time.
				Nodes timeNodes = node.query("time");
				if(timeNodes.size() == 0) {
					throw
						new DomainException(
							"No time was returned for the record.");
				}
				else if(timeNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple times were returned for the record.");
				}
				else {
					time = timeNodes.get(0).getValue().trim();
				}
				// Finally, concatenate the two and parse it using our parser.
				currResult.timestamp =
					DATE_TIME_CONCAT_FORMATTER
						.parseDateTime(date + "T" + time);
				
				// Get the type ID.
				Nodes typeIds = node.query("typeid");
				if(typeIds.size() == 0) {
					throw
						new DomainException(
							"No type ID was returned for the record.");
				}
				else if(typeIds.size() > 1) {
					throw
						new DomainException(
							"Multiple type IDs were returned for the record.");
				}
				else {
					try {
						int typeId = 
							Integer.decode(typeIds.get(0).getValue().trim());
						currResult.type = TYPES.get(typeId);
						
						if(currResult.type == null) {
							throw
								new DomainException(
									"The type ID is unknown: " + typeId);
						}
					}
					catch(NumberFormatException e) {
						throw new DomainException(
							"The type ID is missing.",
							e);
					}
				}
				
				// Get the timeslot.
				Nodes timeslots = node.query("timeslot");
				if(timeslots.size() == 0) {
					throw
						new DomainException(
							"No timeslot was returned for the record.");
				}
				else if(timeslots.size() > 1) {
					throw
						new DomainException(
							"Multiple timeslots were returned for the record.");
				}
				else {
					try {
						int timeslotId = 
							Integer.decode(timeslots.get(0).getValue().trim());
						currResult.timeSlot = TIME_SLOTS.get(timeslotId);
						
						if(currResult.timeSlot == null) {
							throw
								new DomainException(
									"The timeslot ID is unknown: " + 
										timeslotId);
						}
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The timeslot ID is missing.",
								e);
					}
				}
				
				// Get the comment.
				Nodes commentNodes = node.query("comment");
				if(commentNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple comments were returned for the record.");
				}
				else if(commentNodes.size() == 1) {
					currResult.comment = commentNodes.get(0).getValue().trim();
				}
				
				// Get the val1.
				Nodes val1s = node.query("val1");
				if(val1s.size() > 1) {
					throw
						new DomainException(
							"Multiple val1's were returned for the record.");
				}
				else if(val1s.size() == 1) {
					try {
						currResult.vals[0] =
							Double
								.parseDouble(
									commentNodes.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for val1 was not a number.",
								e);
					}
				}
				else {
					currResult.vals[0] = null;
				}
				
				// Get the val2.
				Nodes val2s = node.query("val2");
				if(val2s.size() > 1) {
					throw
						new DomainException(
							"Multiple val2's were returned for the record.");
				}
				else if(val2s.size() == 1) {
					try {
						currResult.vals[1] =
							Double
								.parseDouble(
									commentNodes.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for val2 was not a number.",
								e);
					}
				}
				else {
					currResult.vals[1] = null;
				}
				
				// Get the val3.
				Nodes val3s = node.query("val3");
				if(val3s.size() > 1) {
					throw
						new DomainException(
							"Multiple val3's were returned for the record.");
				}
				else if(val3s.size() == 1) {
					try {
						currResult.vals[2] =
							Double
								.parseDouble(
									commentNodes.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for val3 was not a number.",
								e);
					}
				}
				else {
					currResult.vals[2] = null;
				}
				
				// Get the val4.
				Nodes val4s = node.query("val4");
				if(val4s.size() > 1) {
					throw
						new DomainException(
							"Multiple val4's were returned for the record.");
				}
				else if(val4s.size() == 1) {
					try {
						currResult.vals[3] =
							Double
								.parseDouble(
									commentNodes.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for val4 was not a number.",
								e);
					}
				}
				else {
					currResult.vals[3] = null;
				}
				
				// Get the val5.
				Nodes val5s = node.query("val5");
				if(val5s.size() > 1) {
					throw
						new DomainException(
							"Multiple val5's were returned for the record.");
				}
				else if(val5s.size() == 1) {
					try {
						currResult.vals[4] =
							Double
								.parseDouble(
									commentNodes.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for val5 was not a number.",
								e);
					}
				}
				else {
					currResult.vals[4] = null;
				}
				
				// Get the val6.
				Nodes val6s = node.query("val6");
				if(val6s.size() > 1) {
					throw
						new DomainException(
							"Multiple val6's were returned for the record.");
				}
				else if(val6s.size() == 1) {
					try {
						currResult.vals[5] =
							Double
								.parseDouble(
									commentNodes.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for val6 was not a number.",
								e);
					}
				}
				else {
					currResult.vals[5] = null;
				}
				
				// Finally, add this result to the set of results.
				results.add(currResult);
			}
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

			// Create the reusable DateTimeFormatter. 
			DateTimeFormatter isoDateTimeFormatter = 
				ISODateTimeFormat.dateTime();
			
			// Output the results.
			for(Result result : results) {
				// Start the overall object.
				generator.writeStartObject();
				
				// Write the metadata.
				generator.writeObjectFieldStart("metadata");
				
				// Write the ID.
				generator.writeStringField("id", result.id);
				
				// Write the timestamp.
				generator
					.writeStringField(
						"timestamp", 
						isoDateTimeFormatter.print(result.timestamp));
				
				// End the metadata object.
				generator.writeEndObject();
				
				// Write the data.
				generator.writeObjectFieldStart("data");
				
				// Write the 'type' field.
				generator.writeStringField(JSON_KEY_TYPE, result.type);
				
				// Write the 'timeslot' field.
				generator
					.writeStringField(JSON_KEY_TIME_SLOT, result.timeSlot);
				
				// Write the 'comment' field.
				if(result.comment != null) {
					generator
						.writeStringField(JSON_KEY_COMMENT, result.comment);
				}
				
				// Write the 'val1' field.
				if(result.vals[0] != null) {
					generator.writeNumberField(JSON_KEY_VAL1, result.vals[0]);
				}
				
				// Write the 'val2' field.
				if(result.vals[1] != null) {
					generator.writeNumberField(JSON_KEY_VAL2, result.vals[1]);
				}
				
				// Write the 'val3' field.
				if(result.vals[2] != null) {
					generator.writeNumberField(JSON_KEY_VAL3, result.vals[2]);
				}
				
				// Write the 'val4' field.
				if(result.vals[3] != null) {
					generator.writeNumberField(JSON_KEY_VAL4, result.vals[3]);
				}
				
				// Write the 'val5' field.
				if(result.vals[4] != null) {
					generator.writeNumberField(JSON_KEY_VAL5, result.vals[4]);
				}
				
				// Write the 'val6' field.
				if(result.vals[5] != null) {
					generator.writeNumberField(JSON_KEY_VAL6, result.vals[5]);
				}
				
				// End the data.
				generator.writeEndObject();
				
				// End the overall object.
				generator.writeEndObject();
			}
		}
	}
	
	/**
	 * A factory class for generated {@link EntraMethod} objects.
	 *
	 * @author John Jenkins
	 */
	public static enum EntraMethodFactory {
		GLUCOSE (GlucoseMethod.METHOD, GlucoseMethod.class),
		FITNESS_METHOD (FitnessMethod.METHOD, FitnessMethod.class);
		
		private final String methodString;
		private final Class<? extends EntraMethod> methodClass;
		
		/**
		 * The mapping of method strings to their corresponding objects.
		 */
		private static final Map<String, EntraMethodFactory> FACTORY = 
			new HashMap<String, EntraMethodFactory>();
		static {
			// Populate the 'FACTORY' object.
			for(EntraMethodFactory factory : values()) {
				FACTORY.put(factory.methodString, factory);
			}
		}
		
		/**
		 * Default constructor made private to prevent instantiation.
		 * 
		 * @param methodString The method's string value.
		 * 
		 * @param methodClass The class the implements this method.
		 */
		private EntraMethodFactory(
				final String methodString, 
				final Class<? extends EntraMethod> methodClass) {
			
			if(methodString == null) {
				throw
					new IllegalArgumentException("The method string is null.");
			}
			if(methodString.trim().length() == 0) {
				throw new IllegalArgumentException(
					"The method string is all whitespace.");
			}
			if(methodClass == null) {
				throw new IllegalArgumentException(
					"The method class is null.");
			}
			
			this.methodString = methodString;
			this.methodClass = methodClass;
		}
		
		/**
		 * Returns the method's string value.
		 * 
		 * @return The method's string value.
		 */
		public final String getApi() {
			return methodString;
		}
		
		/**
		 * Returns a new instance of the EntraMethod object specified by the
		 * 'method' parameter.
		 * 
		 * @param method The string to use to lookup a RunKeeperApi object.
		 * 
		 * @return The EntraMethod object that corresponds to the 'method'
		 * 		   parameter.
		 * 
		 * @throws DomainException The method was unknown or there was an error
		 * 						   generating an instance of it.
		 */
		public static final EntraMethod getMethod(
				final String method)
				throws DomainException {
			
			if(FACTORY.containsKey(method)) {
				try {
					return FACTORY.get(method).methodClass.newInstance();
				}
				catch(InstantiationException e) {
					throw new DomainException(
						"The Class for the method cannot be instantiated: " +
							method,
						e);
				}
				catch(IllegalAccessException e) {
					throw new DomainException(
						"The Class for the method does not contain a no-argument constructor: " +
							method,
						e);
				}
				catch(SecurityException e) {
					throw new DomainException(
						"The security manager prevented instantiation for the method: " +
							method,
						e);
				}
				catch(ExceptionInInitializerError e) {
					throw new DomainException(
						"The constructor for the method threw an exception: " +
							method,
						e);
				}
			}
			
			throw new DomainException("The method is unknown: " + method);
		}
	}

	private final DateTime startDate;
	private final DateTime endDate;
	private final long numToSkip;
	private final long numToReturn;
	private final EntraMethod method;
	
	/**
	 * Creates a request to read an Entra API.
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
	 * @param method The method to be called.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 */
	public OmhReadEntraRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn,
			final String method)
			throws IOException, InvalidRequestException {
		
		super(
			httpRequest, 
			hashPassword, 
			tokenLocation, 
			parameters, 
			callClientRequester);
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMG read request for Entra.");
		}
		
		this.startDate = startDate;
		this.endDate = endDate;
		this.numToSkip = numToSkip;
		this.numToReturn = numToReturn;
		
		EntraMethod tMethod = null;
		try {
			tMethod = OmhValidators.validateEntraMethod(method);
		}
		catch(ValidationException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
		this.method = tMethod;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH read request for BodyMedia.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// TODO: Verify that the user is allowed to query this data through
			// ohmage.
			// This is being ignored for now because there is no "user" 
			// parameter. When the "user" parameter is added, the ACL states
			// that the requesting user must be a supervisor in any campaign to
			// which the user is a participant.
			// Note: This should not be pushed to a public server because, by
			// default everyone is a participant in every campaign. Therefore,
			// anyone who manages to elevate their privileges to supervisor of
			// a campaign will be able to execute this call against everyone
			// else in the system.
			
			// Get the authentication information from the database.
			LOGGER
				.info("Getting the authentication credentials for BodyMedia.");
			Map<String, String> entraCredentials =
				OmhServices.instance().getCredentials("entra");
			
			// Get the Entra-generated ohmage ID.
			String appId = entraCredentials.get("app_id");
			if(appId == null) {
				throw
					new ServiceException(
						"The Entra-supplied app ID is missing.");
			}
			
			// Get the Entra-generated ohmage password.
			String appPassword = entraCredentials.get("app_pw");
			if(appPassword == null) {
				throw
					new ServiceException(
						"The Entra-supplied app password is missing.");
			}
			
			// Get the Entra-generated 'source' value.
			String appSource = entraCredentials.get("app_source");
			if(appSource == null) {
				throw
					new ServiceException(
						"The Entra-supplied app source is missing.");
			}
			
			// Get the user's username.
			String userName = 
				entraCredentials.get(getUser().getUsername() + "_username");
			if(userName == null) {
				throw
					new ServiceException(
						"The user's username is missing.");
			}
			
			// Get the user's password.
			String userPassword = 
				entraCredentials.get(getUser().getUsername() + "_password");
			if(userPassword == null) {
				throw
					new ServiceException(
						"The user's password is missing.");
			}
			
			try {
				LOGGER.info("Calling the Entra API.");
				method
					.service(
						appId, 
						appPassword, 
						appSource, 
						userName, 
						userPassword, 
						startDate, 
						endDate, 
						numToSkip, 
						numToReturn);
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
		return method.getNumDataPoints();
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
		
		LOGGER.info("Responding to an OMH read request for Entra.");
		
		method.respond(generator, columns);
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