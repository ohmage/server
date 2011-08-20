package org.ohmage.validator.prompt;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Prompt;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.util.JsonUtils;


/**
 * Validator for the results from a RemoteActivity prompt type.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public final class RemoteActivityPromptValidator extends AbstractPromptValidator {
	private static final Logger LOGGER = Logger.getLogger(RemoteActivityPromptValidator.class);
	
	/**
	 * Validates that the result is a valid JSONArray,that the number of
	 * responses don't exceed that of the number of retries in the XML, that
	 * each entry in the JSONArray is a JSONObject, and that each of those
	 * JSONObjects contains a key 
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		// If it wasn't displayed then it is valid.
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		// If it was skipped then just make sure it was skippable.
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		}
		
		// Get the JSONArray that is the return value. This should always be
		// something even if it is an empty array.
		JSONArray responseJsonArray = JsonUtils.getJsonArrayFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE);
		if(responseJsonArray == null) {
			LOGGER.info("Missing or invalid JSONArray for prompt: " + prompt.getId());
			return false;
		}
		
		// Get the number of retries and ensure that they aren't reporting that
		// they ran it more than that many times.
		int numRetries = Integer.parseInt(prompt.getProperties().get(JsonInputKeys.PROMPT_REMOTE_ACTIVITY_RETRIES).getLabel());
		if(responseJsonArray.length() > (numRetries + 1)) {
			LOGGER.info("Too many responses in JSONArray for prompt: " + prompt.getId());
			return false;
		}
		
		// Get the minimum number of times that they were required to run it 
		// and ensure that they ran it at least that many times.
		// FIXME: Remove try-catch once min_runs has become required.
		if(prompt.getProperties().containsKey(JsonInputKeys.PROMPT_REMOTE_ACTIVITY_MIN_RUNS)) {
			int minRuns = Integer.parseInt(prompt.getProperties().get(JsonInputKeys.PROMPT_REMOTE_ACTIVITY_MIN_RUNS).getLabel());
			if(responseJsonArray.length() < minRuns) {
				LOGGER.info("Not enough runs of the remote Activity. The XML requires " + minRuns + ", but we only received " + responseJsonArray.length() + ".");
				return false;
			}
		}
		else {
			LOGGER.warn("Need to make the 'min_runs' property exist in all campaigns.");
		}
		
		// For each of the individual runs,
		for(int i = 0; i < responseJsonArray.length(); i++) {
			try {
				// Check that it is a valid JSONObject.
				JSONObject currJsonObject = responseJsonArray.getJSONObject(i);
				
				// Check that the score exists and is a valid number.
				Double currScore = Double.valueOf(currJsonObject.get(JsonInputKeys.PROMPT_REMOTE_ACTIVITY_SCORE).toString());
				
				if(currScore == null) {
					LOGGER.info("Missing required return value: " + JsonInputKeys.PROMPT_REMOTE_ACTIVITY_SCORE);
					return false;
				}
				else if(currScore.isInfinite() || currScore.isNaN()) {
					LOGGER.info("Returned single value is an invalid value: " + currScore.toString());
					return false;
				}
			}
			catch(JSONException e) {
				LOGGER.info("Error parsing JSONArray for JSONObject: " + responseJsonArray.toString(), e);
				return false;
			}
			catch(ClassCastException e) {
				LOGGER.info("Error converting the single-value score into a double.", e);
				return false;
			}
		}
		
		return true;
	}
}
