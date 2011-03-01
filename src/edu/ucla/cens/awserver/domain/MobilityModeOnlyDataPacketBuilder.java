package edu.ucla.cens.awserver.domain;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Builder of mobility mode-only data packets from JSONObjects.
 * 
 * @author selsky
 */
public class MobilityModeOnlyDataPacketBuilder extends AbstractDataPacketBuilder {

	public MobilityModeOnlyDataPacketBuilder() {
		
	}
	
	public MetadataDataPacket createDataPacketFrom(JSONObject source, AwRequest awRequest) {
		MobilityModeOnlyDataPacket packet = new MobilityModeOnlyDataPacket();
	
		createCommonFields(source, packet);
		packet.setMode(JsonUtils.getStringFromJsonObject(source, "mode"));
		
		return packet;
	}
}
