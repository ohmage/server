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
package org.ohmage.domain;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;

/**
 * This class represents a server's configuration including its name, version,
 * and state values.
 * 
 * @author John Jenkins
 */
public class ServerConfig {
	/**
	 * The key to use when creating/reading JSON for the application's name.
	 */
	public static final String JSON_KEY_APPLICATION_NAME = "application_name";
	/**
	 * The key to use when creating/reading JSON for the application's version.
	 */
	public static final String JSON_KEY_APPLICATION_VERSION = "application_version";
	/**
	 * The key to use when creating/reading JSON for the application's build.
	 */
	public static final String JSON_KEY_APPLICATION_BUILD = "application_build";
	/**
	 * The key to use when creating/reading JSON for the default survey 
	 * response privacy state for newly uploaded survey responses.
	 */
	public static final String JSON_KEY_DEFAULT_SURVEY_RESPONSE_PRIVACY_STATE = "default_survey_response_sharing_state";
	/**
	 * The key to use when creating/reading JSON for the list of all survey
	 * response privacy states.
	 */
	public static final String JSON_KEY_SURVEY_RESPONSE_PRIVACY_STATES = "survey_response_privacy_states";
	/**
	 * The key to use when creating/reading JSON for the server's default 
	 * campaign creation privilege.
	 */
	public static final String JSON_KEY_DEFAULT_CAMPAIGN_CREATION_PRIVILEGE = "default_campaign_creation_privilege";
	/**
	 * Whether or not Mobility is enabled on this server.
	 */
	public static final String JSON_KEY_MOBILITY_ENABLED = "mobility_enabled";
	/**
	 * The length of time for which an authentication token lives.
	 */
	public static final String JSON_KEY_AUTH_TOKEN_LIFETIME = "auth_token_lifetime";
	/**
	 * The maximum size of the request.
	 */
	public static final String JSON_KEY_MAXIMUM_REQUEST_SIZE = "maximum_request_size";
	/**
	 * The maximum size of a single parameter.
	 */
	public static final String JSON_KEY_MAXIMUM_PARAMETER_SIZE = "maximum_parameter_size";
	
	private final String appName;
	private final String appVersion;
	private final String appBuild;
	private final boolean defaultCampaignCreationPrivilege;
	private final SurveyResponse.PrivacyState defaultSurveyResponsePrivacyState;
	private final List<SurveyResponse.PrivacyState> surveyResponsePrivacyStates;
	private final boolean mobilityEnabled;
	private final long authTokenLifetime;
	private final long maxRequestSize;
	private final long maxParamSize;
	
	/**
	 * Creates a new server configuration.
	 * 
	 * @param appName The application's name.
	 * 
	 * @param appVersion The application's version.
	 * 
	 * @param appBuild The applications build.
	 * 
	 * @param defaultSurveyResponsePrivacyState The default survey response
	 * 											privacy state for newly 
	 * 											uploaded survey responses.
	 * 
	 * @param surveyResponsePrivacyStates A list of all of the survey response
	 * 									  privacy states.
	 * 
	 * @throws DomainException Thrown if any of the values are invalid or null.
	 */
	public ServerConfig(
			final String appName, 
			final String appVersion,
			final String appBuild, 
			final SurveyResponse.PrivacyState defaultSurveyResponsePrivacyState,
			final List<SurveyResponse.PrivacyState> surveyResponsePrivacyStates,
			final boolean defaultCampaignCreationPrivilege,
			final boolean mobilityEnabled,
			final long authTokenLifetime,
			final long maximumRequestSize,
			final long maximumParameterSize) 
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(appName)) {
			throw new DomainException(
					"The application name is null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(appVersion)) {
			throw new DomainException(
					"The application version is null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(appBuild)) {
			throw new DomainException(
					"The application build is null or whitespace only.");
		}
		else if(defaultSurveyResponsePrivacyState == null) {
			throw new DomainException(
					"The default survey response privacy state is null.");
		}
		else if(surveyResponsePrivacyStates == null) {
			throw new DomainException(
					"The list of default survey response privacy states is null.");
		}
		
		this.appName = appName;
		this.appVersion = appVersion;
		this.appBuild = appBuild;
		this.defaultCampaignCreationPrivilege = defaultCampaignCreationPrivilege;
		this.defaultSurveyResponsePrivacyState = defaultSurveyResponsePrivacyState;
		
		this.surveyResponsePrivacyStates = 
			new ArrayList<SurveyResponse.PrivacyState>(surveyResponsePrivacyStates);
		
		this.mobilityEnabled = mobilityEnabled;
		this.authTokenLifetime = authTokenLifetime;
		
		maxRequestSize = maximumRequestSize;
		maxParamSize = maximumParameterSize;
	}
	
	/**
	 * Creates a new server configuration from a JSONObject.
	 * 
	 * @param serverConfigAsJson The information about the server as a
	 * 							 JSONObject.
	 * 
	 * @throws DomainException Thrown if the JSONObject is null or if it is 
	 * 						   missing any of the required keys.
	 */
	public ServerConfig(
			final JSONObject serverConfigAsJson) 
			throws DomainException {
		
		if(serverConfigAsJson == null) {
			throw new DomainException(
					"The server configuration JSON is null.");
		}
		
		try {
			appName = serverConfigAsJson.getString(JSON_KEY_APPLICATION_NAME);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The application name was missing from the JSON.", 
					e);
		}
		
		try {
			appVersion = serverConfigAsJson.getString(JSON_KEY_APPLICATION_VERSION);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The application version was missing from the JSON.", 
					e);
		}
		
		try {
			appBuild = serverConfigAsJson.getString(JSON_KEY_APPLICATION_BUILD);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The application name was missing from the JSON.", 
					e);
		}
		
		try {
			defaultCampaignCreationPrivilege =
				Boolean.valueOf(serverConfigAsJson.getString(JSON_KEY_DEFAULT_CAMPAIGN_CREATION_PRIVILEGE));
		}
		catch(JSONException e) {
			throw new DomainException(
					"The default campaign creation privilege was missing from the JSON.", 
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					"The default campaign creation privilege is not a valid boolean value.", 
					e);
		}
		
		try {
			defaultSurveyResponsePrivacyState = 
				SurveyResponse.PrivacyState.getValue(
						serverConfigAsJson.getString(JSON_KEY_DEFAULT_SURVEY_RESPONSE_PRIVACY_STATE)
					);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The application name was missing from the JSON.", 
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					"The default survey response privacy state is not a known survey response privacy state.", 
					e);
		}
		
		try {
			JSONArray surveyResponsePrivacyStatesJson = 
				serverConfigAsJson.getJSONArray(JSON_KEY_SURVEY_RESPONSE_PRIVACY_STATES);
			
			int numPrivacyStates = surveyResponsePrivacyStatesJson.length();
			surveyResponsePrivacyStates = 
				new ArrayList<SurveyResponse.PrivacyState>(numPrivacyStates);
			
			for(int i = 0; i < numPrivacyStates; i++) {
				surveyResponsePrivacyStates.add( 
						SurveyResponse.PrivacyState.getValue(
								surveyResponsePrivacyStatesJson.getString(i)
						)
				);
			}
		}
		catch(JSONException e) {
			throw new DomainException("The application name was missing from the JSON.", e);
		}
		
		try {
			mobilityEnabled = 
					serverConfigAsJson.getBoolean(JSON_KEY_MOBILITY_ENABLED);
		}
		catch(JSONException e) {
			throw new DomainException(
					"Whether or not Mobility is enabled is missing.", 
					e);
		}
		
		try {
			authTokenLifetime = 
					serverConfigAsJson.getLong(JSON_KEY_AUTH_TOKEN_LIFETIME);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The authentication token's lifetime is missing.",
					e);
		}
		
		try {
			maxRequestSize = 
					serverConfigAsJson.getLong(JSON_KEY_MAXIMUM_REQUEST_SIZE);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The maximum request size is missing.",
					e);
		}
		
		try {
			maxParamSize = 
					serverConfigAsJson.getLong(JSON_KEY_MAXIMUM_PARAMETER_SIZE);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The maximum parameter size is missing.",
					e);
		}
	}
	
	/**
	 * Returns the application's name.
	 * 
	 * @return The application's name.
	 */
	public final String getAppName() {
		return appName;
	}
	
	/**
	 * Returns the application's version.
	 * 
	 * @return The application's version.
	 */
	public final String getAppVersion() {
		return appVersion;
	}
	
	/**
	 * Returns the application's build.
	 * 
	 * @return The application's build.
	 */
	public final String getAppBuild() {
		return appBuild;
	}
	
	/**
	 * Returns the default campaign creation privilege.
	 * 
	 * @return The default campaign creation privilege.
	 */
	public final boolean getDefaultCampaignCreationPrivilege() {
		return defaultCampaignCreationPrivilege;
	}
	
	/**
	 * Returns the default survey response privacy state for newly uploaded
	 * survey responses.
	 * 
	 * @return The default survey response privacy state.
	 */
	public final SurveyResponse.PrivacyState getDefaultSurveyResponsePrivacyState() {
		return defaultSurveyResponsePrivacyState;
	}
	
	/**
	 * Returns an array of all of the survey response privacy states.
	 * 
	 * @return An array of all of the survey response privacy states.
	 */
	public final List<SurveyResponse.PrivacyState> getSurveyResponsePrivacyStates() {
		return new ArrayList<SurveyResponse.PrivacyState>(surveyResponsePrivacyStates);
	}
	
	/**
	 * Returns whether or not Mobility is enabled.
	 * 
	 * @return Whether or not mobility is enabled.
	 */
	public final boolean getMobilityEnabled() {
		return mobilityEnabled;
	}
	
	/**
	 * Returns the maximum lifetime of a token in milliseconds unless 
	 * refreshed.
	 * 
	 * @return The maximum lifetime of a token in milliseconds.
	 */
	public final long getAuthTokenLifetime() {
		return authTokenLifetime;
	}
	
	/**
	 * Returns the maximum allowed size of a request in bytes.
	 * 
	 * @return The maximum allowed size of a request in bytes.
	 */
	public final long getMaximumRequestSize() {
		return maxRequestSize;
	}
	
	/**
	 * Returns the maximum allowed size of a single parameter in bytes.
	 * 
	 * @return The maximum allowed size of a single parameter in bytes.
	 */
	public final long getMaximumParameterSize() {
		return maxParamSize;
	}
	
	/**
	 * Returns this server configuration as a JSONObject.
	 * 
	 * @return This server configuration as a JSONObject.
	 * 
	 * @throws JSONException Thrown if there is an error building the 
	 * 						 JSONObject.
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject result = new JSONObject();
		
		result.put(JSON_KEY_APPLICATION_NAME, appName);
		result.put(JSON_KEY_APPLICATION_VERSION, appVersion);
		result.put(JSON_KEY_APPLICATION_BUILD, appBuild);
		result.put(JSON_KEY_DEFAULT_CAMPAIGN_CREATION_PRIVILEGE, defaultCampaignCreationPrivilege);
		result.put(JSON_KEY_DEFAULT_SURVEY_RESPONSE_PRIVACY_STATE, defaultSurveyResponsePrivacyState);
		result.put(JSON_KEY_SURVEY_RESPONSE_PRIVACY_STATES, new JSONArray(surveyResponsePrivacyStates));
		result.put(JSON_KEY_MOBILITY_ENABLED, mobilityEnabled);
		result.put(JSON_KEY_AUTH_TOKEN_LIFETIME, authTokenLifetime);
		result.put(JSON_KEY_MAXIMUM_REQUEST_SIZE, maxRequestSize);
		result.put(JSON_KEY_MAXIMUM_PARAMETER_SIZE, maxParamSize);
		
		return result;
	}
}
