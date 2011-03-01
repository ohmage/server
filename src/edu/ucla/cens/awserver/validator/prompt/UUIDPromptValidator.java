package edu.ucla.cens.awserver.validator.prompt;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class UUIDPromptValidator extends AbstractPromptValidator {
	private static Logger _logger = Logger.getLogger(UUIDPromptValidator.class);
	private static Pattern _pattern 
		= Pattern.compile("[a-fA-F0-9]{8}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{12}");
	
	/**
	 * Validates that the value in the promptResponse is a correctly formed UUID.
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
				_logger.debug("Missing UUID value for prompt " + prompt.getId());
			}
			return false;
		}
		
		if(! _pattern.matcher(value).matches()) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("invalid UUID for prompt " + prompt.getId() + ". value: " + value);
			}
			return false;
		}
		
		return true;
	}
	
//	public static void main(String args[]) {
//		System.out.println(_pattern.matcher("afda1b74-4f23-4068-a50b-664e1c347264").matches());
//	}

}
