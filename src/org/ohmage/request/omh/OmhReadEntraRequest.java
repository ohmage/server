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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;
import org.ohmage.service.OmhServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
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
				new StringBuilder("omh:entra:");
			surveyPayloadIdBuilder.append(getPayloadIdMethod());
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
		 * Returns the name of the method used to construct the payload ID. 
		 * This may be different than the actual method passed to Entra.
		 * 
		 * @return The name of the method used to construct the payload ID.
		 */
		public abstract String getPayloadIdMethod();
		
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
		 * @return The root Element of the response parsed as XML.
		 * 
		 * @throws DomainException There was a problem making the request.
		 */
		protected final Element makeRequest(
				final String appId,
				final String appPassword,
				final String appSource,
				final String userName,
				final String userPassword,
				final DateTime startDate,
				final DateTime endDate)
				throws DomainException {
			
			// Build the request based on the API's URI.
			StringBuilder uriBuilder = new StringBuilder(API_URI);
			uriBuilder.append('?');
			
			// Create the parameters.
			List<BasicNameValuePair> params = 
				new LinkedList<BasicNameValuePair>();
			
			// Add the ohamge ID.
			params.add(new BasicNameValuePair(PARAM_APP_ID, appId));
			
			// Add the ohmage password.
			params
				.add(new BasicNameValuePair(PARAM_APP_PASSWORD, appPassword));
			
			// Add the ohmage 'source'.
			params.add(new BasicNameValuePair(PARAM_APP_SOURCE, appSource));
			
			// Add the user's username.
			params.add(new BasicNameValuePair(PARAM_USER_NAME, userName));
			
			// Add the user's password.
			params
				.add(
					new BasicNameValuePair(PARAM_USER_PASSWORD, userPassword));
			
			// Add the method.
			params.add(new BasicNameValuePair(PARAM_METHOD, getMethod()));
			
			// The type ID must always be added, even though there is only one
			// value.
			params
				.add(
					new BasicNameValuePair(
						PARAM_TYPE_ID,
						(new Integer(getTypeId()).toString())));
			
			// Add some start date.
			if(startDate == null) {
				params
					.add(
						new BasicNameValuePair(
							PARAM_START_DATE, 
							DATE_TIME_FORMATTER.print(new DateTime(0))));
			}
			else {
				params
					.add(
						new BasicNameValuePair(
							PARAM_START_DATE,
							DATE_TIME_FORMATTER.print(startDate)));
			}
			
			// Add the end date if it is present.
			if(endDate != null) {
				params
					.add(
						new BasicNameValuePair(
							PARAM_END_DATE,
							DATE_TIME_FORMATTER.print(endDate)));
			}
			
			// Add the parameters.
			uriBuilder.append(URLEncodedUtils.format(params, "UTF-8"));
			
			// Create the URI.
			URI uri;
			try {
				uri = new URI(uriBuilder.toString());
			}
			catch(URISyntaxException e) {
				throw new DomainException("The URI was invalid.", e);
			}
			
			// Create the GET request and the client to handle it.
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(uri);
			
			// Make the request.
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
			
			// Process the response for its content.
			String responseString;
			try {
				responseString =
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
			
			// Parse the result as XML. 
			Document document;
			try {
				document = 
					(new Builder()).build(new StringReader(responseString));
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
			return document.getRootElement();
		}
		
		/**
		 * Returns the type ID for a specific type in the request.
		 * 
		 * @return The specific type's ID.
		 */
		public abstract int getTypeId();
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
		 * The date-time formatter for our concatenation of the date and time.
		 */
		private static final DateTimeFormatter DATE_TIME_CONCAT_FORMATTER =
			DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");
		
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
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#getPayloadIdMethod()
		 */
		@Override
		public String getPayloadIdMethod() {
			return METHOD;
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

			// Get the set of "record" nodes.
			Element root = 
				makeRequest(
					appId, 
					appPassword, 
					appSource, 
					userName, 
					userPassword, 
					startDate, 
					endDate);
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

		/**
		 * @return Always returns 1.
		 */
		@Override
		public int getTypeId() {
			return 1;
		}
	}
	
	/**
	 * A {@link EntraMethod} for the user's Entra data.
	 *
	 * @author John Jenkins
	 */
	public abstract static class DataMethod extends EntraMethod {
		/**
		 * This Entra method's method name.
		 */
		private static final String METHOD = "getData";
		
		/**
		 * The date-time formatter for our concatenation of the date and time.
		 */
		private static final DateTimeFormatter DATE_TIME_CONCAT_FORMATTER =
			DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm");
		
		/*
		 * The keys that represent the values. 
		 */
		private static final String XML_KEY_VAL1 = "val1";
		private static final String XML_KEY_VAL2 = "val2";
		private static final String XML_KEY_VAL3 = "val3";
		private static final String XML_KEY_VAL4 = "val4";
		private static final String XML_KEY_VAL5 = "val5";
		private static final String XML_KEY_VAL6 = "val6";
		
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
		 * Creates a {@link EntraMethod} for the user's Entra data.
		 */
		protected DataMethod() {
			super(METHOD);
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
			
			// Get the root of the XML.
			Element root = 
				makeRequest(
					appId, 
					appPassword, 
					appSource, 
					userName, 
					userPassword, 
					startDate, 
					endDate);
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
				
				// Convert the node into a Result object.
				// Get the ID.
				String id;
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
					id = idNodes.get(0).getValue().trim();
				}
				
				// Get the timestamp.
				DateTime timestamp;
				String date, time;
				// First, get the date.
				Nodes dateNodes = node.query("testdate");
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
				Nodes timeNodes = node.query("testtime");
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
				timestamp =
					DATE_TIME_CONCAT_FORMATTER
						.parseDateTime(date + "T" + time);
				
				// Get the timeslot.
				String timeSlot = null;
				Nodes timeslots = node.query("timeslot");
				if(timeslots.size() == 0) {
					throw
						new DomainException(
							"No timeslot was returned for the record.");
				}
				else if((timeslots.size() == 1) && (! StringUtils.isEmptyOrWhitespaceOnly(timeslots.get(0).getValue()))){
					try {
						int timeslotId = 
							Integer.decode(timeslots.get(0).getValue().trim());
						timeSlot = TIME_SLOTS.get(timeslotId);
						
						if(timeSlot == null) {
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
				String comment = null;
				Nodes commentNodes = node.query("comment");
				if(commentNodes.size() > 1) {
					throw
						new DomainException(
							"Multiple comments were returned for the record.");
				}
				else if(commentNodes.size() == 1) {
					comment = commentNodes.get(0).getValue().trim();
				}
				
				// Get the val1.
				Double val1 = null;
				Nodes val1s = node.query(XML_KEY_VAL1);
				if(val1s.size() > 1) {
					throw
						new DomainException(
							"Multiple " +
								XML_KEY_VAL1 +
								"'s were returned for the record.");
				}
				else if((val1s.size() == 1) && 
						(! StringUtils.isEmptyOrWhitespaceOnly(val1s.get(0).getValue()))) {
					
					try {
						val1 =
							Double.parseDouble(val1s.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for " +
									XML_KEY_VAL1 +
									" was not a number.",
								e);
					}
				}
				
				// Get the val2.
				Double val2 = null;
				Nodes val2s = node.query(XML_KEY_VAL2);
				if(val2s.size() > 1) {
					throw
						new DomainException(
							"Multiple " +
								XML_KEY_VAL2 +
								"'s were returned for the record.");
				}
				else if((val2s.size() == 1) && 
						(! StringUtils.isEmptyOrWhitespaceOnly(val2s.get(0).getValue()))) {
					try {
						val2 =
							Double.parseDouble(val2s.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for " +
									XML_KEY_VAL2 +
									" was not a number.",
								e);
					}
				}
				
				// Get the val3.
				Double val3 = null;
				Nodes val3s = node.query(XML_KEY_VAL3);
				if(val3s.size() > 1) {
					throw
						new DomainException(
							"Multiple " +
								XML_KEY_VAL3 +
								"'s were returned for the record.");
				}
				else if((val3s.size() == 1) && 
						(! StringUtils.isEmptyOrWhitespaceOnly(val3s.get(0).getValue()))) {
					try {
						val3 =
							Double.parseDouble(val3s.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for " +
									XML_KEY_VAL3 +
									" was not a number.",
								e);
					}
				}
				
				// Get the val4.
				Double val4 = null;
				Nodes val4s = node.query(XML_KEY_VAL4);
				if(val4s.size() > 1) {
					throw
						new DomainException(
							"Multiple " +
								XML_KEY_VAL4 +
								"'s were returned for the record.");
				}
				else if((val4s.size() == 1) && 
						(! StringUtils.isEmptyOrWhitespaceOnly(val4s.get(0).getValue()))) {
					try {
						val4 =
							Double.parseDouble(val4s.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for " +
									XML_KEY_VAL4 +
									" was not a number.",
								e);
					}
				}
				
				// Get the val5.
				Double val5 = null;
				Nodes val5s = node.query(XML_KEY_VAL5);
				if(val5s.size() > 1) {
					throw
						new DomainException(
							"Multiple " +
								XML_KEY_VAL5 +
								"'s were returned for the record.");
				}
				else if((val5s.size() == 1) && 
						(! StringUtils.isEmptyOrWhitespaceOnly(val5s.get(0).getValue()))) {
					try {
						val5 =
							Double.parseDouble(val5s.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for " +
									XML_KEY_VAL5 +
									" was not a number.",
								e);
					}
				}
				
				// Get the val6.
				Double val6 = null;
				Nodes val6s = node.query(XML_KEY_VAL6);
				if(val6s.size() > 1) {
					throw
						new DomainException(
							"Multiple " +
								XML_KEY_VAL6 +
								"'s were returned for the record.");
				}
				else if((val6s.size() == 1) && 
						(! StringUtils.isEmptyOrWhitespaceOnly(val6s.get(0).getValue()))) {
					try {
						val6 =
							Double.parseDouble(val6s.get(0).getValue().trim());
					}
					catch(NumberFormatException e) {
						throw
							new DomainException(
								"The value for " +
									XML_KEY_VAL6 +
									" was not a number.",
								e);
					}
				}
				
				// Finally, add this result to the set of results.
				handleResult(
					id, 
					timestamp, 
					timeSlot, 
					comment, 
					val1, 
					val2, 
					val3, 
					val4, 
					val5, 
					val6);
			}
		}
		
		/**
		 * Adds a result record for this request.
		 * 
		 * @param id The unique ID for the record.
		 * 
		 * @param timestamp The timestamp for the record.
		 * 
		 * @param timeSlot The String representing the time slot when this 
		 * 				   record was created.
		 * 
		 * @param comment The comment String for this record. This is optional
		 * 				  and may be null.
		 * 
		 * @param val1 The first value returned.
		 * 
		 * @param val2 The second value returned. This may be null if there was
		 * 			   no second value.
		 * 
		 * @param val3 The third value returned. This may be null if there was
		 * 			   no third value.
		 * 
		 * @param val4 The fourth value returned. This may be null if there was
		 * 			   no fourth value.
		 * 
		 * @param val5 The fifth value returned. This may be null if there was
		 * 			   no fifth value.
		 * 
		 * @param val6 The sixth value returned. This may be null if there was
		 * 			   no sixth value.
		 * 
		 * @throws DomainException One of the values was missing or invalid.
		 */
		protected abstract void handleResult(
			final String id,
			final DateTime timestamp,
			final String timeSlot,
			final String comment,
			final Double val1,
			final Double val2,
			final Double val3,
			final Double val4,
			final Double val5,
			final Double val6)
			throws DomainException;
	}
	
	/**
	 * This class uses the {@link DataMethod} class and asks specifically for
	 * the height, weight, and body fat readings.
	 *
	 * @author John Jenkins
	 */
	public static class HeightWeight extends DataMethod {
		/**
		 * Our method name which shadows the larger "getData" method from 
		 * Entra.
		 */
		private static final String METHOD = "getHeightWeight";
		
		/**
		 * This class represents a single data point for this method.
		 *
		 * @author John Jenkins
		 */
		private static final class Result {
			/*
			 * The JSON keys to output.
			 */
			private static final String JSON_KEY_TIME_SLOT = "timeslot";
			private static final String JSON_KEY_COMMENT = "comment";
			private static final String JSON_KEY_HEIGHT = "height";
			private static final String JSON_KEY_WEIGHT = "weight";
			private static final String JSON_KEY_BODY_FAT = "body_fat";
			
			private String id;
			private DateTime timestamp;
			
			@JsonProperty(JSON_KEY_TIME_SLOT)
			private String timeSlot;
			@JsonProperty(JSON_KEY_COMMENT)
			private String comment;
			@JsonProperty(JSON_KEY_HEIGHT)
			private Double height;
			@JsonProperty(JSON_KEY_WEIGHT)
			private double weight;
			@JsonProperty(JSON_KEY_BODY_FAT)
			private Double bodyFat;

			/**
			 * Writes the Concordia definition of this object to the generator
			 * and returns it when it is finished. The definition is a
			 * self-contained JSON object, meaning it both starts and ends the
			 * object.
			 * 
			 * @param generator The generator to use to write the definition.
			 * 
			 * @return The generator after it has finished.
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
				
				// Add the 'timeslot' field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_TIME_SLOT);
				generator.writeStringField("type", "string");
				generator.writeBooleanField("optional", true);
				generator.writeEndObject();
				
				// Add the 'comment' field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_COMMENT);
				generator.writeStringField("type", "string");
				generator.writeBooleanField("optional", true);
				generator.writeEndObject();
				
				// Add the 'val1' field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_HEIGHT);
				generator.writeStringField("type", "number");
				generator.writeBooleanField("optional", true);
				generator.writeEndObject();
				
				// Add the 'val2' field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_WEIGHT);
				generator.writeStringField("type", "number");
				generator.writeEndObject();
				
				// Add the 'val3' field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_BODY_FAT);
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
		}
		private final List<Result> results = new LinkedList<Result>();
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadEntraRequest.EntraMethod#getPayloadIdMethod()
		 */
		@Override
		public String getPayloadIdMethod() {
			return METHOD;
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
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadResponder#getNumDataPoints()
		 */
		@Override
		public long getNumDataPoints() {
			return results.size();
		}

		/**
		 * Adds a height and weight record to the list of results.
		 * 
		 * @param val1 The weight from the record.
		 * 
		 * @param val2 The height from the record.
		 * 
		 * @param val3 The body fat from the record.
		 * 
		 * @param val4 Unused.
		 * 
		 * @param val5 Unused.
		 * 
		 * @param val6 Unused.
		 */
		@Override
		protected void handleResult(
				final String id,
				final DateTime timestamp,
				final String timeSlot,
				final String comment,
				final Double val1,
				final Double val2,
				final Double val3,
				final Double val4,
				final Double val5,
				final Double val6)
				throws DomainException {
			
			if(val1 == null) {
				throw new DomainException("The weight is missing.");
			}
			
			Result result = new Result();
			result.id = id;
			result.timestamp = timestamp;
			result.timeSlot = timeSlot;
			result.comment = comment;
			result.weight = val1;
			result.height = val2;
			result.bodyFat = val3;
			results.add(result);
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
				generator.writeFieldName("data");
				
				// Write this data point.
				generator.writeObject(result);
				
				// End the overall object.
				generator.writeEndObject();
			}
		}

		/**
		 * @return Always returns the height and weight type ID, 23.
		 */
		@Override
		public int getTypeId() {
			return 23;
		}
	}
	
	/**
	 * A factory class for generated {@link EntraMethod} objects.
	 *
	 * @author John Jenkins
	 */
	public static enum EntraMethodFactory {
		GLUCOSE (GlucoseMethod.METHOD, GlucoseMethod.class),
		HEIGHT_WEIGHT (HeightWeight.METHOD, HeightWeight.class);
		
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
		public final String getMethod() {
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

	private final String owner;
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
			final String owner,
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
		
		this.owner = owner;
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
				.info("Getting the authentication credentials for BodyMedia.");
			Map<String, String> entraCredentials =
				OmhServices.instance().getCredentials("entra");
			
			// Get the Entra-generated ohmage ID.
			String appId = entraCredentials.get("app_id");
			if(appId == null) {
				throw
					new ServiceException(
						ErrorCode.OMH_ACCOUNT_NOT_LINKED,
						"The Entra-supplied app ID is missing.");
			}
			
			// Get the Entra-generated ohmage password.
			String appPassword = entraCredentials.get("app_pw");
			if(appPassword == null) {
				throw
					new ServiceException(
						ErrorCode.OMH_ACCOUNT_NOT_LINKED,
						"The Entra-supplied app password is missing.");
			}
			
			// Get the Entra-generated 'source' value.
			String appSource = entraCredentials.get("app_source");
			if(appSource == null) {
				throw
					new ServiceException(
						ErrorCode.OMH_ACCOUNT_NOT_LINKED,
						"The Entra-supplied app source is missing.");
			}
			
			// Switch on either the requester or the given username.
			String requestee = 
				((owner == null) ? getUser().getUsername() : owner);
			
			// Get the user's username.
			String userName = 
				entraCredentials.get(requestee + "_username");
			if(userName == null) {
				throw
					new ServiceException(
						ErrorCode.OMH_ACCOUNT_NOT_LINKED,
						"The user's Entra username is missing: " + requestee);
			}
			
			// Get the user's password.
			String userPassword = 
				entraCredentials.get(requestee + "_password");
			if(userPassword == null) {
				throw
					new ServiceException(
						ErrorCode.OMH_ACCOUNT_NOT_LINKED,
						"The user's Entra password is missing: " + requestee);
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