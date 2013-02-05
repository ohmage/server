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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.SingleChoiceCustomPromptResponse;
import org.ohmage.exception.DomainException;

/**
 * This class represents a single choice prompt with custom choices from the
 * user.
 * 
 * @author John Jenkins
 */
public class SingleChoiceCustomPrompt extends CustomChoicePrompt {
	private static final String JSON_KEY_DEFAULT = "default";
	
	private final Integer defaultKey;
	
	/**
	 * Creates a new single-choice prompt with custom values.
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
	 * @param displayType This prompt's
	 * 					 {@link org.ohmage.domain.campaign.Prompt.DisplayType}.
	 * 
	 * @param displayLabel The display label for this prompt.
	 * 
	 * @param choices The static choices as defined in the XML.
	 * 
	 * @param customChoices Custom choices created by the user.
	 * 
	 * @param defaultKey The key of the default label.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if any of the required parameters are 
	 * 						   missing or invalid. 
	 */
	public SingleChoiceCustomPrompt(
			final String id, 
			final String condition, 
			final String unit, 
			final String text, 
			final String explanationText,
			final boolean skippable, 
			final String skipLabel,
			final DisplayType displayType, 
			final String displayLabel,
			final Map<Integer, LabelValuePair> choices,
			final Map<Integer, LabelValuePair> customChoices,
			final Integer defaultKey, 
			final int index) 
			throws DomainException {

		super(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayType,
			displayLabel,
			choices,
			customChoices,
			Type.SINGLE_CHOICE_CUSTOM,
			index);

		if((defaultKey != null) &&
				(! getAllChoices().containsKey(defaultKey))) {
			throw new DomainException("The default key does not exist.");
		}
		this.defaultKey = defaultKey;
	}
	
	/**
	 * Returns the default value if one was given; otherwise, null is returned.
	 * 
	 * @return The default value if one was given; otherwise, null is returned.
	 */
	public String getDefaultValue() {
		return getAllChoices().get(defaultKey).getLabel();
	}

	/**
	 * Validates that a given value is valid and, if so, converts it into an
	 * appropriate object.
	 * 
	 * @param value The value to be validated. This must be one of the  
	 * 				following:<br />
	 * 				<ul>
	 * 				<li>{@link NoResponse}</li>
	 * 				<li>{@link String} that represents:</li>
	 * 				  <ul>
	 * 				    <li>{@link NoResponse}</li>
	 * 				    <li>The label of the item the user chose.</li>
	 * 				  <ul>
	 * 				</ul>
	 * 
	 * @return A String representing a label or a {@link NoResponse} object.
	 * 
	 * @throws DomainException The value is invalid.
	 */
	@Override
	public Object validateValue(final Object value) throws DomainException {
		// If it's already a NoResponse value, then make sure that if it
		// was skipped that it is skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new DomainException(
						"The prompt, '" +
							getId() +
							"', was skipped, but it is not skippable.");
			}
			
			return value;
		}
		else if(value instanceof String) {
			try {
				return NoResponse.valueOf((String) value);
			}
			catch(IllegalArgumentException notNoResponse) {
				Map<Integer, LabelValuePair> choices = getAllChoices();
				
				boolean exists = false;
				for(LabelValuePair vlp : choices.values()) {
					if(vlp.getLabel().equals(value)) {
						exists = true;
						break;
					}
				}
				
				if(! exists) {
					if(choices.isEmpty()) {
						addChoice(0, (String) value, null);
					} 
					else {
						List<Integer> keys = 
								new ArrayList<Integer>(choices.keySet());
						Collections.sort(keys);
						int key = keys.get(keys.size() - 1) + 1;
						addChoice(key, (String) value, null);
					}
				}
				
				return (String) value;
			}
		}
		else {
			throw new DomainException(
					"The value for prompt '" +
						getId() +
						"' is not decodable as a reponse value: " + 
						value);
		}
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
	public SingleChoiceCustomPromptResponse createResponse(
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException {
		
		return new SingleChoiceCustomPromptResponse(
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
		
		result.put(JSON_KEY_DEFAULT, defaultKey);
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.SurveyItem#toConcordia(org.codehaus.jackson.JsonGenerator)
	 */
	@Override
	public void toConcordia(
			final JsonGenerator generator)
			throws JsonGenerationException, IOException {
		
		// The response is always an object.
		generator.writeStartObject();
		generator.writeStringField("type", "object");
		
		// The fields array.
		generator.writeArrayFieldStart("schema");
		
		// The first field in the object is the prompt's ID.
		generator.writeStartObject();
		generator.writeStringField("name", PromptResponse.JSON_KEY_PROMPT_ID);
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		// The second field in the object is the response's value.
		generator.writeStartObject();
		generator.writeStringField("name", PromptResponse.JSON_KEY_RESPONSE);
		generator.writeStringField("type", "number");
		generator.writeEndObject();
		
		// End the array of fields.
		generator.writeEndArray();
		
		// End the object.
		generator.writeEndObject();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.prompt.CustomChoicePrompt#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result =
			prime *
				result +
				((defaultKey == null) ? 0 : defaultKey.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!super.equals(obj)) {
			return false;
		}
		if(!(obj instanceof SingleChoiceCustomPrompt)) {
			return false;
		}
		SingleChoiceCustomPrompt other = (SingleChoiceCustomPrompt) obj;
		if(defaultKey == null) {
			if(other.defaultKey != null) {
				return false;
			}
		}
		else if(!defaultKey.equals(other.defaultKey)) {
			return false;
		}
		return true;
	}
}
