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
	private Map<String, Object> _payload; // for now generic Objects are stored in the Map 
	                                 // even though most of the data in the Map is of type String 
	                                 // It does not make sense to specialise until there are more features implemented
	
	/**
	 * Creates an instance of this class using a HashMap for the payload.
	 */
	public AwRequestImpl() {
		_payload = new HashMap<String, Object>();
	}
	
	public Map<String, Object> getPayload() {
	 	return _payload;
	}
	
	public User getUser() {
		return _user;
	}

	public void setPayload(Map<String, Object> payload) {
		_payload = payload;
	}

	public void setUser(User user) {
		_user = user;
	}

}
