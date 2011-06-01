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
