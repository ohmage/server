package org.ohmage.domain.exception;

/**
 * <p>
 * An argument to an API was invalid.
 * </p>
 *
 * @author John Jenkins
 */
public class InvalidArgumentException extends OhmageException {
	/**
	 * The default serial version used for serializing an instance of this
	 * class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new exception with only a reason.
	 * 
	 * @param reason The reason this exception was thrown.
	 */
	public InvalidArgumentException(final String reason) {
		super(reason);
	}
	
	/**
	 * Creates a new exception with a reason and an underlying cause.
	 * 
	 * @param reason The reason this exception was thrown.
	 * 
	 * @param cause The underlying exception that caused this exception.
	 */
	public InvalidArgumentException(
		final String reason,
		final Throwable cause) {
		
		super(reason, cause);
	}
}