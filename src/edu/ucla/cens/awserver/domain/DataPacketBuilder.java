package edu.ucla.cens.awserver.domain;

/**
 * Use the builder pattern to create data packets.
 * 
 * @author selsky
 */
public interface DataPacketBuilder {
	
	public DataPacket createDataPacketFrom(Object source);
	
}
