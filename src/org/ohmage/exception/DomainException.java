package org.ohmage.exception;

import org.ohmage.annotator.Annotator.ErrorCode;

/**
 * Namespace-style exception for exception that can be thrown within a domain
 * object.
 * 
 * @author John Jenkins
 */
public class DomainException extends WorkflowException {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new exception with only a message.
	 * 
	 * @param message A String explaining why this exception is being thrown.
	 */
	public DomainException(
			final String message) {
		super(message);
	}
	
	/**
	 * Creates a new exception with a message as to why it's being thrown and 
	 * another Throwable that may have caused this exception to be thrown.
	 * 
	 * @param message A String describing why this exception is being thrown.
	 * 
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public DomainException(
			final String message, 
			final Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Creates a new exception from a previously thrown Throwable.
	 *  
	 * @param cause A Throwable that was caught and is associated with why this
	 * 				exception is being thrown.
	 */
	public DomainException(
			final Throwable cause) {
		super(cause);
	}
	
	/**
	 * Creates a new exception with an error code and error text.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 */
	public DomainException(
			final ErrorCode errorCode, 
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
	public DomainException(
			final ErrorCode errorCode,
			final String errorText, 
			final String message) {
		
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
	public DomainException(
			final ErrorCode errorCode, 
			final String errorText, 
			final String message, 
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
	public DomainException(
			final ErrorCode errorCode,
			final String errorText, 
			final Throwable cause) {
		
		super(errorCode, errorText, cause);
	}
}
