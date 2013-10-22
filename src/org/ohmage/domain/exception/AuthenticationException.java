package org.ohmage.domain.exception;

/**
 * <p>
 * The exception to throw when the user's authentication credentials are
 * missing or invalid.
 * </p>
 *
 * @author John Jenkins
 */
public class AuthenticationException extends OhmageException {
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
	public AuthenticationException(final String reason) {
		super(reason);
	}
	
	/**
	 * Creates a new exception with a reason and an underlying cause.
	 * 
	 * @param reason The reason this exception was thrown.
	 * 
	 * @param cause The underlying exception that caused this exception.
	 */
	public AuthenticationException(
		final String reason,
		final Throwable cause) {
		
		super(reason, cause);
	}
}