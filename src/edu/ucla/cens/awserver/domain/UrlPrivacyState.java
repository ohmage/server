package edu.ucla.cens.awserver.domain;

/**
 * Data transfer object for image queries used for filtering out private images.
 * 
 * @author Joshua Selsky
 */
public class UrlPrivacyState {
	
	private String _privacyState;
	private String _url;
	
	public UrlPrivacyState(String url, String privacyState) {
		_url = url;
		_privacyState = privacyState;
	}
	
	public String getPrivacyState() {
		return _privacyState;
	}
	
	public String getUrl() {
		return _url;
	}
}
