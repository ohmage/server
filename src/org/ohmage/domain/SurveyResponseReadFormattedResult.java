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
package org.ohmage.domain;

import java.util.Map;

/**
 * Helper class for converting rows of db output to columns.
 *  
 * @author selsky
 */
public class SurveyResponseReadFormattedResult {
	
	// Metadata --------
	private String _client;
	
	// Survey Location
	private String _locationStatus;
	private Double _latitude;
	private Double _longitude;
	private Double _accuracy;
	private String _provider;
	private String _locationTimestamp;

	// Trigger info
	private String _launchContext; // TODO split apart into constituent data items
	
	// Sorted by ...
	private String _loginId; // is there a need for an anonymized login id?
	private String _timestamp;
	private String _timezone;
	private String _utcTimestamp;
	private String _surveyId;
	private String _repeatableSetId;
	private Integer _repeatableSetIteration;
	// ---------
	
	private String _surveyTitle;
	private String _surveyDescription;
	private String _privacyState;
	
	// Prompt data -----
	private Map<String, Object> _promptDisplayValueMap;

	public String getClient() {
		return _client;
	}

	public void setClient(String client) {
		_client = client;
	}

	public String getLocationStatus() {
		return _locationStatus;
	}

	public void setLocationStatus(String locationStatus) {
		_locationStatus = locationStatus;
	}

	public Double getLatitude() {
		return _latitude;
	}

	public void setLatitude(Double latitude) {
		_latitude = latitude;
	}

	public Double getLongitude() {
		return _longitude;
	}

	public void setLongitude(Double longitude) {
		_longitude = longitude;
	}

	public Double getAccuracy() {
		return _accuracy;
	}

	public void setAccuracy(Double accuracy) {
		_accuracy = accuracy;
	}

	public String getProvider() {
		return _provider;
	}

	public void setProvider(String provider) {
		_provider = provider;
	}

	public String getLocationTimestamp() {
		return _locationTimestamp;
	}

	public void setLocationTimestamp(String locationTimestamp) {
		_locationTimestamp = locationTimestamp;
	}

	public String getLaunchContext() {
		return _launchContext;
	}

	public void setLaunchContext(String launchContext) {
		_launchContext = launchContext;
	}

	public String getLoginId() {
		return _loginId;
	}

	public void setLoginId(String loginId) {
		_loginId = loginId;
	}

	public String getTimestamp() {
		return _timestamp;
	}

	public void setTimestamp(String timestamp) {
		_timestamp = timestamp;
	}

	public String getTimezone() {
		return _timezone;
	}

	public void setTimezone(String timezone) {
		_timezone = timezone;
	}

	public String getUtcTimestamp() {
		return _utcTimestamp;
	}

	public void setUtcTimestamp(String utcTimestamp) {
		_utcTimestamp = utcTimestamp;
	}

	public String getSurveyId() {
		return _surveyId;
	}

	public void setSurveyId(String surveyId) {
		_surveyId = surveyId;
	}

	public String getRepeatableSetId() {
		return _repeatableSetId;
	}

	public void setRepeatableSetId(String repeatableSetId) {
		_repeatableSetId = repeatableSetId;
	}

	public Integer getRepeatableSetIteration() {
		return _repeatableSetIteration;
	}

	public void setRepeatableSetIteration(Integer repeatableSetIteration) {
		_repeatableSetIteration = repeatableSetIteration;
	}

	public Map<String,Object> getPromptDisplayValueMap() {
		return _promptDisplayValueMap;
	}
	
	public void setPromptDisplayValueMap(Map<String,Object> value) {
		_promptDisplayValueMap = value;
	}
	
	public String getSurveyTitle() {
		return _surveyTitle;
	}

	public void setSurveyTitle(String surveyTitle) {
		_surveyTitle = surveyTitle;
	}

	public String getSurveyDescription() {
		return _surveyDescription;
	}

	public void setSurveyDescription(String surveyDescription) {
		_surveyDescription = surveyDescription;
	}
	
	public String getPrivacyState() {
		return _privacyState;
	}

	public void setPrivacyState(String privacyState) {
		_privacyState = privacyState;
	}

	@Override
	public String toString() {
		return "SurveyResponseReadFormattedResult [_client=" + _client
				+ ", _locationStatus=" + _locationStatus + ", _latitude="
				+ _latitude + ", _longitude=" + _longitude + ", _accuracy="
				+ _accuracy + ", _provider=" + _provider
				+ ", _locationTimestamp=" + _locationTimestamp
				+ ", _launchContext=" + _launchContext + ", _loginId="
				+ _loginId + ", _timestamp=" + _timestamp + ", _timezone="
				+ _timezone + ", _utcTimestamp=" + _utcTimestamp
				+ ", _surveyId=" + _surveyId + ", _repeatableSetId="
				+ _repeatableSetId + ", _repeatableSetIteration="
				+ _repeatableSetIteration + ", _surveyTitle=" + _surveyTitle
				+ ", _surveyDescription=" + _surveyDescription
				+ ", _privacyState=" + _privacyState
				+ ", _promptDisplayValueMap=" + _promptDisplayValueMap + "]";
	}	
}
