package org.ohmage.request.survey;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

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
import org.ohmage.validator.DateValidators;
import org.ohmage.validator.ImageValidators;

/**
 * <p>Stores a survey and its associated images (if any are present in the payload)</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY}</td>
 *     <td>The survey data payload for the survey(s) being uploaded.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>A UUID linking the binary image data to a UUID that must be present
 *      in the survey data payload. There can be many images attached to a
 *      survey upload.</td>
 *     <td></td>
 *     <td>true, only if the survey data payload contains image prompt responses</td>
 *   </tr>
 * </table>
 * 
 * @author Joshua Selsky
 */
public class SurveyUploadRequest extends UserRequest {
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
	private final Map<String, BufferedImage> imageContentsMap;
	
	private Configuration configuration;
	private String jsonData;
	private JSONArray jsonDataArray;
	
	/**
	 * Creates a new image upload request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public SurveyUploadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, false);
		
		LOGGER.info("Creating an atomic survey upload request.");
		
		String tCampaignCreationTimestamp = null;
		String tCampaignUrn = null;
		String tJsonData = null;
		Map<String, BufferedImage> tImageContentsMap = null;
		
		if(! isFailed()) {
			try {
				Map<String, String[]> parameters = getParameters();
				
				// Validate the campaign URN
				
				String[] t = parameters.get(InputKeys.CAMPAIGN_URN);
				if(t == null || t.length != 1) {
					setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "campaign_urn is missing or there is more than one.");
					throw new ValidationException("campaign_urn is missing or there is more than one.");
				} else {
					tCampaignUrn = t[0];
				}
				
				// Validate the campaign creation timestamp
				
				t = parameters.get(InputKeys.CAMPAIGN_CREATION_TIMESTAMP);
				if(t == null || t.length != 1) {
					setFailed(ErrorCodes.SERVER_INVALID_TIMESTAMP, "campaign_creation_timestamp is missing or there is more than one");
					throw new ValidationException("campaign_creation_timestamp is missing or there is more than one");
				} 
				else {
					
					// Make sure it's a valid timestamp
					try {
						DateValidators.validateISO8601DateTime(t[0]);
					} 
					catch(ValidationException e) {
						setFailed(ErrorCodes.SERVER_INVALID_TIMESTAMP, "campaign_creation_timestamp is malformed");
						throw e;
					}
					tCampaignCreationTimestamp = t[0];
				}
				
				t = parameters.get(InputKeys.SURVEYS);
				if(t == null || t.length != 1) {
					setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, "No value found for 'surveys' parameter or multiple surveys parameters were found.");
					throw new ValidationException("No value found for 'surveys' parameter or multiple surveys parameters were found.");
				}
				else {
					tJsonData = StringUtils.urlDecode(t[0]);
				}
				
				// Retrieve and validate images
				
				List<String> imageIds = new ArrayList<String>();
				Collection<Part> parts = null;
				
				try {
					// FIXME - push to base class especially because of the ServletException that gets thrown
					parts = httpRequest.getParts();
					for(Part p : parts) {
						try {
							UUID.fromString(p.getName());
							imageIds.add(p.getName());
						}
						catch (IllegalArgumentException e) {
							// ignore because there may not be any UUIDs/images
						}
					}
				}
				catch(ServletException e) {
					LOGGER.error("cannot parse parts", e);
					setFailed();
					throw new ValidationException(e);
				}
				catch(IOException e) {
					LOGGER.error("cannot parse parts", e);
					setFailed();
					throw new ValidationException(e);
				}
				
				Set<String> stringSet = new HashSet<String>(imageIds);
				
				if(stringSet.size() != imageIds.size()) {
					setFailed(ErrorCodes.IMAGE_INVALID_DATA, "a duplicate image key was detected in the multi-part upload");
					throw new ValidationException("a duplicate image key was detected in the multi-part upload");
				}

				for(String imageId : imageIds) {
					
					BufferedImage bufferedImage = ImageValidators.validateImageContents(this, getMultipartValue(httpRequest, imageId));
					if(tImageContentsMap == null) {
						tImageContentsMap = new HashMap<String, BufferedImage>();
					}
					tImageContentsMap.put(imageId, bufferedImage);
					
					if(LOGGER.isDebugEnabled()) {
						LOGGER.debug("succesfully created a BufferedImage for key " + imageId);
					}
				}
				
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		this.campaignCreationTimestamp = tCampaignCreationTimestamp;
		this.campaignUrn = tCampaignUrn;
		this.jsonData = tJsonData;
		this.imageContentsMap = tImageContentsMap;
	}

	/**
	 * Services the request.
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
			List<String> imageIdList = SurveyUploadServices.validateSurveyUpload(this, jsonDataArray, configuration);
			SurveyUploadServices.validateImageKeys(this, imageIdList, imageContentsMap);
			
			LOGGER.info("Prepping surveys for db insertion.");
			
			int numberOfSurveyResponses = jsonDataArray.length();
			List<SurveyResponse> surveyUploadList = new ArrayList<SurveyResponse>();
			for(int i = 0; i < numberOfSurveyResponses; i++) {
				surveyUploadList.add(SurveyUploadBuilder.createSurveyUploadFrom(configuration, JsonUtils.getJsonObjectFromJsonArray(jsonDataArray, i)));
			}

			LOGGER.info("Saving " + numberOfSurveyResponses + " surveys into the db.");
			
			List<Integer> duplicateIndexList = SurveyUploadDao.insertSurveys(this, getUser(), getClient(), campaignUrn, surveyUploadList, imageContentsMap);

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
	 * Responds to the image upload request with success or a failure message
	 * that contains a failure code and failure text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the survey upload request.");
		
		super.respond(httpRequest, httpResponse, null);
	}
}