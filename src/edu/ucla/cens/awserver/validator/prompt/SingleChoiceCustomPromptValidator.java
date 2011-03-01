package edu.ucla.cens.awserver.validator.prompt;

import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * @author selsky
 */
public class SingleChoiceCustomPromptValidator extends AbstractCustomChoicePromptValidator {
	private static Logger _logger = Logger.getLogger(SingleChoiceCustomPromptValidator.class);
	
	/**
	 * Validates that the promptResponse contains a value (an Integer) that matches a response's custom_choices. For 
	 * single_choice_custom prompts, the promptResponse contains both the prompt's configuration (custom_choices) and the 
	 * associated value the user chose. In addition to validating the value a user chose, the configuration based on the user's
	 * custom choices must also be validated (valid choice_ids and choice_values).
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		}
		
		Integer value = JsonUtils.getIntegerFromJsonObject(promptResponse, "value");
		if(null == value) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("Malformed single_choice_custom message. Missing value for " + prompt.getId());
			}
			return false;
		}
		
		JSONArray choices = JsonUtils.getJsonArrayFromJsonObject(promptResponse, "custom_choices");
		if(null == choices) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("Malformed single_choice_custom message. Missing or invalid custom_choices for " + prompt.getId());
			}
			return false;
		}
		
		Set<Integer> choiceSet = validateCustomChoices(choices, prompt);
		if(null == choiceSet) {
			return false;
		}
			
		if(! choiceSet.contains(value)) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("Malformed single_choice_custom message. Unknown choice value for " + prompt.getId());
			}
			return false;
		}
		
		return true;
	}

}
