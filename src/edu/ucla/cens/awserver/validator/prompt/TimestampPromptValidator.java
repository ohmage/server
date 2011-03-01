package edu.ucla.cens.awserver.validator.prompt;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class TimestampPromptValidator extends AbstractPromptValidator {
	private static Logger _logger = Logger.getLogger(TimestampPromptValidator.class);
	
	/**
	 * Validates that the value in the promptResponse contains a timestamp of the form yyyy-MM-ddThh:mm:ss.
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		}
		
		String timestamp = JsonUtils.getStringFromJsonObject(promptResponse, "value");
		if(StringUtils.isEmptyOrWhitespaceOnly(timestamp)) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("Missing or empty value for prompt " + prompt.getId());
			}
			return false;
		}
		
		SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // the DateFormat classes are not threadsafe
		                                                                           // so they must be created for each run of this 
		                                                                           // method
		tsFormat.setLenient(false);
		
		try {
			
			tsFormat.parse(timestamp);
			
		} catch (ParseException pe) {
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("unparseable timestamp " + timestamp + " for prompt id " + prompt.getId());
			}
			
			return false;
		}
		
		return true;
	}

}
