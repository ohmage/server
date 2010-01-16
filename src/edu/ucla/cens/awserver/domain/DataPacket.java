package edu.ucla.cens.awserver.domain;

import java.util.Date;

/**
 * Domain object representing the shared fields across all data packets.
 * 
 * @author selsky
 */
public abstract class DataPacket {
	private Date _date;
	private long _time;
	private String _timezone;
	private Double _latitude;
	private Double _longitude;
		
	public Date getDate() {
		return _date;
	}

	public void setDate(Date date) {
		_date = date;
	}

	public long getTime() {
		return _time;
	}

	public void setTime(long time) {
		_time = time;
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
}
