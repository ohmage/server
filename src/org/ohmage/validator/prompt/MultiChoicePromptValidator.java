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
import org.ohmage.domain.Prompt;
import org.ohmage.util.JsonUtils;


/**
 * @author selsky
 */
public class MultiChoicePromptValidator extends AbstractPromptValidator {
	private static Logger _logger = Logger.getLogger(MultiChoicePromptValidator.class);
	
	/**
	 * Validates that the value from the promptResponse contains valid keys from the Prompt.
	 */
	@Override
	public boolean validate(Prompt prompt, JSONObject promptResponse) {
		if(isNotDisplayed(prompt, promptResponse)) {
			return true;
		}
		
		if(isSkipped(prompt, promptResponse)) {
			return isValidSkipped(prompt, promptResponse);
		}
		
		JSONArray jsonArray = JsonUtils.getJsonArrayFromJsonObject(promptResponse, "value");
		if(null == jsonArray) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("unparseable or missing JSON array value for prompt id " + prompt.getId());
			}
		}
		
		Set<String> keySet = prompt.getProperties().keySet();
		
		for(int i = 0; i < jsonArray.length(); i++) {
			String selection = JsonUtils.getStringFromJsonArray(jsonArray, i); // the json.org lib autoconverts ints to strings
			if(! keySet.contains(selection)) { 
				
				if(_logger.isDebugEnabled()) {
					_logger.debug("unknown multi_choice selection [" + selection + "] for prompt id " + prompt.getId());
				}
				
				return false;
			}
		}
		
		return true;
	}
}
