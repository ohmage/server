package edu.ucla.cens.awserver.request;

/**
 * Builds an authentication request.
 * 
 * @author John Jenkins
 */
public class AuthRequest extends ResultListAwRequest {
	public static final String HASHED_PASSWORD = "hashed_password";
	
	/**
	 * Default constructor.
	 */
	public AuthRequest() {
		// This is an empty class that does nothing, but it is framed out to
		// be used by authentication requests.
	}
}
