package org.ohmage.lib.exception;

public class RequestErrorException extends ApiException {
	private static final long serialVersionUID = 1L;
	
	private final String errorCode;
	private final String errorText;
	
	public RequestErrorException(String errorCode, String errorText) {
		super("The request returned an error.");
		
		this.errorCode = errorCode;
		this.errorText = errorText;
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	
	public String getErrorText() {
		return errorText;
	}
	
	@Override
	public String toString() {
		return getErrorText();
	}
}
