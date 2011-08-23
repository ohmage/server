package org.ohmage.request.visualization;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.exception.ServiceException;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.VisualizationServices;

/**
 * <p>A request for the timeseries of private survey responses for a campaign. 
 * This specific request requires no additional parameters.<br />
 * <br />
 * See {@link org.ohmage.request.visualization.VisualizationRequest} for other 
 * required parameters.</p>
 * 
 * @author John Jenkins
 */
public class VizSurveyResponsePrivacyStateTimeseriesRequest extends VisualizationRequest {
	private static final Logger LOGGER = Logger.getLogger(VizSurveyResponsePrivacyStateTimeseriesRequest.class);
	
	private static final String REQUEST_PATH = "sharedtimeplot";
	
	/**
	 * Creates a survey response privacy state timeseries request.
	 * 
	 * @param httpRequest A HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public VizSurveyResponsePrivacyStateTimeseriesRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
		
		LOGGER.info("Creating a survey response privacy state timeseries visualization request.");
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the survey response privacy state timeseries visualization request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		super.service();
		
		if(isFailed()) {
			return;
		}
		
		try {
			LOGGER.info("Verifying the user is able to read survey responses about other users.");
			UserCampaignServices.requesterCanViewUsersSurveyResponses(this, getCampaignId(), getUser().getUsername());
			
			Map<String, String> parameters = getVisualizationParameters();
			parameters.remove(VisualizationServices.PARAMETER_KEY_PRIVACY_STATE);
			
			LOGGER.info("Making the request to the visualization server.");
			setImage(VisualizationServices.sendVisualizationRequest(this, REQUEST_PATH, getUser().getToken(), 
					getCampaignId(), getWidth(), getHeight(), parameters));
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}
}
