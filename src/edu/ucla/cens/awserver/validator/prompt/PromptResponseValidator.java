package edu.ucla.cens.awserver.validator.prompt;

/**
 * Validation interface for JSON prompt responses.
 * 
 * @author selsky
 */
public interface PromptResponseValidator {

	public boolean validate(String response);
	
}
