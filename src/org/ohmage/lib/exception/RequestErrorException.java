package org.ohmage.lib.exception;

import org.ohmage.annotator.Annotator.ErrorCode;

public class RequestErrorException extends ApiException {
	private static final long serialVersionUID = 1L;
	
	private final ErrorCode errorCode;
	private final String errorText;
	
	public RequestErrorException(final ErrorCode errorCode, String errorText) {
		super("The request returned an error.");
		
		this.errorCode = errorCode;
		this.errorText = errorText;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}
	
	public String getErrorText() {
		return errorText;
	}
	
	@Override
	public String toString() {
		return getErrorCode().toString() + ": " + getErrorText();
	}
}
