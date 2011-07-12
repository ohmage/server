package org.ohmage.request.visualization;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.service.ServiceException;
import org.ohmage.service.VisualizationServices;

/**
 * <p>A request for the number of survey responses for a campaign. This 
 * specific request requires no additional parameters.<br />
 * <br />
 * See {@link org.ohmage.request.visualization.VisualizationRequest} for other 
 * required parameters.</p>
 * 
 * @author John Jenkins
 */
public class VizSurveyResponseCountRequest extends VisualizationRequest {
	private static final Logger LOGGER = Logger.getLogger(VizSurveyResponseCountRequest.class);
	
	private static final String REQUEST_PATH = "responseplot";
	
	/**
	 * Creates a new request from the 'httpRequest' that contains the 
	 * parameters.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the parameters 
	 * 					  to build this request.
	 */
	public VizSurveyResponseCountRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
		
		LOGGER.info("Creating a survey response count visualization request.");
	}
	
	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the survey response count visualization request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		super.service();
		
		if(isFailed()) {
			return;
		}
		
		try {
			LOGGER.info("Making the request to the visualization server.");
			setImage(VisualizationServices.sendVisualizationRequest(this, REQUEST_PATH, user.getToken(), 
					getCampaignId(), getWidth(), getHeight(), new HashMap<String, String>()));
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}
}
