package edu.ucla.cens.awserver.validator.prompt;

import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;

/**
 * @author selsky
 */
public interface PromptValidator {
	/**
	 * Validates a json object containing a prompt response against its configuration (Prompt). 
	 */
	boolean validate(Prompt prompt, JSONObject promptResponse);
}
