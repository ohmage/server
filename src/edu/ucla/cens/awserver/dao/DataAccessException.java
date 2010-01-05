package edu.ucla.cens.awserver.dao;

/**
 * Simple wrapper for RuntimeExceptions that are thrown from the DAO layer. 
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public class DataAccessException extends RuntimeException {

	public DataAccessException(String message) {
		super(message);
	}
	
	public DataAccessException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DataAccessException(Throwable cause) {
		super(cause);
	}
}
