package org.ohmage.request.visualization;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.VisualizationServices;
import org.ohmage.validator.CampaignValidators;

/**
 * <p>A request for a graph of the number of answers for each possible answer
 * of a specific prompt over time.<br />
 * <br />
 * See {@link org.ohmage.request.visualization.VisualizationRequest} for other 
 * required parameters.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PROMPT_ID}</td>
 *     <td>A prompt ID in the campaign's XML.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class VizPromptTimeseriesRequest extends VisualizationRequest {
private static final Logger LOGGER = Logger.getLogger(VizPromptTimeseriesRequest.class);
	
	private static final String REQUEST_PATH = "timeplot";
	
	private final String promptId;
	
	/**
	 * Creates a prompt timeseries visualization request.
	 * 
	 * @param httpRequest The HttpServletRequest with the required parameters.
	 */
	public VizPromptTimeseriesRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
		
		LOGGER.info("Creating a prompt timeseries request.");
		
		String tPromptId = null;
		
		try {
			tPromptId = CampaignValidators.validatePromptId(this, httpRequest.getParameter(InputKeys.PROMPT_ID));
			if(tPromptId == null) {
				setFailed(ErrorCodes.SURVEY_INVALID_PROMPT_ID, "Missing the parameter: " + InputKeys.PROMPT_ID);
				throw new ValidationException("Missing the parameter: " + InputKeys.PROMPT_ID);
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		promptId = tPromptId;
	}
	
	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the prompt timeseries visualization request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		super.service();
		
		if(isFailed()) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the prompt ID exists in the campaign's XML");
			CampaignServices.ensurePromptExistsInCampaign(this, getCampaignId(), promptId);
			
			Map<String, String> parameters = getVisualizationParameters();
			parameters.put(VisualizationServices.PARAMETER_KEY_PROMPT_ID, promptId);
			
			LOGGER.info("Making the request to the visualization server.");
			setImage(VisualizationServices.sendVisualizationRequest(this, REQUEST_PATH, getUser().getToken(), 
					getCampaignId(), getWidth(), getHeight(), parameters));
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}
}
