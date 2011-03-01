package edu.ucla.cens.awserver.validator.prompt;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class TextWithinRangePromptValidator extends AbstractPromptValidator {
	private static Logger _logger = Logger.getLogger(TextWithinRangePromptValidator.class);
	
	/**
	 * Validates that the value from the promptResponse is within the min and max specified by the Prompt. 
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		}
		
		int min = Integer.parseInt(prompt.getProperties().get("min").getLabel());
		int max = Integer.parseInt(prompt.getProperties().get("max").getLabel());
		
		String value = JsonUtils.getStringFromJsonObject(promptResponse, "value");
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("Missing or empty value for prompt " + prompt.getId());
			}
			return false;
		}
		
		int length = value.length();
		return length >= min && length <= max;
	}
}
