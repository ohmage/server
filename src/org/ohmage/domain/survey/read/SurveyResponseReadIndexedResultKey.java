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

/**
 * Key for determining whether a prompt response in a SurveyResponseReadResult belongs to a particular SurveyResponseReadIndexedResult.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseReadIndexedResultKey {

	private String _username;
	private String _timestamp;
	private Long _epochMillis;
	private String _surveyId;
	private String _repeatableSetId;
	private Integer _repeatableSetIteration;

	public SurveyResponseReadIndexedResultKey(String username, String timestamp, Long epochMillis, String surveyId, String repeatableSetId, Integer repeatableSetIteration) {
		if(null == username) {
			throw new IllegalArgumentException("a username is required");
		}
		if(null == epochMillis) {
			throw new IllegalArgumentException("epochMillis is required");
		}
		if(null == timestamp) {
			throw new IllegalArgumentException("timestamp is required");
		}
		if(null == surveyId) {
			throw new IllegalArgumentException("a surveyId is required");
		}
		
		_username = username;
		_epochMillis = epochMillis;
		_timestamp = timestamp;
		_surveyId = surveyId;
		_repeatableSetId = repeatableSetId;
		_repeatableSetIteration = repeatableSetIteration;
	}
	
	public String getUsername() {
		return _username;
	}
	
	public String getTimestamp() {
		return _timestamp;
	}
	
	public String getSurveyId() {
		return _surveyId;
	}
	
	public String getRepeatableSetId() {
		return _repeatableSetId;
	}
	
	public Integer getRepeatableSetIteration() {
		return _repeatableSetIteration;
	}

	public boolean keysAreEqual(String username, String timestamp, Long epochMillis, String surveyId, String repeatableSetId, Integer repeatableSetIteration) {
		if(null == username) {
			throw new IllegalArgumentException("username cannot be null");
		}
		if(null == timestamp) {
			throw new IllegalArgumentException("timestamp cannot be null");
		}
		if(null == surveyId) {
			throw new IllegalArgumentException("surveyId cannot be null");
		}
		
		return _username.equals(username) && _timestamp.equals(timestamp) && _epochMillis.equals(epochMillis) && _surveyId.equals(surveyId) 
			&& (null == _repeatableSetId ? null == repeatableSetId : _repeatableSetId.equals(repeatableSetId))
			&& (null == _repeatableSetIteration ? null == repeatableSetIteration : _repeatableSetIteration.equals(repeatableSetIteration));
	}

	@Override
	public String toString() {
		return "SurveyResponseReadIndexedResultKey [_username=" + _username
				+ ", _timestamp=" + _timestamp + ", _epochMillis="
				+ _epochMillis + ", _surveyId=" + _surveyId
				+ ", _repeatableSetId=" + _repeatableSetId
				+ ", _repeatableSetIteration=" + _repeatableSetIteration + "]";
	}
}
