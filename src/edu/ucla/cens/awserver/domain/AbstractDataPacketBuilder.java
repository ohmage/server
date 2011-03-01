package edu.ucla.cens.awserver.domain;

import org.json.JSONObject;

import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Abstract helper class for handling fields common to all packets.
 * 
 * @author selsky
 */
public abstract class AbstractDataPacketBuilder implements DataPacketBuilder {
	
	public void createCommonFields(JSONObject source, MetadataDataPacket packet) {
		String date = JsonUtils.getStringFromJsonObject(source, "date");
		long time = JsonUtils.getLongFromJsonObject(source, "time");
		String timezone = JsonUtils.getStringFromJsonObject(source, "timezone");
		String locationStatus = JsonUtils.getStringFromJsonObject(source, "location_status");
		JSONObject o = JsonUtils.getJsonObjectFromJsonObject(source, "location");
		String location = null;
		if(null != o) {
			location = o.toString();
		}
		packet.setDate(date);
		packet.setEpochTime(time);
		packet.setTimezone(timezone);
		packet.setLocationStatus(locationStatus);
		packet.setLocation(location);
	}
}
