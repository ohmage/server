package edu.ucla.cens.awserver.validator;

/**
 * Simple wrapper for RuntimeExceptions that are thrown from the validation layer. 
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public class ValidatorException extends RuntimeException {

	public ValidatorException(String message) {
		super(message);
	}
	
	public ValidatorException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ValidatorException(Throwable cause) {
		super(cause);
	}
}
