package org.ohmage.request.campaign;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ServiceException;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ValidationException;

/**
 * <p>Deletes a campaign.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The unique identifier for the campaign to be deleted.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class CampaignDeletionRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(CampaignDeletionRequest.class);
	
	private final String campaignId;
	
	public CampaignDeletionRequest(HttpServletRequest httpRequest) {
		super(getToken(httpRequest), httpRequest.getParameter(InputKeys.CLIENT));
		
		LOGGER.info("Creating a campaign deletion request.");
		
		String tCampaignId = null;
		
		try {
			tCampaignId = CampaignValidators.validateCampaignId(this, httpRequest.getParameter(InputKeys.CAMPAIGN_URN));
			if(tCampaignId == null) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "A campaign identifier is required: " + InputKeys.CAMPAIGN_URN);
				throw new ValidationException("A campaign identifier is required: " + InputKeys.CAMPAIGN_URN);
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		campaignId = tCampaignId;
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the campaign deletion request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the campaign exists.");
			CampaignServices.checkCampaignExistence(this, campaignId, true);
			
			LOGGER.info("Verifying that the requesting user is allowed to delete the campaign.");
			UserCampaignServices.userCanDeleteCampaign(this, user.getUsername(), campaignId);
			
			LOGGER.info("Deleting the campaign.");
			CampaignServices.deleteCampaign(this, campaignId);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the request with a success or fail message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}