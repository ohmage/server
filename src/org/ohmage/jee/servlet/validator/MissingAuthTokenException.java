package org.ohmage.jee.servlet.validator;

/**
 * An Exception that will be thrown if there is an error during basic, HTTP
 * validation.
 *  
 * @author John Jenkins
 */
public class MissingAuthTokenException extends Exception {
	// This is needed for the purposes of Serialization.
	private final static long serialVersionUID = 2389509238L;
	
	/**
	 * Default constructor.
	 */
	public MissingAuthTokenException() {
		super();
	}
	
	/**
	 * Creates a new exception with a message.
	 * 
	 * @param message The message as to why this exception is being thrown.
	 */
	public MissingAuthTokenException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new exception with a message and a cause as to why this is
	 * being thrown.
	 * 
	 * @param message The message to further describe why this exception is
	 * 				  being thrown.
	 * 
	 * @param cause A previous Throwable object that helped cause this 
	 * 				exception.
	 */
	public MissingAuthTokenException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Creates a new exception from an existing Throwable object.
	 * 
	 * @param cause An existing Throwable object that is causing the creation
	 * 				of this new exception.
	 */
	public MissingAuthTokenException(Throwable cause) {
		super(cause);
	}
}