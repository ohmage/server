package org.ohmage.request.mobility;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.validator.MobilityValidators;

public class MobilityUpdateRequest extends UserRequest {
	private static final Logger LOGGER = 
			Logger.getLogger(MobilityUpdateRequest.class);
	
	private final UUID mobilityId;
	private final MobilityPoint.PrivacyState privacyState;
	
	/**
	 * Creates a new Mobility update request.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public MobilityUpdateRequest(
			final HttpServletRequest httpRequest) 
			throws IOException, InvalidRequestException {
		
		super(httpRequest, TokenLocation.PARAMETER, false);
		
		UUID tMobilityId = null; 
		MobilityPoint.PrivacyState tPrivacyState = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a Mobility update request.");
			String[] t;
			
			try {
				t = getParameterValues(InputKeys.MOBILITY_ID);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.MOBILITY_INVALID_ID,
						"Multiple Mobility IDs were given: " +
							InputKeys.MOBILITY_ID);
				}
				else if(t.length == 1) {
					tMobilityId = MobilityValidators.validateMobilityId(t[0]);
				}
				if(tMobilityId == null) {
					throw new ValidationException(
						ErrorCode.MOBILITY_INVALID_ID,
						"The Mobility ID is missing: " +
							InputKeys.MOBILITY_ID);
				}
				
				t = getParameterValues(InputKeys.PRIVACY_STATE);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.MOBILITY_INVALID_PRIVACY_STATE,
						"Multiple privacy states were given: " +
							InputKeys.PRIVACY_STATE);
				}
				else if(t.length == 1) {
					tPrivacyState = 
							MobilityValidators.validatePrivacyState(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		mobilityId = tMobilityId;
		privacyState = tPrivacyState;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		if(! super.authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the user is allowed to modify the point.");
			MobilityServices.instance().verifyUserCanUpdatePoint(
					getUser().getUsername(), 
					mobilityId);
			
			LOGGER.info("Attempting to update the Mobility request.");
			MobilityServices.instance().updateMobilityPoint(mobilityId, privacyState);
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
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		
		super.respond(httpRequest, httpResponse, null);
	}

}