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

public class MobilityActivityQueryResult {
	private double _value;
	private Timestamp _mobilityTimestamp;
	private String _mobilityTimezone;

	public double getValue() {
		return _value;
	}

	public void setValue(double value) {
		_value = value;
	}

	public Timestamp getMobilityTimestamp() {
		return _mobilityTimestamp;
	}

	public void setMobilityTimestamp(Timestamp mobilityTimestamp) {
		_mobilityTimestamp = mobilityTimestamp;
	}

	public String getMobilityTimezone() {
		return _mobilityTimezone;
	}

	public void setMobilityTimezone(String mobilityTimezone) {
		_mobilityTimezone = mobilityTimezone;
	}
}
