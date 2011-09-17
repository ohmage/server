package org.ohmage.domain;

import java.util.ArrayList;
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
	private final String username;
	private final String campaignId;
	private final String client;
	
	private final Date date;
	private final long time;
	private final TimeZone timezone;
	
	private final String surveyId;
	private final SurveyResponsePrivacyStateCache.PrivacyState privacyState;
	
	/**
	 * Context information gathered by the phone when this survey was launched.
	 * 
	 * @author John Jenkins
	 */
	public final class LaunchContext {
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
		private LaunchContext(final JSONObject launchContext) throws SurveyResponseException {
			if(launchContext == null) {
				throw new SurveyResponseException(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, "The launch context cannot be null.");
			}
			
			try {
				launchTime = StringUtils.decodeDateTime(launchContext.getString(JSON_KEY_LAUNCH_TIME));
				
				if(launchTime == null) {
					throw new SurveyResponseException(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, "The launch time is missing in the launch context.");
				}
			}
			catch(JSONException e) {
				throw new SurveyResponseException(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, "The launch time is missing in the launch context.");
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
				throw new SurveyResponseException(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, "The active triggers list is missing in the launch context.");
			}
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
	
	public static enum LocationStatus { VALID, NETWORK, INACCURATE, STALE, UNAVAILABLE };
	private final LocationStatus locationStatus;
	
	/**
	 * This class contains all of the information associated with a location
	 * record.
	 * 
	 * @author John Jenkins
	 */
	public final class Location {
		private static final String JSON_KEY_LATITUDE = "latitude";
		private static final String JSON_KEY_LONGITUDE = "longitude";
		private static final String JSON_KEY_ACCURACY = "accuracy";
		private static final String JSON_KEY_PROVIDER = "provider";
		private static final String JSON_KEY_TIMESTAMP = "timestamp";
		
		private final Double latitude;
		private final Double longitude;
		private final Double accuracy;
		private final String provider;
		private final Date timestamp;
		
		/**
		 * Creates a new Location object.
		 * 
		 * @param locationData A JSONObject representing all of the data for a
		 * 					   Location object.
		 * 
		 * @throws SurveyResponseException Thrown if the location data is null,
		 * 								   isn't a valid JSONObject, doesn't 
		 * 								   contain all of the required 
		 * 								   information, or any of the 
		 * 								   information is invalid for its type.
		 */
		private Location(JSONObject locationData) throws SurveyResponseException {
			try {
				latitude = locationData.getDouble(JSON_KEY_LATITUDE);
			}
			catch(JSONException e) {
				throw new SurveyResponseException(ErrorCodes.SERVER_INVALID_LOCATION, "The latitude is missing or invalid.", e);
			}
			
			try {
				longitude = locationData.getDouble(JSON_KEY_LONGITUDE);
			}
			catch(JSONException e) {
				throw new SurveyResponseException(ErrorCodes.SERVER_INVALID_LOCATION, "The longitude is missing or invalid.", e);
			}

			try {
				accuracy = locationData.getDouble(JSON_KEY_ACCURACY);
			}
			catch(JSONException e) {
				throw new SurveyResponseException(ErrorCodes.SERVER_INVALID_LOCATION, "The accuracy is missing or invalid.", e);
			}
			
			try {
				provider = locationData.getString(JSON_KEY_PROVIDER);
			}
			catch(JSONException e) {
				throw new SurveyResponseException(ErrorCodes.SERVER_INVALID_LOCATION, "The provider is missing.", e);
			}
			
			try {
				timestamp = StringUtils.decodeDateTime(locationData.getString(JSON_KEY_TIMESTAMP));
				
				if(timestamp == null) {
					throw new SurveyResponseException(ErrorCodes.SERVER_INVALID_TIMESTAMP, "The timestamp is invalid.");
				}
			}
			catch(JSONException e) {
				throw new SurveyResponseException(ErrorCodes.SERVER_INVALID_TIMESTAMP, "The timestamp is missing.", e);
			}
		}

		/**
		 * Returns the latitude of this location.
		 * 
		 * @return The latitude of this location.
		 */
		public final double getLatitude() {
			return latitude;
		}

		/**
		 * Returns the longitude of this location.
		 * 
		 * @return The longitude of this location.
		 */
		public final double getLongitude() {
			return longitude;
		}

		/**
		 * Returns the accuracy of this location.
		 * 
		 * @return The accuracy of this location.
		 */
		public final double getAccuracy() {
			return accuracy;
		}

		/**
		 * Returns the provider of this location information.
		 * 
		 * @return The provider of this location information.
		 */
		public final String getProvider() {
			return provider;
		}

		/**
		 * Returns the timestamp for when this information was gathered.
		 * 
		 * @return The timestamp for when this information was gathered.
		 */
		public final Date getTimestamp() {
			return timestamp;
		}
		
		/**
		 * Creates a JSONObject that represents the information in this object.
		 * 
		 * @return Returns a JSONObject that represents this object or null if
		 * 		   there is an error building the JSONObject.
		 */
		public final JSONObject toJson() {
			try {
				JSONObject result = new JSONObject();
				
				result.put(JSON_KEY_LATITUDE, latitude);
				result.put(JSON_KEY_LONGITUDE, longitude);
				result.put(JSON_KEY_ACCURACY, accuracy);
				result.put(JSON_KEY_PROVIDER, provider);
				result.put(JSON_KEY_TIMESTAMP, TimeUtils.getIso8601DateTimeString(timestamp));
				
				return result;
			}
			catch(JSONException e) {
				return null;
			}
		}
	}
	private final Location location;
	
	/**
	 * This class represents a single prompt response.
	 * 
	 * @author John Jenkins
	 */
	public final class PromptResponse {
		private static final String JSON_KEY_PROMPT_ID = "prompt_id";
		private static final String JSON_KEY_PROMPT_TYPE = "prompt_type";
		private static final String JSON_KEY_REPEATABLE_SET_ID = "repeatable_set_id";
		private static final String JSON_KEY_REPEATABLE_SET_ITERATION = "repeatable_set_iteration";
		private static final String JSON_KEY_RESPONSE = "value";
		
		private final String promptId;
		private final String promptType;
		
		private final String repeatableSetId;
		private final Integer repeatableSetIteration;
		
		private final Object response;
		
		/**
		 * Creates a new prompt response.
		 * 
		 * @param promptId The prompt's identifier, unique to the 
		 * 				   configuration, but not to this prompt response.
		 * 
		 * @param promptType The prompt's type.
		 * 
		 * @param repeatableSetId The repeatable set ID if this was part of a
		 * 						  repeatable set or NULL if not.
		 * 
		 * @param repeatableSetIteration The iteration within the repeatable 
		 * 								 set for this survey response if it was
		 * 								 part of a repeatable set or NULL if 
		 * 								 not.
		 * 
		 * @param response The response value.
		 * 
		 * @throws SurveyResponseException Thrown if the prompt ID is null or 
		 * 								   whitespace only or the response is 
		 * 								   null.
		 * 
		 * @throws IllegalArgumentExcpetion Thrown if the prompt type is null 
		 * 									or whitespace only.
		 */
		public PromptResponse(final String promptId, final String promptType, 
				final String repeatableSetId, final Integer repeatableSetIteration, 
				final Object response) throws SurveyResponseException {
			if(StringUtils.isEmptyOrWhitespaceOnly(promptId)) {
				throw new SurveyResponseException(ErrorCodes.SURVEY_INVALID_PROMPT_ID, "The prompt ID cannot be null.");
			}
			else if(StringUtils.isEmptyOrWhitespaceOnly(promptType)) {
				throw new IllegalArgumentException("The prompt type cannot be null.");
			}
			else if(response == null) {
				throw new SurveyResponseException(ErrorCodes.SURVEY_INVALID_RESPONSES, "The response value cannot be null.");
			}
			
			this.promptId = promptId;
			this.promptType = promptType;
			
			this.repeatableSetId = repeatableSetId;
			this.repeatableSetIteration = repeatableSetIteration;
			
			this.response = response;
		}
		
		/**
		 * Returns the prompt's ID.
		 * 
		 * @return The prompt's ID.
		 */
		public String getPromptId() {
			return promptId;
		}
		
		/**
		 * Returns the prompt's type.
		 * 
		 * @return The prompt's type.
		 */
		public String getPromptType() {
			return promptType;
		}
		
		/**
		 * Returns the repeatable set ID if available.
		 * 
		 * @return The repeatable set ID or null if this wasn't part of a
		 * 		   repeatable set.
		 */
		public String getRepeatableSetId() {
			return repeatableSetId;
		}
		
		/**
		 * Returns the repeatable set iteration if available.
		 * 
		 * @return The repeatable set iteration or null if this wasn't part of
		 * 		   a repeatable set.
		 */
		public Integer getRepeatableSetIteration() {
			return repeatableSetIteration;
		}
		
		/**
		 * Returns the prompt response value.
		 * 
		 * @return The prompt response value.
		 */
		public Object getResponse() {
			return response;
		}
		
		/**
		 * Creates a JSONObject that represents this object.
		 * 
		 * @param longVersion The representation saved in the database only
		 * 					  includes the prompt ID and the response value. If
		 * 					  this flag is set to true it includes the prompt 
		 * 					  type, repeatable set ID if available, and 
		 * 					  repeatable set iteration if available.
		 * 
		 * @return A JSONObject that represents this object.
		 */
		public JSONObject toJson(final boolean longVersion) {
			try {
				JSONObject result = new JSONObject();
				
				result.put(JSON_KEY_PROMPT_ID, promptId);
				result.put(JSON_KEY_RESPONSE, response);
				
				if(longVersion) {
					result.put(JSON_KEY_PROMPT_TYPE, promptType);
					result.put(JSON_KEY_REPEATABLE_SET_ID, repeatableSetId);
					result.put(JSON_KEY_REPEATABLE_SET_ITERATION, repeatableSetIteration);
				}
				
				return result;
			}
			catch(JSONException e) {
				return null;
			}
		}
	}
	private final List<PromptResponse> promptResponses;
	
	/**
	 * This is an exception explicitly for creating a Mobility point from a
	 * JSONObject. This allows for a central place for creating and validating
	 * Mobility uploads and allows them to throw error codes and texts which 
	 * can be caught by validators to report back to the user.
	 * 
	 * @author John Jenkins
	 */
	public final class SurveyResponseException extends Exception {
		private static final long serialVersionUID = 1L;
		
		private final String errorCode;
		private final String errorText;
		
		/**
		 * Creates a new Mobility exception that contains an error code which
		 * corresponds to the error text describing what was wrong with this
		 * Mobility point.
		 * 
		 * @param errorCode The ErrorCode indicating what was wrong with this
		 * 					Mobility point.
		 * 
		 * @param errorText A human-readable description of what caused this 
		 * 					error.
		 */
		private SurveyResponseException(String errorCode, String errorText) {
			super(errorText);
			
			this.errorCode = errorCode;
			this.errorText = errorText;
		}
		
		/**
		 * Creates a new Mobility exception that contains an error code which
		 * corresponds to the error text describing what was wrong with this
		 * Mobility point and includes the Throwable that caused this 
		 * exception.
		 * 
		 * @param errorCode The ErrorCode indicating what was wrong with this
		 * 					Mobility point.
		 * 
		 * @param errorText A human-readable description of what cuased this
		 * 					error.
		 * 
		 * @param cause The Throwable that caused this point to be reached.
		 */
		private SurveyResponseException(String errorCode, String errorText, Throwable cause) {
			super(errorText, cause);
			
			this.errorCode = errorCode;
			this.errorText = errorText;
		}
		
		/**
		 * Returns the error code that was used to create this exception.
		 * 
		 * @return The error code that was used to create this exception.
		 */
		public final String getErrorCode() {
			return errorCode;
		}
		
		/**
		 * Returns the error text that was used to create this exception.
		 * 
		 * @return The error text that was used to create this exception.
		 */
		public final String getErrorText() {
			return errorText;
		}
	}
	
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
	 * @throws SurveyResponseException Thrown if any of the information 
	 * 								   provided is missing or invalid.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the information
	 * 									provided is missing or invalid.
	 */
	public SurveyResponseInformation(final String username, final String campaignId, final String client,
			final Date date, final long time, final TimeZone timezone, 
			final String surveyId, final JSONObject launchContext, 
			final String locationStatus, final JSONObject location,
			final SurveyResponsePrivacyStateCache.PrivacyState privacyState) throws SurveyResponseException {
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
			this.location = new Location(location);
		}
		else {
			this.location = null;
		}
		
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
	 * Adds a new prompt response to this survey response. The prompt ID, 
	 * prompt type, and response are required, but the repeatable set ID and
	 * iteration value are not if it is not part of a survey response.
	 * 
	 * @param promptId The prompt's campaign-wide unique identifier.
	 * 
	 * @param promptType The prompt's response type.
	 * 
	 * @param repeatableSetId The repeatable set's unique identifier if this
	 * 						  prompt is part of a repeatable set or null if it
	 * 						  is not.
	 * 
	 * @param repeatableSetIteration The iteration of this repeatable set if 
	 * 								 this prompt is part of a repeatable set or
	 * 								 null if it is not.
	 * 
	 * @param response The response value that the participant supplied, or the
	 * 				   device generated, representing the participant's 
	 * 				   response.
	 * 
	 * @throws SurveyResponseException Thrown if the prompt ID is null or 
	 * 								   whitespace only or the response is null.
	 * 
	 * @throws IllegalArgumentExcpetion Thrown if the prompt type is null or
	 * 									whitespace only.
	 */
	public final void addPromptResponse(final String promptId, final String promptType, 
			final String repeatableSetId, final Integer repeatableSetIteration, 
			final Object response) throws SurveyResponseException {
		promptResponses.add(new PromptResponse(promptId, promptType, repeatableSetId, repeatableSetIteration, response));
	}
}