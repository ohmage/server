package org.ohmage.request.survey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.dao.SurveyUploadDao;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.domain.upload.SurveyResponse;
import org.ohmage.domain.upload.SurveyUploadBuilder;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyUploadServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.DateValidators;

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
	
	private static final List<CampaignRoleCache.Role> ALLOWED_ROLES;
	private static final CampaignRunningStateCache.RunningState ALLOWED_CAMPAIGN_RUNNING_STATE = CampaignRunningStateCache.RunningState.RUNNING;
	
	static {
		ALLOWED_ROLES = Arrays.asList(new CampaignRoleCache.Role[] {CampaignRoleCache.Role.PARTICIPANT});
	}
	
	// The campaign creation timestamp is stored as a String because it is 
	// never used in any kind of calculation.
	private final String campaignCreationTimestamp;
	private final String campaignUrn;
	
	private Configuration configuration;
	private String jsonData;
	private JSONArray jsonDataArray;
	
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
		super(httpRequest, false);
		
		Date tempCampaignCreationTimestamp = null;
		String tempCampaignUrn = null;
		String tempJsonData = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a survey upload request.");
			 
			tempCampaignCreationTimestamp = null;
			String[] campaignIdArray = getParameterValues(InputKeys.CAMPAIGN_URN);
			if(campaignIdArray.length == 1) {
				tempCampaignUrn = campaignIdArray[0];
			}
			String[] dataArray = getParameterValues(InputKeys.DATA);
			if(dataArray.length == 1) {
				tempJsonData = dataArray[0];
			}
			
			try {
				String[] campaignCreationTimestampArray = getParameterValues(InputKeys.CAMPAIGN_CREATION_TIMESTAMP);
				if(campaignCreationTimestampArray.length == 1) {
					tempCampaignCreationTimestamp = DateValidators.validateISO8601DateTime(campaignCreationTimestampArray[0]);
				}
				LOGGER.debug("tempCampaignCreationTimestamp = " + tempCampaignCreationTimestamp);
			}
			catch(ValidationException e) {
				setFailed(ErrorCodes.SERVER_INVALID_TIMESTAMP, "Invalid " + InputKeys.CAMPAIGN_CREATION_TIMESTAMP);
			}
			
			if(! isFailed() && tempCampaignCreationTimestamp == null) {
				setFailed(ErrorCodes.SERVER_INVALID_TIMESTAMP, "Missing " + InputKeys.CAMPAIGN_CREATION_TIMESTAMP);				
			}
			
			if(! isFailed() && ! StringUtils.isValidUrn(tempCampaignUrn)) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Invalid campaign id: " + tempCampaignUrn + " was provided.");
			}
			
			if(! isFailed() && StringUtils.isEmptyOrWhitespaceOnly(tempJsonData)) {
				setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, "No value found for 'data' parameter.");
			}
		}
		
		this.campaignCreationTimestamp = TimeUtils.getIso8601DateTimeString(tempCampaignCreationTimestamp);
		this.campaignUrn = tempCampaignUrn;
		this.jsonData = tempJsonData;
	}
	
	/**
	 * Performs a survey upload in the following steps:
	 * <ol>
	 * <li>TODO</li>
	 * </ol>
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a survey upload request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
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
			
			LOGGER.info("Populating the logged-in user with their associated campaigns and roles.");
			UserCampaignServices.populateUserWithCampaignRoleInfo(this, this.getUser());
			
			LOGGER.info("Checking the user and campaign ID in order to make sure the user belongs to the campaign ID in the request");
		    UserCampaignServices.campaignExistsAndUserBelongs(this, this.getUser(), campaignUrn);
			
			LOGGER.info("Checking the user and the campaign ID against the allowed roles for this request");
			UserCampaignServices.verifyAllowedUserRoleInCampaign(this, this.getUser(), this.campaignUrn, ALLOWED_ROLES);
		
			LOGGER.info("Checking that the user is attempting to upload to a running campaign");
			CampaignServices.verifyAllowedRunningState(this, this.getUser(), this.campaignUrn, ALLOWED_CAMPAIGN_RUNNING_STATE);
			
			LOGGER.info("Checking the campaign creation timestamp to ensure a user is not attempting to upload to an out-of-date canmpaign.");
			CampaignServices.verifyCampaignCreationTimestamp(this, this.getUser(), this.campaignUrn, this.campaignCreationTimestamp);
			
			LOGGER.info("Retrieving campaign configuration.");
			this.configuration = CampaignServices.findCampaignConfiguration(this, this.campaignUrn);
			
			LOGGER.info("Parsing JSON data upload.");
			// Each survey in an upload is represented by a JSONObject within
			// a JSONArray 
			this.jsonDataArray = SurveyUploadServices.stringToJsonArray(this, this.jsonData);
			
			// Recycle the string because it's no longer needed and it's
			// potentially quite large
			this.jsonData = null;
			
			LOGGER.info("Validating surveys.");
			SurveyUploadServices.validateSurveyUpload(this, jsonDataArray, configuration);
			
			LOGGER.info("Prepping surveys for db insertion.");
			
			int numberOfSurveyResponses = jsonDataArray.length();
			List<SurveyResponse> surveyUploadList = new ArrayList<SurveyResponse>();
			for(int i = 0; i < numberOfSurveyResponses; i++) {
				surveyUploadList.add(SurveyUploadBuilder.createSurveyUploadFrom(configuration, JsonUtils.getJsonObjectFromJsonArray(jsonDataArray, i)));
			}

			LOGGER.info("Saving " + numberOfSurveyResponses + " surveys into the db.");
			List<Integer> duplicateIndexList = SurveyUploadDao.insertSurveys(this, getUser(), getClient(), campaignUrn, surveyUploadList);
			
			LOGGER.info("Found " + duplicateIndexList.size() + " duplicate survey uploads");
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
		catch(DataAccessException e) {
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