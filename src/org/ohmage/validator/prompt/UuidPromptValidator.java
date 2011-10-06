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

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Prompt;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;


/**
 * Validator for prompt responses that are represented by a UUID (currently
 * used by image upload only. 
 * 
 * @author Joshua Selsky
 */
public final class UuidPromptValidator extends AbstractPromptValidator {
	private static Logger logger = Logger.getLogger(UuidPromptValidator.class);
	private static Pattern pattern 
		= Pattern.compile("[a-fA-F0-9]{8}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{12}");

	/**
	 * Validates that the value in the promptResponse is a correctly formed UUID.
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
			if(logger.isDebugEnabled()) {
				logger.debug("Missing UUID value for prompt " + prompt.getId());
			}
			return false;
		}

		if(! pattern.matcher(value).matches()) {
			if(logger.isDebugEnabled()) {
				logger.debug("invalid UUID for prompt " + prompt.getId() + ". value: " + value);
			}
			return false;
		}

		return true;
	}
}