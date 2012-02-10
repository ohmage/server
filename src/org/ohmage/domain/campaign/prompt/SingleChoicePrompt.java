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

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.SingleChoicePromptResponse;
import org.ohmage.exception.DomainException;

/**
 * This class represents a single choice prompt.
 * 
 * @author John Jenkins
 */
public class SingleChoicePrompt extends ChoicePrompt {
	private static final String JSON_KEY_DEFAULT = "default";
	
	private final Integer defaultKey;
	
	/**
	 * Creates a new single-choice prompt.
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
	 * @param choices The static choices as defined in the XML.
	 * 
	 * @param defaultKey The key of the default label.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   missing or invalid. 
	 */
	public SingleChoicePrompt(
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
			final Map<Integer, LabelValuePair> choices, 
			final Integer defaultKey, 
			final int index) 
			throws DomainException {

		super(id, condition, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				choices, Type.SINGLE_CHOICE, index);
		
		if((defaultKey != null) &&
				(! getChoices().containsKey(defaultKey))) {
			throw new DomainException("The default key does not exist.");
		}
		this.defaultKey = defaultKey;
	}
	
	/**
	 * Returns the default label if one was given; otherwise, null is returned.
	 * 
	 * @return The default label if one was given; otherwise, null is returned.
	 */
	public String getDefaultLabel() {
		if(defaultKey == null) {
			return null;
		}
		
		return getChoices().get(defaultKey).getLabel();
	}

	/**
	 * Validates that a given value is valid and, if so, converts it into an
	 * appropriate object.
	 * 
	 * @param value The value to be validated. This must be one of the
	 * 				following:<br />
	 * 				<ul>
	 * 				<li>{@link NoResponse}</li>
	 * 				<li>{@link Integer} that represents the key of the item the
	 * 				  user chose.</li>
	 * 				<li>{@link String} that represents:</li>
	 * 				  <ul>
	 * 				    <li>{@link NoResponse}</li>
	 * 				    <li>The key of the item the user chose.</li>
	 * 				  <ul>
	 * 				</ul>
	 * 
	 * @return An Integer representing a key or a {@link NoResponse} object.
	 * 
	 * @throws DomainException The value is invalid.
	 */
	@Override
	public Object validateValue(final Object value) throws DomainException {
		Integer keyValue;
		
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new DomainException(
						"The prompt was skipped, but it is not skippable.");
			}
			
			return value;
		}
		else if(value instanceof Integer) {
			keyValue = (Integer) value;
		}
		else if(value instanceof String) {
			try {
				return NoResponse.valueOf((String) value);
			}
			catch(IllegalArgumentException notNoResponse) {
				try {
					keyValue = Integer.decode((String) value);
				}
				catch(NumberFormatException notChoiceKey) {
					throw new DomainException(
							"The value was not a valid response value for this prompt.", 
							notChoiceKey);
				}
			}
		}
		else {
			throw new DomainException(
					"The value is not decodable as a reponse value.");
		}

		if(! getChoices().keySet().contains(keyValue)) {
			throw new DomainException("The value is not a value choice.");
		}
		
		return keyValue;
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
	public SingleChoicePromptResponse createResponse(
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException {
		
		return new SingleChoicePromptResponse(
				this,
				repeatableSetIteration,
				response);
	}
	
	/**
	 * Creates a JSONObject that represents this single-choice custom prompt.
	 * 
	 * @return A JSONObject that represents this single-choice custom prompt.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		result.put(JSON_KEY_DEFAULT, getDefaultLabel());
		
		return result;
	}

	/**
	 * Generates a hash code for this prompt.
	 * 
	 * @return A hash code for this prompt.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((defaultKey == null) ? 0 : defaultKey.hashCode());
		return result;
	}

	/**
	 * Determines if this prompt is logically equivalent to another prompt.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if the other object is logically equivalent to this
	 * 		   prompt.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SingleChoicePrompt other = (SingleChoicePrompt) obj;
		if (defaultKey == null) {
			if (other.defaultKey != null)
				return false;
		} else if (!defaultKey.equals(other.defaultKey))
			return false;
		return true;
	}
}
