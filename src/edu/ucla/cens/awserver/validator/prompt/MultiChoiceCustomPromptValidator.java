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
public class MultiChoiceCustomPromptValidator extends AbstractCustomChoicePromptValidator {
	private static Logger _logger = Logger.getLogger(MultiChoiceCustomPromptValidator.class);
	
	/**
	 * Validates that the promptResponse contains values (a JSONArray) that match the response's custom_choices. For 
	 * multi_choice_custom prompts, the PromptResponse contains both the prompt's configuration (custom_choices) and the associated
	 * values the user chose. In addition to validating the values a user chose, the configuration based on the user's custom 
	 * choices must also be validated (valid choice_ids and choice_values).
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		} 
		
		JSONArray values = JsonUtils.getJsonArrayFromJsonObject(promptResponse, "value");
		if(null == values) {
			_logger.warn("Malformed multi_choice_custom message. Missing or malformed value for " + prompt.getId());
			return false;
		}
		
		JSONArray choices = JsonUtils.getJsonArrayFromJsonObject(promptResponse, "custom_choices");
		if(null == choices) {
			_logger.warn("Malformed multi_choice_custom message. Missing or malformed custom_choices for " + prompt.getId());
			return false;
		}
		
		Set<Integer> choiceSet = validateCustomChoices(choices, prompt);
		if(null == choiceSet) {
			return false;
		}
		
		int numberOfValues = values.length();
		for(int j = 0; j < numberOfValues; j++) {
			Integer value = JsonUtils.getIntegerFromJsonArray(values, j);
			if(null == value) {
				_logger.warn("Malformed multi_choice_custom message. Expected an integer value at value index " 
					+ j + "  for " + prompt.getId());
				return false;
			}
			
			if(! choiceSet.contains(value)) {
				_logger.warn("Malformed multi_choice_custom message. Unknown choice value at value index " 
						+ j + "  for " + prompt.getId());
				return false;
			}
		}
		
		return true;
	}
}
