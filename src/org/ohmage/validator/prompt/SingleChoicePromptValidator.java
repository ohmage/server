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

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Prompt;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;


/**
 * Validates the value of a single_choice prompt according to the prompt's configuration.
 * 
 * @author Joshua Selsky
 */
public final class SingleChoicePromptValidator extends AbstractPromptValidator {
	private static final Logger LOGGER = Logger.getLogger(SingleChoicePromptValidator.class);
	
	/**
	 * Validates that the value within the promptResponse is a valid single_choice key in the Prompt.
	 * 
	 * @param prompt The prompt containing the allowed single_choice values.
	 * @param promptResponse The response to validate.
	 * @return true if the prompt response is valid according to the allowed single_choice values.
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		}
		
		String value = JsonUtils.getStringFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE);
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Missing or empty string for single_choice value in prompt " + prompt.getId());
			}
			return false;
		}
		
		Iterator<String> keySetIterator = prompt.getProperties().keySet().iterator();
		while(keySetIterator.hasNext()) {
			if(value.equals(keySetIterator.next())) {
				return true;
			}
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("single_choice value does not exist for prompt " + prompt.getId() + ". value: " + value);
		}
		
		return false;
	}
}
