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
package org.ohmage.domain.campaign;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Prompt.LabelValuePair;
import org.ohmage.domain.campaign.prompt.ChoicePrompt;
import org.ohmage.domain.campaign.prompt.CustomChoicePrompt;
import org.ohmage.domain.campaign.prompt.RemoteActivityPrompt;
import org.ohmage.domain.campaign.response.RemoteActivityPromptResponse;
import org.ohmage.exception.DomainException;

/**
 * Base class for all prompt responses.
 * 
 * @author John Jenkins
 */
public abstract class PromptResponse extends Response {
	private static final String JSON_KEY_PROMPT_ID = "prompt_id";
	private static final String JSON_KEY_RESPONSE = "value";
	
	/**
	 * The prompt that was presented to the user to create this response.
	 */
	private final Prompt prompt;
	
	/**
	 * The iteration of this prompt that generated this prompt.
	 */
	private final Integer repeatableSetIteration;

	/**
	 * Creates a prompt response.
	 * 
	 * @param prompt The prompt that was presented to the user in order to
	 * 				 generate this prompt response.
	 * 
	 * @param noResponse A NoResponse object which represents the allowed 
	 * 					 values if the prompt does not have a response.
	 * 
	 * @param repeatableSetIteration If the 'prompt' was part of a repeatable
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made.
	 * 
	 * @throws DomainException Thrown if the prompt is null.
	 */
	public PromptResponse(
			final Prompt prompt, 
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException {
		
		super(prompt.validateValue(response));
		
		if(
				(repeatableSetIteration == null) && 
				(prompt.getParent() != null)) {
			
			throw new DomainException(
					"The repeatable set iteration is null, but this prompt is part of a repeatable set.");
		}
		else if(
				(repeatableSetIteration != null) &&
				(prompt.getParent() == null)) {
			
			throw new DomainException(
					"The repeatable set iteration is not null, but this prompt is not part of a repeatable set.");
		}
		else if(
				(repeatableSetIteration != null) && 
				(repeatableSetIteration < 0)) {
			
			throw new DomainException(
					"The repeatable set iteration value is negative.");
		}
		
		this.prompt = prompt;
		this.repeatableSetIteration = repeatableSetIteration;
	}
	
	/**
	 * Returns the prompt on which this prompt response is based.
	 * 
	 * @return The prompt on which this prompt response is based.
	 */
	public Prompt getPrompt() {
		return prompt;
	}
	
	/**
	 * If the prompt used to generate this prompt response was part of a 
	 * repeatable set, then this will return the iteration of that repeatable
	 * set on which this response was generated. If it is not part of a 
	 * repeatable set, null will be returned.
	 * 
	 * @return The iteration of the repeatable set that is this prompt's parent
	 * 		   on which this response was generated. If the prompt was not part
	 * 		   of a repeatable set, null is returned.
	 */
	public Integer getRepeatableSetIteration() {
		return repeatableSetIteration;
	}
	
	/**
	 * Creates a JSONObject that represents this object.
	 * 
	 * @return A JSONObject that represents this object.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	@Override
	public JSONObject toJson(final boolean withId) throws JSONException {
		JSONObject result = new JSONObject();
		
		if(withId) {
			result.put(JSON_KEY_PROMPT_ID, prompt.getId());
			
			result.put(JSON_KEY_RESPONSE, getResponse());
		}
		else {
			result.put("prompt_text", prompt.getText());
			result.put("prompt_type", prompt.getType().toString());
			result.put("prompt_display_type", prompt.getDisplayType().toString());
			result.put("prompt_index", prompt.getIndex());
			result.put("prompt_unit", prompt.getUnit());
			
			if((prompt instanceof ChoicePrompt) && 
					(! (prompt instanceof CustomChoicePrompt))) {
				
				ChoicePrompt choicePrompt = (ChoicePrompt) prompt;
				Map<Integer, LabelValuePair> choices = 
						choicePrompt.getChoices();
				
				JSONObject choiceGlossary = new JSONObject();
				for(Integer choiceKey : choices.keySet()) {
					choiceGlossary.put(
							choiceKey.toString(), 
							choices.get(choiceKey).toJson());
				}
				
				result.put("prompt_choice_glossary", choiceGlossary);
			}
			
			if(this instanceof RemoteActivityPromptResponse &&
					(! wasNotDisplayed()) && (! wasSkipped())) {
				JSONArray gameResults = (JSONArray) getResponse();
				double numResults = gameResults.length();
				
				double total = 0;
				for(int i = 0; i < numResults; i++) {
					total += gameResults.getJSONObject(i).getDouble(RemoteActivityPrompt.JSON_KEY_SCORE);
				}
				result.put("prompt_response", total / numResults);
			}
			else {
				result.put("prompt_response", getResponse());
			}
		}
		
		return result;
	}
	
	/**
	 * Return's the prompt's unique identifier.
	 */
	@Override
	public String getId() {
		return prompt.getId();
	}
	
	/**
	 * Generates a hash code for this prompt response.
	 * 
	 * @return This prompt's hash code.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((prompt == null) ? 0 : prompt.hashCode());
		result = prime
				* result
				+ ((repeatableSetIteration == null) ? 0
						: repeatableSetIteration.hashCode());
		return result;
	}

	/**
	 * Determines if this prompt response is equal to another object.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if this prompt response is logically equal to the other
	 * 		   object; false, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PromptResponse other = (PromptResponse) obj;
		if (prompt == null) {
			if (other.prompt != null)
				return false;
		} else if (!prompt.equals(other.prompt))
			return false;
		if (repeatableSetIteration == null) {
			if (other.repeatableSetIteration != null)
				return false;
		} else if (!repeatableSetIteration.equals(other.repeatableSetIteration))
			return false;
		return true;
	}
}
