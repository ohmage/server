package org.ohmage.domain.exception;

/**
 * <p>
 * The operation is not allowed because the requesting user did not provide
 * sufficient credentials to modify the given resource.
 * </p> 
 *
 * @author John Jenkins
 */
public class InsufficientPermissionsException extends AuthenticationException {
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
	public InsufficientPermissionsException(final String reason) {
		super(reason);
	}
	
	/**
	 * Creates a new exception with a reason and an underlying cause.
	 * 
	 * @param reason The reason this exception was thrown.
	 * 
	 * @param cause The underlying exception that caused this exception.
	 */
	public InsufficientPermissionsException(
		final String reason,
		final Throwable cause) {
		
		super(reason, cause);
	}
}