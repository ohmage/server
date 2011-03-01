package edu.ucla.cens.awserver.domain;


/**
 * Domain object representing the shared fields across all data packets.
 * 
 * @author selsky
 */
public abstract class MetadataDataPacket implements DataPacket {
	private String _date;
	private long _epochTime;
	private String _timezone;
	
	private String _locationStatus;
	private String _location; // a JSON string
	
	public String getDate() {
		return _date;
	}

	public void setDate(String date) {
		_date = date;
	}

	public long getEpochTime() {
		return _epochTime;
	}

	public void setEpochTime(long time) {
		_epochTime = time;
	}

	public String getTimezone() {
		return _timezone;
	}

	public void setTimezone(String timezone) {
		_timezone = timezone;
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
