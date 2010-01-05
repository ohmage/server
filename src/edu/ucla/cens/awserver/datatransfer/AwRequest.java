package edu.ucla.cens.awserver.datatransfer;

import java.util.Map;

import edu.ucla.cens.awserver.domain.User;

/**
 * Container for transferring data throughout application layers. Instances of classes which implement this inferface are 
 * instended to be request-scoped (i.e., they are scoped to a unique JVM Thread based on a user action). 
 * 
 * TODO this needs a better name and a better package name as well
 * 
 * @author selsky
 */
public interface AwRequest {

	public User getUser();
	public void setUser(User user);
	
	// TODO the payload should not be accessed directly (exposing the underlying storage mechanism) 
	// there shoud be getAttribute() and setAttribute() 
	public Map<String, Object> getPayload();
	public void setPayload(Map<String, Object> payload);
}
