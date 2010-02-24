package edu.ucla.cens.awserver.controller;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Controllers are the interface shown to the "outside world" (e.g., Servlets) for access to application features. 
 *
 * @author selsky
 */
public interface Controller {
	
	/**
	 * Executes feature-specific logic using the incoming request.
	 * 
	 * @param awRequest - feature parameters and user specific data 
	 */
	public void execute(AwRequest awRequest);
	
}
