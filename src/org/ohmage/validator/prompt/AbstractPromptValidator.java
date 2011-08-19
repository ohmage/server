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

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Prompt;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.util.JsonUtils;


/**
 * Abstract base class for common behavior amongst PromptValidators.
 * 
 * @author Joshua Selsky
 */
public abstract class AbstractPromptValidator implements PromptValidator {
	private static final Logger LOGGER = Logger.getLogger(AbstractPromptValidator.class);
	
	/**
	 * Returns true if the promptResponse contains the value NOT_DISPLAYED. NOT_DISPLAYED is considered to be a valid prompt
	 * response in all cases. This is not exactly true as the only way a prompt can be NOT_DISPLAYED is if its condition 
	 * evaluated to false. The server does not evaluate whether a client interpreted a condition correctly.  
	 * 
	 * @param prompt
	 * @param promptResponse
	 * @return
	 */
	protected boolean isNotDisplayed(Prompt prompt, JSONObject promptResponse) {
		String value = JsonUtils.getStringFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE);
		if(null == value) { // not a string, it must've been displayed
			return false;
		}
		return JsonInputKeys.PROMPT_NOT_DISPLAYED.equals(value);
	}
	
	/**
	 * Checks if the promptResponse contains the value SKIPPED and returns true if the Prompt is skippable.
	 * 
	 * @param prompt
	 * @param promptResponse
	 * @return
	 */
	protected boolean isValidSkipped(Prompt prompt, JSONObject promptResponse) {
		String value = JsonUtils.getStringFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE);
		
		if(null == value) { // not a string, therefore not skipped
			return true;
		}
		
		if(JsonInputKeys.PROMPT_SKIPPED.equals(value) && prompt.isSkippable()) {
			return true;
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(JsonInputKeys.PROMPT_SKIPPED + " found, but prompt " + prompt.getId() + " is not skippable");
		}
		
		return false;
	}
	
	/**
	 * Returns true if the promptResponse contains the value SKIPPED.
	 * 
	 * @param prompt
	 * @param promptResponse
	 * @return
	 */
	protected boolean isSkipped(Prompt prompt, JSONObject promptResponse) {
		String value = JsonUtils.getStringFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE);
		
		if(null == value) { // not a string, therefore not skipped
			return false;
		}
		
		return JsonInputKeys.PROMPT_SKIPPED.equals(value);
	}
}
