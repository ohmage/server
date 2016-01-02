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
package org.ohmage.domain.campaign.prompt;

import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.response.NumberPromptResponse;
import org.ohmage.exception.DomainException;

/**
 * This class represents number prompts.
 * 
 * @author John Jenkins
 */
public class NumberPrompt extends BoundedPrompt {
	/**
	 * The JSON key for the whole number flag.
	 */
	private static final String JSON_KEY_WHOLE_NUMBER = "whole_number";
	
	/**
	 * The campaign configuration property key for the whole number flag.
	 */
	public static final String XML_KEY_WHOLE_NUMBER = "wholeNumber";
	
	
	/**
	 * Creates a number prompt.
	 * 
	 * @param id The unique identifier for the prompt within its survey item
	 * 			 group.
	 * 
	 * @param condition The condition determining if this prompt should be
	 * 					displayed.
	 * 
	 * @param unit The unit value for this prompt.
	 * 
	 * @param text The text to be displayed to the user for this prompt.
	 * 
	 * @param explanationText A more-verbose version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param skippable Whether or not this prompt may be skipped.
	 * 
	 * @param skipLabel The text to show to the user indicating that the prompt
	 * 					may be skipped.
	 * 
	 * @param displayLabel The display label for this prompt.
	 * 
	 * @param min The lower bound for a response to this prompt.
	 * 
	 * @param max The upper bound for a response to this prompt.
	 * 
	 * @param defaultValue The default value for this prompt. This is optional
	 * 					   and may be null if one doesn't exist.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   missing or invalid. 
	 */
	public NumberPrompt(
			final String id, 
			final String condition, 
			final String unit, 
			final String text, 
			final String explanationText,
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final BigDecimal min, 
			final BigDecimal max, 
			final BigDecimal defaultValue,
			final int index,
			final Boolean wholeNumber) 
			throws DomainException {

		super(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			min,
			max,
			defaultValue,
			wholeNumber,
			Type.NUMBER,
			index);
	}
		
	
	/**
	 * Creates a response to this prompt based on a response value.
	 * 
	 * @param response The response from the user as an Object.
	 * 
	 * @param repeatableSetIteration If this prompt belongs to a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which the response to
	 * 								 this prompt was made.
	 * 
	 * @throws DomainException Thrown if this prompt is part of a repeatable 
	 * 						   set but the repeatable set iteration value is 
	 * 						   null, if the repeatable set iteration value is 
	 * 						   negative, or if the value is not a valid 
	 * 						   response value for this prompt.
	 */
	@Override
	public NumberPromptResponse createResponse(
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException {
		
		return new NumberPromptResponse(
				this, 
				repeatableSetIteration, 
				response);
	}
	
	/**
	 * Creates a JSONObject that represents this bounded prompt.
	 * 
	 * @return A JSONObject that represents this bounded prompt.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		result.put(JSON_KEY_WHOLE_NUMBER, getWholeNumber());
		
		return result;
	}

}