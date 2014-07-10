package org.ohmage.domain.exception;

import uk.org.lidalia.slf4jext.Level;

/**
 * <p>
 * The generic exception superclass for all ohmage exceptions.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class OhmageException extends RuntimeException {
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
	public OhmageException(final String reason) {
		super(reason);
	}
	
	/**
	 * Creates a new exception with a reason and an underlying cause.
	 * 
	 * @param reason The reason this exception was thrown.
	 * 
	 * @param cause The underlying exception that caused this exception.
	 */
	public OhmageException(final String reason, final Throwable cause) {
		super(reason, cause);
	}
	
	/**
	 * Returns the logging level for this exception. By default, this is
	 * {@link Level#INFO}, however sub-exceptions that are more severe should
	 * override this method and increase this. It should never be decreased.
	 * 
	 * @return The logging level when reporting this exception.
	 */
	public Level getLevel() {
		return Level.INFO;
	}
}
