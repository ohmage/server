package org.ohmage.domain.prompt.response;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ErrorCodeException;
import org.ohmage.util.StringUtils;

/**
 * Base class for all prompt responses.
 * 
 * @author John Jenkins
 */
public abstract class PromptResponse {
	private static final String JSON_KEY_PROMPT_ID = "prompt_id";
	private static final String JSON_KEY_PROMPT_TYPE = "prompt_type";
	private static final String JSON_KEY_REPEATABLE_SET_ID = "repeatable_set_id";
	private static final String JSON_KEY_REPEATABLE_SET_ITERATION = "repeatable_set_iteration";
	private static final String JSON_KEY_RESPONSE = "value";
	
	public static enum NoResponse { SKIPPED, NOT_DISPLAYED };
	
	// Instead of the first 3 items, this should point to a Prompt which has  
	// all of the metadata about the prompt. The 'repeatableSetIteration'  
	// should remain.
	// Then, the constructors of the subclasses can take a Prompt object and
	// validate the response.
	private final String promptId;
	private final String promptType;
	
	private final String repeatableSetId;
	private final Integer repeatableSetIteration;
	
	private final NoResponse noResponse;
	
	/**
	 * Creates a new prompt response.
	 * 
	 * @param promptId The prompt's identifier, unique to the 
	 * 				   configuration, but not to this prompt response.
	 * 
	 * @param promptType The prompt's type.
	 * 
	 * @param repeatableSetId The repeatable set ID if this was part of a
	 * 						  repeatable set or NULL if not.
	 * 
	 * @param repeatableSetIteration The iteration within the repeatable 
	 * 								 set for this survey response if it was
	 * 								 part of a repeatable set or NULL if 
	 * 								 not.
	 * 
	 * @param response The response value.
	 * 
	 * @throws IllegalArgumentExcpetion Thrown if the prompt type is null 
	 * 									or whitespace only.
	 * 
	 * @throws SurveyResponseException Thrown if the prompt ID is null or 
	 * 								   whitespace only.
	 */
	public PromptResponse(final String promptId, final String promptType, 
			final String repeatableSetId, final Integer repeatableSetIteration,
			final NoResponse noResponse)
			throws ErrorCodeException {
		if(StringUtils.isEmptyOrWhitespaceOnly(promptId)) {
			throw new ErrorCodeException(ErrorCodes.SURVEY_INVALID_PROMPT_ID, "The prompt ID cannot be null.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(promptType)) {
			throw new IllegalArgumentException("The prompt type cannot be null.");
		}
		
		this.promptId = promptId;
		this.promptType = promptType;
		
		this.repeatableSetId = repeatableSetId;
		this.repeatableSetIteration = repeatableSetIteration;
		
		this.noResponse = noResponse;
	}
	
	/**
	 * Returns the prompt's ID.
	 * 
	 * @return The prompt's ID.
	 */
	public String getPromptId() {
		return promptId;
	}
	
	/**
	 * Returns the prompt's type.
	 * 
	 * @return The prompt's type.
	 */
	public String getPromptType() {
		return promptType;
	}
	
	/**
	 * Returns the repeatable set ID if available.
	 * 
	 * @return The repeatable set ID or null if this wasn't part of a
	 * 		   repeatable set.
	 */
	public String getRepeatableSetId() {
		return repeatableSetId;
	}
	
	/**
	 * Returns the repeatable set iteration if available.
	 * 
	 * @return The repeatable set iteration or null if this wasn't part of
	 * 		   a repeatable set.
	 */
	public Integer getRepeatableSetIteration() {
		return repeatableSetIteration;
	}
	
	/**
	 * Returns whether or not this prompt was skipped.
	 * 
	 * @return Whether or not this prompt was skipped.
	 */
	public boolean wasSkipped() {
		return NoResponse.SKIPPED.equals(noResponse);
	}
	
	/**
	 * Returns whether or not this prompt was not displayed.
	 * 
	 * @return Whether or not this prompt was not displayed.
	 */
	public boolean wasNotDisplayed() {
		return NoResponse.NOT_DISPLAYED.equals(noResponse);
	}
	
	/**
	 * Creates a JSONObject that represents this object.
	 * 
	 * @param longVersion The representation saved in the database only
	 * 					  includes the prompt ID and the response value. If
	 * 					  this flag is set to true it includes the prompt 
	 * 					  type, repeatable set ID if available, and 
	 * 					  repeatable set iteration if available.
	 * 
	 * @return A JSONObject that represents this object.
	 */
	public JSONObject toJson(final boolean longVersion) {
		try {
			JSONObject result = new JSONObject();
			
			result.put(JSON_KEY_PROMPT_ID, promptId);
			result.put(JSON_KEY_RESPONSE, getResponseValue());
			
			if(longVersion) {
				result.put(JSON_KEY_PROMPT_TYPE, promptType);
				result.put(JSON_KEY_REPEATABLE_SET_ID, repeatableSetId);
				result.put(JSON_KEY_REPEATABLE_SET_ITERATION, repeatableSetIteration);
			}
			
			return result;
		}
		catch(JSONException e) {
			return null;
		}
	}
	
	/**
	 * Returns a String representation of the prompt response's value or, if
	 * there was no response, a string representing why.
	 * 
	 * @return A String representation of the prompt response's value or, if
	 * 		   there was no response, a string representing why.
	 */
	public String getResponseValue() {
		if(wasSkipped()) {
			return NoResponse.SKIPPED.toString();
		}
		else if(wasNotDisplayed()) {
			return NoResponse.NOT_DISPLAYED.toString();
		}
		else {
			return null;
		}
	}
}