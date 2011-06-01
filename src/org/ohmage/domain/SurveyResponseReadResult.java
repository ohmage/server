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
 * Cf. DataPointQueryResult except this domain class handles results for multiple users and additional properties from 
 * XML configurations.
 * 
 * @author selsky
 */
public class SurveyResponseReadResult {
	private Object _response;
	private Integer _repeatableSetIteration;
	private String _locationStatus;
	private String _location;
	private String _timestamp;
	private String _timezone;
	private String _surveyId;
	private String _promptId;
	private String _promptType;
	private String _repeatableSetId;
	private String _displayLabel;
	private Object _displayValue;
	private String _unit;
	private String _displayType;
	private String _utcTimestamp;
	private String _client;
	private String _launchContext;
	private String _surveyTitle;
	private String _surveyDescription;
	private String _loginId;
	private Map<String, PromptProperty> _choiceGlossary;
	private int _surveyPrimaryKeyId;
	private String _privacyState;
	private String _promptText;
	
	public String getPromptText() {
		return _promptText;
	}
	
	public void setPromptText(String promptText) {
		_promptText = promptText;
	}
	
	public String getPrivacyState() {
		return _privacyState;
	}
	
	public void setPrivacyState(String state) {
		_privacyState = state;
	}
	
	public int getSurveyPrimaryKeyId() {
		return _surveyPrimaryKeyId;
	}
	
	public void setSurveyPrimaryKeyId(int id) {
		_surveyPrimaryKeyId = id;
	}
	
	public String getLocationStatus() {
		return _locationStatus;
	}

	public void setLocationStatus(String locationStatus) {
		_locationStatus = locationStatus;
	}

	public String getLocation() {
		return _location;
	}

	public void setLocation(String location) {
		_location = location;
	}

	public String getDisplayType() {
		return _displayType;
	}

	public void setDisplayType(String displayType) {
		_displayType = displayType;
	}

	public Object getResponse() {
		return _response;
	}

	public void setResponse(Object response) {
		_response = response;
	}
	
	public Integer getRepeatableSetIteration() {
		return _repeatableSetIteration;
	}
	
	public void setRepeatableSetIteration(Integer repeatableSetIteration) {
		_repeatableSetIteration = repeatableSetIteration;
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
	
	public String getPromptId() {
		return _promptId;
	}
	
	public void setPromptId(String promptId) {
		_promptId = promptId;
	}
	
	public String getPromptType() {
		return _promptType;
	}
	
	public void setPromptType(String promptType) {
		_promptType = promptType;
	}
	
	public String getRepeatableSetId() {
		return _repeatableSetId;
	}
	
	public void setRepeatableSetId(String repeatableSetId) {
		_repeatableSetId = repeatableSetId;
	}

	public boolean isRepeatableSetResult() {
		return null != _repeatableSetId;
	}
	
	public String getSurveyId() {
		return _surveyId;
	}
	
	public void setSurveyId(String surveyId) {
		_surveyId = surveyId;
	}
	
	public String getUnit() {
		return _unit;
	}

	public void setUnit(String unit) {
		_unit = unit;
	}
	
	public String getDisplayLabel() {
		return _displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		_displayLabel = displayLabel;
	}

	public Object getDisplayValue() {
		return _displayValue;
	}

	public void setDisplayValue(Object displayValue) {
		_displayValue = displayValue;
	}

	public String getUtcTimestamp() {
		return _utcTimestamp;
	}

	public void setUtcTimestamp(String utcTimestamp) {
		_utcTimestamp = utcTimestamp;
	}

	public String getClient() {
		return _client;
	}

	public void setClient(String client) {
		_client = client;
	}

	public String getLaunchContext() {
		return _launchContext;
	}

	public void setLaunchContext(String launchContext) {
		_launchContext = launchContext;
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
	
	public String getLoginId() {
		return _loginId;
	}

	public void setLoginId(String loginId) {
		_loginId = loginId;
	}
	
	public Map<String, PromptProperty> getChoiceGlossary() {
		return _choiceGlossary;
	}

	public void setChoiceGlossary(Map<String, PromptProperty> choiceGlossary) {
		_choiceGlossary = choiceGlossary;
	}

	@Override
	public String toString() {
		return "SurveyResponseReadResult [_response=" + _response
				+ ", _repeatableSetIteration=" + _repeatableSetIteration
				+ ", _locationStatus=" + _locationStatus + ", _location="
				+ _location + ", _timestamp=" + _timestamp + ", _timezone="
				+ _timezone + ", _surveyId=" + _surveyId + ", _promptId="
				+ _promptId + ", _promptType=" + _promptType
				+ ", _repeatableSetId=" + _repeatableSetId + ", _displayLabel="
				+ _displayLabel + ", _displayValue=" + _displayValue
				+ ", _unit=" + _unit + ", _displayType=" + _displayType
				+ ", _utcTimestamp=" + _utcTimestamp + ", _client=" + _client
				+ ", _launchContext=" + _launchContext + ", _surveyTitle="
				+ _surveyTitle + ", _surveyDescription=" + _surveyDescription
				+ ", _loginId=" + _loginId + ", _choiceGlossary="
				+ _choiceGlossary + ", _surveyPrimaryKeyId="
				+ _surveyPrimaryKeyId + ", _privacyState=" + _privacyState
				+ ", _promptText=" + _promptText + "]";
	}	
}
