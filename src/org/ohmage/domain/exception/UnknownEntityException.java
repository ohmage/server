package org.ohmage.domain.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * The exception to be thrown when a user requests an entity that does not
 * exist.
 * </p>
 *
 * @author John Jenkins
 */
public class UnknownEntityException
	extends OhmageException
	implements HttpStatusCodeExceptionResponder {

	/**
	 * The unique identifier for this version of this class for serialization
	 * purposes.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception with a reason.
	 * 
	 * @param reason The user-friendly reason this exception was thrown.
	 */
	public UnknownEntityException(final String reason) {
		super(reason);
	}

	/**
	 * Creates a new exception with a reason and a cause.
	 * 
	 * @param reason The user-friendly reason this exception was thrown.
	 * 
	 * @param cause An underlying exception that caused this exception.
	 */
	public UnknownEntityException(final String reason, final Throwable cause) {
		super(reason, cause);
	}

	/**
	 * Returns a {@link HttpServletResponse#SC_NOT_FOUND}.
	 */
	@Override
	public int getStatusCode() {
		return HttpServletResponse.SC_NOT_FOUND;
	}
}