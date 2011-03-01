package edu.ucla.cens.awserver.validator.prompt;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * @author selsky
 */
public abstract class AbstractPromptValidator implements PromptValidator {
	private static Logger _logger = Logger.getLogger(AbstractPromptValidator.class);
	
	/**
	 * Returns true if the promptResponse contains the value NOT_DISPLAYED. NOT_DISPLAYED is considered to be a valid prompt
	 * response in all cases. This is not exactly true as the only way a prompt can be NOT_DISPLAYED is if its condition 
	 * evaluated to false. The server does not evaluate whether a client interpreted a condition correctly.  
	 */
	protected boolean isNotDisplayed(Prompt prompt, JSONObject promptResponse) {
		String value = JsonUtils.getStringFromJsonObject(promptResponse, "value");
		if(null == value) { // not a string, it must've been displayed
			return false;
		}
		return "NOT_DISPLAYED".equals(value);
	}
	
	/**
	 * Checks if the promptResponse contains the value SKIPPED and returns true if the Prompt is skippable.
	 */
	protected boolean isValidSkipped(Prompt prompt, JSONObject promptResponse) {
		String value = JsonUtils.getStringFromJsonObject(promptResponse, "value");
		
		if(null == value) { // not a string, therefore not skipped
			return true;
		}
		
		if("SKIPPED".equals(value) && prompt.isSkippable()) {
			return true;
		}
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("SKIPPED found, but prompt " + prompt.getId() + " is not skippable");
		}
		
		return false;
	}
	
	/**
	 * Returns true if the promptResponse contains the value SKIPPED.
	 */
	protected boolean isSkipped(Prompt prompt, JSONObject promptResponse) {
		String value = JsonUtils.getStringFromJsonObject(promptResponse, "value");
		
		if(null == value) { // not a string, therefore not skipped
			return false;
		}
		
		return "SKIPPED".equals(value);
	}
}
