package edu.ucla.cens.awserver.domain;

/**
 * @author selsky
 */
public class EmaQueryResult {	
	private String _jsonData;
	private String _timezone;
	private String _timestamp;
	private double _latitude;
	private double _longitude;
	private int _promptConfigId;
	private int _promptGroupId;
	
	public double getLatitude() {
		return _latitude;
	}
	public void setLatitude(double latitude) {
		_latitude = latitude;
	}
	public double getLongitude() {
		return _longitude;
	}
	public void setLongitude(double longitude) {
		_longitude = longitude;
	}
	public int getPromptConfigId() {
		return _promptConfigId;
	}
	public void setPromptConfigId(int promptConfigId) {
		_promptConfigId = promptConfigId;
	}
	public int getPromptGroupId() {
		return _promptGroupId;
	}
	public void setPromptGroupId(int promptGroupId) {
		_promptGroupId = promptGroupId;
	}
	public String getJsonData() {
		return _jsonData;
	}
	public void setJsonData(String jsonData) {
		_jsonData = jsonData;
	}
	public String getTimezone() {
		return _timezone;
	}
	public void setTimezone(String timezone) {
		_timezone = timezone;
	}
	public String getTimestamp() {
		return _timestamp;
	}
	public void setTimestamp(String timestamp) {
		_timestamp = timestamp;
	}

}
