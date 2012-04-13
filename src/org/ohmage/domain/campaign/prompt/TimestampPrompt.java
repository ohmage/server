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

import java.util.Calendar;
import java.util.Date;

import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.TimestampPromptResponse;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;

/**
 * This class represents a timestamp prompt.
 * 
 * @author John Jenkins
 */
public class TimestampPrompt extends Prompt {
	/**
	 * Creates a timestamp prompt.
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
	 * @param abbreviatedText An abbreviated version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param explanationText A more-verbose version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param skippable Whether or not this prompt may be skipped.
	 * 
	 * @param skipLabel The text to show to the user indicating that the prompt
	 * 					may be skipped.
	 * 
	 * @param displayType This prompt's
	 * 					 {@link org.ohmage.domain.campaign.Prompt.DisplayType}.
	 * 
	 * @param displayLabel The display label for this prompt.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   missing or invalid. 
	 */
	public TimestampPrompt(
			final String id, 
			final String condition, 
			final String unit, 
			final String text, 
			final String abbreviatedText, 
			final String explanationText,
			final boolean skippable, 
			final String skipLabel,
			final DisplayType displayType, 
			final String displayLabel,
			final int index) 
			throws DomainException {
		
		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				Type.TIMESTAMP, index);
	}
	
	/**
	 * Conditions are not allowed for timestamp prompts.
	 * 
	 * @param pair The pair to validate.
	 * 
	 * @throws DomainException Always thrown because conditions are not allowed
	 * 						   for timestamp prompts.
	 */
	@Override
	public void validateConditionValuePair(
			final ConditionValuePair pair)
			throws DomainException {
		
		throw new DomainException("Conditions are not allowed for timestamp prompts.");
	}
	
	/**
	 * Validates that a given value is valid and, if so, converts it into an
	 * appropriate object.
	 * 
	 * @param value The value to be validated. This must be one of the  
	 * 				following:<br />
	 * 				<ul>
	 * 				<li>{@link NoResponse}</li>
	 * 				<li>{@link Date}</li>
	 * 				<li>{@link Calendar}</li>
	 * 				<li>{@link String} that represents:</li>
	 * 				  <ul>
	 * 				    <li>{@link NoResponse}</li>
	 * 				    <li>ISO 8601 formatted date with or without the time.
	 * 				      </li>
	 * 				  <ul>
	 * 				</ul>
	 * 
	 * @return A Date object or a {@link NoResponse} object.
	 * 
	 * @throws DomainException The value is invalid.
	 */
	@Override
	public Object validateValue(final Object value) throws DomainException {
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new DomainException(
						"The prompt, '" +
							getId() +
							"', was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it's already a date, return it.
		else if(value instanceof Date) {
			return value;
		}
		// If it's a Calendar, convert it to a Date and return it.
		else if(value instanceof Calendar) {
			return new Date(((Calendar) value).getTimeInMillis());
		}
		// If it's a String, attempt to convert it to a Date and return it.
		else if(value instanceof String) {
			Date result = null;
			
			try {
				return NoResponse.valueOf((String) value);
			}
			catch(IllegalArgumentException iae) {
				result = StringUtils.decodeDateTime((String) value);
				if(result != null) {
					return result;
				}
				
				result = StringUtils.decodeDate((String) value);
				if(result != null) {
					return result;
				}
			
				throw new DomainException(
						"The string value could not be converted to a date for prompt '" +
							getId() +
							"'.");
			}
		}
		
		throw new DomainException(
				"The value could not be converted to a valid date for prompt '" +
					getId() +
					"'.");
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
	public TimestampPromptResponse createResponse(
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException {
		
		return new TimestampPromptResponse(
				this,
				repeatableSetIteration,
				response);
	}
}
