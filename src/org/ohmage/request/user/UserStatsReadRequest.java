package org.ohmage.request.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserMobilityServices;
import org.ohmage.service.UserSurveyResponseServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.UserValidators;

/**
 * Reads some statistical information about a user pertaining to the number of
 * uploads and the location status of those uploads. The requesting user must 
 * have sufficient permissions to view the user's survey responses, at least 
 * the shared ones, and their Mobility points, at least the shared ones. The
 * user may authenticate themselves with a username and password or by 
 * supplying an authentication token.
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
 *     <td>The unique identifier for the campaign to which the survey data will
 *       pertain.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>The username of the user whose statistical information is desired.
 *       </td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class UserStatsReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserStatsReadRequest.class);
	
	private static final String JSON_KEY_RESULT = "stats";
	
	private static final String JSON_KEY_HOURS_SINCE_LAST_SURVEY_UPLOAD = "Hours Since Last Survey Upload";
	private static final String JSON_KEY_HOURS_SINCE_LAST_MOBILITY_UPLOAD = "Hours Since Last Mobility Upload";
	private static final String JSON_KEY_PAST_DAY_SUCCESSFUL_SURVEY_LOCATION_UPDATES_PERCENTAGE = "Past Day Percent Successful Survey Location Updates";
	private static final String JSON_KEY_PAST_DAY_SUCCESSFUL_MOBILITY_LOCATION_UPDATES_PERCENTAGE = "Past Day Percent Successful Mobility Location Updates";
	
	private static final Double DEFAULT_VALUE_IF_NO_MOBILITY_UPLOADS = Double.MAX_VALUE;
	private static final Double DEFAULT_VALUE_IF_NO_MOBILITY_UPLOADS_IN_LAST_DAY = -1.0;
	
	// Parameters
	private final String campaignId;
	private final String username;
	
	// Results
	private Double hoursSinceLastSurveyUpload;
	private Double hoursSinceLastMobilityUpload;
	private Double pastDaySuccessfulSurveyLocationUpdatesPercentage;
	private Double pastDatSuccessfulMobilityLocationUpdatesPercentage;
	
	/**
	 * Creates a new user stats read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public UserStatsReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating a user stats read request.");
		
		String tCampaignId = null;
		String tUsername = null;
		
		try {
			tCampaignId = CampaignValidators.validateCampaignId(this, httpRequest.getParameter(InputKeys.CAMPAIGN_URN));
			if(tCampaignId == null) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Missing the required campaign ID: " + InputKeys.CAMPAIGN_URN);
				throw new ValidationException("Missing the required campaign ID: " + InputKeys.CAMPAIGN_URN);
			}
			else if(httpRequest.getParameterValues(InputKeys.CAMPAIGN_URN).length > 1) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Multiple campaign ID parameters were given.");
				throw new ValidationException("Multiple campaign ID parameters were given.");
			}
			
			tUsername = UserValidators.validateUsername(this, httpRequest.getParameter(InputKeys.USERNAME));
			if(tUsername == null) {
				setFailed(ErrorCodes.USER_INVALID_USERNAME, "Missing the required username: " + InputKeys.USERNAME);
				throw new ValidationException("Missing the required username: " + InputKeys.USERNAME);
			}
			else if(httpRequest.getParameterValues(InputKeys.USERNAME).length > 1) {
				setFailed(ErrorCodes.USER_INVALID_USERNAME, "Multiple username parameters were given.");
				throw new ValidationException("Multiple username parameters were given.");
			}
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		campaignId = tCampaignId;
		username = tUsername;
		
		hoursSinceLastSurveyUpload = Double.MAX_VALUE;
		hoursSinceLastMobilityUpload = Double.MAX_VALUE;
		pastDaySuccessfulSurveyLocationUpdatesPercentage = Double.MAX_VALUE;
		pastDatSuccessfulMobilityLocationUpdatesPercentage = Double.MAX_VALUE;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user stats read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the requester has permissions to view the survey information.");
			UserCampaignServices.requesterCanViewUsersSurveyResponses(this, campaignId, getUser().getUsername(), username);
			
			LOGGER.info("Verifying that the requester has permissions to view the mobility information.");
			UserMobilityServices.requesterCanViewUsersMobilityData(this, getUser().getUsername(), username);
			
			LOGGER.info("Gathering the number of hours since the last survey upload.");
			hoursSinceLastSurveyUpload = UserSurveyResponseServices.getHoursSinceLastSurveyUplaod(this, getUser().getUsername(), username);
			
			LOGGER.info("Gathering the number of hours since the last Mobility upload.");
			hoursSinceLastMobilityUpload = UserMobilityServices.getHoursSinceLastMobilityUpload(this, username);
			
			LOGGER.info("Gathering the percentage of successful location uploads from surveys in the last day.");
			pastDaySuccessfulSurveyLocationUpdatesPercentage = UserSurveyResponseServices.getPercentageOfNonNullLocationsOverPastDay(this, getUser().getUsername(), username);
			
			LOGGER.info("Gathering the percentage of successful location updates from Mobility in the last day.");
			pastDatSuccessfulMobilityLocationUpdatesPercentage = UserMobilityServices.getPercentageOfNonNullLocationsOverPastDay(this, username);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the user's request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		JSONObject jsonResult = new JSONObject();
		
		if(! isFailed()) {
			try {
				jsonResult.put(
						JSON_KEY_HOURS_SINCE_LAST_SURVEY_UPLOAD, 
						hoursSinceLastSurveyUpload);
				
				jsonResult.put(
						JSON_KEY_HOURS_SINCE_LAST_MOBILITY_UPLOAD, 
						((hoursSinceLastMobilityUpload == null) ? 
								DEFAULT_VALUE_IF_NO_MOBILITY_UPLOADS : 
								hoursSinceLastMobilityUpload)
						);
				
				jsonResult.put(
						JSON_KEY_PAST_DAY_SUCCESSFUL_SURVEY_LOCATION_UPDATES_PERCENTAGE, 
						pastDaySuccessfulSurveyLocationUpdatesPercentage);
				
				jsonResult.put(
						JSON_KEY_PAST_DAY_SUCCESSFUL_MOBILITY_LOCATION_UPDATES_PERCENTAGE, 
						((pastDatSuccessfulMobilityLocationUpdatesPercentage == null) ?
								DEFAULT_VALUE_IF_NO_MOBILITY_UPLOADS_IN_LAST_DAY : 
								pastDatSuccessfulMobilityLocationUpdatesPercentage)
						);
			}
			catch(JSONException e) {
				LOGGER.error("There was an error creating the JSONArray result object.", e);
				setFailed();
			}
		}
		
		super.respond(httpRequest, httpResponse, JSON_KEY_RESULT, jsonResult);
	}
}