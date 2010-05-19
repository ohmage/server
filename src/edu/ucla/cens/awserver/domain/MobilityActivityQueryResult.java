package edu.ucla.cens.awserver.domain;

import java.sql.Timestamp;

public class MobilityActivityQueryResult {
	private String _userName;
	private double _value;
	
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
