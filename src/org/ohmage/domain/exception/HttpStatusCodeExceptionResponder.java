package org.ohmage.domain.exception;

/**
 * <p>
 * An interface for OhmageException sub-classes that tells the exception filter
 * to use a specialized status code when this exception is thrown.
 * </p>
 *
 * @author John Jenkins
 */
public interface HttpStatusCodeExceptionResponder {
	/**
	 * Returns the specialized status code for this exception.
	 * 
	 * @return The specialized status code for this exception
	 */
	public int getStatusCode();
}