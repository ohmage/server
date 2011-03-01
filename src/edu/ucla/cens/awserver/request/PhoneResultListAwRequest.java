package edu.ucla.cens.awserver.request;

/**
 * Adds the phone version property to ResultListAwRequest.
 * 
 * @author selsky
 */
public class PhoneResultListAwRequest extends ResultListAwRequest {
	
	private String _client;

	public String getClient() {
		return _client;
	}

	public void setClient(String client) {
		_client = client;
	}
}
