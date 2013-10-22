package org.ohmage.domain.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * This exception represents when an update on an entity in the database was
 * attempted, but it failed due to a conflicting update that occurred between
 * when the initial state of the entity was read and when the update was
 * attempted.
 * </p>
 *
 * @author John Jenkins
 */
public class InconsistentDatabaseException
	extends OhmageException
	implements HttpStatusCodeExceptionResponder {
	
	/**
	 * The unique version of this class for serialization purposes.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new exception with a reason.
	 * 
	 * @param reason
	 *        The user-friendly reason this exception was thrown.
	 */
	public InconsistentDatabaseException(
		final String reason) {
		
		super(reason);
	}

	/**
	 * Creates a new exception with a reason and a cause.
	 * 
	 * @param reason
	 *        The user-friendly reason this exception was thrown.
	 * 
	 * @param cause
	 *        An underlying exception that caused this exception.
	 */
	public InconsistentDatabaseException(
		final String reason,
		final Throwable cause) {
		
		super(reason, cause);
	}

	/**
	 * Returns a {@link HttpServletResponse.SC_CONFLICT}.
	 */
	@Override
	public int getStatusCode() {
		return HttpServletResponse.SC_CONFLICT;
	}
}