package edu.ucla.cens.awserver.validator.prompt;

/**
 * For all prompts, the string null is an allowable response (signifying "no response").
 * 
 * @author selsky
 */
public class NullValidator implements PromptResponseValidator {

	/**
	 * @return true if the provided string contains "null"
	 */
	public boolean validate(String response) {
		return "null".equals(response);
	}

}
