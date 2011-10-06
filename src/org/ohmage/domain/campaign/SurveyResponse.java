package org.ohmage.domain.campaign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.Location;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.exception.ErrorCodeException;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

/**
 * This class is responsible for converting an uploaded or database-stored copy
 * of a survey response including the metadata about the response as well as 
 * the individual prompt responses.
 * 
 * @author John Jenkins
 */
public class SurveyResponse {
	private static final String JSON_KEY_USERNAME = "user";
	private static final String JSON_KEY_CAMPAIGN_ID = "campaign_id";
	private static final String JSON_KEY_CLIENT = "client";
	private static final String JSON_KEY_DATE = "date";
	private static final String JSON_KEY_TIME = "time";
	private static final String JSON_KEY_TIMEZONE = "timezone";
	private static final String JSON_KEY_LOCATION_STATUS = "location_status";
	private static final String JSON_KEY_LOCATION = "location";
	private static final String JSON_KEY_SURVEY_ID = "survey_id";
	private static final String JSON_KEY_SURVEY_NAME = "survey_title";
	private static final String JSON_KEY_SURVEY_DESCRIPTION = "survey_description";
	private static final String JSON_KEY_SURVEY_LAUNCH_CONTEXT = "survey_launch_context";
	private static final String JSON_KEY_RESPONSES = "responses";
	private static final String JSON_KEY_PRIVACY_STATE = "privacy_state";
	
	private static final String JSON_KEY_SURVEY_RESPONSE_ID = "survey_key";
	
	private static final String JSON_KEY_PROMPT_ID = "prompt_id";
	private static final String JSON_KEY_REPEATABLE_SET_ID = "repeatable_set_id";
	
	private static final String JSON_KEY_PROMPT_VALUE = "value";
	
	private final String username;
	private final String campaignId;
	private final String client;
	
	private final Date date;
	private final long time;
	private final TimeZone timezone;
	
	/**
	 * The possible status values of a location.
	 * 
	 * @author John Jenkins
	 */
	public static enum LocationStatus { 
		VALID,
		NETWORK,
		INACCURATE,
		STALE,
		UNAVAILABLE;
		
		/**
		 * Returns the location status value as an all-lower-case string.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	private final LocationStatus locationStatus;
	private final Location location;
	
	private final Survey survey;
	private final long surveyResponseId;
	private final Map<Integer, Response> promptResponses;
	
	/**
	 * Survey response privacy states.
	 * 
	 * @author John Jenkins
	 */
	public static enum PrivacyState {
		PRIVATE,
		SHARED,
		INVISIBLE;
		
		/**
		 * Converts a String value into a PrivacyState or throws an exception
		 * if there is no comparable privacy state.
		 * 
		 * @param privacyState The privacy state to be converted into a 
		 * 					   PrivacyState enum.
		 * 
		 * @return A comparable PrivacyState enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									PrivacyState enum.
		 */
		public static PrivacyState getValue(final String privacyState) {
			return valueOf(privacyState.toUpperCase());
		}
		
		/**
		 * Returns all of the privacy states that are being used.
		 * 
		 * @return A list of privacy states.
		 */
		public static PrivacyState[] getPrivacyStates() {
			return new PrivacyState[] { PRIVATE, SHARED };
		}
		
		/**
		 * Converts the privacy state to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	private final PrivacyState privacyState;
	
	/**
	 * Context information gathered by the phone when this survey was launched.
	 * 
	 * @author John Jenkins
	 */
	public static final class LaunchContext {
		private static final String JSON_KEY_LAUNCH_TIME = "launch_time";
		private static final String JSON_KEY_ACTIVE_TRIGGERS = "active_triggers";
		
		private final Date launchTime;
		private final List<String> activeTriggers;
		
		/**
		 * Creates a LaunchContext object from a JSONObject.
		 * 
		 * @param launchContext A JSONObject that contains the launch context
		 * 						information.
		 * 
		 * @throws ErrorCodeException Thrown if the launchContext is null, if 
		 * 							  any of the keys are missing from the 
		 * 							  launchContext, or if any of the values 
		 * 							  for those keys are invalid.
		 */
		private LaunchContext(final JSONObject launchContext) throws ErrorCodeException {
			if(launchContext == null) {
				throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, "The launch context cannot be null.");
			}
			
			try {
				launchTime = StringUtils.decodeDateTime(launchContext.getString(JSON_KEY_LAUNCH_TIME));
				
				if(launchTime == null) {
					throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, "The launch time is missing in the launch context.");
				}
			}
			catch(JSONException e) {
				throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, "The launch time is missing in the launch context.");
			}
			
			try {
				JSONArray jsonActiveTriggers = launchContext.getJSONArray(JSON_KEY_ACTIVE_TRIGGERS);
				int triggersLength = jsonActiveTriggers.length();
				activeTriggers = new ArrayList<String>(triggersLength);
				
				for(int i = 0; i < triggersLength; i++) {
					activeTriggers.add(jsonActiveTriggers.getString(i));
				}
			}
			catch(JSONException e) {
				throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, "The active triggers list is missing in the launch context.");
			}
		}
		
		/**
		 * Creates a new LaunchContext.
		 * 
		 * @param launchTime The date and time that the survey was launched.
		 * 
		 * @param activeTriggers A, possibly empty, list of trigger IDs that 
		 * 						 were active when the survey was launched. 
		 */
		public LaunchContext(final Date launchTime, 
				final Collection<String> activeTriggers) {
			if(launchTime == null) {
				throw new IllegalArgumentException("The date cannot be null.");
			}
			else if(activeTriggers == null) {
				throw new IllegalArgumentException("The collection of trigger IDs cannot be null");
			}
			
			this.launchTime = launchTime;
			this.activeTriggers = new ArrayList<String>(activeTriggers);
		}
		
		/**
		 * Returns a new Date object that represents this launch time.
		 * 
		 * @return A new Date object that represents this launch time.
		 */
		public final Date getLaunchTime() {
			return new Date(launchTime.getTime());
		}
		
		/**
		 * Returns a new List object that contains all of the active triggers.
		 * 
		 * @return A new List object that contains all of the active triggers.
		 */
		public final List<String> getActiveTriggers() {
			return new ArrayList<String>(activeTriggers);
		}
		
		/**
		 * Creates a JSONObject that represents this object.
		 * 
		 * @param longVersion Whether or not include the extra data or just the
		 * 				 	  basics, "false" is just the basics "true" is 
		 * 					  everything.
		 * 
		 * @return A JSONObject that represents this object or null if there 
		 * 		   was an error.
		 */
		public final JSONObject toJson(final boolean longVersion) {
			try {
				JSONObject result = new JSONObject();
				
				result.put(JSON_KEY_LAUNCH_TIME, TimeUtils.getIso8601DateTimeString(launchTime));
				
				if(longVersion) {
					result.put(JSON_KEY_ACTIVE_TRIGGERS, activeTriggers);
				}
				
				return result;
			}
			catch(JSONException e) {
				return null;
			}
		}

		/**
		 * Generates a hash code for this launch context.
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((activeTriggers == null) ? 0 : activeTriggers.hashCode());
			result = prime * result
					+ ((launchTime == null) ? 0 : launchTime.hashCode());
			return result;
		}

		/**
		 * Compares this launch context to another object and returns true only
		 * if both objects are logically the same.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LaunchContext other = (LaunchContext) obj;
			if (activeTriggers == null) {
				if (other.activeTriggers != null)
					return false;
			} else if (!activeTriggers.equals(other.activeTriggers))
				return false;
			if (launchTime == null) {
				if (other.launchTime != null)
					return false;
			} else if (!launchTime.equals(other.launchTime))
				return false;
			return true;
		}
	}
	private final LaunchContext launchContext;
	
	/**
	 * Creates a new survey response information object based on the 
	 * parameters. All parameters are required unless otherwise specified.
	 * 
	 * @param survey The Survey to which this survey response belongs.
	 * 
	 * @param username The username of the user that created this survey 
	 * 				   response.
	 * 
	 * @param campaignId The unique identifier for the campaign to which this
	 * 					 survey belongs.
	 * 
	 * @param client The client value that was given when this survey response
	 * 				 was uploaded.
	 * 
	 * @param date The date at which this survey response was generated.
	 * 
	 * @param time The date as milliseconds since the epoch when this survey
	 * 			   response was generated.
	 * 
	 * @param timezone The timezone of the device that generated this survey
	 * 				   response at the time is was generated.
	 * 
	 * @param launchContext Context information provided by the device about  
	 * 						its state while this survey was being taken.
	 *  
	 * @param locationStatus The status of the location information.
	 * 
	 * @param location The location information. This may be null if it  
	 * 				   correlates with the location status.
	 * 
	 * @throws ErrorCodeException Thrown if any of the information provided is
	 * 							  missing or invalid.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the information
	 * 									provided is missing or invalid.
	 */
	public SurveyResponse(final Survey survey, final long surveyResponseId,
			final String username, final String campaignId, final String client,
			final Date date, final long time, final TimeZone timezone, 
			final JSONObject launchContext, 
			final String locationStatus, final JSONObject location,
			final PrivacyState privacyState) 
			throws ErrorCodeException {

		if(survey == null) {
			throw new IllegalArgumentException("The survey cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new IllegalArgumentException("The username cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new IllegalArgumentException("The campaign ID cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			throw new IllegalArgumentException("The client cannot be null or whitespace only.");
		}
		else if(date == null) {
			throw new IllegalArgumentException("The date cannot be null.");
		}
		else if(timezone == null) {
			throw new IllegalArgumentException("The timezone cannot be null.");
		}
		else if(launchContext == null) {
			throw new IllegalArgumentException("The launch context cannot be null.");
		}
		else if(locationStatus == null) {
			throw new IllegalArgumentException("The location status cannot be null.");
		}
		else if(privacyState == null) {
			throw new IllegalArgumentException("The privacy state cannot be null.");
		}
		
		this.username = username;
		this.campaignId = campaignId;
		this.client = client;
		
		this.date = date;
		this.time = time;
		this.timezone = timezone;
		
		this.surveyResponseId = surveyResponseId;
		this.survey = survey;
		this.privacyState = privacyState;
		
		this.launchContext = new LaunchContext(launchContext);
		
		try {
			this.locationStatus = LocationStatus.valueOf(locationStatus.toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown location status.", e);
		}
		if(location != null) {
			this.location = new Location(location);
		}
		else {
			this.location = null;
		}
		
		promptResponses = new HashMap<Integer, Response>();
	}
	
	/**
	 * Creates a new SurveyResponse object.
	 * 
	 * @param survey The Survey to which this survey response belongs.
	 * 
	 * @param username The user's that is generating this survey response's
	 * 				   username.
	 * 
	 * @param campaignId The unique identifier for the campaign to which this
	 * 					 survey belongs.
	 * 
	 * @param client The client value.
	 * 
	 * @param date The date and time for which this survey response was 
	 * 			   created.
	 * 
	 * @param time The milliseconds since the epoch denoting when this survey
	 * 			   response was created.
	 * 
	 * @param timezone The timezone of the device when this survey response was
	 * 				   created.
	 * 
	 * @param launchContext Context information about the device when this
	 * 						survey response was created.
	 * 
	 * @param locationStatus The status of the location.
	 * 
	 * @param location The location of the device when this survey response was
	 * 				   created. This may be null if 'locationStatus' concurs.
	 * 
	 * @param privacyState The privacy state of this survey response.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required 
	 * 									parameters are null.
	 */
	public SurveyResponse(final Survey survey, final long surveyResponseId,
			final String username, final String campaignId, final String client,
			final Date date, final long time, final TimeZone timezone, 
			final LaunchContext launchContext, 
			final LocationStatus locationStatus, final Location location,
			final PrivacyState privacyState, 
			final Map<Integer, Response> responses) 
			throws ErrorCodeException {

		if(survey == null) {
			throw new IllegalArgumentException("The survey cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new IllegalArgumentException("The username cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new IllegalArgumentException("The campaign ID cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			throw new IllegalArgumentException("The client cannot be null or whitespace only.");
		}
		else if(date == null) {
			throw new IllegalArgumentException("The date cannot be null.");
		}
		else if(timezone == null) {
			throw new IllegalArgumentException("The timezone cannot be null.");
		}
		else if(launchContext == null) {
			throw new IllegalArgumentException("The launch context cannot be null.");
		}
		else if(locationStatus == null) {
			throw new IllegalArgumentException("The location status cannot be null.");
		}
		else if(privacyState == null) {
			throw new IllegalArgumentException("The privacy state cannot be null.");
		}
		
		this.username = username;
		this.campaignId = campaignId;
		this.client = client;
		
		this.date = date;
		this.time = time;
		this.timezone = timezone;
		
		this.surveyResponseId = surveyResponseId;
		this.survey = survey;
		this.privacyState = privacyState;
		
		this.launchContext = launchContext;
		
		this.locationStatus = locationStatus;
		if((! LocationStatus.UNAVAILABLE.equals(locationStatus)) && 
				(location == null)) {
			throw new IllegalArgumentException("The location cannot be null unless the location status is unavailable.");
		}
		this.location = location;
		
		promptResponses = new HashMap<Integer, Response>(responses);
	}
	
	/**
	 * Creates a SurveyResponse object based on a JSONObject.
	 * 
	 * @param username The username of the user that created this survey 
	 * 				   response.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaign The campaign.
	 * 
	 * @param response The survey response as a JSONObject.
	 * 
	 * @throws ErrorCodeException Thrown if the JSONObject could not be decoded
	 * 							  as a survey response.
	 */
	public SurveyResponse(final long surveyResponseId,
			final String username, final String campaignId,
			final String client, final Campaign campaign,
			final JSONObject response) throws ErrorCodeException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new ErrorCodeException(ErrorCodes.USER_INVALID_USERNAME, "The username is invalid.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new ErrorCodeException(ErrorCodes.CAMPAIGN_INVALID_ID, "The campaign ID is invalid.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_CLIENT, "The client value is invalid.");
		}
		
		this.surveyResponseId = surveyResponseId;
		this.username = username;
		this.campaignId = campaignId;
		this.client = client;
		
		try {
			date = StringUtils.decodeDateTime(response.getString(JSON_KEY_DATE));
			
			if(date == null) {
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_DATE, "The date was not a valid date.");
			}
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_DATE, "The date is missing.", e);
		}
		
		try {
			time = response.getLong(JSON_KEY_TIME);
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIME, "The time is missing.", e);
		}
		
		try {
			timezone = TimeZone.getTimeZone(response.getString(JSON_KEY_TIMEZONE));
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_TIMEZONE, "The timezone is missing.", e);
		}
		
		String surveyId;
		try {
			surveyId = response.getString(JSON_KEY_SURVEY_ID);
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_SURVEY_ID, "The survey ID is missing.", e);
		}
		
		survey = campaign.getSurveys().get(surveyId);
		if(survey == null) {
			throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_SURVEY_ID, "The survey ID doesn't refer to any known surveys in the campaign.");
		}
		
		try {
			launchContext = new LaunchContext(response.getJSONObject(JSON_KEY_SURVEY_LAUNCH_CONTEXT));
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, "The launch context is missing.", e);
		}
		
		try {
			locationStatus = LocationStatus.valueOf(response.getString(JSON_KEY_LOCATION_STATUS));
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION_STATUS, "The location status is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION_STATUS, "The location status is unknown.", e);
		}
		
		Location tLocation = null;
		try {
			tLocation = new Location(response.getJSONObject(JSON_KEY_LOCATION));
		}
		catch(JSONException e) {
			if(!LocationStatus.UNAVAILABLE.equals(locationStatus)) {
				throw new ErrorCodeException(ErrorCodes.SERVER_INVALID_LOCATION, "The location is missing.", e);
			}
		}
		location = tLocation;
		
		this.privacyState = PrivacyState.PRIVATE;
		
		JSONArray responses;
		try {
			responses = response.getJSONArray(JSON_KEY_RESPONSES);
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_RESPONSES, "There weren't any responses for the survey.", e);
		}
		this.promptResponses = processResponses(campaign.getSurveys().get(surveyId).getSurveyItems(), responses, 0);
	}
	
	/**
	 * Returns the survey response's unique identifier.
	 * 
	 * @return The survey response's unique identifier.
	 */
	public final long getSurveyResponseId() {
		return surveyResponseId;
	}

	/**
	 * Returns the username.
	 * 
	 * @return The username.
	 */
	public final String getUsername() {
		return username;
	}

	/**
	 * Returns the campaign ID for the campaign to which this survey belongs.
	 *  
	 * @return The campaign ID.
	 */
	public final String getCampaignId() {
		return campaignId;
	}

	/**
	 * Returns the client value that was given when this survey response was
	 * uploaded.
	 * 
	 * @return The client value.
	 */
	public final String getClient() {
		return client;
	}

	/**
	 * Returns a copy of the date object representing when this survey response
	 * was generated. This should be correlated with {@link #getTimezone()}.
	 * 
	 * @return A copy of the date object.
	 * 
	 * @see #getTimezone()
	 */
	public final Date getDate() {
		return new Date(date.getTime());
	}

	/**
	 * Returns the time this survey response was generated as a long value from
	 * the epoch.
	 * 
	 * @return The time in milliseconds since the epoch.
	 */
	public final long getTime() {
		return time;
	}

	/**
	 * Returns a copy of the timezone object representing the timezone of the
	 * device that generated this survey response at the time it was generated.
	 * 
	 * @return The phone's timezone.
	 */
	public final TimeZone getTimezone() {
		return new SimpleTimeZone(timezone.getRawOffset(), timezone.getID());
	}

	/**
	 * Returns the campaign-wide unique identifier for this survey response.
	 * 
	 * @return The unique identifier for the survey for which this is a 
	 * 		   response.
	 */
	public final Survey getSurvey() {
		return survey;
	}

	/**
	 * Returns context information from the phone when this survey was 
	 * launched.
	 * 
	 * @return Context information from the phone.
	 */
	public final LaunchContext getLaunchContext() {
		return launchContext;
	}

	/**
	 * Returns the status of the location information collected when this 
	 * survey response was generated.
	 * 
	 * @return The location's status.
	 */
	public final LocationStatus getLocationStatus() {
		return locationStatus;
	}

	/**
	 * Returns the location information about the phone when the survey 
	 * response was generated if applicable.
	 * 
	 * @return The location information if available or null if not.
	 */
	public final Location getLocation() {
		return location;
	}
	
	/**
	 * Returns the privacy state of this survey response.
	 * 
	 * @return The privacy state.
	 */
	public final PrivacyState getPrivacyState() {
		return privacyState;
	}
	
	/**
	 * Returns an unmodifiable list of the responses.
	 * 
	 * @return An unmodifiable list of the responses.
	 */
	public final Map<Integer, Response> getPromptResponses() {
		return Collections.unmodifiableMap(promptResponses);
	}
	
	/**
	 * Adds a prompt response to this survey response.
	 * 
	 * @param promptResponse The prompt response.
	 * 
	 * @throws IllegalArgumentException Thrown if the prompt response is null.
	 */
	public final void addPromptResponse(final PromptResponse promptResponse) {
		if(promptResponse == null) {
			throw new IllegalArgumentException("The prompt response cannot be null.");
		}
		
		promptResponses.put(promptResponses.size() + 1, promptResponse);
	}
	
	/**
	 * Creates a JSONObject that represents this survey response object based
	 * on the given flags.
	 * 
	 * @param withUsername Whether or not to include the username.
	 *  
	 * @param withCampaignId Whether or not to include the campaign's ID.
	 *  
	 * @param withClient Whether or not to include the client value.
	 *  
	 * @param withPrivacyState Whether or not to include the privacy state.
	 *  
	 * @param withDate Whether or not to include the date.
	 *  
	 * @param withTime Whether or not to include the time.
	 *  
	 * @param withTimezone Whether or not to include the timezone.
	 *  
	 * @param withLocationStatus Whether or not to include the location status.
	 *  
	 * @param withLocation Whether or not to include the location information.
	 * 					   This may not be included anyway if there was no
	 * 					   location information for this survey response.
	 *  
	 * @param withSurveyId Whether or not to include the survey's ID.
	 *  
	 * @param withSurveyTitle Whether or not to include the survey's title.
	 *  
	 * @param withSurveyDescription Whether or not to include the survey's
	 * 								description.
	 *  
	 * @param withSurveyLaunchContext Whether or not to include the survey's
	 * 								  launch context.
	 *  
	 * @param surveyLaunchContextLong If we are including the launch context,
	 * 								  this indicates whether we are adding 
	 * 								  everything in the launch context or only
	 * 								  the launch date and time.
	 *  
	 * @param withResponses Whether or not to include the prompt responses.
	 * 
	 * @return A JSONObject that represents this object or null if there was an
	 * 		   error.
	 */
	public final JSONObject toJson(final boolean withUsername, 
			final boolean withCampaignId, final boolean withClient, 
			final boolean withPrivacyState, 
			final boolean withDate, final boolean withTime, 
			final boolean withTimezone, 
			final boolean withLocationStatus, final boolean withLocation, 
			final boolean withSurveyId, 
			final boolean withSurveyTitle, final boolean withSurveyDescription,
			final boolean withSurveyLaunchContext, 
			final boolean surveyLaunchContextLong, 
			final boolean withResponses, final boolean withId) {
		
		try {
			JSONObject result = new JSONObject();
			
			if(withUsername) {
				result.put(JSON_KEY_USERNAME, username);
			}
			if(withCampaignId) {
				result.put(JSON_KEY_CAMPAIGN_ID, campaignId);
			}
			if(withClient) {
				result.put(JSON_KEY_CLIENT, client);
			}
			
			if(withPrivacyState) {
				result.put(JSON_KEY_PRIVACY_STATE, privacyState.toString());
			}
			
			if(withDate) {
				result.put(JSON_KEY_DATE, TimeUtils.getIso8601DateTimeString(date));
			}
			if(withTime) {
				result.put(JSON_KEY_TIME, time);
			}
			if(withTimezone) {
				result.put(JSON_KEY_TIMEZONE, timezone.getID());
			}
			
			if(withLocationStatus) {
				result.put(JSON_KEY_LOCATION_STATUS, locationStatus.toString());
			}
			if(withLocation && (location != null)) {
				result.put(JSON_KEY_LOCATION, location.toJson(false));
			}
			
			if(withSurveyId && (survey != null)) {
				result.put(JSON_KEY_SURVEY_ID, survey.getId());
			}
			if(withSurveyTitle && (survey != null)) {
				result.put(JSON_KEY_SURVEY_NAME, survey.getTitle());
			}
			if(withSurveyDescription && (survey != null)) {
				result.put(JSON_KEY_SURVEY_DESCRIPTION, survey.getDescription());
			}
			
			if(withSurveyLaunchContext) {
				result.put(JSON_KEY_SURVEY_LAUNCH_CONTEXT, launchContext.toJson(surveyLaunchContextLong));
			}
			
			if(withResponses) {
				List<Integer> indices = new ArrayList<Integer>(promptResponses.keySet());
				Collections.sort(indices);

				JSONArray responses = new JSONArray();
				for(Integer index : indices) {
					responses.put(promptResponses.get(index).toJson());
				}
				result.put(JSON_KEY_RESPONSES, responses);
			}
			
			if(withId) {
				result.put(JSON_KEY_SURVEY_RESPONSE_ID, surveyResponseId);
			}
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}
	
	/**
	 * Creates a hash code for this survey response.
	 * 
	 * @return A hash code for this survey response.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((campaignId == null) ? 0 : campaignId.hashCode());
		result = prime * result + ((client == null) ? 0 : client.hashCode());
		result = prime * result
				+ ((promptResponses == null) ? 0 : promptResponses.hashCode());
		result = prime * result + ((survey == null) ? 0 : survey.hashCode());
		result = prime * result
				+ (int) (surveyResponseId ^ (surveyResponseId >>> 32));
		result = prime * result + (int) (time ^ (time >>> 32));
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	/**
	 * Determines if this survey response is equal to another object.
	 * 
	 * @return True if this survey response is equal to the other object; false
	 * 		   otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyResponse other = (SurveyResponse) obj;
		if (campaignId == null) {
			if (other.campaignId != null)
				return false;
		} else if (!campaignId.equals(other.campaignId))
			return false;
		if (client == null) {
			if (other.client != null)
				return false;
		} else if (!client.equals(other.client))
			return false;
		if (promptResponses == null) {
			if (other.promptResponses != null)
				return false;
		} else if (!promptResponses.equals(other.promptResponses))
			return false;
		if (survey == null) {
			if (other.survey != null)
				return false;
		} else if (!survey.equals(other.survey))
			return false;
		if (surveyResponseId != other.surveyResponseId)
			return false;
		if (time != other.time)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	/**
	 * Processes an array of response objects from JSON.
	 * 
	 * @param campaign The campaign definition.
	 * 
	 * @param currArray The responses as a JSONArray.
	 * 
	 * @param repeatableSetIteration The iteration of the repeatable set for
	 * 								 which these prompts belong.
	 * 
	 * @return A map of the respone's index to the response.
	 * 
	 * @throws ErrorCodeException Thrown if the list or any of its elements are
	 * 							  malformed.
	 */
	
	/**
	 * Processes an JSONArray of survey responses based on their survey item
	 * counterparts. 
	 * 
	 * @param surveyItems The survey items to which the responses should 
	 * 					  pertain.
	 * 
	 * @param currArray A JSONArray of JSONObjects where each object is a 
	 * 					response.
	 * 
	 * @param repeatableSetIteration If this is processing the responses of a
	 * 								 repeatable set, then this is the iteration
	 * 								 of that repeatable set. Otherwise, it may
	 * 								 be null.
	 * 
	 * @return A map of a response's index to its Response object.
	 * 
	 * @throws ErrorCodeException Thrown if any of the responses are invalid
	 * 							  either syntactically or as compared to the
	 * 							  survey objects.
	 */
	private Map<Integer, Response> processResponses(
			final Map<Integer, SurveyItem> surveyItems, 
			final JSONArray currArray, final Integer repeatableSetIteration) 
			throws ErrorCodeException {
		
		int numResponses = currArray.length();
		Map<Integer, Response> results = new HashMap<Integer, Response>(numResponses);
		
		for(int i = 0; i < numResponses; i++) {
			try {
				JSONObject currResponse = currArray.getJSONObject(i);
				
				try {
					String promptId = currResponse.getString(JSON_KEY_PROMPT_ID);
					Prompt prompt = (Prompt) surveyItems.get(promptId);
					
					results.put(
							prompt.getIndex(),
							processPromptResponse(
									prompt, 
									currResponse, 
									repeatableSetIteration));
				}
				catch(JSONException notPrompt) {
					try {
						String repeatableSetId = currResponse.getString(JSON_KEY_REPEATABLE_SET_ID);
						RepeatableSet repeatableSet = (RepeatableSet) surveyItems.get(repeatableSetId);
						
						results.put(
								repeatableSet.getIndex(),
								processRepeatableSet(
										repeatableSet, 
										currResponse));
					}
					catch(JSONException notRepeatableSet) {
						throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_RESPONSES, "The response wasn't a prompt response or repeatable set.");
					}
				}
				catch(ClassCastException e) {
					throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_RESPONSES, "The response and XML disagree on the type of a survey item.", e);
				}
			}
			catch(JSONException e) {
				throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_RESPONSES, "A response was not valid JSON.");
			}
		}
		
		for(SurveyItem surveyItem : surveyItems.values()) {
			String surveyItemId = surveyItem.getId();
			boolean found = false;
		
			for(Response response : results.values()) {
				if(response.getId().equals(surveyItemId)) {
					found = true;
					break;
				}
			}
			
			if(! found) {
				throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_RESPONSES, "The response is missing a response for the prompt: " + surveyItemId);
			}
		}
		
		return results;
	}
	
	/**
	 * Creates a PromptResponse object from a Prompt and the JSONObject that
	 * represents the response to the prompt.
	 * 
	 * @param prompt The Prompt from which the response was generated.
	 *  
	 * @param response The response from the user as a JSONObject.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set.
	 * 
	 * @return A PromptResponse generated by the 'prompt' based on the value in 
	 * 		   the 'response'.
	 * 
	 * @throws ErrorCodeException Thrown if the JSONObject is invalid for
	 * 							  getting a response value.
	 */
	private PromptResponse processPromptResponse(final Prompt prompt,
			final JSONObject response, final Integer repeatableSetIteration) 
			throws ErrorCodeException {
		
		Object responseObject;
		try {
			responseObject = response.get(JSON_KEY_PROMPT_VALUE);
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_RESPONSES, "The response value was missing.", e);
		}
		
		if(responseObject instanceof String) {
			try {
				return prompt.createResponse(
						NoResponse.valueOf((String) responseObject), 
						(repeatableSetIteration == 0) ? null : repeatableSetIteration);
			}
			catch(IllegalArgumentException e) {
				// The response has a value so, it will be interpreted as such.
			}
		}
		
		return prompt.createResponse(
				responseObject, 
				(repeatableSetIteration == 0) ? null : repeatableSetIteration);
	}
	
	/**
	 * Creates a RepeatableSetResponse object from a RepeatableSet and the
	 * responses in a JSONObject.
	 * 
	 * @param repeatableSet The RepeatableSet that generated the responses.
	 * 
	 * @param response The responses as a JSONObject.
	 * 
	 * @return A RepeatableSetResponse object that represents the responses
	 * 		   to this repeatable set gathered from the JSONObject.
	 *   
	 * @throws ErrorCodeException Thrown if the repeatable set JSONObject is
	 * 							  malformed.
	 */
	private RepeatableSetResponse processRepeatableSet(
			final RepeatableSet repeatableSet, final JSONObject response) 
			throws ErrorCodeException {
		
		try {
			if(response.getBoolean(RepeatableSetResponse.NOT_DISPLAYED)) {
				return new RepeatableSetResponse(repeatableSet, NoResponse.NOT_DISPLAYED);
			}
		}
		catch(JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_RESPONSES, "The not displayed value is missing.", e);
		}
		RepeatableSetResponse result = new RepeatableSetResponse(repeatableSet, null);
		
		JSONArray responses;
		try {
			responses = response.getJSONArray(JSON_KEY_RESPONSES);
		} catch (JSONException e) {
			throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_RESPONSES, "The responses array for the repeatable set are missing.", e);
		}
		
		int numIterations = responses.length();
		for(int i = 0; i < numIterations; i++) {
			try {
				result.addResponseGroup(
						i + 1, 
						processResponses(
								repeatableSet.getPrompts(), 
								responses.getJSONArray(i),
								i
							)
					);
			} catch (JSONException e) {
				throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_RESPONSES, "One of the response array objects for a repeatable set is not a JSONArray.", e);
			}
		}
		return result;
	}
}