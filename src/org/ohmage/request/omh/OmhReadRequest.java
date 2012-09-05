package org.ohmage.request.omh;

import java.io.IOException;
import java.io.OutputStream;
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
import org.ohmage.validator.ObserverValidators;
import org.ohmage.validator.OmhValidators;

public class OmhReadRequest extends Request {
	private static final Logger LOGGER = 
		Logger.getLogger(OmhReadRequest.class);
	
	/**
	 * The single factory instance for the writer.
	 */
	private static final JsonFactory JSON_FACTORY = 
		(new MappingJsonFactory()).configure(Feature.AUTO_CLOSE_TARGET, true);
	
	private final UserRequest userRequest;
	private final ColumnNode<String> columns;
	
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
		
		UserRequest tUserRequest = null;
		ColumnNode<String> tColumns = null;
		
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
				
				DateTime startDate = null;
				t = getParameterValues(InputKeys.OMH_START_TIMESTAMP);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_START_TIMESTAMP,
						"Multiple start times were given: " +
							InputKeys.OMH_START_TIMESTAMP);
				}
				else if(t.length == 1) {
					startDate = ObserverValidators.validateDate(t[0]);
				}
				
				DateTime endDate = null;
				t = getParameterValues(InputKeys.OMH_END_TIMESTAMP);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_END_TIMESTAMP,
						"Multiple end times were given: " +
							InputKeys.OMH_END_TIMESTAMP);
				}
				else if(t.length == 1) {
					endDate = ObserverValidators.validateDate(t[0]);
				}
				
				Long numToSkip = null;
				t = getParameterValues(InputKeys.OMH_NUM_TO_SKIP);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_NUM_TO_SKIP,
						"Multiple \"number of results to skip\" values were given: " +
							InputKeys.OMH_NUM_TO_SKIP);
				}
				else if(t.length == 1) {
					numToSkip = ObserverValidators.validateNumToSkip(t[0]);
				}
				
				Long numToReturn = null;
				t = getParameterValues(InputKeys.OMH_NUM_TO_RETURN);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_NUM_TO_RETURN,
						"Multiple \"number of results to return\" values were given: " +
							InputKeys.OMH_NUM_TO_RETURN);
				}
				else if(t.length == 1) {
					numToReturn = 
						ObserverValidators
							.validateNumToReturn(
								t[0], 
								StreamReadRequest.MAX_NUMBER_TO_RETURN);
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
						tColumns = 
							ObserverValidators.validateColumnList(
								t[0]);
					}
					catch(ValidationException e) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_COLUMN_LIST,
							"The column list was invalid.",
							e);
					}
				}
				
				Long version = null;
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
						version = Long.decode(versionStrings[0]); 
					}
					catch(NumberFormatException e) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
							"The payload version was not a number: " +
								versionStrings[0],
							e);
					}
				}
				if(version == null) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
						"The payload version is unknown.");
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
					payloadId = OmhValidators.validatePayloadId(t[0]);
				}
				if(payloadId == null) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"No payload ID was given.");
				}
				
				try {
					tUserRequest = 
						payloadId
							.generateSubRequest(
								httpRequest, 
								parameters, 
								true, 
								TokenLocation.EITHER, 
								retrieveFirstRequesterValue(httpRequest),
								version, 
								startDate, 
								endDate, 
								numToSkip, 
								numToReturn);
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
		
		userRequest = tUserRequest;
		columns = tColumns;
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
		if(isFailed() || userRequest.isFailed()) {
			if(
				ErrorCode
					.SYSTEM_GENERAL_ERROR
					.equals(getAnnotator().getErrorCode())) {
					
				httpResponse
					.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			else {
				httpResponse.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			}
			
			// Then, force the appropriate request to respond.
			if(isFailed()) {
				super.respond(httpRequest, httpResponse, null);
			}
			else {
				userRequest.respond(httpRequest, httpResponse);
			}
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
				
				// TODO: Write the previous.
				
				// TODO: Write the next.
				
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
}