package edu.ucla.cens.awserver.domain;

import org.json.JSONObject;

import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Builder of mobility mode-only data packets from JSONObjects.
 * 
 * @author selsky
 */
public class MobilityModeFeaturesDataPacketBuilder extends AbstractDataPacketBuilder {

	public MobilityModeFeaturesDataPacketBuilder() {
		
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
		MobilityModeFeaturesDataPacket packet = new MobilityModeFeaturesDataPacket();
	
		createCommonFields(source, packet);
		
		JSONObject featuresObject = JsonUtils.getJsonObjectFromJsonObject(source, "features");
		packet.setMode(JsonUtils.getStringFromJsonObject(featuresObject, "mode"));
		packet.setSpeed(JsonUtils.getDoubleFromJsonObject(featuresObject, "speed"));
		packet.setVariance(JsonUtils.getDoubleFromJsonObject(featuresObject, "variance"));
		packet.setAverage(JsonUtils.getDoubleFromJsonObject(featuresObject, "average"));
		
		String fftArrayAsString = JsonUtils.getJsonArrayFromJsonObject(featuresObject, "fft").toString();
		packet.setFftArray(fftArrayAsString);
		
		return packet;
	}
}
