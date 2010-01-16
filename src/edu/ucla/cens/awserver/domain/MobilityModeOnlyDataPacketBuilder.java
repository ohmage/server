package edu.ucla.cens.awserver.domain;

import org.json.JSONObject;

/**
 * Builder of mobility mode-only data packets from JSONObjects.
 * 
 * TODO need a base class to build all of the common fields
 * 
 * @author selsky
 */
public class MobilityModeOnlyDataPacketBuilder implements DataPacketBuilder {

	public MobilityModeOnlyDataPacketBuilder() {
		
	}
	
	/**
	 * Assumes the data in the incoming object has already been validated. 
	 * 
	 * Performs some conversions on the incoming data to get it ready for database insertion.
	 * <ul>
	 * <li> Converts dates to UTC using the timezone provided in the source object
	 * <li> Converts Double.NaN to null for latitude and longitude
	 * <li> TODO - does anything else belong in this list?
	 * </ul>
     *
	 * @throws IllegalArgumentException of the source object is not an instance of JSONObject
	 */
	public DataPacket createDataPacketFrom(Object source) {
		if(! (source instanceof JSONObject)) {
			throw new IllegalArgumentException("source object is not an instance of JSONObject");
		}
		
		JSONObject json = (JSONObject) source;		
		DataPacket packet = new MobilityModeOnlyDataPacket();
		
		// TODO
		
		
		
		return packet;
	}

}
