package org.ohmage.config.grammar.custom;

import org.ohmage.config.grammar.parser.ParseException;

/**
 * Wraps the JavaCC ParseException in a more friendly container (RuntimeException instead of the standard Exception).
 * 
 * @author selsky
 */
public class ConditionParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public ConditionParseException(String message, ParseException cause) {
		super(message, cause);
	}
	
	public ConditionParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
