package edu.ucla.cens.awserver.domain;

/**
 * Domain object representing a mobility mode_only JSON data packet.
 * 
 * @author selsky
 */
public class MobilityModeOnlyDataPacket extends DataPacket {
	private String _mode;
	
	public MobilityModeOnlyDataPacket() {
		
	}

	public String getMode() {
		return _mode;
	}

	public void setMode(String mode) {
		_mode = mode;
	}
	
}
