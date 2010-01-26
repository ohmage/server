package edu.ucla.cens.awserver.domain;

import org.json.JSONObject;

import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Builder of mobility mode-only data packets from JSONObjects.
 * 
 * @author selsky
 */
public class MobilityModeOnlyDataPacketBuilder extends AbstractDataPacketBuilder {

	public MobilityModeOnlyDataPacketBuilder() {
		
	}
	
	/**
	 * Assumes the data in the incoming object has already been validated. 
	 * 
	 * Performs some conversions on the incoming data to get it ready for database insertion.
	 * <ul>
	 * <li> Converts dates (date and time elements from the JSON message) to UTC using the timezone provided in the source object
	 * <li> Converts Double.NaN to null for latitude and longitude.
	 * </ul>
     *
	 * @throws IllegalArgumentException of the source object is not an instance of JSONObject
	 */
	public MetadataDataPacket createDataPacketFrom(JSONObject source) {
		MobilityModeOnlyDataPacket packet = new MobilityModeOnlyDataPacket();
	
		createCommonFields(source, packet);
		packet.setMode(JsonUtils.getStringFromJsonObject(source, "mode"));
		
		return packet;
	}
}
