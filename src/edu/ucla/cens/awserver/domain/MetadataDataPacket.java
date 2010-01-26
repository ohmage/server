package edu.ucla.cens.awserver.domain;


/**
 * Domain object representing the shared fields across all data packets.
 * 
 * @author selsky
 */
public abstract class MetadataDataPacket implements DataPacket {
	private String _utcDate;
	private long _utcTime;
	private String _timezone;
	
	private Double _latitude;
	private Double _longitude;
		
	public String getUtcDate() {
		return _utcDate;
	}

	public void setUtcDate(String date) {
		_utcDate = date;
	}

	public long getUtcTime() {
		return _utcTime;
	}

	public void setUtcTime(long time) {
		_utcTime = time;
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
		return "MetadataDataPacket [_utcDate=" + _utcDate + ", _latitude=" + _latitude
				+ ", _longitude=" + _longitude + ", _utcTime=" + _utcTime
				+ ", _timezone=" + _timezone + "]";
	}
}
