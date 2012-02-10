/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.domain.campaign.response;

import java.util.Collection;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.MultiChoiceCustomPrompt;
import org.ohmage.exception.DomainException;

/**
 * A response to a multiple-choice prompt with custom values.
 * 
 * @author John Jenkins
 */
public class MultiChoiceCustomPromptResponse extends PromptResponse {
	/**
	 * Creates a response to a multiple-choice prompt with custom choices.
	 * 
	 * @param prompt The HoursBeforeNowPrompt used to generate this response.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made. Otherwise, null.
	 * 
	 * @param response The response from the user. See
	 * 				   {@link MultiChoiceCustomPrompt#validateValue(Object)}.
	 * 
	 * @throws DomainException The repeatable set iteration is null and this 
	 * 						   was part of a repeatable set, the repeatable set
	 * 						   iteration is not null and this was not part of a
	 * 						   repeatable set, or the response could not be 
	 * 						   properly decoded. 
	 * 
	 * @see MultiChoiceCustomPrompt#validateValue(Object) Validation Rules
	 */
	public MultiChoiceCustomPromptResponse(
			final MultiChoiceCustomPrompt prompt,
			final Integer repeatableSetIteration, 
			final Object response) 
			throws DomainException {
		
		super(prompt, repeatableSetIteration, response);
	}
	
	/**
	 * Returns the choices from the user.
	 * 
	 * @return The choices from the user.
	 * 
	 * @throws DomainException The prompt does not have a response.
	 */
	@SuppressWarnings("unchecked")
	public Collection<String> getChoices() throws DomainException {
		if(wasNotDisplayed()) {
			throw new DomainException("The prompt was not displayed.");
		}
		if(wasSkipped()) {
			throw new DomainException("The prompt was skipped.");
		}
		
		return (Collection<String>) getResponse();
	}
}
