package edu.ucla.cens.awserver.domain;

/**
 * @author selsky
 */
public class MobilityQueryResult {
	private Object _value;
	private String _timestamp;
	private String _timezone;
	private String _utcTimestamp;
	private String _locationStatus;
	private String _location;
	
	public Object getValue() {
		return _value;
	}
	public void setValue(Object value) {
		_value = value;
	}
	public String getTimestamp() {
		return _timestamp;
	}
	public void setTimestamp(String timestamp) {
		_timestamp = timestamp;
	}
	public String getTimezone() {
		return _timezone;
	}
	public void setTimezone(String timezone) {
		_timezone = timezone;
	}
	public String getUtcTimestamp() {
		return _utcTimestamp;
	}
	public void setUtcTimestamp(String utcTimestamp) {
		_utcTimestamp = utcTimestamp;
	}
	public String getLocationStatus() {
		return _locationStatus;
	}
	public void setLocationStatus(String locationStatus) {
		_locationStatus = locationStatus;
	}
	public String getLocation() {
		return _location;
	}
	public void setLocation(String location) {
		_location = location;
	}
}
