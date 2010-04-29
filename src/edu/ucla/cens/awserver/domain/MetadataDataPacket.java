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
	
	private Double _latitude;
	private Double _longitude;
		
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

	public Double getLatitude() {
		return _latitude;
	}

	public void setLatitude(Double latitude) {
		_latitude = latitude;
	}

	public Double getLongitude() {
		return _longitude;
	}

	public void setLongitude(Double longitude) {
		_longitude = longitude;
	}
	
	@Override
	public String toString() {
		return "MetadataDataPacket [_date=" + _date + ", _latitude=" + _latitude
				+ ", _longitude=" + _longitude + ", _epochTime=" + _epochTime
				+ ", _timezone=" + _timezone + "]";
	}
}
