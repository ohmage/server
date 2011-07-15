package org.ohmage.request.survey;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.DateValidators;
import org.ohmage.validator.UserCampaignValidators;

/**
 * <p>Uploads a survey.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>The username of the uploader.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PASSWORD}</td>
 *     <td>The password for the associated username/</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The campaign URN for the survey(s) being uploaded.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_CREATION_TIMESTAMP}</td>
 *     <td>The creation timestamp for the campaign. This parameter is used to
 *     ensure that the client's campaign is up-to-date.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DATA}</td>
 *     <td>The data payload for the survey(s) being uploaded.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author Joshua Selsky
 */
public final class SurveyUploadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyUploadRequest.class);
	
	private final Date campaignCreationTimestamp;
	private final String campaignUrn;
	private final String jsonData;
	private static final List<String> allowedRoles;
	private static final String allowedCampaignRunningState = CampaignRunningStateCache.RUNNING_STATE_RUNNING;
	
	static {
		allowedRoles = Arrays.asList(new String[] {CampaignRoleCache.ROLE_PARTICIPANT});
	}
	
	/**
	 * Builds this request based on the information in the HTTP request.
	 * 
	 * First, dispatches to the parent constructor; then checks the 
	 * following parameters from the inbound request for well-formedness
	 * and existence: {@value org.ohmage.request.InputKeys#CAMPAIGN_URN}, 
	 * {@value org.ohmage.request.InputKeys#CAMPAIGN_CREATION_TIMESTAMP}, and 
	 * {@value org.ohmage.request.InputKeys#DATA}. If the parameters all pass 
	 * their validation checks, the appropriate instance variables are set. If 
	 * initialization is not successful this instance is marked as failed and 
	 * annotated with the appropriate error.
	 * 
	 * @param httpRequest A HttpServletRequest object that contains the
	 * 					  parameters to and metadata for this request.
	 */
	public SurveyUploadRequest(HttpServletRequest httpRequest) {
		super(httpRequest.getParameter(InputKeys.USER), httpRequest.getParameter(InputKeys.PASSWORD), false, httpRequest.getParameter(InputKeys.CLIENT));
		
		Date tempCampaignCreationTimestamp = null;
		String tempCampaignUrn = null;
		String tempJsonData = null;
		
		if(! failed) {
			LOGGER.info("Creating a survey upload request.");
			 
			tempCampaignCreationTimestamp = null;
			tempCampaignUrn = httpRequest.getParameter(InputKeys.CAMPAIGN_URN);
			tempJsonData = httpRequest.getParameter(InputKeys.DATA);
			
//			try {
//				tempCampaignCreationTimestamp = DateValidators.validateISO8601DateTime(httpRequest.getParameter(InputKeys.CAMPAIGN_CREATION_TIMESTAMP));
//			}
//			catch(ValidationException e) {
//				setFailed(ErrorCodes.SURVEY_UPLOAD_INVALID_CAMPAIGN_CREATION_DATETIME, "Invalid " + InputKeys.CAMPAIGN_CREATION_TIMESTAMP);
//			}
//			
//			if(this.campaignCreationTimestamp == null) {
//				setFailed(ErrorCodes.SURVEY_UPLOAD_INVALID_CAMPAIGN_CREATION_DATETIME, "Missing " + InputKeys.CAMPAIGN_CREATION_TIMESTAMP);
//			}
//			
//			if(! StringUtils.isValidUrn(tempCampaignUrn)) {
//				setFailed(ErrorCodes.SURVEY_UPLOAD_INVALID_CAMPAIGN_ID, "Invalid campaign id: " + tempCampaignUrn + " was provided.");
//			}
//			
//			if(! StringUtils.isEmptyOrWhitespaceOnly(tempJsonData)) {
//				setFailed(ErrorCodes.SURVEY_UPLOAD_MISSING_RESPONSES, "No value found for 'data' parameter.");
//			}
		} 
		
		this.campaignCreationTimestamp = tempCampaignCreationTimestamp;
		this.campaignUrn = tempCampaignUrn;
		this.jsonData = tempJsonData;
	}
	
	
	/**
	 * Performs a survey upload in the following steps:
	 * TODO
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a survey upload request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			// 1. Populate the User with their campaign info
			// 2. Validate the logged-in user has access to the campaign
			// 3. Checks that it is a participant performing the upload
			// 4. Checks the campaign running state
			// 5. Checks that the campaign creation time matches what we have in the db
			// 6. Looks up the campaign configuration
			// 7. Validates the JSON data
			// 8. Validates each message in the JSON data
			// 9. Converts the JSON surveys into POJOs/DTOs
			// 10. Stores the surveys in the db.
			// 11. Logs the upload
			
			LOGGER.info("Populating the logged-in user with their associated campaigns and roles.");
			UserCampaignServices.populateUserWithCampaignRoleInfo(this, this.getUser());
			
			LOGGER.info("Checking the user and campaign ID in order to make sure the user belongs to the campaign ID in the request");
		    UserCampaignValidators.campaignExistsAndUserBelongs(this, this.getUser(), campaignUrn);
			
			LOGGER.info("Checking the user and the campaign ID against the allowed roles for this request");
			UserCampaignValidators.verifyAllowedUserRoleInCampaign(this, this.getUser(), this.campaignUrn, allowedRoles);
		
			LOGGER.info("Checking that the user is attempting to upload to a running campaign");
			// CampaignValidators.verifyAllowedRunningState(this, this.getUser(), this.campaignUrn, allowedCampaignRunningState);
		}
		catch(ValidationException e) {
			e.logException(LOGGER);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with a success or failure message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
	
}