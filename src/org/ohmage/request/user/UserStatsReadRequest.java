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
import org.ohmage.service.UserSurveyServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.UserValidators;

/**
 * <p>Reads some statistical information about a user pertaining to the number
 * of uploads and the location status of those uploads. The requesting user 
 * must have sufficient permissions to view the user's survey responses, at 
 * least the shared ones, and their Mobility points, at least the shared ones.
 * The user may authenticate themselves with a username and password or by
 * supplying an authentication token.</p>
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
	
	// Parameters
	private final String campaignId;
	private final String username;
	
	// Results
	private double hoursSinceLastSurveyUpload;
	private double hoursSinceLastMobilityUpload;
	private double pastDaySuccessfulSurveyLocationUpdatesPercentage;
	private double pastDatSuccessfulMobilityLocationUpdatesPercentage;
	
	/**
	 * Creates a new user stats read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public UserStatsReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest.getParameter(InputKeys.USER), httpRequest.getParameter(InputKeys.PASSWORD), false,
				getToken(httpRequest), httpRequest.getParameter(InputKeys.CLIENT));
		
		LOGGER.info("Creating a user stats read request.");
		
		String tCampaignId = null;
		String tUsername = null;
		
		try {
			tCampaignId = CampaignValidators.validateCampaignId(this, httpRequest.getParameter(InputKeys.CAMPAIGN_URN));
			if(tCampaignId == null) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Missing the required campaign ID: " + InputKeys.CAMPAIGN_URN);
				throw new ValidationException("Missing the required campaign ID: " + InputKeys.CAMPAIGN_URN);
			}
			
			tUsername = UserValidators.validateUsername(this, httpRequest.getParameter(InputKeys.USERNAME));
			if(tUsername == null) {
				setFailed(ErrorCodes.USER_INVALID_USERNAME, "Missing the required username: " + InputKeys.USERNAME);
				throw new ValidationException("Missing the required username: " + InputKeys.USERNAME);
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
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the requester has permissions to view the survey information.");
			UserCampaignServices.requesterCanViewUsersSurveyResponses(this, campaignId, user.getUsername(), username);
			
			LOGGER.info("Verifying that the requester has permissions to view the mobility information.");
			UserMobilityServices.requesterCanViewUsersMobilityData(this, user.getUsername(), username);
			
			LOGGER.info("Gathering the number of hours since the last survey upload.");
			hoursSinceLastSurveyUpload = UserSurveyServices.getHoursSinceLastSurveyUplaod(this, user.getUsername(), username);
			
			LOGGER.info("Gathering the number of hours since the last Mobility upload.");
			hoursSinceLastMobilityUpload = UserMobilityServices.getHoursSinceLastMobilityUpload(this, user.getUsername(), username);
			
			LOGGER.info("Gathering the percentage of successful location uploads from surveys in the last day.");
			pastDaySuccessfulSurveyLocationUpdatesPercentage = UserSurveyServices.getPercentageOfNonNullLocationsOverPastDay(this, user.getUsername(), username);
			
			LOGGER.info("Gathering the percentage of successful location updates from Mobility in the last day.");
			pastDatSuccessfulMobilityLocationUpdatesPercentage = UserMobilityServices.getPercentageOfNonNullLocationsOverPastDay(this, user.getUsername(), username);
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
		
		if(! failed) {
			try {
				jsonResult.put(JSON_KEY_HOURS_SINCE_LAST_SURVEY_UPLOAD, hoursSinceLastSurveyUpload);
				jsonResult.put(JSON_KEY_HOURS_SINCE_LAST_MOBILITY_UPLOAD, hoursSinceLastMobilityUpload);
				jsonResult.put(JSON_KEY_PAST_DAY_SUCCESSFUL_SURVEY_LOCATION_UPDATES_PERCENTAGE, pastDaySuccessfulSurveyLocationUpdatesPercentage);
				jsonResult.put(JSON_KEY_PAST_DAY_SUCCESSFUL_MOBILITY_LOCATION_UPDATES_PERCENTAGE, pastDatSuccessfulMobilityLocationUpdatesPercentage);
			}
			catch(JSONException e) {
				LOGGER.error("There was an error creating the JSONArray result object.", e);
				setFailed();
			}
		}
		
		super.respond(httpRequest, httpResponse, JSON_KEY_RESULT, jsonResult);
	}
}