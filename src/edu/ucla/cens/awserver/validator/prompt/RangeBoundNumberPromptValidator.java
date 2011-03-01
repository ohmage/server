package edu.ucla.cens.awserver.validator.prompt;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * @author selsky
 */
public class RangeBoundNumberPromptValidator extends AbstractPromptValidator {
	private static Logger _logger = Logger.getLogger(RangeBoundNumberPromptValidator.class);
	
	/**
	 * Validates that the value in the promptResponse is within the bounds set by the Prompt.
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
		Integer value = JsonUtils.getIntegerFromJsonObject(promptResponse, "value");
		
		_logger.info("found value " + value);
		
		if(null == value) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("unparseable or missing range-bound number value for prompt id " + prompt.getId());
			}
			return false;
		}
		return value >= min && value <= max;
	}
}
