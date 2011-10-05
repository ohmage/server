package org.ohmage.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.domain.Location.LocationException;
import org.ohmage.domain.prompt.response.PromptResponse;
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
public class SurveyResponseInformation {
	private static final String JSON_KEY_USERNAME = "user";
	private static final String JSON_KEY_CAMPAIGN_ID = "campaign_id";
	private static final String JSON_KEY_CLIENT = "client";
	private static final String JSON_KEY_DATE = "date";
	private static final String JSON_KEY_TIME = "time";
	private static final String JSON_KEY_TIMEZONE = "timezone";
	private static final String JSON_KEY_LOCATION_STATUS = "location_status";
	private static final String JSON_KEY_LOCATION = "location";
	private static final String JSON_KEY_SURVEY_ID = "survey_id";
	private static final String JSON_KEY_SURVEY_LAUNCH_CONTEXT = "survey_launch_context";
	private static final String JSON_KEY_RESPONSES = "responses";
	private static final String JSON_KEY_PRIVACY_STATE = "privacy_state";
	
	private final String username;
	private final String campaignId;
	private final String client;
	
	private final Date date;
	private final long time;
	private final TimeZone timezone;
	
	public static enum LocationStatus { VALID, NETWORK, INACCURATE, STALE, UNAVAILABLE };
	private final LocationStatus locationStatus;
	private final Location location;
	
	private final String surveyId;
	private final List<PromptResponse> promptResponses;
	private final SurveyResponsePrivacyStateCache.PrivacyState privacyState;
	
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
		 * @throws SurveyResponseException Thrown if the launchContext is null,
		 * 								   if any of the keys are missing from
		 * 								   the launchContext, or if any of the
		 * 								   values for those keys are invalid.
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
		 * @return A JSONObject that represents this object or null if there 
		 * 		   was an error.
		 */
		public final JSONObject toJson() {
			try {
				JSONObject result = new JSONObject();
				
				result.put(JSON_KEY_LAUNCH_TIME, TimeUtils.getIso8601DateTimeString(launchTime));
				result.put(JSON_KEY_ACTIVE_TRIGGERS, activeTriggers);
				
				return result;
			}
			catch(JSONException e) {
				return null;
			}
		}
	}
	private final LaunchContext launchContext;
	
	/**
	 * Creates a new survey response information object based on the 
	 * parameters. All parameters are required unless otherwise specified.
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
	 * @param surveyId The campaign-wide unique identifier for this survey.
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
	public SurveyResponseInformation(final String username, 
			final String campaignId, final String client,
			final Date date, final long time, final TimeZone timezone, 
			final String surveyId, final JSONObject launchContext, 
			final String locationStatus, final JSONObject location,
			final SurveyResponsePrivacyStateCache.PrivacyState privacyState) 
			throws ErrorCodeException {
		
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
		else if(surveyId == null) {
			throw new IllegalArgumentException("The survey ID cannot be null.");
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
		
		this.surveyId = surveyId;
		this.privacyState = privacyState;
		
		this.launchContext = new LaunchContext(launchContext);
		
		try {
			this.locationStatus = LocationStatus.valueOf(locationStatus.toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown location status.", e);
		}
		if(location != null) {
			try {
				this.location = new Location(location);
			}
			catch(LocationException e) {
				throw new ErrorCodeException(e.getErrorCode(), e.getErrorText(), e);
			}
		}
		else {
			this.location = null;
		}
		
		promptResponses = new LinkedList<PromptResponse>();
	}
	
	/**
	 * Creates a new SurveyResponseInformation object.
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
	 * @param surveyId The campaign-unique identifier for the survey for which
	 * 				   this survey response belongs.
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
	public SurveyResponseInformation(final String username, 
			final String campaignId, final String client,
			final Date date, final long time, final TimeZone timezone, 
			final String surveyId, final LaunchContext launchContext, 
			final LocationStatus locationStatus, final Location location,
			final SurveyResponsePrivacyStateCache.PrivacyState privacyState) 
			throws ErrorCodeException {
		
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
		else if(surveyId == null) {
			throw new IllegalArgumentException("The survey ID cannot be null.");
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
		
		this.surveyId = surveyId;
		this.privacyState = privacyState;
		
		this.launchContext = launchContext;
		
		this.locationStatus = locationStatus;
		if(! LocationStatus.UNAVAILABLE.equals(locationStatus) && 
				(location == null)) {
			throw new IllegalArgumentException("The location cannot be null unless the location status is unavailable.");
		}
		this.location = location;
		
		promptResponses = new LinkedList<PromptResponse>();
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
	public final String getSurveyId() {
		return surveyId;
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
	public final SurveyResponsePrivacyStateCache.PrivacyState getPrivacyState() {
		return privacyState;
	}
	
	/**
	 * Returns an unmodifiable list of the prompt responses.
	 * 
	 * @return An unmodifiable list of the prompt responses.
	 */
	public final List<PromptResponse> getPromptResponses() {
		return Collections.unmodifiableList(promptResponses);
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
			throw new IllegalArgumentException("The prompt response cannot be null., e");
		}
		
		promptResponses.add(promptResponse);
	}
	
	/**
	 * Creates a JSONObject that represents this survey response object.
	 * 
	 * @param withExtras If set, it will include all values that would not 
	 * 					 normally be added to the JSON including the username,
	 * 					 campaign ID, client, and privacy state.
	 * 
	 * @return A JSONObject that represents this object or null if there was an
	 * 		   error.
	 */
	public final JSONObject toJson(final boolean withExtras) {
		try {
			JSONObject result = new JSONObject();
			
			if(withExtras) {
				result.put(JSON_KEY_USERNAME, username);
				result.put(JSON_KEY_CAMPAIGN_ID, campaignId);
				result.put(JSON_KEY_CLIENT, client);
				
				result.put(JSON_KEY_PRIVACY_STATE, privacyState.toString().toLowerCase());
			}
			
			result.put(JSON_KEY_DATE, TimeUtils.getIso8601DateTimeString(date));
			result.put(JSON_KEY_TIME, time);
			result.put(JSON_KEY_TIMEZONE, timezone.getID());
			result.put(JSON_KEY_LOCATION_STATUS, locationStatus.toString().toLowerCase());
			if(location != null) {
				result.put(JSON_KEY_LOCATION, location.toJson(false));
			}
			
			result.put(JSON_KEY_SURVEY_ID, surveyId);
			result.put(JSON_KEY_SURVEY_LAUNCH_CONTEXT, launchContext.toJson());
			
			JSONArray responses = new JSONArray();
			for(PromptResponse promptResponse : promptResponses) {
				responses.put(promptResponse.toJson(false));
			}
			result.put(JSON_KEY_RESPONSES, responses);
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}
}