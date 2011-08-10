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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Prompt;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;


/**
 * Validator for the 'timestamp' prompt type.
 * 
 * @author Joshua Selsky
 */
public final class TimestampPromptValidator extends AbstractPromptValidator {
	private static Logger logger = Logger.getLogger(TimestampPromptValidator.class);
	
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
		
		String timestamp = JsonUtils.getStringFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE);
		if(StringUtils.isEmptyOrWhitespaceOnly(timestamp)) {
			if(logger.isDebugEnabled()) {
				logger.debug("Missing or empty value for prompt " + prompt.getId());
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
			
			if(logger.isDebugEnabled()) {
				logger.debug("unparseable timestamp " + timestamp + " for prompt id " + prompt.getId());
			}
			
			return false;
		}
		
		return true;
	}

}
