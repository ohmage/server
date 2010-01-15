package edu.ucla.cens.awserver.datatransfer;


import edu.ucla.cens.awserver.domain.User;

/**
 * Container for transferring data throughout application layers. Instances of classes which implement this inferface are 
 * instended to be request-scoped (i.e., they are scoped to a unique JVM Thread most likely created by the container (app server)
 * and based on a user or device request to the server). 
 * 
 * @author selsky
 */
public interface AwRequest {
	
	public boolean isFailedRequest();
	public void setFailedRequest(boolean failedRequest);
		
	public String getFailedRequestErrorMessage();
	public void setFailedRequestErrorMessage(String message);
	
//	public String getFailedRequestErrorCode();
//	public void setFailedRequestErrorCode(String errorCode);
	
	public User getUser();
	public void setUser(User user);

	// TODO ? 
	// Add getters and setters for Boolean, Object, String, List types? This would avoid clients having to cast, but is the 
	// extra API complexity worth it? Would identical keys be allowed for objects of different types, etc, etc
	
	public Object getAttribute(String name);
	public void setAttribute(String name, Object value);
	
}
