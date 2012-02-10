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

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.NumberPrompt;
import org.ohmage.exception.DomainException;

/**
 * A number prompt response.
 * 
 * @author John Jenkins
 */
public class NumberPromptResponse extends PromptResponse {
	//private final Long number;
	
	/**
	 * Creates a number prompt response.
	 * 
	 * @param prompt The HoursBeforeNowPrompt used to generate this response.
	 * 
	 * @param noResponse A 
	 * 					 {@link org.ohmage.domain.campaign.Response.NoResponse}
	 * 					 value if the user didn't supply an answer to this 
	 * 					 prompt.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made.
	 * 
	 * @param number The response from the user.
	 * 
	 * @throws DomainException Thrown if any of the parameters are invalid.
	 */
	public NumberPromptResponse(
			final NumberPrompt prompt,
			final Integer repeatableSetIteration, 
			final Object response) 
			throws DomainException {
		
		super(prompt, repeatableSetIteration, response);
		/*
		if((number == null) && (noResponse == null)) {
			throw new IllegalArgumentException("Both number and no response cannot be null.");
		}
		else if((number != null) && (noResponse != null)) {
			throw new IllegalArgumentException("Both number and no response were given.");
		}
		
		this.number = number;
		*/
	}
	
	/**
	 * Returns the number response from the user.
	 * 
	 * @return The number response from the user.
	 * 
	 * @throws DomainException The prompt does not have a response.
	 */
	public Long getNumber() throws DomainException {
		if(wasNotDisplayed()) {
			throw new DomainException("The prompt was not displayed.");
		}
		else if(wasSkipped()) {
			throw new DomainException("The prompt was skipped.");
		}
		
		return (Long) getResponse();
	}
	
	/**
	 * Returns the number as a string.
	 * 
	 * @return A String representing the number.
	 *
	@Override
	public Object getResponseValue() {
		Object noResponseObject = super.getResponseValue();
		
		if(noResponseObject == null) {
			return number;
		}
		else {
			return noResponseObject;
		}
	}*/

	/**
	 * Generates a hash code for this response.
	 * 
	 * @return A hash code for this prompt response.
	 *
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		return result;
	}*/

	/**
	 * Determines if this prompt response is logically equivalent to another
	 * object.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if this response is logically equivalent to the other 
	 * 		   object; false, otherwise.
	 *
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NumberPromptResponse other = (NumberPromptResponse) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}*/
}
