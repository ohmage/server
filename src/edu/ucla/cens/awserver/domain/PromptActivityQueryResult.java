package edu.ucla.cens.awserver.domain;

import java.sql.Timestamp;

public class PromptActivityQueryResult {
	private String _userName;
	private double _value;
	
	private Timestamp _promptTimestamp;
	private String _promptTimezone;
	
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
}
