package org.ohmage.request.omh;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonProcessingException;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Observer.Stream;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.service.ObserverServices;
import org.ohmage.validator.ObserverValidators;

public class OmhCatalogRequest extends UserRequest {
	private static final Logger LOGGER =
		Logger.getLogger(OmhCatalogRequest.class);
	
	/**
	 * The single factory instance for the writer.
	 */
	private static final JsonFactory JSON_FACTORY = 
		(new JsonFactory()).configure(Feature.AUTO_CLOSE_TARGET, true);
	
	private final String observerId;
	private final String streamId;
	private final Long streamVersion;
	
	private final Map<String, Collection<Stream>> streams;
	
	/**
	 * Creates an OMH catalog request.
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
	public OmhCatalogRequest(
			final HttpServletRequest httpRequest)
			throws IOException, InvalidRequestException {
		
		super(
			httpRequest, 
			true, 
			TokenLocation.EITHER, 
			null,
			retrieveFirstRequesterValue(httpRequest));

		String tObserverId = null;
		String tStreamId = null;
		Long tStreamVersion = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH catalog request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.OMH_PAYLOAD_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"Multiple payload IDs were given: " +
							InputKeys.OMH_PAYLOAD_ID);
				}
				else if(t.length == 1) {
					String[] payloadIdParts = t[0].split(":");
					
					if(payloadIdParts.length != 5) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_PAYLOAD_ID,
							"The payload ID is invalid: " + t[0]);
					}
					else if(! "omh".equals(payloadIdParts[0])) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_PAYLOAD_ID,
							"The first part of the payload ID must be \"omh\": " + 
								t[0]);
					}
					else if(! "ohmage".equals(payloadIdParts[1])) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_PAYLOAD_ID,
							"The second part of the payload ID must be \"ohmage\": " + 
								t[0]);
					}
					
					try {
						tObserverId = 
							ObserverValidators.validateObserverId(
								payloadIdParts[2]);
						if(tObserverId == null) {
							throw new ValidationException(
								ErrorCode.OMH_INVALID_PAYLOAD_ID,
								"The payload ID is unknown.");
						}
						
						tStreamId = ObserverValidators.validateStreamId(
							payloadIdParts[3]);
						if(tStreamId == null) {
							throw new ValidationException(
								ErrorCode.OMH_INVALID_PAYLOAD_ID,
								"The payload ID is unknown.");
						}
						
						tStreamVersion = 
							ObserverValidators.validateStreamVersion(
								payloadIdParts[4]);
						if(tStreamVersion == null) {
							throw new ValidationException(
								ErrorCode.OMH_INVALID_PAYLOAD_ID,
								"The payload ID is unknown.");
						}
					}
					catch(ValidationException e) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_PAYLOAD_ID,
							"The payload ID is unknown.",
							e);
						
					}
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		observerId = tObserverId;
		streamId = tStreamId;
		streamVersion = tStreamVersion;

		streams = new HashMap<String, Collection<Stream>>();
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
			// If the observer ID is null, we are returning all of the observers'
			// information.
			if(observerId == null) {
				streams.putAll(
					ObserverServices.instance().getObserverIdToStreamsMap());
			}
			// If the observer ID is non-null, we are returning the information for
			// only one observer.
			else {
				Collection<Stream> singleStream = new ArrayList<Stream>(1);
				singleStream.add(
					ObserverServices.instance().getStream(
						observerId, 
						streamId, 
						streamVersion
					)
				);
				
				streams.put(observerId, singleStream);
			}
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
			generator.writeStartArray();
			
			for(String observerId : streams.keySet()) {
				for(Stream stream : streams.get(observerId)) {
					generator.writeStartObject();
					
					// Set the max page size. 
					// FIXME: The specification says the maximum number of 
					// seconds, but it appears that that will change to the 
					// maximum number of records. Therefore, this anticipates 
					// that change.
					generator.writeNumberField(
						"chunk_size", 
						StreamReadRequest.MAX_NUMBER_TO_RETURN);
					
					// Set all of the time-stamps as being valid for the data.
					generator.writeBooleanField(
						"local_tz_authoritative",
						true);
					
					// Write a user-friendly name for this payload.
					generator.writeStringField("name", stream.getName());
					
					// Build and then set the payload ID.
					StringBuilder payloadIdBuilder = 
						new StringBuilder("omh:ohmage:");
					payloadIdBuilder.append(observerId).append(':');
					payloadIdBuilder.append(stream.getId()).append(':');
					payloadIdBuilder.append(stream.getVersion());
					generator.writeStringField(
						"payload_id", 
						payloadIdBuilder.toString());
					
					// Build the payload definition. Include strict or optional
					// in each column's definition.
					
					
					// Set this as not being summarizable. This would be an
					// interesting and potentially useful feature, but it would
					// require more work on the observer side first.
					generator.writeBooleanField("summarizable", false);
					
					generator.writeEndObject();
				}
			}
			
			generator.writeEndArray();
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
	 * Retrieves the client value from the request and returns it if there is
	 * only one. Otherwise, it returns null.
	 * 
	 * @param httpRequest The HTTP request that made this call.
	 * 
	 * @return The client value or null if no such value exists.
	 */
	private static String retrieveFirstRequesterValue(
			final HttpServletRequest httpRequest) {
		
		String[] requesters = 
			httpRequest.getParameterMap().get(InputKeys.OMH_REQUESTER);
		
		if(requesters == null) {
			return null;
		}
		else if (requesters.length == 0) {
			return null;
		}
		else {
			return requesters[0];
		}
	}
}