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

import java.util.List;

import edu.ucla.cens.mobilityclassifier.Sample;

/**
 * Domain object representing a mobility sensor_data data packet.
 * 
 * @author selsky
 */
public class MobilitySensorDataPacket extends MobilityModeOnlyDataPacket {
	private String _sensorDataString;
	
	// Classifier input from inbound sensor data
	private Double _speed;
	private List<Sample> _samples;
	
	// Classifier output
//	private List<Double> _n95Fft;
//	private List<Double> _fft;
//	private Double _n95Variance;
//	private Double _variance;
//	private Double _average;
	private String _features; // n95Fft, fft, n95Variance, variance, average, mode 
	private String _classifierMode;
	private String _classifierVersion;
	
	public String getSensorDataString() {
		return _sensorDataString;
	}

	public void setSensorDataString(String sensorDataString) {
		_sensorDataString = sensorDataString;
	}

	public String getClassifierVersion() {
		return _classifierVersion;
	}

	public void setClassifierVersion(String classifierVersion) {
		_classifierVersion = classifierVersion;
	}

	public List<Sample> getSamples() {
		return _samples;
	}

	public void setSamples(List<Sample> samples) {
		_samples = samples;
	}
	
	public Double getSpeed() {
		return _speed;
	}

	public void setSpeed(Double speed) {
		_speed = speed;
	}

	public String getClassifierMode() {
		return _classifierMode;
	}

	public void setClassifierMode(String classifierMode) {
		_classifierMode = classifierMode;
	}
	
	public String getFeatures() {
		return _features;
	}

	public void setFeatures(String features) {
		_features = features;
	}
}
