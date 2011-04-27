/**
 * 
 */
package edu.ucla.cens.awserver.validator.prompt;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.Prompt;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Validator for the results from a RemoteActivity prompt type.
 * 
 * @author John Jenkins
 */
public class RemoteActivityPromptValidator extends AbstractPromptValidator {
	private static Logger _logger = Logger.getLogger(RemoteActivityPromptValidator.class);
	
	private static final String SINGLE_VALUE_KEY = "score";
	
	/**
	 * Validates that the result is a valid JSONArray,that the number of
	 * responses don't exceed that of the number of retries in the XML, that
	 * each entry in the JSONArray is a JSONObject, and that each of those
	 * JSONObjects contains a key 
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		_logger.debug("Recieved message");
		if(isNotDisplayed(prompt, promptResponse))
		{
			return true;
		}
		
		if(isSkipped(prompt, promptResponse))
		{
			return isValidSkipped(prompt, promptResponse);
		}
		
		JSONArray responseJsonArray = JsonUtils.getJsonArrayFromJsonObject(promptResponse, "value");
		if(responseJsonArray == null)
		{
			_logger.info("Missing or invalid JSONArray for prompt: " + prompt.getId());
			return false;
		}
		
		int numRetries = Integer.parseInt(prompt.getProperties().get("retries").getLabel());
		if(responseJsonArray.length() > (numRetries + 1)) {
			_logger.info("Too many responses in JSONArray for prompt: " + prompt.getId());
		}
		
		for(int i = 0; i < numRetries; i++) {
			try {
				JSONObject currJsonObject = responseJsonArray.getJSONObject(i);
				
				Double currScore = (Double) currJsonObject.get(SINGLE_VALUE_KEY);
				if(currScore == null) {
					_logger.info("Missing required return value: " + SINGLE_VALUE_KEY);
					return false;
				}
				else if(currScore.isInfinite() || currScore.isNaN()) {
					_logger.info("Returned single value is an invalid value: " + currScore.toString());
					return false;
				}
			}
			catch(JSONException e) {
				_logger.info("Error parsing JSONArray for JSONObject: " + responseJsonArray.toString(), e);
				return false;
			}
		}
		
		return true;
	}
}
