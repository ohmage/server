package edu.ucla.cens.awserver.datatransfer;

import java.util.HashMap;
import java.util.Map;

import edu.ucla.cens.awserver.domain.User;

/**
 * The default implementation of the AwRequest interface. 
 * 
 * @author selsky
 */
public class AwRequestImpl implements AwRequest {
	private User _user;
	private Map<String, Object> _payload; // For now, generic Objects are stored in the Map even though most of the data in the Map 
	                                      // is of type String. It does not make sense to specialize until there are more features
	                                      // implemented. The more abstract the data in the payload is kept, the less impact it 
	                                      // will have on the API coupling with the rest of the code. Conversely, returning Object
	                                      // requires casting when specific data types are needed.
	private boolean _failedRequest;
	private String _failedRequestErrorMessage;
		
	/**
	 * Creates an instance of this class using a HashMap for the payload.
	 */
	public AwRequestImpl() {
		_payload = new HashMap<String, Object>();
	}
	
	public Object getAttribute(String name) {
	 	return _payload.get(name);
	}
	
	public User getUser() {
		return _user;
	}

	public void setAttribute(String name, Object value) {
		_payload.put(name, value);
	}

	public void setUser(User user) {
		_user = user;
	}
	
	public boolean isFailedRequest() {
		return _failedRequest; 
	}
	
	public void setFailedRequest(boolean failedRequest) {
		_failedRequest = failedRequest;
	}
	
	public String getFailedRequestErrorMessage() {
		return _failedRequestErrorMessage;
	}
	
	public void setFailedRequestErrorMessage(String message) {
		_failedRequestErrorMessage = message;
	}
}
