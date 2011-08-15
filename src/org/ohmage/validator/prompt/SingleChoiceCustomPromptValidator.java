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
 * Validator for the 'single_choice_custom' prompt type.
 * 
 * @author Joshua Selsky
 */
public final class SingleChoiceCustomPromptValidator extends AbstractCustomChoicePromptValidator {
	private static Logger logger = Logger.getLogger(SingleChoiceCustomPromptValidator.class);
	
	/**
	 * Validates that the promptResponse contains a value (an Integer) that matches a response's custom_choices. For 
	 * single_choice_custom prompts, the promptResponse contains both the prompt's configuration (custom_choices) and the 
	 * associated value the user chose. In addition to validating the value a user chose, the configuration based on the user's
	 * custom choices must also be validated (valid choice_ids and choice_values).
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		}
		
		Integer value = JsonUtils.getIntegerFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE);
		if(null == value) {
			if(logger.isDebugEnabled()) {
				logger.debug("Malformed single_choice_custom message. Missing value for " + prompt.getId());
			}
			return false;
		}
		
		JSONArray choices = JsonUtils.getJsonArrayFromJsonObject(promptResponse, JsonInputKeys.PROMPT_CUSTOM_CHOICES);
		if(null == choices) {
			if(logger.isDebugEnabled()) {
				logger.debug("Malformed single_choice_custom message. Missing or invalid custom_choices for " + prompt.getId());
			}
			return false;
		}
		
		Set<Integer> choiceSet = validateCustomChoices(choices, prompt);
		if(null == choiceSet) {
			return false;
		}
		
		return true;
	}

}
