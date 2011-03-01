package edu.ucla.cens.awserver.domain;

/**
 * @author selsky
 */
public class ChunkedMobilityQueryResult extends MobilityQueryResult {
	private long _duration;
	
	public long getDuration() {
		return _duration;
	}
	public void setDuration(long duration) {
		_duration = duration;
	}	
}
