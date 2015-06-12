/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.domain.campaign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Location;
import org.ohmage.domain.Location.LocationColumnKey;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.exception.DomainException;
import org.ohmage.util.DateTimeUtils;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for converting an uploaded or database-stored copy
 * of a survey response including the metadata about the response as well as 
 * the individual prompt responses.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class SurveyResponse {
	private static Logger LOGGER = Logger.getLogger(SurveyResponse.class);
	
	private static final String JSON_KEY_USERNAME = "user";
	private static final String JSON_KEY_CAMPAIGN_ID = "campaign_id";
	private static final String JSON_KEY_CLIENT = "client";
	private static final String JSON_KEY_TIME = "time";
	private static final String JSON_KEY_TIMEZONE = "timezone";
	private static final String JSON_KEY_LOCATION_STATUS = "location_status";
	private static final String JSON_KEY_LOCATION = "location";
	/**
	 * The survey's unique identifier.
	 */
	public static final String JSON_KEY_SURVEY_ID = "survey_id";
	private static final String JSON_KEY_SURVEY_NAME = "survey_title";
	private static final String JSON_KEY_SURVEY_DESCRIPTION = "survey_description";
	
	/**
	 * The survey launch context for the response.
	 */
	public static final String JSON_KEY_SURVEY_LAUNCH_CONTEXT = "survey_launch_context";
	// TODO - I added the short and long keys because that's how the 
	// original spec worked and the Android app was breaking with only 
	// survey_launch_context. We can revisit for 2.9. -Josh
	private static final String JSON_KEY_SURVEY_LAUNCH_CONTEXT_SHORT = "launch_context_short";
	private static final String JSON_KEY_SURVEY_LAUNCH_CONTEXT_LONG = "launch_context_long";
	
	/**
	 * The individual prompt responses for this survey response.
	 */
	public static final String JSON_KEY_RESPONSES = "responses";
	
	/**
	 * The privacy state of this survey response.
	 */
	public static final String JSON_KEY_PRIVACY_STATE = "privacy_state";
	
	/**
	 * The unique identifier for this survey response.
	 */
	public static final String JSON_KEY_SURVEY_RESPONSE_ID = "survey_key";
	
	private static final String JSON_KEY_PROMPT_ID = "prompt_id";
	private static final String JSON_KEY_REPEATABLE_SET_ID = "repeatable_set_id";
	
	private static final String JSON_KEY_PROMPT_VALUE = "value";
	
	private static final String JSON_KEY_COUNT = "count";
	
	private final String username;
	private final String campaignId;
	private final String client;
	
	private final long time;
	private final DateTimeZone timezone;
	
	private long count = 1;
	
	/**
	 * The possible status values of a location.
	 * 
	 * @author John Jenkins
	 */
	public static enum LocationStatus { 
		VALID,
		STALE,
		INACCURATE,
		GPS_OFF,
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
	private final UUID surveyResponseId;
	private final Map<Integer, Response> responses;
	
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
		/**
		 * The time the survey was launched.
		 */
		public static final String JSON_KEY_LAUNCH_TIME = "launch_time";
		/**
		 * The time-zone for the time the survey was launched.
		 */
		public static final String JSON_KEY_LAUNCH_TIMEZONE = "launch_timezone";
		/**
		 * The active triggers when the survey was launched.
		 */
		public static final String JSON_KEY_ACTIVE_TRIGGERS = "active_triggers";
		
		private final long time;
		private final DateTimeZone timezone;
		// TODO: I was hoping to avoid keeping JSON in the system and only
		// using it as a serialization format. However, this is never 
		// referenced in the code and decoding the JSON only to recode it again
		// introduces some unnecessary overhead, so for now it doesn't really
		// matter.
		private final JSONArray activeTriggers;
		
		/**
		 * Creates a LaunchContext object from a JSONObject.
		 * 
		 * @param launchContext A JSONObject that contains the launch context
		 * 						information.
		 * 
		 * @throws DomainException Thrown if the launchContext is null or if 
		 * 						   the launchContext is missing any required
		 * 						   keys (launch_time and active_triggers)
		 */
		private LaunchContext(
				final JSONObject launchContext) 
				throws DomainException {
			
			if(launchContext == null) {
				throw new DomainException(
						ErrorCode.SURVEY_INVALID_LAUNCH_CONTEXT, 
						"The launch context cannot be null.");
			}
			
			try {
				time = launchContext.getLong(JSON_KEY_LAUNCH_TIME);
			}
			catch(JSONException e) {
				throw new DomainException(
						ErrorCode.SURVEY_INVALID_LAUNCH_CONTEXT, 
						"The launch time is missing from the survey launch context: " + 
								JSON_KEY_LAUNCH_TIME, 
						e);
			}
			
			try {
				timezone = 
						DateTimeUtils.getDateTimeZoneFromString(
							launchContext.getString(JSON_KEY_LAUNCH_TIMEZONE));
			}
			catch(JSONException e) {
				throw new DomainException(
						ErrorCode.SURVEY_INVALID_LAUNCH_CONTEXT, 
						"The launch timezone is missing from the survey launch context: " +
								JSON_KEY_LAUNCH_TIMEZONE,
						e);
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
						ErrorCode.SURVEY_INVALID_LAUNCH_CONTEXT,
						"The lauch timezone is unknown.",
						e);
			}
			
			try {
				activeTriggers = launchContext.getJSONArray(JSON_KEY_ACTIVE_TRIGGERS);
			}
			catch(JSONException e) {
				throw new DomainException(
						ErrorCode.SURVEY_INVALID_LAUNCH_CONTEXT, 
						"The active triggers list is missing from the survey launch context: " +
								JSON_KEY_ACTIVE_TRIGGERS, 
						e);
			}
		}
		
		/**
		 * Creates a new LaunchContext.
		 * 
		 * @param launchTime The time that the survey was launched.
		 * 
		 * @param activeTriggers A possibly null list of trigger IDs that 
		 * 						 were active when the survey was launched. 
		 * 
		 * @throws DomainException The time zone or active triggers was null.
		 */
		public LaunchContext(
				final long launchTime, 
				final DateTimeZone launchTimezone,
				final JSONArray activeTriggers) 
				throws DomainException {
			
			if(launchTimezone == null) {
				throw new DomainException(
						"The launch timezone cannot be null.");
			}
			if(activeTriggers == null) {
				throw new DomainException(
						"The activeTriggers array cannot be null.");
			}
			
			this.time = launchTime;
			this.timezone = launchTimezone;
			this.activeTriggers = activeTriggers;
		}
		
		/**
		 * Returns the epoch milliseconds that represents this launch time.
		 * 
		 * @return The epoch milliseconds that represents this launch time.
		 */
		public final long getLaunchTime() {
			return time;
		}
		
		/**
		 * Returns the timezone of the device at this launch time.
		 * 
		 * @return The timezone of the device at this launch time.
		 */
		public final DateTimeZone getTimeZone() {
			return timezone;
		}
		
		/**
		 * Returns a new JSONArray object that contains all of the active 
		 * triggers.
		 * 
		 * @return A new JSONArray object that contains all of the active 
		 * 		   triggers.
		 */
		public final JSONArray getActiveTriggers() {
			return activeTriggers;
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
		 * 
		 * @throws JSONException There was a problem creating the JSONObject.
		 */
		public final JSONObject toJson(
				final boolean longVersion) 
				throws JSONException {
			
			JSONObject result = new JSONObject();
			
			result.put(JSON_KEY_LAUNCH_TIME, time);
			result.put(JSON_KEY_LAUNCH_TIMEZONE, timezone.getID());
			
			if(longVersion) {
				result.put(JSON_KEY_ACTIVE_TRIGGERS, activeTriggers);
			}
			
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((activeTriggers == null) ? 0 : activeTriggers.hashCode());
			result = prime * result + (int) (time ^ (time >>> 32));
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
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
			if (time != other.time)
				return false;
			return true;
		}
	}
	private final LaunchContext launchContext;
	
	/**
	 * The possible column keys that can be requested for survey response read.
	 * 
	 * @author John Jenkins
	 */
	public static enum ColumnKey {
		/**
		 * The request-wide client key.
		 */
		CONTEXT_CLIENT ("urn:ohmage:context:client"),
		/**
		 * The survey-wide timestamp key.
		 * 
		 * @see #CONTEXT_TIMEZONE
		 */
		CONTEXT_TIMESTAMP ("urn:ohmage:context:timestamp"),
		/**
		 * The survey-wide timestamp key where the time is adjusted to UTC.
		 */
		CONTEXT_UTC_TIMESTAMP ("urn:ohmage:context:utc_timestamp"),
		/**
		 * The survey-wide timestamp key in milliseconds.
		 */
		CONTEXT_EPOCH_MILLIS ("urn:ohmage:context:epoch_millis"),
		/**
		 * The survey-wide timezone key.
		 * 
		 * @see #CONTEXT_TIMESTAMP
		 */
		CONTEXT_TIMEZONE ("urn:ohmage:context:timezone"),
		/**
		 * The survey-wide date key.
		 * 
		 * @see #CONTEXT_TIMEZONE
		 */
		CONTEXT_DATE ("urn:ohmage:context:date"),
		/**
		 * The key for the survey's entire launch context.
		 * 
		 * @see ColumnKey#CONTEXT_LAUNCH_CONTEXT_SHORT
		 */
		CONTEXT_LAUNCH_CONTEXT_LONG ("urn:ohmage:context:launch_context_long"),
		/**
		 * The key for only the survey's launch timestamp.
		 * 
		 * @see ColumnKey#CONTEXT_LAUNCH_CONTEXT_SHORT
		 */
		CONTEXT_LAUNCH_CONTEXT_SHORT ("urn:ohmage:context:launch_context_short"),
		/**
		 * The survey's location status key.
		 * 
		 * @see ColumnKey#CONTEXT_LOCATION_ACCURACY
		 * @see ColumnKey#CONTEXT_LOCATION_LATITUDE
		 * @see ColumnKey#CONTEXT_LOCATION_LONGITUDE
		 * @see ColumnKey#CONTEXT_LOCATION_PROVIDER
		 * @see ColumnKey#CONTEXT_LOCATION_TIMESTAMP
		 */
		CONTEXT_LOCATION_STATUS ("urn:ohmage:context:location:status"),
		/**
		 * The survey's location's latitude key.
		 * 
		 * @see ColumnKey#CONTEXT_LOCATION_STATUS
		 */
		CONTEXT_LOCATION_LATITUDE ("urn:ohmage:context:location:latitude"),
		/**
		 * The survey's location's longitude key.
		 * 
		 * @see ColumnKey#CONTEXT_LOCATION_STATUS
		 */
		CONTEXT_LOCATION_LONGITUDE ("urn:ohmage:context:location:longitude"),
		/**
		 * The survey's location's time key.
		 * 
		 * @see ColumnKey#CONTEXT_LOCATION_STATUS
		 */
		CONTEXT_LOCATION_TIMESTAMP ("urn:ohmage:context:location:timestamp"),
		/**
		 * The survey's location's time zone key.
		 * 
		 * @see ColumnKey#CONTEXT_LOCATION_STATUS
		 */
		CONTEXT_LOCATION_TIMEZONE ("urn:ohmage:context:location:timezone"),
		/**
		 * The survey's location's accuracy key.
		 * 
		 * @see ColumnKey#CONTEXT_LOCATION_STATUS
		 */
		CONTEXT_LOCATION_ACCURACY ("urn:ohmage:context:location:accuracy"),
		/**
		 * The survey's location's provider key.
		 * 
		 * @see ColumnKey#CONTEXT_LOCATION_STATUS
		 */
		CONTEXT_LOCATION_PROVIDER ("urn:ohmage:context:location:provider"),
		/**
		 * The survey's user ID key.
		 */
		USER_ID ("urn:ohmage:user:id"),
		/**
		 * The survey's ID key.
		 */
		SURVEY_ID ("urn:ohmage:survey:id"),
		/**
		 * The survey's title key.
		 */
		SURVEY_TITLE ("urn:ohmage:survey:title"),
		/**
		 * The survey's description key.
		 */
		SURVEY_DESCRIPTION ("urn:ohmage:survey:description"),
		/**
		 * The survey privacy state key.
		 * 
		 * @see PrivacyState
		 */
		SURVEY_PRIVACY_STATE ("urn:ohmage:survey:privacy_state"),
		/**
		 * The prompt's repeatable set ID if the prompt was part of a 
		 * repeatable set.
		 */
		REPEATABLE_SET_ID ("urn:ohmage:repeatable_set:id"),
		/**
		 * The prompt's repeatable set iteration if the prompt was part of a
		 * repeatable set.
		 */
		REPEATABLE_SET_ITERATION ("urn:ohmage:repeatable_set:iteration"),
		/**
		 * The key used to indicate that the survey response's unique 
		 * identifier is desired.
		 */
		SURVEY_RESPONSE_ID ("urn:ohmage:survey_response:id"),
		/**
		 * The key used to indicate if responses are desired; however, the 
		 * response from the server will include either only the prompts' ID
		 * in the case of {@link OutputFormat#JSON_ROWS} and 
		 * {@link OutputFormat#CSV} or the prompts' ID prepended with 
		 * {@link ColumnKey#URN_PROMPT_ID_PREFIX} in the case of 
		 * {@link OutputFormat#JSON_COLUMNS}.
		 */
		PROMPT_RESPONSE ("urn:ohmage:prompt:response");
		
		/**
		 * The prefix to prompt IDs in the {@link OutputFormat#JSON_COLUMNS}.
		 * 
		 * @see OutputFormat#JSON_COLUMNS
		 */
		public static final String URN_PROMPT_ID_PREFIX = "urn:ohmage:prompt:id:";
		
		private final String key;
		
		/**
		 * Assigns the key to the enum constant.
		 * 
		 * @param key The key.
		 */
		private ColumnKey(final String key) {
			this.key = key;
		}
		
		/**
		 * Returns a ColumnKey enum for the given key or throws an exception.
		 * 
		 * @param key The key.
		 * 
		 * @return A ColumnKey enum.
		 * 
		 * @throws IllegalArgumentException Thrown if the key could not be
		 * 									converted into a ColumnKey enum.
		 */
		public static ColumnKey getValue(final String key) {
			ColumnKey[] values = ColumnKey.values();
			
			for(int i = 0; i < values.length; i++) {
				if(values[i].key.equals(key)) {
					return values[i];
				}
			}
			
			throw new IllegalArgumentException("Unknown key: " + key);
		}
		
		/**
		 * Returns the key value.
		 * 
		 * @return The key value.
		 */
		@Override
		public String toString() {
			return key;
		}
	}
	
	/**
	 * The known "survey response function" functions.
	 * 
	 * @author John Jenkins
	 */
	public static enum Function {
		PRIVACY_STATE;
		
		/**
		 * Generates the Function enum for the key.
		 * 
		 * @param key The Function as a string.
		 * 
		 * @return The Function as an enum.
		 * 
		 * @throws IllegalArgumentException Thrown if no such Function enum
		 * 									represents the key.
		 */
		public static Function getValue(final String key) {
			return valueOf(key.toUpperCase());
		}
		
		/**
		 * Returns this Function as a human-readable value.
		 * 
		 * @return This Function as a human-readable value.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	/**
	 * The items to use to further subdivide the results of the privacy state
	 * function in survey_response/read.
	 * 
	 * @author John Jenkins
	 */
	public static enum FunctionPrivacyStateItem {
		SURVEY,
		DATE;
		
		/**
		 * Generates the privacy state item for the key.
		 * 
		 * @param key The privacy state item as a key.
		 * 
		 * @return The privacy state item.
		 * 
		 * @throws IllegalArgumentException The key could not be decoded to a
		 * 									privacy state item.
		 */
		public static FunctionPrivacyStateItem getValue(final String key) {
			return valueOf(key.toUpperCase());
		}
		
		/**
		 * Returns this privacy state item as a human-readable value.
		 * 
		 * @return This privacy state item as a human-readable value.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	/**
	 * The possible output formats for reading survey responses.
	 * 
	 * @author John Jenkins
	 */
	public static enum OutputFormat {
		/**
		 * This will result in a JSONArray of JSONObjects where each JSONObject
		 * represents a single survey response.
		 */
		JSON_ROWS ("json-rows"),
		/**
		 * This will result in a JSONObject where the key is one of the  
		 * requested keys and the value is a JSONArray with at least the key
		 * "values" which is a JSONArray of the type-appropriate values. There
		 * may also be a key called "context" which further describes the 
		 * prompt responses.
		 * 
		 * @see org.ohmage.request.survey.SurveyResponseReadRequest#JSON_KEY_CONTEXT JSON_KEY_CONTEXT
		 * @see org.ohmage.request.survey.SurveyResponseReadRequest#JSON_KEY_VALUES JSON_KEY_VALUES
		 */
		JSON_COLUMNS ("json-columns"),
		/**
		 * This will result in a file attachment which contains CSV-formatted 
		 * results. The results will contain headers for the requested columns
		 * and one additional column for each of the requested prompts. If a
		 * row does not contain a response to the question, "null" will be
		 * output instead.
		 */
		CSV ("csv");
		
		private final String key;
		
		/**
		 * Assigns the key to the enum constant.
		 * 
		 * @param key The key value to be associated with the enum.
		 */
		private OutputFormat(final String key) {
			this.key = key;
		}
		
		/**
		 * Translates a key value into an appropriate OutputFormat enum or
		 * throws an exception.
		 * 
		 * @param key The key value.
		 * 
		 * @return An OutputFormat enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no applicable
		 * 									OutputFormat for the key.
		 */
		public static OutputFormat getValue(final String key) {
			OutputFormat[] values = OutputFormat.values();
			
			for(int i = 0; i < values.length; i++) {
				if(values[i].key.equals(key)) {
					return values[i];
				}
			}
			
			throw new IllegalArgumentException("Unknown key: " + key);
		}
		
		/**
		 * Returns the key value.
		 * 
		 * @return The key value.
		 */
		@Override
		public String toString() {
			return key;
		}
	}
	
	/**
	 * This represents the different sort parameters influencing how the final
	 * results are presented to the user.
	 * 
	 * @author John Jenkins
	 */
	public static enum SortParameter {
		SURVEY ("survey_id"),
		TIMESTAMP ("epoch_millis"),
		USER ("username");
		
		// The string used to represent the columns on which to order.
		private final String sqlColumn;
		
		/**
		 * Creates a sort parameter with a String representing the SQL 
		 * column(s) to use to order the result.
		 * 
		 * @param sqlColumn The SQL string to use to order this column.
		 */
		private SortParameter(final String sqlColumn) {
			this.sqlColumn = sqlColumn;
		}
		
		/**
		 * Converts a string value into its appropriate SortParameter object or
		 * throws an exception of no appropriate SortParameter object exists.
		 * 
		 * @param value The string value.
		 * 
		 * @return A SortParameter object.
		 * 
		 * @throws IllegalArgumentException Thrown if no appropriate 
		 * 									SortParameter object applies to 
		 * 									the given value.
		 */
		public static SortParameter getValue(final String value) {
			return SortParameter.valueOf(value.toUpperCase());
		}
		
		/**
		 * A String representing the SQL to use to order the column(s).
		 * 
		 * @return A String representing the SQL to use to order the column(s)
		 * 		   without the "ORDER BY" text. This is only the column('s)
		 * 		   name. 
		 */
		public String getSqlColumn() {
			return sqlColumn;
		}
		
		/**
		 * Returns this sort parameter as a lower case version of its name.
		 * 
		 * @return This sort parameter as a lower case version of its name.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	/**
	 * Creates a new survey response information object based on the 
	 * parameters. All parameters are required unless otherwise specified.
	 * 
	 * @param survey The Survey to which this survey response belongs.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
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
	 * @throws DomainException Thrown if any of the information provided is 
	 * 						   missing or invalid.
	 */
	public SurveyResponse(
			final Survey survey, 
			final UUID surveyResponseId,
			final String username, 
			final String campaignId, 
			final String client,
			final long time, 
			final DateTimeZone timezone, 
			final JSONObject launchContext, 
			final String locationStatus, 
			final JSONObject location,
			final PrivacyState privacyState) 
			throws DomainException {

		if(survey == null) {
			throw new DomainException("The survey cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new DomainException(
					"The username cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new DomainException(
					"The campaign ID cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			throw new DomainException(
					"The client cannot be null or whitespace only.");
		}
		else if(timezone == null) {
			throw new DomainException("The timezone cannot be null.");
		}
		else if(launchContext == null) {
			throw new DomainException("The launch context cannot be null.");
		}
		else if(locationStatus == null) {
			throw new DomainException("The location status cannot be null.");
		}
		else if(privacyState == null) {
			throw new DomainException("The privacy state cannot be null.");
		}
		
		this.username = username;
		this.campaignId = campaignId;
		this.client = client;
		
		this.time = time;
		this.timezone = timezone;
		
		this.surveyResponseId = surveyResponseId;
		this.survey = survey;
		this.privacyState = privacyState;
		
		this.launchContext = new LaunchContext(launchContext);
		
		try {
			this.locationStatus = 
					LocationStatus.valueOf(locationStatus.toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("Unknown location status.", e);
		}
		if(location != null) {
			this.location = new Location(location, timezone);
		}
		else {
			this.location = null;
		}
		
		responses = new HashMap<Integer, Response>();
	}
	
	/**
	 * Creates a new SurveyResponse object.
	 * 
	 * @param survey The Survey to which this survey response belongs.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
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
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   null.
	 */
	public SurveyResponse(
			final Survey survey, 
			final UUID surveyResponseId,
			final String username, 
			final String campaignId, 
			final String client,
			final long time, 
			final DateTimeZone timezone, 
			final LaunchContext launchContext, 
			final LocationStatus locationStatus, 
			final Location location,
			final PrivacyState privacyState, 
			final Map<Integer, Response> responses) 
			throws DomainException {

		if(survey == null) {
			throw new DomainException("The survey cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new DomainException(
					"The username cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new DomainException(
					"The campaign ID cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			throw new DomainException(
					"The client cannot be null or whitespace only.");
		}
		else if(timezone == null) {
			throw new DomainException("The timezone cannot be null.");
		}
		else if(launchContext == null) {
			throw new DomainException("The launch context cannot be null.");
		}
		else if(locationStatus == null) {
			throw new DomainException("The location status cannot be null.");
		}
		else if((! LocationStatus.UNAVAILABLE.equals(locationStatus)) && 
				(location == null)) {
			throw new DomainException(
					"The location status is not unavailable, but the location status is null.");
		}
		else if(privacyState == null) {
			throw new DomainException("The privacy state cannot be null.");
		}
		
		this.username = username;
		this.campaignId = campaignId;
		this.client = client;
		
		this.time = time;
		this.timezone = timezone;
		
		this.surveyResponseId = surveyResponseId;
		this.survey = survey;
		this.privacyState = privacyState;
		
		this.launchContext = launchContext;
		
		this.locationStatus = locationStatus;
		if((! LocationStatus.UNAVAILABLE.equals(locationStatus)) && 
				(location == null)) {
			throw new DomainException(
					"The location cannot be null unless the location status is unavailable.");
		}
		this.location = location;
		
		this.responses = new HashMap<Integer, Response>(responses);
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
	 * @throws DomainException Thrown if the JSONObject could not be decoded
	 * 							  as a survey response.
	 */
	public SurveyResponse(
			final String username, 
			final String campaignId,
			final String client, 
			final Campaign campaign,
			final JSONObject response) 
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new DomainException(
					ErrorCode.USER_INVALID_USERNAME, 
					"The username is invalid.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new DomainException(
					ErrorCode.CAMPAIGN_INVALID_ID, 
					"The campaign ID is invalid.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_CLIENT, 
					"The client value is invalid.");
		}
		
		this.username = username;
		this.campaignId = campaignId;
		this.client = client;
		
		String surveyResponseIdString;
		try {
			surveyResponseIdString = response.getString(JSON_KEY_SURVEY_RESPONSE_ID);
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_SURVEY_ID, 
					"The survey ID is missing: " + JSON_KEY_SURVEY_RESPONSE_ID,
					e);
		}
		
		try {
			surveyResponseId = UUID.fromString(surveyResponseIdString);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_SURVEY_ID, 
					"The survey ID is not a valid UUID: " + 
						surveyResponseIdString, 
					e);
		}
		
		try {
			time = response.getLong(JSON_KEY_TIME);
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_TIME, 
					"The time is missing.", 
					e);
		}
		
		try {
			timezone = 
					DateTimeUtils.getDateTimeZoneFromString(
						response.getString(JSON_KEY_TIMEZONE));
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_TIMEZONE, 
					"The time zone is missing.", 
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_TIMEZONE,
					"The time zone is unknown.",
					e);
		}
		
		String surveyId;
		try {
			surveyId = response.getString(JSON_KEY_SURVEY_ID);
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_SURVEY_ID, 
					"The survey ID is missing.", 
					e);
		}
		
		Set<String> allowedPromptIds = null;
		CampaignMask mask = campaign.getLatestMask();
		// If a mask exists, ensure that this survey ID is allowed.
		if(mask != null) {
			// Get the allowed prompt IDs.
			allowedPromptIds = mask.getSurveyPromptMap().get(surveyId);
			
			// If the prompt IDs are null, then this survey is not allowed.
			if(allowedPromptIds == null) {
				throw
					new DomainException(
						"The survey is masked and survey responses are not " +
							"allowed.");
			}
		}
		
		survey = campaign.getSurveys().get(surveyId);
		if(survey == null) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_SURVEY_ID, 
					"The survey ID doesn't refer to any known surveys in the campaign: " + surveyId);
		}
		
		try {
			launchContext = new LaunchContext(response.getJSONObject(JSON_KEY_SURVEY_LAUNCH_CONTEXT));
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_LAUNCH_CONTEXT, 
					"The launch context is missing.", 
					e);
		}
		
		try {
			locationStatus = LocationStatus.valueOf(response.getString(JSON_KEY_LOCATION_STATUS).toUpperCase());
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_LOCATION_STATUS, 
					"The location status is missing.", 
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_LOCATION_STATUS, 
					"The location status is unknown.", 
					e);
		}
		
		Location tLocation = null;
		try {
			tLocation = 
				new Location(
					response.getJSONObject(JSON_KEY_LOCATION),
					timezone);
		}
		catch(JSONException e) {
			if(!(
				LocationStatus.UNAVAILABLE.equals(locationStatus) ||
				LocationStatus.GPS_OFF.equals(locationStatus))) {
				
				throw new DomainException(
						ErrorCode.SERVER_INVALID_LOCATION, 
						"The location is missing.", 
						e);
			}
		}
		location = tLocation;
		
		PrivacyState tPrivacyState;
		if(response.has(JSON_KEY_PRIVACY_STATE)) {
			try {
				tPrivacyState = 
					PrivacyState
						.getValue(response.getString(JSON_KEY_PRIVACY_STATE));
			}
			catch(JSONException e) {
				throw new DomainException(
					ErrorCode.MOBILITY_INVALID_PRIVACY_STATE,
					"The privacy state was not a string.",
					e);
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
					ErrorCode.MOBILITY_INVALID_PRIVACY_STATE,
					"The privacy state is unknown: " + e.getMessage(),
					e);
			}
		}
		else {
			tPrivacyState = PrivacyState.PRIVATE;
		}
		this.privacyState = tPrivacyState;
		
		JSONArray responses;
		try {
			responses = response.getJSONArray(JSON_KEY_RESPONSES);
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_RESPONSES, 
					"There weren't any responses for the survey.", 
					e);
		}
		this.responses = 
			processResponses(
				campaign.getSurveys().get(surveyId).getSurveyItems(), 
				responses,
				null,
				allowedPromptIds);
	}
	
	/**
	 * Returns the survey response's unique identifier.
	 * 
	 * @return The survey response's unique identifier.
	 */
	public final UUID getSurveyResponseId() {
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
	 * Returns the time this survey response was generated as a long value from
	 * the epoch.
	 * 
	 * @return The time in milliseconds since the epoch.
	 */
	public final long getTime() {
		return time;
	}

	/**
	 * Returns the time zone object representing the time zone of the device
	 * that generated this survey response at the time it was generated.
	 * 
	 * @return The phone's time zone.
	 */
	public final DateTimeZone getTimezone() {
		return timezone;
	}
	
	/**
	 * Returns a DateTime object that represents this object's date and time.
	 * 
	 * @return This object's date and time.
	 */
	public final DateTime getDate() {
		return new DateTime(time, timezone);
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
	public final Map<Integer, Response> getResponses() {
		return Collections.unmodifiableMap(responses);
	}
	
	/**
	 * Adds a prompt response to this survey response.
	 * 
	 * @param promptResponse The prompt response.
	 * 
	 * @throws DomainException Thrown if the prompt response is null.
	 */
	public final void addPromptResponse(
			final PromptResponse promptResponse) 
			throws DomainException {
		
		if(promptResponse == null) {
			throw new DomainException("The prompt response is null.");
		}
		
		RepeatableSet parent = promptResponse.getPrompt().getParent();
		if(parent == null) {
			responses.put(
					promptResponse.getPrompt().getIndex(), 
					promptResponse);
		}
		else {
			// FIXME: This assumes repeatable sets cannot contain repeatable
			// sets. This needs to be fixed if we ever allow it.
			int index = parent.getIndex();
			
			RepeatableSetResponse rsResponse = 
					(RepeatableSetResponse) responses.get(index);
			
			if(rsResponse == null) {
				rsResponse = new RepeatableSetResponse(parent, null);
				responses.put(index, rsResponse);
			}
			rsResponse.addResponse(
					promptResponse.getRepeatableSetIteration(), 
					index, 
					promptResponse);
		}
	}
	
	/**
	 * Removes the any prompt responses that do not belong to the provided 
	 * collection of prompt ids from this survey response.
	 * 
	 * @param promptIds  A collection of prompt ids belonging to some campaign. 
	 * 
	 * @throws DomainException if promptIds is null or empty.
	 */
	public final void filterPromptResponseByPromptIds(
			final Collection<String> promptIds) 
			throws DomainException {
		
		if(promptIds == null || promptIds.isEmpty()) {
			throw new DomainException("a list of prompt ids is required");
		}
		
		if(responses != null) {
			Iterator<Integer> responsesKeySetIterator = responses.keySet().iterator();
			while(responsesKeySetIterator.hasNext()) {
				Response response = responses.get(responsesKeySetIterator.next());
				
				if(! promptIds.contains(response.getId())) {
					responsesKeySetIterator.remove();
				}
			}
		} 
		else {
			LOGGER.warn("attempt to filter prompt responses out of a SurveyResponse that has no prompt responses.");
		}
	}
	
	/**
	 * Returns a set of all of the prompt IDs for all of the responses in this
	 * survey response.
	 * 
	 * @return A set of the prompt IDs.
	 */
	public Set<String> getPromptIds() {
		return getPromptIds(responses.values());
	}
	
	/**
	 * Sets the number of survey responses that are like this survey response.
	 * <br /><br />
	 * The primary purpose of this was when aggregating data from the database
	 * and only being concerned about a subset of the data. For example, 
	 * multiple survey responses may have been generated by the same user;
	 * however, they may have nothing else in common. One could generate only
	 * one SurveyResponse object from the data of any one of those survey
	 * responses and then set this count to the number of survey responses that
	 * were generated by that one user. This way, only one SurveyResponse 
	 * object needs to be created, and it can represent all of the other survey
	 * response objects.
	 * 
	 * @param count The number of survey responses that match some subset of
	 * 				the information provided by this survey response.
	 * 
	 * @throws DomainException If the count is not positive.
	 */
	public void setCount(final long count) throws DomainException {
		if(count < 1) {
			throw new DomainException("The count must be positive.");
		}
		
		this.count = count;
	}
	
	/**
	 * Returns the number of survey responses that match some subset of the
	 * information contained in this survey response.<br />
	 * <br />
	 * This count will always be at least one to indicate this survey response.
	 * It may be greater if some subset of this survey response is shared with
	 * other survey responses, but this survey response will not keep track of
	 * what those fields are. For example, this value could be 100 to indicate
	 * that some user has uploaded 100 survey responses and the only value that
	 * this survey response shares with other survey responses is the username.
	 * It could be more than one column as well. It could be that this user has
	 * uploaded 150 survey responses, but only 100 are from this survey. A user
	 * may have upload another 50 survey responses from another survey.
	 *  
	 * @return The number of survey responses that share some subset of the 
	 * 		   data in this survey response.
	 */
	public long getCount() {
		return count;
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
	 * @param arrayInsteadOfObject Valid only if 'withResponses' is true, this
	 * 							   will determine how the responses are output,
	 * 							   either as a JSONObject where the keys are 
	 * 							   the prompt IDs and their value is another 
	 * 							   JSONObject that describes the prompt and the
	 * 							   user's response or as a JSONArray of 
	 * 							   JSONObjects that describe the prompt and the
	 * 							   user's response. If false, the former will
	 * 							   happen; if true, the latter will happen.
	 * 
	 * @return A JSONObject that represents this object or null if there was an
	 * 		   error.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	public final JSONObject toJson(
			final boolean withUsername, 
			final boolean withCampaignId, 
			final boolean withClient, 
			final boolean withPrivacyState, 
			final boolean withTime, 
			final boolean withTimezone, 
			final boolean withLocationStatus, 
			final boolean withLocation, 
			final boolean withSurveyId, 
			final boolean withSurveyTitle, 
			final boolean withSurveyDescription,
			final boolean withLaunchContextShort, 
			final boolean withLaunchContextLong, 
			final boolean withResponses, 
			final boolean arrayInsteadOfObject, 
			final boolean withId,
			final boolean withCount) 
			throws JSONException, DomainException {
		
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
			result.put(JSON_KEY_LOCATION, location.toJson(false, LocationColumnKey.ALL_COLUMNS));
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
		
		if(withLaunchContextShort) {
			result.put(JSON_KEY_SURVEY_LAUNCH_CONTEXT_SHORT, launchContext.toJson(false));
		}
		
		if(withLaunchContextLong) {
			result.put(JSON_KEY_SURVEY_LAUNCH_CONTEXT_LONG, launchContext.toJson(true));
		}
		
		if(withResponses) {
			List<Integer> indices = new ArrayList<Integer>(responses.keySet());
			Collections.sort(indices);
			
			if(arrayInsteadOfObject) {
				JSONArray responses = new JSONArray();
				for(Integer index : indices) {
					responses.put(this.responses.get(index).toJson(true));
				}
				result.put(JSON_KEY_RESPONSES, responses);
			}
			else {
				// HT: attempt to preserve the order of json object
				JSONObject responses = new JSONObject(new LinkedHashMap<String, String>());
				for(Integer index : indices) {
					Response response = this.responses.get(index);
		
					responses.put(response.getId(), response.toJson(false));
				}
				
				result.put(JSON_KEY_RESPONSES, responses);
			}
		}
		
		if(withId) {
			result.put(JSON_KEY_SURVEY_RESPONSE_ID, surveyResponseId.toString());
		}
		
		if(withCount) {
			result.put(JSON_KEY_COUNT, count);
		}
		
		// LOGGER.debug("HT: result:" + result.toString());
		return result;
	}
	
	/**
	 * Generates a hash code for this survey response.
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
				+ ((launchContext == null) ? 0 : launchContext.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result
				+ ((locationStatus == null) ? 0 : locationStatus.hashCode());
		result = prime * result
				+ ((privacyState == null) ? 0 : privacyState.hashCode());
		result = prime * result
				+ ((responses == null) ? 0 : responses.hashCode());
		result = prime * result + ((survey == null) ? 0 : survey.hashCode());
		result = prime * result + (int) (time ^ (time >>> 32));
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	/**
	 * Determines if this survey response is equivalent to another object.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if the other object is logically equivalent to this survey
	 * 		   response; false, otherwise.
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
		if (launchContext == null) {
			if (other.launchContext != null)
				return false;
		} else if (!launchContext.equals(other.launchContext))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (locationStatus != other.locationStatus)
			return false;
		if (privacyState != other.privacyState)
			return false;
		if (responses == null) {
			if (other.responses != null)
				return false;
		} else if (!responses.equals(other.responses))
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
	 * @throws DomainException Thrown if any of the responses are invalid 
	 * 						   either syntactically or as compared to the
	 * 						   survey objects.
	 */
	private Map<Integer, Response> processResponses(
		final Map<Integer, SurveyItem> surveyItems, 
		final JSONArray currArray, 
		final Integer repeatableSetIteration,
		final Set<String> allowedPromptIds) 
		throws DomainException {
		
		int numResponses = currArray.length();
		Map<Integer, Response> results = 
			new HashMap<Integer, Response>(numResponses);
		
		for(int i = 0; i < numResponses; i++) {
			try {
				JSONObject currResponse = currArray.getJSONObject(i);
				
				try {
					String promptId = currResponse.getString(JSON_KEY_PROMPT_ID);
					Prompt prompt = null;
					for(SurveyItem surveyItem : surveyItems.values()) {
						if(surveyItem.getId().equals(promptId)) {
							prompt = (Prompt) surveyItem;
							break;
						}
					}
					if(prompt == null) {
						throw new DomainException(
								ErrorCode.SURVEY_INVALID_RESPONSES, 
								"The prompt ID is unknown: " + promptId);
					}
					
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
						RepeatableSet repeatableSet = null;
						for(SurveyItem surveyItem : surveyItems.values()) {
							if(surveyItem.getId().equals(repeatableSetId)) {
								repeatableSet = (RepeatableSet) surveyItem;
							}
						}
						if(repeatableSet == null) {
							throw new DomainException(
									ErrorCode.SURVEY_INVALID_RESPONSES, 
									"The repeatable set ID is unknown: " + 
										repeatableSetId);
						}
						
						results.put(
								repeatableSet.getIndex(),
								processRepeatableSet(
										repeatableSet, 
										currResponse,
										allowedPromptIds));
					}
					catch(JSONException notRepeatableSet) {
						throw new DomainException(
								ErrorCode.SURVEY_INVALID_RESPONSES, 
								"The response wasn't a prompt response or repeatable set.");
					}
				}
				catch(ClassCastException e) {
					throw new DomainException(
							ErrorCode.SURVEY_INVALID_RESPONSES, 
							"The response and XML disagree on the type of a survey item.", 
							e);
				}
			}
			catch(JSONException e) {
				throw new DomainException(
						ErrorCode.SURVEY_INVALID_RESPONSES, 
						"A response was not valid JSON.");
			}
		}
		
		// Cycle through the prompts and ensure that a response exists for each
		// prompt, unless it was masked.
		for(SurveyItem surveyItem : surveyItems.values()) {
			// If it's a message, it won't have a response.
			if(! (surveyItem instanceof Message)) {
				String surveyItemId = surveyItem.getId();
				
				// If it's not part of the mask, throw an exception.
				if(
					(allowedPromptIds != null) &&
					(! allowedPromptIds.contains(surveyItemId))) {
					
					continue;
				}
				
				boolean found = false;

				for(Response response : results.values()) {
					if(response.getId().equals(surveyItemId)) {
						found = true;
						break;
					}
				}
				
				if(! found) {
					throw new DomainException(
							ErrorCode.SURVEY_INVALID_RESPONSES, 
							"The response is missing a response for the prompt: " + 
								surveyItemId);
				}
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
	 * @throws DomainException Thrown if the JSONObject is invalid for getting 
	 * 						   a response value.
	 */
	private PromptResponse processPromptResponse(
			final Prompt prompt,
			final JSONObject response, 
			final Integer repeatableSetIteration) 
			throws DomainException {
		
		try {
			return prompt.createResponse(
					repeatableSetIteration, 
					response.get(JSON_KEY_PROMPT_VALUE));
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_RESPONSES, 
					"The response value was missing.", 
					e);
		}
		catch(DomainException e) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_RESPONSES, 
					"The response value was invalid.", 
					e);
		}
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
	 * @throws DomainException Thrown if the repeatable set JSONObject is
	 * 						   malformed.
	 */
	private RepeatableSetResponse processRepeatableSet(
			final RepeatableSet repeatableSet, 
			final JSONObject response,
			final Set<String> allowedPromptIds) 
			throws DomainException {
		
		try {
			if(response.getBoolean(RepeatableSetResponse.NOT_DISPLAYED)) {
				return new RepeatableSetResponse(
						repeatableSet, 
						NoResponse.NOT_DISPLAYED);
			}
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_RESPONSES, 
					"The not displayed value is missing.", 
					e);
		}
		RepeatableSetResponse result = new RepeatableSetResponse(repeatableSet, null);
		
		JSONArray responses;
		try {
			responses = response.getJSONArray(JSON_KEY_RESPONSES);
		} catch (JSONException e) {
			throw new DomainException(
					ErrorCode.SURVEY_INVALID_RESPONSES, 
					"The responses array for the repeatable set are missing.", 
					e);
		}
		
		int numIterations = responses.length();
		for(int i = 0; i < numIterations; i++) {
			try {
				result.addResponseGroup(
						i + 1, 
						processResponses(
								repeatableSet.getSurveyItems(), 
								responses.getJSONArray(i),
								i,
								allowedPromptIds
							)
					);
			} catch (JSONException e) {
				throw new DomainException(
						ErrorCode.SURVEY_INVALID_RESPONSES, 
						"One of the response array objects for a repeatable set is not a JSONArray.", 
						e);
			}
		}
		return result;
	}
	
	/**
	 * Returns a set of all of the prompt IDs for all of the responses in this
	 * survey response.
	 * 
	 * @param responses A collection of responses.
	 * 
	 * @return A set of the prompt IDs.
	 */
	private Set<String> getPromptIds(final Collection<Response> responses) {
		Set<String> result = new HashSet<String>();
		
		for(Response response : responses) {
			if(response instanceof PromptResponse) {
				result.add(response.getId());
			}
			else if(response instanceof RepeatableSetResponse) {
				for(Map<Integer, Response> responseGroup :
					((RepeatableSetResponse) response).getResponseGroups().values()) {
					
					result.addAll(getPromptIds(responseGroup.values()));
				}
			}
		}
		
		return result;
	}
}
