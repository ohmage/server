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
package org.ohmage.domain.survey.read;

import java.util.Map;

import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.domain.configuration.PromptProperty;

/**
 * Bean-style wrapper for query results for the /app/survey_response/read API call.
 * 
 * TODO -- make this class immutable
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseReadResult {
	private Object _response;
	private Integer _repeatableSetIteration;
	private String _locationStatus;
	private String _location;
	private String _timestamp;
	private Long _epochMillis;
	private String _timezone;
	private String _surveyId;
	private String _promptId;
	private String _promptType;
	private String _repeatableSetId;
	private String _displayLabel;
	private Object _displayValue;
	private Object _singleChoiceOrdinalValue;
	private String _singleChoiceLabel; 
	private String _unit;
	private String _displayType;
	private String _utcTimestamp;
	private String _client;
	private String _launchContext;
	private String _surveyTitle;
	private String _surveyDescription;
	private String _username;
	private Map<String, PromptProperty> _choiceGlossary;
	private int _surveyPrimaryKeyId;
	private SurveyResponsePrivacyStateCache.PrivacyState _privacyState;
	private String _promptText;
	
	public String getPromptText() {
		return _promptText;
	}
	
	public void setPromptText(String promptText) {
		_promptText = promptText;
	}
	
	public SurveyResponsePrivacyStateCache.PrivacyState getPrivacyState() {
		return _privacyState;
	}
	
	public void setPrivacyState(SurveyResponsePrivacyStateCache.PrivacyState state) {
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
	
	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		_username = username;
	}

	public Object getSingleChoiceOrdinalValue() {
		return _singleChoiceOrdinalValue;
	}

	public void setSingleChoiceOrdinalValue(Object singleChoiceOrdinalValue) {
		_singleChoiceOrdinalValue = singleChoiceOrdinalValue;
	}
	
	public String getSingleChoiceLabel() {
		return _singleChoiceLabel;
	}

	public void setSingleChoiceLabel(String singleChoiceLabel) {
		_singleChoiceLabel = singleChoiceLabel;
	}

	public Map<String, PromptProperty> getChoiceGlossary() {
		return _choiceGlossary;
	}

	public void setChoiceGlossary(Map<String, PromptProperty> choiceGlossary) {
		_choiceGlossary = choiceGlossary;
	}
	
	public Long getEpochMillis() {
		return _epochMillis;
	}

	public void setEpochMillis(Long epochMillis) {
		_epochMillis = epochMillis;
	}
}
