package edu.ucla.cens.awserver.cache;

/**
 * Checked cache exception. Thrown when a cache miss occurs.
 * 
 * @author John Jenkins
 */
public class CacheMissException extends Exception {
	private static final long serialVersionUID = 87592345648758L;
	
	/**
	 * Sets the message of the exception.
	 * 
	 * @param message The message being conveyed when this exception occurs.
	 */
	public CacheMissException(String message) {
		super(message);
	}
	
	/**
	 * Sets the message of the exception and the Throwable that caused this
	 * exception to be reached.
	 * 
	 * @param message The message being conveyed when this exception occurs.
	 * 
	 * @param cause The Throwable that caused this exception to be reached.
	 */
	public CacheMissException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Sets the Throwable that caused this exception to be reached.
	 * 
	 * @param cause The Throwable that caused this exception to be reached.
	 */
	public CacheMissException(Throwable cause) {
		super(cause);
	}
}
