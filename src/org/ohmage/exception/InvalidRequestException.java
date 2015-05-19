package org.ohmage.exception;

import org.ohmage.annotator.Annotator.ErrorCode;

/**
 * This is basically a wrapper class for requests to throw that will be throw
 * directly to the request handler. The class was originally designed to 
 * return an HTTP error code. For consistency with the rest of the API, 
 * it also supports ohmage internal error code. 
 *
 * @author John Jenkins
 * @author Hongsuda T. 
 */
public class InvalidRequestException extends Exception {
	/**
	 * A default serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	private final int httpErrorCode;
	private final String errorText;
	private final ErrorCode errorCode; // ohmage error code
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
		this.errorCode = ErrorCode.SYSTEM_GENERAL_ERROR;
	}
	
	public InvalidRequestException(
			final ErrorCode errorCode,
			final String errorText) {
		
		this.errorCode = errorCode;
		this.errorText = errorText;
		this.httpErrorCode = 200;
	}
	
	/**
	 * The HTTP error code to return to the user.
	 * 
	 * @return The HTTP error code to return to the user.
	 */
	public int getHttpErrorCode() {
		return httpErrorCode;
	}
	
	/**
	 * The ohmage error code to return to the user.
	 * 
	 * @return The HTTP error code to return to the user.
	 */
	public ErrorCode getErrorCode() {
		return errorCode;
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
