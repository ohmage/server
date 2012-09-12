package org.ohmage.request.omh;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;
import org.ohmage.validator.ObserverValidators;

public class OmhReadRequest extends Request {
	private static final Logger LOGGER = 
		Logger.getLogger(OmhReadRequest.class);
	
	private final StreamReadRequest streamReadRequest;
	
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
		
		StreamReadRequest tStreamReadRequest = null;
		
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
				
				String observerId = null;
				String streamId = null;
				Long streamVersion = null;
				t = getParameterValues(InputKeys.OMH_PAYLOAD_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"Multiple payload IDs were given: " +
							InputKeys.OMH_PAYLOAD_ID);
				}
				else if(t.length == 0) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"No payload ID was given.");
				}
				else {
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
						observerId = 
							ObserverValidators.validateObserverId(
								payloadIdParts[2]);
						if(observerId == null) {
							throw new ValidationException(
								ErrorCode.OMH_INVALID_PAYLOAD_ID,
								"The payload ID is unknown.");
						}
						
						streamId = ObserverValidators.validateStreamId(
							payloadIdParts[3]);
						if(streamId == null) {
							throw new ValidationException(
								ErrorCode.OMH_INVALID_PAYLOAD_ID,
								"The payload ID is unknown.");
						}
						
						streamVersion = 
							ObserverValidators.validateStreamVersion(
								payloadIdParts[4]);
						if(streamVersion == null) {
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
				
				ColumnNode<String> columns = null;
				t = getParameterValues(InputKeys.OMH_COLUMN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_COLUMN_LIST,
						"Multiple column lists were given: " + 
								InputKeys.OMH_COLUMN_LIST);
				}
				else if(t.length == 1) {
					try {
						columns = ObserverValidators.validateColumnList(t[0]);
					}
					catch(ValidationException e) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_COLUMN_LIST,
							"The column list was invalid.",
							e);
					}
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
				
				tStreamReadRequest = 
					new StreamReadRequest(
						httpRequest,
						parameters,
						true,
						TokenLocation.EITHER,
						true,
						null,
						observerId,
						null,
						streamId,
						streamVersion,
						startDate,
						endDate,
						columns,
						numToSkip,
						numToReturn);
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		streamReadRequest = tStreamReadRequest;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH read request.");
		
		if((streamReadRequest != null) && (! streamReadRequest.isFailed())) {
			streamReadRequest.service();
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
		if(isFailed() || streamReadRequest.isFailed()) {
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
				streamReadRequest.respond(httpRequest, httpResponse);
			}
			return;
		}
		
		streamReadRequest.respond(httpRequest, httpResponse);
	}
}