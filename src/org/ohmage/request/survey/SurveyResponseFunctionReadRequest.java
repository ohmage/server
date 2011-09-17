package org.ohmage.request.survey;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.domain.SurveyResponseInformation;
import org.ohmage.domain.SurveyResponseInformation.Location;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.SurveyResponseValidators;
import org.ohmage.validator.SurveyResponseValidators.Function;
import org.ohmage.validator.UserValidators;

/**
 * <p>Gathers information about survey responses based on the given function
 * ID.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUTH_TOKEN}</td>
 *     <td>The requesting user's authentication token. This may be a parameter
 *       or may be a cookie in the HTTP request.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The campaigns's unique identifier.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_FUNCTION_ID}</td>
 *     <td>A survey function ID that dictates the type of information that will
 *       be returned. Must be one of 
 *       {@link org.ohmage.validator.SurveyResponseValidators.Function}.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#START_DATE}</td>
 *     <td>The date where all survey responses analyzed must be on or after 
 *       this date.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>The date where all survey responses analyzed must be on or before
 *       this date.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_RESPONSE_OWNER}</td>
 *     <td>This will limit the survey responses being analyzed to only include
 *       those from this user.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class SurveyResponseFunctionReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseFunctionReadRequest.class);
	
	private static final String JSON_KEY_VALUE = "value";
	private static final String JSON_KEY_TIMESTAMP = "timestamp";
	private static final String JSON_KEY_TIMEZONE = "timezone";
	private static final String JSON_KEY_LOCATION_STATUS = "location_status";
	private static final String JSON_KEY_LOCATION = "location";
	
	private final String campaignId;
	private final Function functionId;
	private final Date startDate;
	private final Date endDate;
	private final String ownersUsername;
	
	private List<SurveyResponseInformation> surveyResponses;
	
	/**
	 * Creates a new survey response function read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for the
	 * 					  request.
	 */
	public SurveyResponseFunctionReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating a survey response function read request.");
		
		String tCampaignId = null;
		Date tStartDate = null;
		Date tEndDate = null;
		String tOwnersUsername = null;
		Function tFunctionId = null;
		
		if(! isFailed()) {
			try {
				String[] campaignIds = getParameterValues(InputKeys.CAMPAIGN_URN);
				if(campaignIds.length == 0) {
					setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The required campaign ID is missing: " + InputKeys.CAMPAIGN_URN);
					throw new ValidationException("The required campaign ID is missing: " + InputKeys.CAMPAIGN_URN);
				}
				else if(campaignIds.length == 1) {
					tCampaignId = CampaignValidators.validateCampaignId(this, campaignIds[0]);
					
					if(tCampaignId == null) {
						setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The required campaign ID is missing: " + InputKeys.CAMPAIGN_URN);
						throw new ValidationException("The required campaign ID is missing: " + InputKeys.CAMPAIGN_URN);
					}
				}
				else {
					setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "Multiple campaign IDs were found: " + InputKeys.CAMPAIGN_URN);
					throw new ValidationException("Multiple campaign IDs were found: " + InputKeys.CAMPAIGN_URN);
				}
				
				String[] functionIds = getParameterValues(InputKeys.SURVEY_FUNCTION_ID);
				if(functionIds.length == 0) {
					setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_FUNCTION_ID, "The survey function ID is missing: " + InputKeys.SURVEY_FUNCTION_ID);
					throw new ValidationException("The survey function ID is missing: " + InputKeys.SURVEY_FUNCTION_ID);
				}
				else if(functionIds.length == 1) {
					tFunctionId = SurveyResponseValidators.validateFunction(this, functionIds[0]);
					
					if(tFunctionId == null) {
						setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_FUNCTION_ID, "The survey function ID is missing: " + InputKeys.SURVEY_FUNCTION_ID);
						throw new ValidationException("The survey function ID is missing: " + InputKeys.SURVEY_FUNCTION_ID);
					}
				}
				else {
					setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_FUNCTION_ID, "Multiple survey function IDs were given: " + InputKeys.SURVEY_FUNCTION_ID);
					throw new ValidationException("Multiple survey function IDs were given: " + InputKeys.SURVEY_FUNCTION_ID);
				}
				
				String[] startDates = getParameterValues(InputKeys.START_DATE);
				if(startDates.length == 0) {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "The required start date is missing: " + InputKeys.START_DATE);
					throw new ValidationException("The required start date is missing: " + InputKeys.START_DATE);
				}
				else if(startDates.length == 1) {
					tStartDate = SurveyResponseValidators.validateStartDate(this, startDates[0]);
					
					if(tStartDate == null) {
						setFailed(ErrorCodes.SERVER_INVALID_DATE, "The required start date is missing: " + InputKeys.START_DATE);
						throw new ValidationException("The required start date is missing: " + InputKeys.START_DATE);
					}
				}
				else {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "Mutliple start dates were found: " + InputKeys.START_DATE);
					throw new ValidationException("Mutliple start dates were found: " + InputKeys.START_DATE);
				}
				
				String[] endDates = getParameterValues(InputKeys.END_DATE);
				if(endDates.length == 0) {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "The required end date is missing: " + InputKeys.END_DATE);
					throw new ValidationException("The required end date is missing: " + InputKeys.END_DATE);
				}
				else if(endDates.length == 1) {
					tEndDate = SurveyResponseValidators.validateEndDate(this, endDates[0]);
					
					if(tEndDate == null) {
						setFailed(ErrorCodes.SERVER_INVALID_DATE, "The required end date is missing: " + InputKeys.END_DATE);
						throw new ValidationException("The required end date is missing: " + InputKeys.END_DATE);
					}
				}
				else {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "Mutliple end dates were found: " + InputKeys.END_DATE);
					throw new ValidationException("Mutliple end dates were found: " + InputKeys.END_DATE);
				}
				
				String[] usernames = getParameterValues(InputKeys.SURVEY_RESPONSE_OWNER);
				if(usernames.length == 1) {
					tOwnersUsername = UserValidators.validateUsername(this, usernames[0]);
				}
				else if(usernames.length > 1) {
					setFailed(ErrorCodes.USER_INVALID_USERNAME, "Multiple owner usernames were found: " + InputKeys.SURVEY_RESPONSE_OWNER);
					throw new ValidationException("Multiple owner usernames were found: " + InputKeys.SURVEY_RESPONSE_OWNER);
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		campaignId = tCampaignId;
		startDate = tStartDate;
		endDate = tEndDate;
		ownersUsername = tOwnersUsername;
		functionId = tFunctionId;
		
		surveyResponses = Collections.emptyList();
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing survey response function read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the user is allowed to read the requested survey responses.");
			UserCampaignServices.requesterCanViewUsersSurveyResponses(
					this, 
					campaignId, 
					getUser().getUsername(), 
					(ownersUsername == null) ?
						getUser().getUsername() :
						ownersUsername);
			
			LOGGER.info("Gathering the survey response information.");
			surveyResponses = SurveyResponseServices.readSurveyResponseInformation(
					this, 
					campaignId, 
					ownersUsername, 
					null, 
					startDate, 
					endDate, 
					null, 
					null, 
					null, 
					null);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Respond to the request based on the function ID.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the survey response function read request.");
		
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
			return;
		}
		
		try {
			switch(functionId) {
			// Returns metadata information about each of the survey responses.
			case COMPLETED_SURVEYS:
				JSONArray completedSurveysResult = new JSONArray();
				
				for(SurveyResponseInformation surveyResponse : surveyResponses) {
					JSONObject currResult = new JSONObject();
					
					currResult.put(JSON_KEY_VALUE, surveyResponse.getSurveyId());
					currResult.put(JSON_KEY_TIMESTAMP, TimeUtils.getIso8601DateTimeString(surveyResponse.getDate()));
					currResult.put(JSON_KEY_TIMEZONE, surveyResponse.getTimezone().getID());
					currResult.put(JSON_KEY_LOCATION_STATUS, surveyResponse.getLocationStatus().toString().toLowerCase());
					Location location = surveyResponse.getLocation();
					if(location == null) {
						currResult.put(JSON_KEY_LOCATION, new JSONObject());
					}
					else {
						currResult.put(JSON_KEY_LOCATION, location.toJson());
					}
					
					completedSurveysResult.put(currResult);
				}
				
				super.respond(httpRequest, httpResponse, JSON_KEY_DATA, completedSurveysResult);
				break;
			// Returns aggregated statistics about the survey responses.
			case STATS:
				JSONObject statsResult = new JSONObject();
				
				Map<SurveyResponsePrivacyStateCache.PrivacyState, Integer> privacyStates = new HashMap<SurveyResponsePrivacyStateCache.PrivacyState, Integer>();
				for(SurveyResponseInformation surveyResponse : surveyResponses) {
					SurveyResponsePrivacyStateCache.PrivacyState privacyState = surveyResponse.getPrivacyState();
					
					Integer count = privacyStates.get(privacyState);
					if(count == null) {
						privacyStates.put(privacyState, 1);
					}
					else {
						privacyStates.put(privacyState, count + 1);
					}
				}
				
				for(SurveyResponsePrivacyStateCache.PrivacyState privacyState : privacyStates.keySet()) {
					statsResult.put(privacyState.toString(), privacyStates.get(privacyState));
				}
				
				respond(httpRequest, httpResponse, statsResult);
				break;
			}
		}
		catch(JSONException e) {
			LOGGER.error("Error building response JSON.", e);
			setFailed();
			super.respond(httpRequest, httpResponse, null);
		}
	}
}