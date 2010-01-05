package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Handler for pushing inbound Java EE parameters into the internal AwRequest. Helps to decouple AW logic from Java EE. 
 * 
 * @author selsky
 */
public interface AwRequestCreator {
	
	public AwRequest createFrom(HttpServletRequest request);
	
}
