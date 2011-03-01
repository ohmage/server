package edu.ucla.cens.awserver.domain;

/**
 * @author selsky
 */
public class DataPointFunctionQueryResult {
	private String _locationStatus;
	private String _location;
	private String _timestamp;
	private String _timezone;
	private String _utcTimestamp;
	private String _value;
	
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

	public String getValue() {
		return _value;
	}

	public void setValue(String value) {
		_value = value;
	}

	@Override
	public String toString() {
		return "DataPointFunctionQueryResult [_location=" + _location
				+ ", _locationStatus=" + _locationStatus + ", _timestamp="
				+ _timestamp + ", _timezone=" + _timezone + ", _utcTimestamp="
				+ _utcTimestamp + ", _value=" + _value + "]";
	}
}
