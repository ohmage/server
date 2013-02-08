package org.ohmage.request.omh;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.PayloadId;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.UserRequest;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.ObserverValidators;
import org.ohmage.validator.OmhValidators;
import org.ohmage.validator.UserValidators;

public class OmhReadRequest extends Request {
	private static final Logger LOGGER = 
		Logger.getLogger(OmhReadRequest.class);
	
	/**
	 * The encoding for the previous and next URLs.
	 */
	private static final String URL_ENCODING_UTF_8 = "UTF-8";
	
	/**
	 * The single factory instance for the writer.
	 */
	private static final JsonFactory JSON_FACTORY = 
		(new MappingJsonFactory()).configure(Feature.AUTO_CLOSE_TARGET, true);
	
	private final String payloadIdString;
	private final Long payloadVersion;
	private final String owner;
	private final DateTime startDate;
	private final DateTime endDate;
	private final ColumnNode<String> columns;
	private final Long numToSkip;
	private final Long numToReturn;
	
	private final UserRequest userRequest;
	
	/**
	 * Creates an OMH read request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed. This is only applicable in the
	 * 								   event of the HTTP parameters being 
	 * 								   parsed.
	 */
	public OmhReadRequest(
			final HttpServletRequest httpRequest) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest, null);
		
		String tPayloadIdString = null;
		Long tPayloadVersion = null;
		String tOwner = null;
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		ColumnNode<String> tColumns = new ColumnNode<String>();
		Long tNumToSkip = 0L;
		Long tNumToReturn = StreamReadRequest.MAX_NUMBER_TO_RETURN;
		
		UserRequest tUserRequest = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH read request.");
			String[] t;
			
			try {
				Map<String, String[]> parameters = 
					new HashMap<String, String[]>(getParameterMap());
				t = getParameterValues(InputKeys.OMH_REQUESTER);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_REQUESTER,
						"Multiple requester values were given: " +
							InputKeys.OMH_REQUESTER);
				}
				else if(t.length == 0) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_REQUESTER,
						"No requester value was given: " +
							InputKeys.OMH_REQUESTER);
				}
				else {
					parameters.put(
						InputKeys.CLIENT, 
						parameters.get(InputKeys.OMH_REQUESTER));
				}
				
				PayloadId payloadId = null;
				t = getParameterValues(InputKeys.OMH_PAYLOAD_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"Multiple payload IDs were given: " +
							InputKeys.OMH_PAYLOAD_ID);
				}
				else if(t.length == 1) {
					tPayloadIdString = t[0];
					payloadId = 
						OmhValidators.validatePayloadId(tPayloadIdString);
				}
				if(payloadId == null) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"No payload ID was given.");
				}
				
				String[] versionStrings = 
					getParameterValues(InputKeys.OMH_PAYLOAD_VERSION);
				if(versionStrings.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
						"Multiple payload versions were given: " +
							InputKeys.OMH_PAYLOAD_VERSION);
				}
				else if(versionStrings.length == 1) {
					try {
						tPayloadVersion = Long.decode(versionStrings[0]); 
					}
					catch(NumberFormatException e) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
							"The payload version was not a number: " +
								versionStrings[0],
							e);
					}
				}
				if(tPayloadVersion == null) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
						"The payload version is unknown.");
				}
				
				t = getParameterValues(InputKeys.OMH_OWNER);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_OWNER,
						"Multiple owner values were given: " +
							InputKeys.OMH_OWNER);
				}
				else if(t.length == 1) {
					tOwner = UserValidators.validateUsername(t[0]);
				}
				
				t = getParameterValues(InputKeys.OMH_START_TIMESTAMP);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_START_TIMESTAMP,
						"Multiple start times were given: " +
							InputKeys.OMH_START_TIMESTAMP);
				}
				else if(t.length == 1) {
					tStartDate = ObserverValidators.validateDate(t[0]);
				}
				
				t = getParameterValues(InputKeys.OMH_END_TIMESTAMP);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_END_TIMESTAMP,
						"Multiple end times were given: " +
							InputKeys.OMH_END_TIMESTAMP);
				}
				else if(t.length == 1) {
					tEndDate = ObserverValidators.validateDate(t[0]);
				}
				
				t = getParameterValues(InputKeys.OMH_COLUMN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_COLUMN_LIST,
						"Multiple column lists were given: " + 
								InputKeys.OMH_COLUMN_LIST);
				}
				else if(t.length == 1) {
					try {
						tColumns = ObserverValidators.validateColumnList(t[0]);
					}
					catch(ValidationException e) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_COLUMN_LIST,
							"The column list was invalid.",
							e);
					}
				}
				
				t = getParameterValues(InputKeys.OMH_NUM_TO_SKIP);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_NUM_TO_SKIP,
						"Multiple \"number of results to skip\" values were given: " +
							InputKeys.OMH_NUM_TO_SKIP);
				}
				else if(t.length == 1) {
					tNumToSkip = ObserverValidators.validateNumToSkip(t[0]);
				}
				
				t = getParameterValues(InputKeys.OMH_NUM_TO_RETURN);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_NUM_TO_RETURN,
						"Multiple \"number of results to return\" values were given: " +
							InputKeys.OMH_NUM_TO_RETURN);
				}
				else if(t.length == 1) {
					tNumToReturn = 
						ObserverValidators
							.validateNumToReturn(
								t[0], 
								StreamReadRequest.MAX_NUMBER_TO_RETURN);
				}
			
				try {
					LOGGER
						.info(
							"Creating a sub-request based on this payload ID.");
					tUserRequest = 
						payloadId
							.generateSubRequest(
								httpRequest, 
								parameters, 
								true, 
								TokenLocation.EITHER, 
								true,
								tPayloadVersion, 
								tOwner,
								tStartDate, 
								tEndDate, 
								tNumToSkip, 
								tNumToReturn);
				}
				catch(DomainException e) {
					throw new ValidationException(
						"There was an error creating the underlying request.",
						e);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		payloadIdString = tPayloadIdString;
		payloadVersion = tPayloadVersion;
		owner = tOwner;
		startDate = tStartDate;
		endDate = tEndDate;
		columns = tColumns;
		numToSkip = tNumToSkip;
		numToReturn = tNumToReturn;
		
		userRequest = tUserRequest;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH read request.");
		
		if((userRequest != null) && (! userRequest.isFailed())) {
			userRequest.service();
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

		LOGGER.info("Responding to an OMH read request.");

		// If either request has failed, set the response's status code.
		if(isFailed()) {
			if(
				ErrorCode
					.SYSTEM_GENERAL_ERROR
					.equals(getAnnotator().getErrorCode())) {
					
				httpResponse
					.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			else {
				httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			
			super.respond(httpRequest, httpResponse, null);
			return;
		}
		else if(userRequest.isFailed()) {
			if(
				ErrorCode
					.SYSTEM_GENERAL_ERROR
					.equals(userRequest.getAnnotator().getErrorCode())) {
					
				httpResponse
					.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			else {
				httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			
			// Then, force the request to respond.
			userRequest.respond(httpRequest, httpResponse);
			return;
		}
		else if(userRequest instanceof OmhReadResponder) {
			OmhReadResponder omhReadResponder = (OmhReadResponder) userRequest;
			
			// Refresh the token cookie.
			userRequest.refreshTokenCookie(httpResponse);
			
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
				
			try {
				// Start the response object.
				generator.writeStartObject();
				
				// Create the "success" message.
				generator.writeStringField("result", "success");
				
				// Create the metadata.
				generator.writeObjectFieldStart("metadata");
				
				// Write the count.
				generator.writeNumberField(
					"count",
					omhReadResponder.getNumDataPoints());

				try {
					// Build the root string for the previous and next 
					// pointers.
					StringBuilder rootBuilder = buildRootPreviousNextString();
					
					// For the previous pointer, add the number to skip.
					if(numToSkip > 0) {
						// Clone the root string for the previous string.
						StringBuilder previousBuilder =
							new StringBuilder(rootBuilder);
						
						// The number of results to return in the previous page
						// is either, all of the data points before this page 
						// or the same number that were returned from this
						// request.
						long prevNumToReturn = 
							Math.min(numToSkip, numToReturn);
						
						// Add the number of points to skip.
						previousBuilder
							.append('&')
							.append(
								URLEncoder
									.encode(
										InputKeys.OMH_NUM_TO_SKIP,
										URL_ENCODING_UTF_8))
							.append('=')
							.append(
								URLEncoder
									.encode(
										Long.toString(
											numToSkip - prevNumToReturn),
										URL_ENCODING_UTF_8));
						
						// Add the number of points to return.
						previousBuilder
						.append('&')
						.append(
							URLEncoder
								.encode(
									InputKeys.OMH_NUM_TO_RETURN,
									URL_ENCODING_UTF_8))
						.append('=')
						.append(
							URLEncoder
								.encode(
									Long.toString(prevNumToReturn),
									URL_ENCODING_UTF_8));
						
						// Write the previous pointer.
						generator
							.writeStringField(
								"previous", 
								previousBuilder.toString());
					}
					
					// If we filled this page, then there might be another.
					if(numToReturn == omhReadResponder.getNumDataPoints()) {
						// Clone the root string for the previous string.
						StringBuilder nextBuilder = 
							new StringBuilder(rootBuilder);
						
						// For the next pointer, add the number to skip.
						nextBuilder
							.append('&')
							.append(
								URLEncoder
									.encode(
										InputKeys.OMH_NUM_TO_SKIP,
										URL_ENCODING_UTF_8))
							.append('=')
							.append(
								URLEncoder
									.encode(
										Long.toString(numToSkip + numToReturn),
										URL_ENCODING_UTF_8));
						
						// For the next pointer, add the number to return.
						nextBuilder
							.append('&')
							.append(
								URLEncoder
									.encode(
										InputKeys.OMH_NUM_TO_RETURN,
										URL_ENCODING_UTF_8))
							.append('=')
							.append(
								URLEncoder
									.encode(
										Long.toString(numToReturn),
										URL_ENCODING_UTF_8));
						
						// Write the next pointer.
						generator
							.writeStringField("next", nextBuilder.toString());
					}
				}
				catch(DomainException e) {
					LOGGER
						.warn("There was an error building the root URL.", e);
				}
				
				// End the metadata.
				generator.writeEndObject();
				
				// Start the data.
				generator.writeArrayFieldStart("data");
				
				// Dispatch the writing of the data to the request.
				omhReadResponder.respond(generator, columns);
				
				// End the data.
				generator.writeEndArray();
				
				// End the response object.
				generator.writeEndObject();
			}
			catch(JsonGenerationException e) {
				LOGGER.error(e);
			}
			catch(IOException e) {
				LOGGER.error(e);
			}
			catch(DomainException e) {
				LOGGER.error(e);
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
		else {
			userRequest.respond(httpRequest, httpResponse);
		}
	}
	
	/**
	 * Generates the next and previous URLs' base URL. A caller is expected to
	 * clone the resulting StringBuilder and append their own paging 
	 * information.
	 * 
	 * @return A StringBuilder that is the base URL for previous and next
	 * 		   parameters.
	 * 
	 * @throws DomainException There was an error building the URL.
	 */
	private StringBuilder buildRootPreviousNextString() 
			throws DomainException {
		
		StringBuilder rootBuilder = new StringBuilder();
		
		// Adds the default URL without a path.
		rootBuilder.append(CookieUtils.buildServerRootUrl());
		
		// Add the path to the Open mHealth read API.
		rootBuilder.append("/app/omh/v1.0/read?");
			
		try {
			// Add the authentication token.
			rootBuilder
				.append(
					URLEncoder
						.encode(
							InputKeys.AUTH_TOKEN,
							URL_ENCODING_UTF_8))
				.append('=')
				.append(
					URLEncoder
						.encode(
							userRequest.getUser().getToken(),
							URL_ENCODING_UTF_8));

			// Add the requester value..
			rootBuilder
				.append('&')
				.append(
					URLEncoder
						.encode(
							InputKeys.OMH_REQUESTER,
							URL_ENCODING_UTF_8))
				.append('=')
				.append(
					URLEncoder
						.encode(
							userRequest.getClient(),
							URL_ENCODING_UTF_8));
			
			// Add the payload ID.
			rootBuilder
				.append('&')
				.append(
					URLEncoder
						.encode(
							InputKeys.OMH_PAYLOAD_ID,
							URL_ENCODING_UTF_8))
				.append('=')
				.append(
					URLEncoder
						.encode(
							payloadIdString,
							URL_ENCODING_UTF_8));
			
			// Add the payload version.
			rootBuilder
				.append('&')
				.append(
					URLEncoder
						.encode(
							InputKeys.OMH_PAYLOAD_VERSION,
							URL_ENCODING_UTF_8))
				.append('=')
				.append(
					URLEncoder
						.encode(
							payloadVersion.toString(),
							URL_ENCODING_UTF_8));
			
			// Add the owner, if given.
			if(owner != null) {
				rootBuilder
					.append('&')
					.append(
						URLEncoder
							.encode(
								InputKeys.OMH_OWNER,
								URL_ENCODING_UTF_8))
					.append('=')
					.append(
						URLEncoder
							.encode(
								owner,
								URL_ENCODING_UTF_8));
			}
			
			DateTimeFormatter isoDateTimeFormatter =
				ISODateTimeFormat.dateTime();
			
			// Add the start date, if given.
			if(startDate != null) {
				rootBuilder
					.append('&')
					.append(
						URLEncoder
							.encode(
								InputKeys.OMH_START_TIMESTAMP,
								URL_ENCODING_UTF_8))
					.append('=')
					.append(
						URLEncoder
							.encode(
								isoDateTimeFormatter.print(startDate),
								URL_ENCODING_UTF_8));
			}
			
			// Add the end date, if given.
			if(endDate != null) {
				rootBuilder
					.append('&')
					.append(
						URLEncoder
							.encode(
								InputKeys.OMH_END_TIMESTAMP,
								URL_ENCODING_UTF_8))
					.append('=')
					.append(
						URLEncoder
							.encode(
								isoDateTimeFormatter.print(endDate),
								URL_ENCODING_UTF_8));
			}
			
			// Add the end date, if given.
			if((columns != null) && (! columns.isLeaf())) {
				rootBuilder
					.append('&')
					.append(
						URLEncoder
							.encode(
								InputKeys.OMH_COLUMN_LIST,
								URL_ENCODING_UTF_8))
					.append('=')
					.append(
						URLEncoder
							.encode(
								columns.toListString(),
								URL_ENCODING_UTF_8));
			}
		}
		catch(UnsupportedEncodingException e) {
			throw
				new DomainException(
					"The encoding is unknown: " + URL_ENCODING_UTF_8,
					e);
		}
		
		return rootBuilder;
	}
}