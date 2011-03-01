package edu.ucla.cens.awserver.validator.prompt;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class SingleChoicePromptValidator extends AbstractPromptValidator {
	private static Logger _logger = Logger.getLogger(SingleChoicePromptValidator.class);
	
	/**
	 * Validates that the value within the promptResponse is a valid single_choice key in the Prompt. 
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		}
		
		String value = JsonUtils.getStringFromJsonObject(promptResponse, "value");
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("Missing or empty string for single_choice value in prompt " + prompt.getId());
			}
			return false;
		}
		
		Iterator<String> keySetIterator = prompt.getProperties().keySet().iterator();
		while(keySetIterator.hasNext()) {
			if(value.equals(keySetIterator.next())) {
				return true;
			}
		}
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("single_choice value does not exist for prompt " + prompt.getId() + ". value: " + value);
		}
		
		return false;
	}
}
