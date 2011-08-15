package org.ohmage.request.mobility;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.MobilityInformation;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.MobilityServices;
import org.ohmage.validator.MobilityValidators;

public class MobilityUploadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(MobilityUploadRequest.class);
	
	private final List<MobilityInformation> data;
	
	/**
	 * Creates a Mobility upload request.
	 * 
	 * @param httpRequest A HttpServletRequest that contains the parameters for
	 * 					  this request.
	 */
	public MobilityUploadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, false);
		
		LOGGER.debug("Creating a Mobility upload request.");
		
		List<MobilityInformation> tData = null;
		
		if(! isFailed()) {
			try {
				tData = MobilityValidators.validateDataAsJsonArray(this, httpRequest.getParameter(InputKeys.DATA));
				if(tData == null) {
					setFailed(ErrorCodes.MOBILITY_INVALID_DATA, "The upload data is missing: " + ErrorCodes.MOBILITY_INVALID_DATA);
					throw new ValidationException("The upload data is missing: " + ErrorCodes.MOBILITY_INVALID_DATA);
				}
				else if(httpRequest.getParameterValues(InputKeys.DATA).length > 1) {
					setFailed(ErrorCodes.MOBILITY_INVALID_DATA, "Multiple data parameters were given: " + ErrorCodes.MOBILITY_INVALID_DATA);
					throw new ValidationException("Multiple data parameters were given: " + ErrorCodes.MOBILITY_INVALID_DATA);
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		data = tData;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the Mobility upload request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			LOGGER.info("Running the server-side classifier.");
			MobilityServices.classifyData(this, data);
			
			LOGGER.info("Storing the Mobility upload.");
			MobilityServices.createMobilityPoint(this, getUser().getUsername(), getClient(), data);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the request with either a success message or a failure 
	 * message that contains an error code and an error text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the Mobility upload request.");
		
		super.respond(httpRequest, httpResponse, null);
	}
}