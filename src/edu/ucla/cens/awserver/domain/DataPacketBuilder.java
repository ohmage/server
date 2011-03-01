package edu.ucla.cens.awserver.domain;

import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Use the builder pattern to create data packets.
 * 
 * @author selsky
 */
public interface DataPacketBuilder {
	
	public DataPacket createDataPacketFrom(JSONObject source, AwRequest awRequest);
	
}
