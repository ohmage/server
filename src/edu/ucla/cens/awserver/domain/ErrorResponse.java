package edu.ucla.cens.awserver.domain;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Wrapper for error code data.
 * 
 * @author selsky
 */
public class ErrorResponse {
	private String _code;
	private String _text;
	
	public String getCode() {
		return _code;
	}
	
	public void setCode(String code) {
		if(StringUtils.isEmptyOrWhitespaceOnly(code)) {
			throw new IllegalArgumentException("code must not be empty");
		}
		_code = code;
	}
	
	public String getText() {
		return _text;
	}
	
	public void setText(String text) {
		if(StringUtils.isEmptyOrWhitespaceOnly(text)) {
			throw new IllegalArgumentException("text must not be empty");
		}
		_text = text;
	}
}
