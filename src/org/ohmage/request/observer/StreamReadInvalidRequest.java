package org.ohmage.request.observer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Observer;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ObserverServices;
import org.ohmage.service.ObserverServices.InvalidPoint;
import org.ohmage.validator.ObserverValidators;

public class StreamReadInvalidRequest extends UserRequest {
	/**
	 * The logger for this request.
	 */
	private static final Logger LOGGER = 
		Logger.getLogger(StreamReadInvalidRequest.class);
	
	/**
	 * The single factory instance for the writer.
	 */
	private static final JsonFactory JSON_FACTORY = 
		(new MappingJsonFactory()).configure(Feature.AUTO_CLOSE_TARGET, true);
	
	/**
	 * The maximum number of records that can be returned.
	 */
	public static final long MAX_NUMBER_TO_RETURN = 2000;
	
	// Required.
	private final String observerId;
	
	// Required.
	private final Long observerVersion;
	
	// Optional parameters.
	private final DateTime startDate;
	private final DateTime endDate;
	
	// Optional parameters, but they must be given a value.
	private final long numToSkip;
	private final long numToReturn;
	
	// The collection results from this request.
	private final List<InvalidPoint> results;
	
	/**
	 * Creates a stream read request for invalid data.
	 * 
	 * @param httpRequest
	 *        The HTTP request.
	 * 
	 * @throws InvalidRequestException
	 *         Thrown if the parameters cannot be parsed.
	 * 
	 * @throws IOException
	 *         There was an error reading from the request.
	 */
	public StreamReadInvalidRequest(
		final HttpServletRequest httpRequest)
		throws IOException, InvalidRequestException {
		
		super(httpRequest, false, TokenLocation.EITHER, null);
		
		String tObserverId = null;
		Long tObserverVersion = null;
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		long tNumToSkip = 0;
		long tNumToReturn = MAX_NUMBER_TO_RETURN;
		
		if(! isFailed()) {
			LOGGER.info("Creating a stream read request for invalid data.");
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
		
		observerId = tObserverId;
		observerVersion = tObserverVersion;
		startDate = tStartDate;
		endDate = tEndDate;
		numToSkip = tNumToSkip;
		numToReturn = tNumToReturn;
		
		results = new LinkedList<InvalidPoint>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a stream read request for invalid data.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifing that the observer exists.");
			Observer observer =
				ObserverServices
					.instance()
					.getObserver(observerId, observerVersion);
			
			LOGGER
				.info(
					"Verifying that the requesting user is the owner of the " +
						"observer.");
			ObserverServices
				.instance()
				.verifyUserOwnsObserver(getUser().getUsername(), observerId);
			
			LOGGER.info("Retrieveing the invalid data.");
			results
				.addAll(
					ObserverServices
						.instance()
						.getInvalidStreamData(
							observer, 
							startDate, 
							endDate, 
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

		// Check for failure.
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, (JSONObject) null);
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
		
		/*
		 * Example output:
		 * 
		 * 	{
		 * 		"result":"success",
		 * 		"data":[
		 * 			{
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
			
			// Add a "data" key that is an array of the results.
			generator.writeArrayFieldStart("data");
			for(InvalidPoint invalidPoint : results) {
				// Write the invalid point.
				generator.writeObject(invalidPoint);
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