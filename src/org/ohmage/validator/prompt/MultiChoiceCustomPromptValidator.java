/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.validator.prompt;

import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Prompt;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.util.JsonUtils;


/**
 * Validator for the multi_choice_custom prompt type.
 * 
 * @author Joshua Selsky
 */
public final class MultiChoiceCustomPromptValidator extends AbstractCustomChoicePromptValidator {
	private static Logger logger = Logger.getLogger(MultiChoiceCustomPromptValidator.class);
	
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
		
		JSONArray values = JsonUtils.getJsonArrayFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE);
		if(null == values) {
			logger.warn("Malformed multi_choice_custom message. Missing or malformed value for " + prompt.getId());
			return false;
		}
		
		JSONArray choices = JsonUtils.getJsonArrayFromJsonObject(promptResponse, JsonInputKeys.PROMPT_CUSTOM_CHOICES);
		if(null == choices) {
			logger.warn("Malformed multi_choice_custom message. Missing or malformed custom_choices for " + prompt.getId());
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
				logger.warn("Malformed multi_choice_custom message. Expected an integer value at value index " 
					+ j + "  for " + prompt.getId());
				return false;
			}
		}
		
		return true;
	}
}
