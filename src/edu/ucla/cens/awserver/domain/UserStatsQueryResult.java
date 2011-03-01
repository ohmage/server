package edu.ucla.cens.awserver.domain;

/**
 * Data transfer object for returning results from multiple user stat queries.
 * 
 * @author selsky
 */
public class UserStatsQueryResult {
	private Long _mostRecentSurveyUploadTime;
	private Long _mostRecentMobilityUploadTime;
	private Double _surveyLocationUpdatesPercentage;
	private Double _mobilityLocationUpdatesPercentage;
	
	public Long getMostRecentSurveyUploadTime() {
		return _mostRecentSurveyUploadTime;
	}
	
	public void setMostRecentSurveyUploadTime(Long time) {
		_mostRecentSurveyUploadTime = time;
	}
	
	public Long getMostRecentMobilityUploadTime() {
		return _mostRecentMobilityUploadTime;
	}
	
	public void setMostRecentMobilityUploadTime(Long time) {
		_mostRecentMobilityUploadTime = time;
	}

	public Double getSurveyLocationUpdatesPercentage() {
		return _surveyLocationUpdatesPercentage;
	}

	public void setSurveyLocationUpdatesPercentage(Double surveyLocationUpdatesPercentage) {
		_surveyLocationUpdatesPercentage = surveyLocationUpdatesPercentage;
	}

	public Double getMobilityLocationUpdatesPercentage() {
		return _mobilityLocationUpdatesPercentage;
	}

	public void setMobilityLocationUpdatesPercentage(Double mobilityLocationUpdatesPercentage) {
		_mobilityLocationUpdatesPercentage = mobilityLocationUpdatesPercentage;
	}

	@Override
	public String toString() {
		return "UserStatsQueryResult [_mostRecentMobilityUploadTime="
				+ _mostRecentMobilityUploadTime
				+ ", _mobilityLocationUpdatesPercentage="
				+ _mobilityLocationUpdatesPercentage
				+ ", _mostRecentSurveyUploadTime=" + _mostRecentSurveyUploadTime
				+ ", _surveyLocationUpdatesPercentage="
				+ _surveyLocationUpdatesPercentage + "]";
	}
}
