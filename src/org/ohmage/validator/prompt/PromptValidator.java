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

import org.json.JSONObject;
import org.ohmage.domain.configuration.Prompt;


/**
 * A PromptValidator is responsible for validating a prompt response against
 * its associated configuration.
 * 
 * @author Joshua Selsky
 */
public interface PromptValidator {
	
	/**
	 * Validates a prompt response against the syntax defined by the prompt type.
	 * 
	 * @param prompt  A prompt retrieved from a configuration for a particular
	 * surveyId-promptId or surveyId-repeatableSetId-promptId combination.
	 * @param promptResponse  The response to validate.
	 * @return  False if the response is invalid, true if the response is valid.
	 */
	boolean validate(Prompt prompt, JSONObject promptResponse);
}
