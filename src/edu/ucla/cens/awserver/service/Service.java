package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * A service performs a discrete piece of application logic.
 * 
 * @author selsky
 */
public interface Service {
	
	public void execute(AwRequest awRequest);
	
}
