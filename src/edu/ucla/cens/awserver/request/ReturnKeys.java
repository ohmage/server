package edu.ucla.cens.awserver.request;

/**
 * Keys that are put into the toReturn map to be returned to the requester.
 * 
 * @author John Jenkins
 */
public class ReturnKeys {
	/**
	 * Default constructor that is private such that no instance can ever be
	 * made.
	 */
	private ReturnKeys() {
		// Do nothing.
	}
	
	// General response keys.
	public static final String RESULT = "result";
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	public static final String DATA = "data";
	
	// Document-specific response keys.
	public static final String DOCUMENT_ID = "document_id";
}