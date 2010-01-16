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
	private String _failedRequestErrorMessage;  // a general error message for logging or a specific JSON message, depending on the
	                                            // context in which this class is used
	
//	private String _failedRequestErrorCode;     // an error code specific to JSON output
		
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
	
	/**
	 * If a value already exists for <code>name</code>, it is simply overwritten with the new value.
	 */
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
	
//	public String getFailedRequestErrorCode() {
//		return _failedRequestErrorCode;
//	}
//	
//	public void setFailedRequestErrorCode(String errorCode) {
//		_failedRequestErrorCode = errorCode;
//	}
}
