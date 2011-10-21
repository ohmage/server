package org.ohmage.exception;

import org.ohmage.annotator.Annotator.ErrorCode;

/**
 * This Exception class defines exceptions that contain error codes and 
 * corresponding error text as defined by the ohmage system.
 * 
 * @author John Jenkins
 */
public class ErrorCodeException extends WorkflowException {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new exception with an error code and error text.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 */
	public ErrorCodeException(final ErrorCode errorCode, 
			final String errorText) {
		
		super(errorCode, errorText);
	}
	
	/**
	 * Creates an exception with an error code, error text, and a custom 
	 * message that will be printed to the log instead of the error text.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 * 
	 * @param message The message for the server log.
	 */
	public ErrorCodeException(final ErrorCode errorCode,
			final String errorText, final String message) {
		
		super(errorCode, errorText, message);
	}
	
	/**
	 * Creates an exception with an error code, error text, a custom message 
	 * that will be printed to the log instead of the error text, and another
	 * Throwable that caused this exception.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 * 
	 * @param message The message for the server log.
	 * 
	 * @param cause The Throwable that caused this exception.
	 */
	public ErrorCodeException(final ErrorCode errorCode, 
			final String errorText, final String message, 
			final Throwable cause) {
		
		super(errorCode, errorText, message, cause);
	}
	
	/**
	 * Creates an exception with an error code, error text, and another 
	 * Throwable that caused this exception.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 * 
	 * @param cause The Throwable that caused this exception.
	 */
	public ErrorCodeException(final ErrorCode errorCode,
			final String errorText, final Throwable cause) {
		
		super(errorCode, errorText, cause);
	}
}