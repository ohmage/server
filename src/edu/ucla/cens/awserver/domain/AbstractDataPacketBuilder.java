package edu.ucla.cens.awserver.domain;

import org.json.JSONObject;

import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Abstract helper class for handling fields common to all packets.
 * 
 * @author selsky
 */
public abstract class AbstractDataPacketBuilder implements DataPacketBuilder {
	
	/**
	 * Sets the values for the fields common to all packets: date, time (millis since epoch), timezone, latitude, longitude.  
	 */
	public void createCommonFields(JSONObject source, MetadataDataPacket packet) {
		String date = JsonUtils.getStringFromJsonObject(source, "date");
		long time = JsonUtils.getLongFromJsonObject(source, "time");
		String timezone = JsonUtils.getStringFromJsonObject(source, "timezone");
		JSONObject location = JsonUtils.getJsonObjectFromJsonObject(source, "location");
		Double latitude = checkForDoubleNaN(location, "latitude");
		Double longitude = checkForDoubleNaN(location, "longitude");
		
//		String stringUtcDate = DateUtils.convertDateToUtc(date, timezone);
//		long utcTime = DateUtils.convertTimeToUtc(time, timezone);
//		packet.setDate(stringUtcDate);
//		packet.setEpochTime(utcTime);
		
		packet.setDate(date);
		packet.setEpochTime(time);
		packet.setTimezone(timezone);
		packet.setLatitude(latitude);
		packet.setLongitude(longitude);
	}
	
	private Double checkForDoubleNaN(JSONObject source, String key) {
		// latitude and longtidue may be sent with the string value "Double.NaN"
		Double value = JsonUtils.getDoubleFromJsonObject(source, key);
		
		return null == value? Double.NaN : value;
	}
}
