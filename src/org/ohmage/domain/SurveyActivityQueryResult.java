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

import java.sql.Timestamp;

public class SurveyActivityQueryResult {
	private double _value;
	private Timestamp _promptTimestamp;
	private String _promptTimezone;

	public double getValue() {
		return _value;
	}

	public void setValue(double value) {
		_value = value;
	}

	public Timestamp getPromptTimestamp() {
		return _promptTimestamp;
	}

	public void setPromptTimestamp(Timestamp promptTimestamp) {
		_promptTimestamp = promptTimestamp;
	}

	public String getPromptTimezone() {
		return _promptTimezone;
	}

	public void setPromptTimezone(String promptTimezone) {
		_promptTimezone = promptTimezone;
	}

	@Override
	public String toString() {
		return "SurveyActivityQueryResult [_promptTimestamp="
				+ _promptTimestamp + ", _promptTimezone=" + _promptTimezone
				+ ", _value=" + _value + "]";
	}
}
