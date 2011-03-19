package edu.ucla.cens.awserver.domain;

import java.util.Map;

/**
 * Helper class for converting rows of db output to columns.
 *  
 * @author selsky
 */
public class NewDataPointQueryFormattedResult {
	
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
	
	// Prompt data -----
	private Map<String, PromptOutput> _promptOutputMap;

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

	public Map<String, PromptOutput> getPromptOutputMap() {
		return _promptOutputMap;
	}

	public void setPromptOutputMap(Map<String, PromptOutput> promptOutputMap) {
		_promptOutputMap = promptOutputMap;
	}

	@Override
	public String toString() {
		return "NewDataPointQueryFormattedResult [_accuracy=" + _accuracy
				+ ", _client=" + _client + ", _latitude=" + _latitude
				+ ", _launchContext=" + _launchContext + ", _locationStatus="
				+ _locationStatus + ", _locationTimestamp="
				+ _locationTimestamp + ", _loginId=" + _loginId
				+ ", _longitude=" + _longitude + ", _promptResponseMap="
				+ _promptOutputMap + ", _provider=" + _provider
				+ ", _repeatableSetId=" + _repeatableSetId
				+ ", _repeatableSetIteration=" + _repeatableSetIteration
				+ ", _surveyId=" + _surveyId + ", _timestamp=" + _timestamp
				+ ", _timezone=" + _timezone + ", _utcTimestamp="
				+ _utcTimestamp + "]";
	}
}
