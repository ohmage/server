package edu.ucla.cens.awserver.service;

/**
 * Simple wrapper for RuntimeExceptions that are thrown from the service layer. 
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public class ServiceException extends RuntimeException {

	public ServiceException(String message) {
		super(message);
	}
	
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ServiceException(Throwable cause) {
		super(cause);
	}
}
