package org.ohmage.request.omh;

import java.io.IOException;
import java.io.OutputStream;
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
import org.codehaus.jackson.map.MappingJsonFactory;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Observer.Stream;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.service.ObserverServices;
import org.ohmage.validator.ObserverValidators;
import org.ohmage.validator.OmhValidators;

public class OmhRegistryReadRequest extends Request {
	private static final Logger LOGGER =
		Logger.getLogger(OmhRegistryReadRequest.class);
	
	/**
	 * The single factory instance for the writer.
	 */
	private static final JsonFactory JSON_FACTORY = 
		(new MappingJsonFactory()).configure(Feature.AUTO_CLOSE_TARGET, true);
	
	private final String observerId;
	private final String streamId;
	private final Long streamVersion;
	
	private final long numToSkip;
	private final long numToReturn;

	private final Map<String, Collection<Stream>> streams = 
		new HashMap<String, Collection<Stream>>();
	
	/**
	 * Creates an OMH registry read request.
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
	public OmhRegistryReadRequest(
			final HttpServletRequest httpRequest) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest, null);
		
		String tObserverId = null;
		String tStreamId = null;
		Long tStreamVersion = null;
		
		long tNumToSkip = 0;
		long tNumToReturn = StreamReadRequest.MAX_NUMBER_TO_RETURN;
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH registry read request.");
			String[] t;
			
			try {
				Map<String, String> observerIdToStreamId = null;
				t = getParameterValues(InputKeys.OMH_PAYLOAD_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"Multiple payload IDs were given: " +
							InputKeys.OMH_PAYLOAD_ID);
				}
				else if(t.length == 1) {
					observerIdToStreamId = 
						OmhValidators.validatePayloadId(t[0]);
					
					if(
						(observerIdToStreamId != null) && 
						(observerIdToStreamId.size() > 0)) {
						
						
						tObserverId = 
							observerIdToStreamId.keySet().iterator().next();
						tStreamId = observerIdToStreamId.get(tObserverId);
					}
				}
					
				t = getParameterValues(InputKeys.OMH_PAYLOAD_VERSION);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
						"Multiple payload versions were given: " +
							InputKeys.OMH_PAYLOAD_VERSION);
				}
				else if(t.length == 1) {
					tStreamVersion = 
						OmhValidators.validatePayloadVersion(t[0]);
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
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		observerId = tObserverId;
		streamId = tStreamId;
		streamVersion = tStreamVersion;
		
		numToSkip = tNumToSkip;
		numToReturn = tNumToReturn;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH registry read request.");
		
		try {
			LOGGER.info("Gathering the requested registry entries.");
			streams
				.putAll(
					ObserverServices
						.instance()
						.getStreams(
							null,
							observerId, 
							null, 
							streamId, 
							streamVersion, 
							numToSkip, 
							numToReturn));
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
		
		LOGGER.info("Responding to an OMH registry read request");

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
				httpResponse.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			}
			
			// Then, force the appropriate request to respond.
			super.respond(httpRequest, httpResponse, null);
			return;
		}

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
			// Start the JSON output.
			generator.writeStartArray();
			
			// For each observer,
			for(String observerId : streams.keySet()) {
				for(Stream stream : streams.get(observerId)) {
					// Start this "payload ID's" object.
					generator.writeStartObject();
					
					// Output the chunk size which will be the same for all 
					// observers.
					generator.writeNumberField(
						"chunk_size", 
						StreamReadRequest.MAX_NUMBER_TO_RETURN);
					
					// There are no external IDs yet. This may change to link   
					// to observer/read, but there are some discrepancies in 
					// the parameters.
					
					// Set the local timezone as authoritative.
					generator.writeBooleanField(
						"local_tz_authoritative",
						true);
					
					// Set the summarizable as false for the time being.
					generator.writeBooleanField("summarizable", false);

					// Set the payload ID.
					StringBuilder payloadIdBuilder = 
						new StringBuilder("omh:ohmage:");
					payloadIdBuilder.append(observerId).append(':');
					payloadIdBuilder.append(stream.getId());
					generator.writeStringField(
						"payload_id", 
						payloadIdBuilder.toString());
					
					// Set the payload version.
					generator.writeStringField(
						"payload_version", 
						String.valueOf(stream.getVersion()));
					
					// Set the payload definition.
					generator.writeObjectField(
						"payload_definition", 
						stream.getSchema().readValueAsTree());

					// End this "payload ID's" object.
					generator.writeEndObject();
				}
			}
			
			// End the JSON output.
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
}