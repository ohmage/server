package edu.ucla.cens.awserver.domain;

import java.sql.Timestamp;

/**
 * Data transfer object for returning the most recent time since some activity occurred.
 * 
 * @author selsky
 */
public class MostRecentActivityQueryResult {
	private String _userName;
	private Timestamp _timestamp;
	private double _value;
    private String _timezone;
	
	public String getUserName() {
		return _userName;
	}
	
	public void setUserName(String userName) {
		_userName = userName;
	}
	
	public Timestamp getTimestamp() {
		return _timestamp;
	}
	
	public void setTimestamp(Timestamp timestamp) {
		_timestamp = timestamp;
	}

	public double getValue() {
		return _value;
	}

	public void setValue(double value) {
		_value = value;
	}

	public String getTimezone() {
		return _timezone;
	}

	public void setTimezone(String timezone) {
		_timezone = timezone;
	}	
}
