package org.ohmage.lib.exception;

public class ApiException extends Exception {
	private static final long serialVersionUID = 1L;

	public ApiException(String reason) {
		super(reason);
	}
	
	public ApiException(String reason, Throwable cause) {
		super(reason, cause);
	}
}