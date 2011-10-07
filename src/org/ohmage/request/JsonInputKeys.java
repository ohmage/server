/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.request;

/**
 * A true singleton that contains all the constants for keys within uploaded JSON messages.
 * 
 * @author Joshua Selsky
 */
public final class JsonInputKeys {
	/**
	 * Only constructor. Can never be instantiated.
	 */
	private JsonInputKeys() {
		// Do nothing.
	}
	
	// Metadata
	public static final String METADATA_DATE = "date";
	public static final String METADATA_TIME = "time";
	public static final String METADATA_TIMEZONE = "timezone";
	public static final String METADATA_LOCATION_STATUS = "location_status";
	public static final String METADATA_LOCATION = "location";
	public static final String METADATA_LOCATION_LATITUDE = "latitude";
	public static final String METADATA_LOCATION_LONGITUDE = "longitude";
	public static final String METADATA_LOCATION_ACCURACY = "accuracy";
	public static final String METADATA_LOCATION_PROVIDER = "provider";
	public static final String METADATA_LOCATION_TIMESTAMP = "timestamp";
	public static final String METADATA_LOCATION_STATUS_UNAVAILABLE = "unavailable";
	public static final String METADATA_LOCATION_STATUS_INACCURATE = "inaccurate";
	public static final String METADATA_LOCATION_STATUS_VALID = "valid";
	public static final String METADATA_LOCATION_STATUS_STALE = "stale";
	
	// Surveys
	public static final String SURVEY_ID = "survey_id";
	public static final String SURVEY_RESPONSES = "responses";
	public static final String SURVEY_LAUNCH_CONTEXT = "survey_launch_context";
	public static final String SURVEY_LAUNCH_TIME = "launch_time";
	public static final String SURVEY_REPEATABLE_SET_ID = "repeatable_set_id";
	public static final String SURVEY_REPEATABLE_SET_SKIPPED = "skipped";
	public static final String SURVEY_REPEATABLE_SET_NOT_DISPLAYED = "not_displayed";
	public static final String SURVEY_PROMPT_ID = "prompt_id";
	public static final String PROMPT_VALUE = "value";
	public static final String PROMPT_SKIPPED = "SKIPPED";
	public static final String PROMPT_NOT_DISPLAYED = "NOT_DISPLAYED";
	public static final String PROMPT_PROPERTY_MIN = "min";
	public static final String PROMPT_PROPERTY_MAX = "max";
	public static final String PROMPT_REMOTE_ACTIVITY_SCORE = "score";
	public static final String PROMPT_REMOTE_ACTIVITY_RETRIES = "retries";
	public static final String PROMPT_REMOTE_ACTIVITY_MIN_RUNS = "min_runs";
	public static final String PROMPT_CUSTOM_CHOICES = "custom_choices";
	public static final String PROMPT_CUSTOM_CHOICE_ID = "choice_id";
	public static final String PROMPT_CUSTOM_CHOICE_VALUE = "choice_value";
}
