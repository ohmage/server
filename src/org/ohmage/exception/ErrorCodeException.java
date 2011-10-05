package org.ohmage.exception;

/**
 * This Exception class defines exceptions that contain error codes and 
 * corresponding error text as defined by the ohmage system.
 * 
 * @author John Jenkins
 */
public class ErrorCodeException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final String errorCode;
	private final String errorText;
	
	/**
	 * Creates a new ErrorCodeException with an error code and corresponding
	 * text.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 */
	public ErrorCodeException(final String errorCode, final String errorText) {
		super(errorText);
		
		this.errorCode = errorCode;
		this.errorText = errorText;
	}
	
	/**
	 * Creates a new ErrorCodeException with an error code, corresponding text,
	 * and another Throwable.
	 * 
	 * @param errorCode The error code.
	 * 
	 * @param errorText The error text.
	 * 
	 * @param cause The Throwable that caused this exception.
	 */
	public ErrorCodeException(final String errorCode, final String errorText, final Throwable cause) {
		super(errorText, cause);

		this.errorCode = errorCode;
		this.errorText = errorText;
	}
	
	/**
	 * Returns the error code.
	 * 
	 * @return The error code. This may be null.
	 */
	public String getErrorCode() {
		return errorCode;
	}
	
	/**
	 * Returns the error text.
	 * 
	 * @return The error text. This may be null.
	 */
	public String getErrorText() {
		return errorText;
	}
}
