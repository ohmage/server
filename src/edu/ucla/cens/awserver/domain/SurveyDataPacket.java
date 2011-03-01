package edu.ucla.cens.awserver.domain;

import java.util.List;


/**
 * Data packet implementation for the storage of a survey response: the metadata associated with a survey, the entire survey
 * itself as a String representation of JSON, and each prompt response.
 * 
 * @author selsky
 */
public class SurveyDataPacket implements DataPacket {
	// fields shared with mobility
	private String _date;
	private long _epochTime;
	private String _timezone;
	private String _locationStatus;
	private String _location; // a JSON string
	private String _survey;
	private String _surveyId;
	private String _launchContext; // a JSON string
	private List<PromptResponseDataPacket> _responses;
//	private int _surveyResponseKey = -1;

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
	
	public String getDate() {
		return _date;
	}

	public void setDate(String date) {
		_date = date;
	}

	public long getEpochTime() {
		return _epochTime;
	}

	public void setEpochTime(long time) {
		_epochTime = time;
	}

	public String getTimezone() {
		return _timezone;
	}

	public void setTimezone(String timezone) {
		_timezone = timezone;
	}
	
	public String getLaunchContext() {
		return _launchContext;
	}

	public void setLaunchContext(String launchContext) {
		_launchContext = launchContext;
	}

	public String getSurvey() {
		return _survey;
	}

	public void setSurvey(String survey) {
		_survey = survey;
	}

	public String getSurveyId() {
		return _surveyId;
	}

	public void setSurveyId(String surveyId) {
		_surveyId = surveyId;
	}
	
	public List<PromptResponseDataPacket> getResponses() {
		return _responses;
	}

	public void setResponses(List<PromptResponseDataPacket> responses) {
		_responses = responses;
	}

	@Override
	public String toString() {
		return "SurveyDataPacket [_date=" + _date + ", _epochTime="
				+ _epochTime + ", _launchContext=" + _launchContext
				+ ", _location=" + _location + ", _locationStatus="
				+ _locationStatus + ", _responses=" + _responses + ", _survey="
				+ _survey + ", _surveyId=" + _surveyId + ", _timezone="
				+ _timezone + "]";
	}
	
//	public int getSurveyResponseKey() {
//		return _surveyResponseKey;
//	}
//
//	public void setSurveyResponseKey(int surveyResponseKey) {
//		_surveyResponseKey = surveyResponseKey;
//	}
	
}
