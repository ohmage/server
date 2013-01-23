package org.ohmage.request.omh;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.PayloadId;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.UserRequest;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.validator.OmhValidators;

/**
 * <p>Uploads data to the server via the Open mHealth write API.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OMH_REQUESTER}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OMH_PAYLOAD_ID}</td>
 *     <td>The Open mHealth payload ID to which the data belongs.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OMH_PAYLOAD_VERSION}</td>
 *     <td>The version of the payload ID to which the data belongs.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OMH_DATA}</td>
 *     <td>The data to upload per the Open mHealth write specification.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class OmhWriteRequest extends Request {
	private static final Logger LOGGER =
		Logger.getLogger(OmhWriteRequest.class);
	
	private final UserRequest userRequest;
	
	/**
	 * Creates an Open mHealth write request. 
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public OmhWriteRequest(
		final HttpServletRequest httpRequest)
		throws IOException, InvalidRequestException {
		
		super(httpRequest, null);
		
		UserRequest tUserRequest = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH write request.");
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
					payloadId = 
						OmhValidators.validatePayloadId(t[0]);
				}
				if(payloadId == null) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_ID,
						"No payload ID was given.");
				}
				
				Long payloadVersion = null;
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
						payloadVersion = Long.decode(versionStrings[0]); 
					}
					catch(NumberFormatException e) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
							"The payload version was not a number: " +
								versionStrings[0],
							e);
					}
				}
				if(payloadVersion == null) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
						"The payload version is unknown.");
				}
				
				String data = null;
				byte[] binaryData =
					getMultipartValue(httpRequest, InputKeys.OMH_DATA);
				if(binaryData == null) {
					throw new ValidationException(
						ErrorCode.OMH_INVALID_DATA,
						"Multiple data were given: " +
							InputKeys.OMH_DATA);
				}
				else {
					try {
						data = new String(binaryData); 
					}
					catch(NumberFormatException e) {
						throw new ValidationException(
							ErrorCode.OMH_INVALID_PAYLOAD_VERSION,
							"The payload version was not a number: " +
								versionStrings[0],
							e);
					}
				}
			
				try {
					LOGGER
						.info(
							"Creating a sub-request based on this payload ID.");
					tUserRequest = 
						payloadId
							.generateWriteRequest(
								httpRequest, 
								parameters, 
								true, 
								TokenLocation.EITHER, 
								true,
								payloadVersion, 
								data);
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
		else {
			LOGGER.debug("Failed: " + getFailureMessage());
		}
		
		userRequest = tUserRequest;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH write request.");
		
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
		else {
			userRequest.respond(httpRequest, httpResponse);
		}
	}
}