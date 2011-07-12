package org.ohmage.request.visualization;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.InputKeys;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ServiceException;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserServices;
import org.ohmage.service.VisualizationServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.UserValidators;
import org.ohmage.validator.ValidationException;

public class VizUserTimeseriesRequest extends VisualizationRequest {
	private static final Logger LOGGER = Logger.getLogger(VizUserTimeseriesRequest.class);
	
	private static final String REQUEST_PATH = "userplot";
	
	private final String promptId;
	private final String username;
	
	/**
	 * Creates a user distribution visualization request.
	 * 
	 * @param httpRequest The HttpServletRequest with the required parameters.
	 */
	public VizUserTimeseriesRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
		
		LOGGER.info("Creating a user timeseries request.");
		
		String tPromptId = null;
		String tUsername = null;
		
		try {
			tPromptId = CampaignValidators.validatePromptId(this, httpRequest.getParameter(InputKeys.PROMPT_ID));
			if(tPromptId == null) {
				setFailed(ErrorCodes.VISUALIZATION_MISSING_PROMPT_ID, "Missing the parameter: " + InputKeys.PROMPT_ID);
				throw new ValidationException("Missing the parameter: " + InputKeys.PROMPT_ID);
			}
			
			tUsername = UserValidators.validateUsername(this, httpRequest.getParameter(InputKeys.USER));
			if(tUsername == null) {
				setFailed(ErrorCodes.VISUALIZATION_MISSING_USERNAME, "Missing the parameter: " + InputKeys.USER);
				throw new ValidationException("Missing the parameter: " + InputKeys.USER);
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		promptId = tPromptId;
		username = tUsername;
	}
	
	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user timeseries visualization request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		super.service();
		
		if(isFailed()) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the prompt ID exists in the campaign's XML");
			CampaignServices.ensurePromptExistsInCampaign(this, getCampaignId(), promptId);
			
			LOGGER.info("Verifying that the user exists.");
			UserServices.verifyUserExists(this, username);
			
			LOGGER.info("TODO: Verifying that the requester has permissions to view another user's data.");
			UserCampaignServices.requesterCanViewUsersSharedSurveyResponses(this, getCampaignId(), user.getUsername(), username);
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put(VisualizationServices.PARAMETER_KEY_PROMPT_ID, promptId);
			parameters.put(VisualizationServices.PARAMETER_KEY_USERNAME, username);
			
			LOGGER.info("Making the request to the visualization server.");
			setImage(VisualizationServices.sendVisualizationRequest(this, REQUEST_PATH, user.getToken(), 
					getCampaignId(), getWidth(), getHeight(), parameters));
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}
}