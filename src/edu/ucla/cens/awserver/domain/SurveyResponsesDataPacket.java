package edu.ucla.cens.awserver.domain;

import java.util.List;

/**
 * Data packet implementation for the storage of survey responses: promptId-value pairs where the value is dependent on the 
 * promptType. The primary key for the storage of the survey response is also stored in this class.
 * 
 * @author selsky
 */
public class SurveyResponsesDataPacket implements DataPacket {
	private List<PromptResponseDataPacket> _responses;
	private int _surveyResponseKey = -1;
	
	public List<PromptResponseDataPacket> getResponses() {
		return _responses;
	}

	public void setResponses(List<PromptResponseDataPacket> responses) {
		_responses = responses;
	}
	
	public int getSurveyResponseKey() {
		return _surveyResponseKey;
	}

	public void setSurveyResponseKey(int surveyResponseKey) {
		_surveyResponseKey = surveyResponseKey;
	}

	@Override
	public String toString() {
		return "SurveyResponsesDataPacket [_responses=" + _responses
				+ ", _surveyResponseKey=" + _surveyResponseKey + "]";
	}	
}
