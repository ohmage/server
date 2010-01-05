package edu.ucla.cens.awserver.controller;

/**
 * Simple wrapper for RuntimeExceptions that are thrown from the service layer. 
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public class ControllerException extends RuntimeException {

	public ControllerException(String message) {
		super(message);
	}
	
	public ControllerException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ControllerException(Throwable cause) {
		super(cause);
	}
}
