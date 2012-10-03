package org.ohmage.request.omh;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
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
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class OmhReadBodyMediaRequest 
		extends UserRequest
		implements OmhReadResponder {
	
	private static final Logger LOGGER =
		Logger.getLogger(OmhReadBodyMediaRequest.class);
	
	/**
	 * The base URL for all requests to the BodyMedia API.
	 */
	private static final String BASE_URL = "https://api.bodymedia.com/";
	
	/**
	 * This manages the OAuth URLs and creates a connection through BodyMedia's
	 * OAuth APIs.
	 *
	 * @author John Jenkins
	 */
	public static class BodyMediaOauthApi extends DefaultApi10a {
		// The APIs for the Scribe library use a general form of the URL and
		// then prepend "http://" or "https://" where necessary; however,
		// BodyMedia requires that all requests be HTTPS. So, the base URL
		// defines the HTTPS version and no HTTP versions are allowed.
		/**
		 * The base URL for all of the OAuth calls.
		 */
		private static final String OAUTH_BASE_URL = BASE_URL + "oauth";
		/**
		 * The URL for getting a request token.
		 */
		private static final String REQUEST_TOKEN_RESOURCE = 
			OAUTH_BASE_URL + "/request_token";
		/**
		 * The URL for retrieving where to redirect the user to authenticate
		 * the request token.
		 * 
		 * Note: This requires that the token
		 */
		private static final String AUTHORIZE_URL = 
			OAUTH_BASE_URL + "/authorize?oauth_token=";
		/**
		 * The URL that OAuth specifies each implementer must generate
		 * themselves. It is used where the user would submit their approval or
		 * rejection to the authentication request. We are doing this 
		 * programmatically because we cannot have user intervention.
		 */
		public static final String DO_LOGIN_RESOURCE =
			OAUTH_BASE_URL + "/doLogin";
		/**
		 * The URL for converting an authenticated request token into an access
		 * token.
		 */
		private static final String ACCESS_TOKEN_RESOURCE = 
			OAUTH_BASE_URL + "/access_token";
		
		/**
		 * Represents an OAuth access token and the date and time when it
		 * expires.
		 *
		 * @author John Jenkins
		 */
		private static final class TokenExpiryPair {
			/**
			 * The token.
			 */
			private final Token token;
			/**
			 * The date and time when the token expires.
			 */
			private final DateTime expiry;
			
			/**
			 * Builds a TokenExpiryPair object.
			 * 
			 * @param token The token.
			 * 
			 * @param expiry The date and time when the token expires.
			 * 
			 * @throws IllegalArgumentException The token and/or expiry were 
			 * 									null.
			 */
			public TokenExpiryPair(final Token token, final DateTime expiry) {
				if(token == null) {
					throw new IllegalArgumentException("The token is null.");
				}
				if(expiry == null) {
					throw new IllegalArgumentException("The expiry is null.");
				}
				
				this.token = token;
				this.expiry = expiry;
			}
			
			/**
			 * Returns the token.
			 * 
			 * @return The token.
			 */
			public Token getToken() {
				return token;
			}
			
			/**
			 * Returns the date and time when this token expires.
			 * 
			 * @return The date and time when this token expires. 
			 */
			public DateTime getExpiry() {
				return expiry;
			}
		}
		private static final Map<String, TokenExpiryPair> AUTH_LOOKUP =
			new HashMap<String, TokenExpiryPair>();

		/*
		 * (non-Javadoc)
		 * @see org.scribe.builder.api.DefaultApi10a#getRequestTokenEndpoint()
		 */
		@Override
		public String getRequestTokenEndpoint()
		{
			LOGGER.debug(REQUEST_TOKEN_RESOURCE);
			return REQUEST_TOKEN_RESOURCE;
		}

		/*
		 * (non-Javadoc)
		 * @see org.scribe.builder.api.DefaultApi10a#getAuthorizationUrl(org.scribe.model.Token)
		 */
		@Override
		public String getAuthorizationUrl(Token requestToken)
		{
			return AUTHORIZE_URL + requestToken.getToken();
		}

		/*
		 * (non-Javadoc)
		 * @see org.scribe.builder.api.DefaultApi10a#getAccessTokenEndpoint()
		 */
		@Override
		public String getAccessTokenEndpoint()
		{
			return ACCESS_TOKEN_RESOURCE;
		}
		
		/**
		 * Authenticates a user and returns the authenticated access token.
		 * 
		 * @param service An OAuthService generated by this BodyMediaApiOauth
		 * 				  class.
		 * 
		 * @param apiKey The API key registered with BodyMedia.
		 * 
		 * @param username The BodyMedia username of the user whose data is 
		 * 				   requested.
		 * 
		 * @param password The BodyMedia password of the user whose data is 
		 * 				   requested.
		 * 
		 * @return The authenticated access token.
		 * 
		 * @throws ServiceException There was a problem retrieving the token.
		 */
		public static Token authenticate(
				final OAuthService service,
				final String apiKey,
				final String username,
				final String password) 
				throws ServiceException {
			
			// First check the cache to see if the user's information exists 
			// there and, if so, return that token if it won't expire before
			// the request should be complete..
			if(AUTH_LOOKUP.containsKey(username)) {
				TokenExpiryPair pair = AUTH_LOOKUP.get(username);
				
				// We add a second from now, because that is the absolute 
				// longest it should take for us to send the request and have
				// it authenticated.
				DateTime timeRequestShouldBeMade = 
					(new DateTime()).plusSeconds(1);
				
				// If the request should be made before it expires, then just
				// return the cached token. Otherwise, retrieve a new token.
				if(timeRequestShouldBeMade.isBefore(pair.getExpiry())) {
					return pair.getToken();
				}
			}
			
			// Retrieve the request token.
			Token requestToken;
			try {
				requestToken = service.getRequestToken();
			}
			catch(OAuthException e) {
				throw new ServiceException(
					"The request token could not be extracted.",
					e);
			}
			
			// The Scribe documentation says we need this 'verifier' value, but
			// as far as I can tell there is no such thing in BodyMedia. Using
			// the request token appears to do the trick.
			String verifierString = requestToken.getToken();
			
			// Now, we fake the acceptance of the registration by sending a
			// POST to the 'doLogin' resource, which validates our key.
			HttpPost doLogin =
				new HttpPost(BodyMediaOauthApi.DO_LOGIN_RESOURCE);
			
			// Create the parameters.
			List<NameValuePair> params = new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("access", "allow"));
			params.add(new BasicNameValuePair("oauth_token", verifierString));
			params.add(new BasicNameValuePair("api_key", apiKey));
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("password", password));
			
			// Add the parameters.
			try {
				doLogin.setEntity(new UrlEncodedFormEntity(params));
			}
			catch(UnsupportedEncodingException e) {
				throw new ServiceException(
					"Could not URL encode the parameters.",
					e);
			}
			
			// Make the request.
			HttpClient doLoginClient = new DefaultHttpClient();
			HttpResponse doLoginResponse;
			try {
				doLoginResponse = doLoginClient.execute(doLogin);
			}
			catch(ClientProtocolException e) {
				throw new ServiceException(e);
			}
			catch(IOException e) {
				throw new ServiceException(e);
			}
			
			// Make sure the HTTP code is good.
			if(doLoginResponse.getStatusLine().getStatusCode() != 200) {
				throw 
					new ServiceException(
						"BodyMedia returned a non-200 response.");
			}
			
			// Get the access token.
			Verifier verifier = new Verifier(verifierString);
			Token accessToken = service.getAccessToken(requestToken, verifier);
			
			// Save the access token in the lookup table.
			AUTH_LOOKUP
				.put(
					username, 
					new TokenExpiryPair(
						accessToken, 
						(new DateTime()).plusSeconds(300)));
			
			// Return the access token.
			return accessToken;
		}
	}
	
	/**
	 * The superclass for all BodyMedia APIs.
	 *
	 * @author John Jenkins
	 */
	public abstract static class BodyMediaApi implements OmhReadResponder {
		/**
		 * The base URI for all API calls based on the 
		 * {@link #API_BASE_URL API base URL}.
		 */
		public static final String API_BASE_URL = BASE_URL + "v2"; 
		
		/**
		 * The formatter for the date parameter in the sleep queries.
		 */
		public static final DateTimeFormatter 
			SLEEP_PARAMETER_DATE_TIME_FORMATTER =
				DateTimeFormat.forPattern("yyyyMMdd");
		
		/**
		 * A JSON factory to be used by the sub-classes.
		 */
		protected static final JsonFactory JSON_FACTORY = 
			(new MappingJsonFactory())
				.configure(Feature.AUTO_CLOSE_JSON_CONTENT, true)
				.configure(Feature.AUTO_CLOSE_TARGET, true);
		
		/**
		 * The path to be used with the base URL.
		 */
		private final String path;
		
		/**
		 * A flag to indicate if this request has been made yet or not.
		 */
		private boolean madeRequest = false;
		
		/**
		 * Builds the {@link BodyMediaApi BodyMedia API} with its default path
		 * from the {@link #API_BASE_URL API base URL}.
		 * 
		 * @param path The path after the {@link #API_BASE_URL API base URL}.
		 * 
		 * @throws IllegalArgumentException The path is null or only 
		 * 									whitespace.
		 */
		private BodyMediaApi(final String path) {
			if(path == null) {
				throw new IllegalArgumentException("The path is null.");
			}
			else if(path.trim().length() == 0) {
				throw new IllegalArgumentException(
					"The path is all whitespace.");
			}
			
			this.path = path;
		}
		
		/**
		 * Builds the fully-qualified URI for this path based on the 
		 * {@link #API_BASE_URL API base URL}.
		 * 
		 * @return A URI built from the {@link #API_BASE_URL API base URL} and
		 * 		   this path.
		 * 
		 * @throws DomainException There was a problem creating the URI.
		 */
		public URI getUri() throws DomainException {
			try {
				return new URI(API_BASE_URL + "/" + path);
			}
			catch(URISyntaxException e) {
				throw new DomainException(
					"The URL and/or path don't form a valid URI: " + 
						API_BASE_URL + path);
			}
		}

		/**
		 * Returns whether or not the records at this API will have an ID 
		 * associated with each one.
		 * 
		 * @return Whether or not these records have IDs.
		 */
		public abstract boolean hasId();
		
		/**
		 * Returns whether or not the records at this API will have a timestamp
		 * associated with each one.
		 * 
		 * @return Whether or not these records have timestamps.
		 */
		public abstract boolean hasTimestamp();
		
		/**
		 * Returns whether or not the records at this API will have a location
		 * associated with each one.
		 * 
		 * @return Whether or not these records have locations.
		 */
		public abstract boolean hasLocation();
		
		/**
		 * Creates the registry entry for this BodyMedia API.
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
			
			// BodyMedia definition
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
				new StringBuilder("omh:body_media:");
			surveyPayloadIdBuilder.append(path);
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

			// End the BodyMedia definition.
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
		 * Makes the request to the API and stores the received data.
		 * 
		 * @param service A pre-built OAuthService based on the 
		 * 				  BodyMediaOauthApi.
		 * 
		 * @param apiKey The API key for our application.
		 * 
		 * @param accessToken An access token for the user.
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
				final OAuthService service,
				final String apiKey,
				final Token accessToken,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException {
			
			if(madeRequest) {
				return;
			}
			
			makeRequest(
				service,
				apiKey,
				accessToken, 
				startDate, 
				endDate, 
				numToSkip, 
				numToReturn);
			
			madeRequest = true;
		}
		
		/**
		 * Makes the request to the API and stores the received data. This will
		 * be called while the
		 * {@link #service(OAuthService, Token, DateTime, DateTime, long, long)}
		 * call is being made. This allows the superclass to do some work
		 * before the subclasses make their call.
		 * 
		 * @param service A pre-built OAuthService based on the 
		 * 				  BodyMediaOauthApi.
		 * 
		 * @param apiKey The API key for our application.
		 * 
		 * @param accessToken An access token for the user.
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
			final OAuthService service,
			final String apiKey,
			final Token accessToken,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws DomainException;
		
		/**
		 * Builds and makes the HTTP GET request. This will return the data as
		 * a string.
		 * 
		 * @param service A pre-built OAuthService based on the 
		 * 				  BodyMediaOauthApi.
		 * 
		 * @param apiKey The API key for our application.
		 * 
		 * @param accessToken An access token for the user.
		 * 
		 * @param startDate Limits the data to only those points on or after
		 * 					this date and time.
		 * 
		 * @param endDate Limits the data to only those points on or before 
		 * 				  this date and time.
		 * 
		 * @return The response from BodyMedia as a String.
		 * 
		 * @throws DomainException There was a problem making the request.
		 */
		protected final String makeRequest(
				final OAuthService service,
				final String apiKey,
				final Token accessToken,
				final DateTime startDate,
				final DateTime endDate)
				throws DomainException {
			
			// Build the sleep URI.
			StringBuilder uriBuilder = 
				new StringBuilder(getUri().toString() + "/");
			uriBuilder
				.append(
					BodyMediaApi
						.SLEEP_PARAMETER_DATE_TIME_FORMATTER.print(startDate));
			uriBuilder.append('/');
			uriBuilder
				.append(
					BodyMediaApi
						.SLEEP_PARAMETER_DATE_TIME_FORMATTER.print(endDate));
			uriBuilder.append("?api_key=");
			uriBuilder.append(apiKey);
			
			// Perform the request and store the data.
			OAuthRequest request = 
				new OAuthRequest(Verb.GET, uriBuilder.toString());
			request.addHeader("Accept", "application/json");
			service.signRequest(accessToken, request);
			
			// Make sure the request didn't fail.
			Response response = request.send();
			if(response.getCode() != 200) {
				throw 
					new DomainException(
						"BodyMedia returned a non-200 response (" +
							response.getCode() +
							"): " +
							response.getBody());
			}

			// Return the results of the request
			return response.getBody();
		}
	}
	
	/**
	 * A {@link BodyMediaApi} for the user's sleep data.
	 *
	 * @author John Jenkins
	 */
	public static class SleepApi extends BodyMediaApi {
		/**
		 * The URL's path to the profile. Should be used in conjunction with 
		 * the {@link #API_BASE_URL base URL}.
		 */
		private static final String PATH = "sleep";
		
		private static final class Result implements Comparable<Result> {
			private static final String JSON_KEY_DATE = "date";
			private static final String JSON_KEY_TOTAL_LYING = "totalLying";
			private static final String JSON_KEY_TOTAL_SLEEP = "totalSleep";
			private static final String JSON_KEY_EFFICIENCY = "efficiency";
			
			private DateTime date;
			private int totalLying;
			private int totalSleep;
			private double efficiency;
			
			/**
			 * Generates the Concordia schema for this path.
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
				
				// Start the definition.
				generator.writeStartObject();
				
				// The data will always be a JSON object.
				generator.writeStringField("type", "object");
				generator.writeArrayFieldStart("schema");
				
				// Add the 'date' field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_DATE);
				generator.writeStringField("type", "string");
				generator.writeEndObject();
				
				// Add the 'totalLying' field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_TOTAL_LYING);
				generator.writeStringField("type", "number");
				generator.writeEndObject();
				
				// Add the 'totalSleep' field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_TOTAL_SLEEP);
				generator.writeStringField("type", "number");
				generator.writeEndObject();
				
				// Add the 'efficiency' field.
				generator.writeStartObject();
				generator.writeStringField("name", JSON_KEY_EFFICIENCY);
				generator.writeStringField("type", "number");
				generator.writeEndObject();
				
				// End the overall schema array.
				generator.writeEndArray();
				
				// End the definition.
				generator.writeEndObject();
				
				// Return the generator.
				return generator;
			}

			/**
			 * Sorts the Result objects in reverse chronological order.
			 */
			@Override
			public int compareTo(Result other) {
				if(date.isBefore(other.date)) {
					return 1;
				}
				else if(date.isAfter(other.date)) {
					return -1;
				}
				else {
					return 0;
				}
			}
		}
		List<Result> results = new LinkedList<Result>();
		
		/**
		 * Creates a {@link BodyMediaApi} to the user's BodyMedia sleep data.
		 */
		public SleepApi() {
			super(PATH);
		}

		/**
		 * @return False, sleep data points are based on their timestamp not an
		 * 		   ID.
		 */
		@Override
		public boolean hasId() {
			return false;
		}

		/**
		 * @return True, each point has a timestamp based on a day.
		 */
		@Override
		public boolean hasTimestamp() {
			return true;
		}

		/**
		 * @return False, BodyMedia does not collection location information.
		 */
		@Override
		public boolean hasLocation() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadBodyMediaRequest.BodyMediaApi#toConcordia(org.codehaus.jackson.JsonGenerator)
		 */
		@Override
		public JsonGenerator toConcordia(
				final JsonGenerator generator)
				throws JsonGenerationException, IOException {
			
			// Return the generator.
			return Result.toConcordia(generator);
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadBodyMediaRequest.BodyMediaApi#getUri()
		 */
		@Override
		public URI getUri() throws DomainException {
			// Get the parent's URI.
			String parentUri = super.getUri().toString();
			
			// Updates the URI to the BodyMedia-specific URI.
			try {
				return new URI(parentUri + "/day");
			}
			catch(URISyntaxException e) {
				throw new DomainException(
					"The URL and/or path don't form a valid URI: " + 
						parentUri + "/day");
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.ohmage.request.omh.OmhReadBodyMediaRequest.BodyMediaApi#makeRequest(org.scribe.oauth.OAuthService, org.scribe.model.Token, org.joda.time.DateTime, org.joda.time.DateTime, long, long)
		 */
		@Override
		protected void makeRequest(
				final OAuthService service,
				final String apiKey,
				final Token accessToken,
				final DateTime startDate,
				final DateTime endDate,
				final long numToSkip,
				final long numToReturn)
				throws DomainException {
			
			// Get the result as a string.
			String result = 
				makeRequest(service, apiKey, accessToken, startDate, endDate);
			
			try {
				// Pass the results into the JSON parser.
				JsonParser parser = JSON_FACTORY.createJsonParser(result);
				
				// Ensure that the response is a JSON object.
				if(parser.nextToken() != JsonToken.START_OBJECT) {
					throw 
						new DomainException(
							"The response was not a JSON object.");
				}
				
				while(parser.nextToken() != null) {
					// Get the field's name.
					String fieldName = parser.getCurrentName();
					
					// Get the array of days.
					if("days".equals(fieldName)) {
						// Ensure that the "days"'s value is an array.
						if(parser.nextToken() != JsonToken.START_ARRAY) {
							throw new DomainException(
								"The 'items' field was not a JSON array.");
						}
						
						// Loop through each index.
						JsonToken currToken;
						while((currToken = parser.nextToken()) != JsonToken.END_ARRAY) {
							// The data at each index must be an object.
							if(currToken != JsonToken.START_OBJECT) {
								throw new DomainException(
									"The array element is not a JSON object: " +
										currToken.toString());
							}
							
							// Create the new Result object.
							Result currResult = new Result();
							
							// Loop through all of the elements in the object.
							while(parser.nextToken() != JsonToken.END_OBJECT) {
								// Get the field's name.
								String currFieldName = parser.getCurrentName();
								
								// Advance the pointer to the field's value.
								parser.nextToken();
								
								if(Result.JSON_KEY_DATE.equals(currFieldName)) {
									currResult.date =
										SLEEP_PARAMETER_DATE_TIME_FORMATTER
											.parseDateTime(parser.getText());
								}
								else if(Result.JSON_KEY_TOTAL_LYING.equals(currFieldName)) {
									currResult.totalLying = 
										parser.getNumberValue().intValue();
								}
								else if(Result.JSON_KEY_TOTAL_SLEEP.equals(currFieldName)) {
									currResult.totalSleep =
										parser.getNumberValue().intValue();
								}
								else if(Result.JSON_KEY_EFFICIENCY.equals(currFieldName)) {
									currResult.efficiency =
										parser.getNumberValue().doubleValue();
								}
							}
							
							// Add the Result object to the list of results.
							results.add(currResult);
						}
					}
					// Otherwise, it was a value we didn't understand or care
					// about and will ignore it now.
				}
				
				// Sort the results to put them in reverse-chronological order.
				Collections.sort(results);
			}
			catch(JsonParseException e) {
				throw 
					new DomainException(
						"There was an error generating the JSON.",
						e);
			}
			catch(IOException e) {
				throw 
					new DomainException(
						"There was an error writing to the generator.",
						e);
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
			
			// For each object,
			for(Result result : results) {
				// Start the overall object.
				generator.writeStartObject();
				
				// Write the metadata.
				generator.writeObjectFieldStart("metadata");
				
				// Write the timestamp.
				generator
					.writeStringField(
						"timestamp", 
						isoDateTimeFormatter.print(result.date));
				
				// End the metadata object.
				generator.writeEndObject();
				
				// Write the data.
				generator.writeObjectFieldStart("data");
				
				// Write the 'duration' field.
				generator
					.writeStringField(
						Result.JSON_KEY_DATE,
						SLEEP_PARAMETER_DATE_TIME_FORMATTER.print(result.date));
				
				// Write the 'duration' field.
				generator
					.writeNumberField(
						Result.JSON_KEY_TOTAL_LYING,
						result.totalLying);
				
				// Write the 'duration' field.
				generator
					.writeNumberField(
						Result.JSON_KEY_TOTAL_SLEEP,
						result.totalSleep);
				
				// Write the 'duration' field.
				generator
					.writeNumberField(
						Result.JSON_KEY_EFFICIENCY,
						result.efficiency);
				
				// End the data object.
				generator.writeEndObject();
				
				// End the overall object.
				generator.writeEndObject();
			}
		}
	}
	
	/**
	 * A factory class for generating {@link BodyMediaApi} objects. Each path
	 * subclass must register with the factory in order to be built in that
	 * fashion.
	 *
	 * @author John Jenkins
	 */
	public static enum BodyMediaApiFactory {
		SLEEP (SleepApi.PATH, SleepApi.class);
		
		private final String apiString;
		private final Class<? extends BodyMediaApi> apiClass;
		
		/**
		 * The mapping of path strings to their corresponding objects.
		 */
		private static final Map<String, BodyMediaApiFactory> FACTORY = 
			new HashMap<String, BodyMediaApiFactory>();
		static {
			// Populate the 'FACTORY' object.
			BodyMediaApiFactory[] paths = values();
			for(int i = 0; i < paths.length; i++) {
				BodyMediaApiFactory path = paths[i];
				FACTORY.put(path.apiString, path);
			}
		}
		
		/**
		 * Default constructor made private to prevent instantiation.
		 */
		private BodyMediaApiFactory(
				final String apiString, 
				final Class<? extends BodyMediaApi> apiClass) {
			
			if(apiString == null) {
				throw new IllegalArgumentException("The API string is null.");
			}
			if(apiString.trim().length() == 0) {
				throw new IllegalArgumentException(
					"The API string is all whitespace.");
			}
			if(apiClass == null) {
				throw new IllegalArgumentException(
					"The API class is null.");
			}
			
			this.apiString = apiString;
			this.apiClass = apiClass;
		}
		
		/**
		 * Returns the API's string value.
		 * 
		 * @return The API's string value.
		 */
		public final String getApi() {
			return apiString;
		}
		
		/**
		 * Returns a new instance of the BodyMediaApi object specified by the
		 * 'api' parameter.
		 * 
		 * @param api The string to use to lookup a BodyMediaApi object.
		 * 
		 * @return The BodyMediaApi object that corresponds to the 'api'
		 * 		   parameter.
		 * 
		 * @throws DomainException The API was unknown or there was an error
		 * 						   generating an instance of it.
		 */
		public static final BodyMediaApi getApi(
				final String api)
				throws DomainException {
			
			if(FACTORY.containsKey(api)) {
				try {
					return FACTORY.get(api).apiClass.newInstance();
				}
				catch(InstantiationException e) {
					throw new DomainException(
						"The Class for the path cannot be instantiated: " +
							api,
						e);
				}
				catch(IllegalAccessException e) {
					throw new DomainException(
						"The Class for the path does not contain a no-argument constructor: " +
							api,
						e);
				}
				catch(SecurityException e) {
					throw new DomainException(
						"The security manager prevented instantiation for the path: " +
							api,
						e);
				}
				catch(ExceptionInInitializerError e) {
					throw new DomainException(
						"The constructor for the path threw an exception: " +
							api,
						e);
				}
			}
			
			throw new DomainException("The path is unknown: " + api);
		}
	}
	private final BodyMediaApi api;

	private final String owner;
	private final DateTime startDate;
	private final DateTime endDate;
	private final long numToSkip;
	private final long numToReturn;
	
	/**
	 * Creates a request to read a BodyMedia API.
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
	 * @param api The path to the API to be called.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 */
	public OmhReadBodyMediaRequest(
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
			final String api)
			throws IOException, InvalidRequestException {
		
		super(httpRequest, hashPassword, tokenLocation, parameters, callClientRequester);
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH read request for BodyMedia.");
		}
		
		// Save the owner value.
		this.owner = owner;

		// Calculate / save the start and end date based on the given values.
		if(endDate == null) {
			this.endDate = new DateTime();
		}
		else {
			this.endDate = endDate;
		}
		// The largest time range is one year.
		if(startDate == null) {
			this.startDate = this.endDate.minusYears(1).plusDays(1);
		}
		else {
			this.startDate = startDate;
		}
		
		this.numToSkip = numToSkip;
		this.numToReturn = numToReturn;
		
		BodyMediaApi tApi = null;
		try {
			tApi = OmhValidators.validateBodyMediaApi(api);
		}
		catch(ValidationException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
		this.api = tApi;
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
			Map<String, String> bodyMediaCredentials =
				OmhServices.instance().getCredentials("body_media");
			
			// Get the API key.
			String apiKey = bodyMediaCredentials.get("api_key");
			if(apiKey == null) {
				throw 
					new ServiceException(
						"The BodyMedia API key is missing from the OMH credentials table in the database.");
			}

			// Get the API key.
			String sharedSecret = bodyMediaCredentials.get("shared_secret");
			if(sharedSecret == null) {
				throw 
					new ServiceException(
						"The BodyMedia shared secret is missing from the OMH credentials table in the database.");
			}
			
			// Switch on either the requester or the given username.
			String requestee = 
				((owner == null) ? getUser().getUsername() : owner);
			
			// Get the BodyMedia username for this ohamge user.
			String username = 
				bodyMediaCredentials
					.get(requestee + "_username");
			if(username == null) {
				throw 
					new ServiceException(
						"There is no BodyMedia mapping for the user: " + 
							requestee);
			}
			
			// Get the BodyMedia password for this ohamge user.
			String password = 
				bodyMediaCredentials
					.get(requestee + "_password");
			if(password == null) {
				throw 
					new ServiceException(
						"There is no BodyMedia mapping for the user: " + 
							requestee);
			}
			
			// Create the service for making the OAuth requests.
			LOGGER.info("Creating the BodyMedia OAuth service.");
			OAuthService service =
				(new ServiceBuilder())
					.provider(BodyMediaOauthApi.class)
					.apiKey(apiKey)
					.apiSecret(sharedSecret)
					.build();
			
			// Get the token from the BodyMediaOauthApi class.
			LOGGER.info("Getting the BodyMedia access token for this user.");
			Token accessToken =
				BodyMediaOauthApi
					.authenticate(service, apiKey, username, password);
			
			// Get the data and massage it into a form we like.
			try {
				LOGGER
					.info(
						"Calling the BodyMedia API: " + 
							api.getUri().toString());
				api.service(
					service, 
					apiKey, 
					accessToken, 
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
		return api.getNumDataPoints();
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

		LOGGER.info("Responding to an OMH read request for BodyMedia data.");
		
		// We call through to the API to respond.
		api.respond(generator, columns);
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