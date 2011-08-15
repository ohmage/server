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
 * Validator for the 'number' prompt type.
 * 
 * @author Joshua Selsky
 */
public final class RangeBoundNumberPromptValidator extends AbstractPromptValidator {
	private static Logger logger = Logger.getLogger(RangeBoundNumberPromptValidator.class);
	
	/**
	 * Validates that the value in the promptResponse is within the bounds set by the Prompt.
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		}
		
		int min = Integer.parseInt(prompt.getProperties().get(JsonInputKeys.PROMPT_PROPERTY_MIN).getLabel());
		int max = Integer.parseInt(prompt.getProperties().get(JsonInputKeys.PROMPT_PROPERTY_MAX).getLabel());
		Integer value = JsonUtils.getIntegerFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE);
		
		if(logger.isDebugEnabled()) {
			logger.debug("found value " + value);
		}
		
		if(null == value) {
			if(logger.isDebugEnabled()) {
				logger.debug("unparseable or missing range-bound number value for prompt id " + prompt.getId());
			}
			return false;
		}
		return value >= min && value <= max;
	}
}
