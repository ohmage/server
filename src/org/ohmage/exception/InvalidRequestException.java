package org.ohmage.exception;

/**
 * This is basically a wrapper class for requests to throw that will be throw
 * directly to the request handler and will simply return an HTTP error code.
 *
 * @author John Jenkins
 */
public class InvalidRequestException extends Exception {
	/**
	 * A default serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private final int httpErrorCode;
	private final String errorText;
	
	/**
	 * Creates a request with a default error code.
	 * 
	 * @param httpErrorCode The error code.
	 * 
	 * @param reason A string to be sent back to the user explaining why the
	 * 				 request failed.
	 */
	public InvalidRequestException(
			final int httpErrorCode,
			final String errorText) {
		
		this.httpErrorCode = httpErrorCode;
		this.errorText = errorText;
	}
	
	/**
	 * The HTTP error code to return to the user.
	 * 
	 * @return The HTTP error code to return to the user.
	 */
	public int getErrorCode() {
		return httpErrorCode;
	}
	
	/**
	 * The text to be returned to the user.
	 * 
	 * @return The text to be returned to the user.
	 */
	public String getErrorText() {
		return errorText;
	}
}
