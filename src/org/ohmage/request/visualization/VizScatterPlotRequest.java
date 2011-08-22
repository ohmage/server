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
 * <p>A request for a scatter plot of two prompt types in the same campaign.
 * <br />
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
 *     <td>The first prompt ID in the campaign's XML.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PROMPT2_ID}</td>
 *     <td>The second prompt ID in the campaign's XML.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class VizScatterPlotRequest extends VisualizationRequest {
	private static final Logger LOGGER = Logger.getLogger(VizScatterPlotRequest.class);
	
	private static final String REQUEST_PATH = "scatterplot";
	
	private final String promptId;
	private final String prompt2Id;
	
	/**
	 * Creates a scatter plot visualization request.
	 * 
	 * @param httpRequest The HttpServletRequest with the required parameters.
	 */
	public VizScatterPlotRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
		
		LOGGER.info("Creating a scatter plot request.");
		
		String tPromptId = null;
		String tPrompt2Id = null;
		
		try {
			tPromptId = CampaignValidators.validatePromptId(this, httpRequest.getParameter(InputKeys.PROMPT_ID));
			if(tPromptId == null) {
				setFailed(ErrorCodes.SURVEY_INVALID_PROMPT_ID, "Missing the parameter: " + InputKeys.PROMPT_ID);
				throw new ValidationException("Missing the parameter: " + InputKeys.PROMPT_ID);
			}
			
			tPrompt2Id = CampaignValidators.validatePromptId(this, httpRequest.getParameter(InputKeys.PROMPT2_ID));
			if(tPrompt2Id == null) {
				setFailed(ErrorCodes.SURVEY_INVALID_PROMPT_ID, "Missing the parameter: " + InputKeys.PROMPT2_ID);
				throw new ValidationException("Missing the parameter: " + InputKeys.PROMPT2_ID);
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		promptId = tPromptId;
		prompt2Id = tPrompt2Id;
	}
	
	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the scatter plot visualization request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		super.service();
		
		if(isFailed()) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the first prompt ID exists in the campaign's XML");
			CampaignServices.ensurePromptExistsInCampaign(this, getCampaignId(), promptId);
			
			LOGGER.info("Verifying that the second prompt ID exists in the campaign's XML");
			CampaignServices.ensurePromptExistsInCampaign(this, getCampaignId(), prompt2Id);
			
			Map<String, String> parameters = getVisualizationParameters();
			parameters.put(VisualizationServices.PARAMETER_KEY_PROMPT_ID, promptId);
			parameters.put(VisualizationServices.PARAMETER_KEY_PROMPT2_ID, prompt2Id);
			
			LOGGER.info("Making the request to the visualization server.");
			setImage(VisualizationServices.sendVisualizationRequest(this, REQUEST_PATH, getUser().getToken(), 
					getCampaignId(), getWidth(), getHeight(), parameters));
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}
}