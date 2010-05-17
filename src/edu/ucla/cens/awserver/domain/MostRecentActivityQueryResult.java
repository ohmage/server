package edu.ucla.cens.awserver.domain;

import java.sql.Timestamp;

/**
 * Data transfer object for returning the time since either the most recent prompt response upload or most recent mobility upload
 * occurred. The value in this class represents that time in hours.
 * 
 * @author selsky
 */
public class MostRecentActivityQueryResult {
	private String _userName;
	private double _value;
	private String _maxFieldLabel;
	
	private Timestamp _promptResponseTimestamp;
	private String _promptTimezone;
	
	private Timestamp _mobilityTimestamp;
	private String _mobilityTimezone;
	
	public String getUserName() {
		return _userName;
	}
	
	public void setUserName(String userName) {
		_userName = userName;
	}

	public double getValue() {
		return _value;
	}

	public void setValue(double value) {
		_value = value;
	}

	public Timestamp getPromptResponseTimestamp() {
		return _promptResponseTimestamp;
	}

	public void setPromptResponseTimestamp(Timestamp promptResponseTimestamp) {
		_promptResponseTimestamp = promptResponseTimestamp;
	}

	public String getPromptTimezone() {
		return _promptTimezone;
	}

	public void setPromptTimezone(String promptTimezone) {
		_promptTimezone = promptTimezone;
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

	public String getMaxFieldLabel() {
		return _maxFieldLabel;
	}

	public void setMaxFieldLabel(String maxFieldLabel) {
		_maxFieldLabel = maxFieldLabel;
	}
}
